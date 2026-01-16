package io.elev8.resources.leaderelection;

import io.elev8.resources.ResourceException;
import io.elev8.resources.lease.Lease;
import io.elev8.resources.lease.LeaseManager;
import io.elev8.resources.lease.LeaseSpec;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * LeaderElector implements leader election using Kubernetes Lease objects.
 *
 * <p>The election algorithm works as follows:</p>
 * <ol>
 *   <li>Try to acquire the lease by creating or updating it with our identity</li>
 *   <li>If successful, we become the leader and start the leader loop</li>
 *   <li>As leader, periodically renew the lease before it expires</li>
 *   <li>If we fail to renew, we lose leadership</li>
 *   <li>Non-leaders watch the lease and try to acquire it when it expires</li>
 * </ol>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * LeaderElectionConfig config = LeaderElectionConfig.builder()
 *     .leaseManager(eksClient.leases())
 *     .namespace("kube-system")
 *     .leaseName("my-controller")
 *     .identity("pod-" + UUID.randomUUID())
 *     .callbacks(new LeaderCallbacks() {
 *         public void onStartLeading() {
 *             // Run main controller loop
 *         }
 *         public void onStopLeading() {
 *             // Clean up
 *         }
 *         public void onNewLeader(String identity) {
 *             log.info("New leader: {}", identity);
 *         }
 *     })
 *     .build();
 *
 * LeaderElector elector = new LeaderElector(config);
 * elector.run(); // Blocks until shutdown
 * }</pre>
 */
@Slf4j
public class LeaderElector implements AutoCloseable {

    private final LeaderElectionConfig config;
    private final LeaseManager leaseManager;
    private final AtomicBoolean running;
    private final AtomicBoolean isLeader;
    private final AtomicReference<String> observedLeader;
    private final AtomicReference<Lease> observedLease;

    private volatile ExecutorService executor;
    private volatile Future<?> leaderWorkFuture;

    public LeaderElector(final LeaderElectionConfig config) {
        config.validate();
        this.config = config;
        this.leaseManager = config.getLeaseManager();
        this.running = new AtomicBoolean(false);
        this.isLeader = new AtomicBoolean(false);
        this.observedLeader = new AtomicReference<>();
        this.observedLease = new AtomicReference<>();
    }

    /**
     * Starts the leader election process.
     * This method blocks until the elector is stopped via {@link #stop()}.
     */
    public void run() {
        if (!running.compareAndSet(false, true)) {
            log.warn("LeaderElector is already running");
            return;
        }

        log.info("Starting leader election for lease {}/{} with identity {}",
                config.getNamespace(), config.getLeaseName(), config.getIdentity());

        executor = Executors.newSingleThreadExecutor(r -> {
            final Thread thread = new Thread(r, "leader-work-" + config.getLeaseName());
            thread.setDaemon(true);
            return thread;
        });

        try {
            acquireLoop();
        } finally {
            if (isLeader.get() && config.isReleaseOnCancel()) {
                tryReleaseLease();
            }
            running.set(false);
            log.info("Leader election stopped for {}/{}", config.getNamespace(), config.getLeaseName());
        }
    }

    /**
     * Stops the leader election process.
     */
    public void stop() {
        log.info("Stopping leader election...");
        running.set(false);

        if (leaderWorkFuture != null) {
            leaderWorkFuture.cancel(true);
        }

        if (executor != null) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void close() {
        stop();
    }

    /**
     * Returns whether this instance is currently the leader.
     *
     * @return true if this instance holds the lease
     */
    public boolean isLeader() {
        return isLeader.get();
    }

    /**
     * Returns the identity of the current leader.
     *
     * @return the leader identity, or null if unknown
     */
    public String getLeader() {
        return observedLeader.get();
    }

    private void acquireLoop() {
        while (running.get()) {
            if (!tryAcquireOrRenew()) {
                sleepWithJitter(config.getRetryPeriod());
                continue;
            }

            if (!isLeader.get()) {
                becomeLeader();
            }

            renewLoop();

            if (isLeader.get()) {
                loseLeadership();
            }
        }
    }

    private void renewLoop() {
        final Instant renewDeadline = Instant.now().plus(config.getRenewDeadline());

        while (running.get() && Instant.now().isBefore(renewDeadline)) {
            if (tryAcquireOrRenew()) {
                sleepWithJitter(config.getRetryPeriod());
            } else {
                log.warn("Failed to renew lease, will retry...");
                sleepWithJitter(config.getRetryPeriod());
            }
        }
    }

    private boolean tryAcquireOrRenew() {
        final Instant now = Instant.now();

        try {
            Lease lease = getOrCreateLease();
            final LeaseSpec spec = lease.getSpec();

            if (spec == null) {
                return tryUpdateLease(lease, now, true);
            }

            final String currentHolder = spec.getHolderIdentity();
            final boolean isCurrentLeader = config.getIdentity().equals(currentHolder);

            if (isCurrentLeader) {
                return tryUpdateLease(lease, now, false);
            }

            if (isLeaseExpired(spec, now)) {
                log.info("Lease expired, attempting to acquire...");
                return tryUpdateLease(lease, now, true);
            }

            maybeReportNewLeader(currentHolder);
            return false;

        } catch (ResourceException e) {
            log.debug("Failed to acquire/renew lease: {}", e.getMessage());
            return false;
        }
    }

    private Lease getOrCreateLease() throws ResourceException {
        try {
            return leaseManager.get(config.getNamespace(), config.getLeaseName());
        } catch (ResourceException e) {
            if (e.getStatusCode() == 404) {
                return createNewLease();
            }
            throw e;
        }
    }

    private Lease createNewLease() throws ResourceException {
        final Instant now = Instant.now();

        final Lease lease = Lease.builder()
                .namespace(config.getNamespace())
                .name(config.getLeaseName())
                .holderIdentity(config.getIdentity())
                .leaseDurationSeconds((int) config.getLeaseDuration().toSeconds())
                .acquireTime(now)
                .renewTime(now)
                .leaseTransitions(0)
                .build();

        log.info("Creating new lease {}/{}", config.getNamespace(), config.getLeaseName());
        return leaseManager.create(lease);
    }

    private boolean tryUpdateLease(final Lease lease, final Instant now, final boolean acquiring) {
        try {
            final LeaseSpec currentSpec = lease.getSpec();
            final int transitions = currentSpec != null && currentSpec.getLeaseTransitions() != null
                    ? currentSpec.getLeaseTransitions()
                    : 0;

            final LeaseSpec newSpec = LeaseSpec.builder()
                    .holderIdentity(config.getIdentity())
                    .leaseDurationSeconds((int) config.getLeaseDuration().toSeconds())
                    .acquireTime(acquiring ? now : (currentSpec != null ? currentSpec.getAcquireTime() : now))
                    .renewTime(now)
                    .leaseTransitions(acquiring ? transitions + 1 : transitions)
                    .build();

            lease.setSpec(newSpec);

            final Lease updated = leaseManager.update(lease);
            observedLease.set(updated);

            if (acquiring) {
                log.info("Successfully acquired lease {}/{}", config.getNamespace(), config.getLeaseName());
            } else {
                log.debug("Successfully renewed lease {}/{}", config.getNamespace(), config.getLeaseName());
            }

            maybeReportNewLeader(config.getIdentity());
            return true;

        } catch (ResourceException e) {
            log.debug("Failed to update lease: {} (status: {})", e.getMessage(), e.getStatusCode());
            return false;
        }
    }

    private boolean isLeaseExpired(final LeaseSpec spec, final Instant now) {
        if (spec.getRenewTime() == null) {
            return true;
        }

        final int durationSeconds = spec.getLeaseDurationSeconds() != null
                ? spec.getLeaseDurationSeconds()
                : (int) config.getLeaseDuration().toSeconds();

        final Instant expireTime = spec.getRenewTime().plusSeconds(durationSeconds);
        return now.isAfter(expireTime);
    }

    private void becomeLeader() {
        log.info("This instance is now the leader: {}", config.getIdentity());
        isLeader.set(true);

        leaderWorkFuture = executor.submit(() -> {
            try {
                config.getCallbacks().onStartLeading();
            } catch (Exception e) {
                log.error("Error in onStartLeading callback", e);
            }
        });
    }

    private void loseLeadership() {
        log.info("This instance is no longer the leader: {}", config.getIdentity());
        isLeader.set(false);

        if (leaderWorkFuture != null) {
            leaderWorkFuture.cancel(true);
        }

        try {
            config.getCallbacks().onStopLeading();
        } catch (Exception e) {
            log.error("Error in onStopLeading callback", e);
        }
    }

    private void maybeReportNewLeader(final String leader) {
        final String previousLeader = observedLeader.getAndSet(leader);
        if (leader != null && !leader.equals(previousLeader)) {
            log.info("Observed leader change: {} -> {}", previousLeader, leader);
            try {
                config.getCallbacks().onNewLeader(leader);
            } catch (Exception e) {
                log.error("Error in onNewLeader callback", e);
            }
        }
    }

    private void tryReleaseLease() {
        try {
            final Lease lease = leaseManager.get(config.getNamespace(), config.getLeaseName());
            final LeaseSpec spec = lease.getSpec();

            if (spec != null && config.getIdentity().equals(spec.getHolderIdentity())) {
                log.info("Releasing lease {}/{}", config.getNamespace(), config.getLeaseName());

                spec.setHolderIdentity(null);
                spec.setRenewTime(Instant.now());
                lease.setSpec(spec);

                leaseManager.update(lease);
            }
        } catch (ResourceException e) {
            log.warn("Failed to release lease: {}", e.getMessage());
        }
    }

    private void sleepWithJitter(final Duration duration) {
        try {
            final long jitterMs = (long) (duration.toMillis() * 0.2 * Math.random());
            Thread.sleep(duration.toMillis() + jitterMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
