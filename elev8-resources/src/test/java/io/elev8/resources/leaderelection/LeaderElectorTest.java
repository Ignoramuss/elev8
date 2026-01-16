package io.elev8.resources.leaderelection;

import io.elev8.resources.Metadata;
import io.elev8.resources.ResourceException;
import io.elev8.resources.lease.Lease;
import io.elev8.resources.lease.LeaseManager;
import io.elev8.resources.lease.LeaseSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderElectorTest {

    private static final String NAMESPACE = "test-ns";
    private static final String LEASE_NAME = "test-leader";
    private static final String IDENTITY = "pod-test-123";

    @Mock
    private LeaseManager leaseManager;

    private TestCallbacks callbacks;
    private LeaderElectionConfig config;

    @BeforeEach
    void setUp() {
        callbacks = new TestCallbacks();
        config = LeaderElectionConfig.builder()
                .leaseManager(leaseManager)
                .namespace(NAMESPACE)
                .leaseName(LEASE_NAME)
                .identity(IDENTITY)
                .leaseDuration(Duration.ofSeconds(5))
                .renewDeadline(Duration.ofSeconds(3))
                .retryPeriod(Duration.ofMillis(100))
                .callbacks(callbacks)
                .build();
    }

    @Test
    void shouldAcquireLeadershipWhenLeaseDoesNotExist() throws Exception {
        when(leaseManager.get(NAMESPACE, LEASE_NAME))
                .thenThrow(new ResourceException("Not found", 404));

        final Lease createdLease = createLease(IDENTITY);
        when(leaseManager.create(any(Lease.class))).thenReturn(createdLease);

        final LeaderElector elector = new LeaderElector(config);

        final Thread electionThread = new Thread(elector::run);
        electionThread.start();

        assertThat(callbacks.waitForLeadership(2, TimeUnit.SECONDS)).isTrue();
        assertThat(elector.isLeader()).isTrue();
        assertThat(elector.getLeader()).isEqualTo(IDENTITY);

        elector.stop();
        electionThread.join(1000);
    }

    @Test
    void shouldAcquireLeadershipWhenLeaseExpired() throws Exception {
        final Instant expiredTime = Instant.now().minusSeconds(60);
        final Lease expiredLease = createLeaseWithTimes("old-holder", expiredTime, expiredTime);

        when(leaseManager.get(NAMESPACE, LEASE_NAME)).thenReturn(expiredLease);
        when(leaseManager.update(any(Lease.class))).thenReturn(createLease(IDENTITY));

        final LeaderElector elector = new LeaderElector(config);

        final Thread electionThread = new Thread(elector::run);
        electionThread.start();

        assertThat(callbacks.waitForLeadership(2, TimeUnit.SECONDS)).isTrue();
        assertThat(elector.isLeader()).isTrue();

        elector.stop();
        electionThread.join(1000);
    }

    @Test
    void shouldNotAcquireLeadershipWhenLeaseHeld() throws Exception {
        final Instant now = Instant.now();
        final Lease heldLease = createLeaseWithTimes("other-holder", now, now);

        when(leaseManager.get(NAMESPACE, LEASE_NAME)).thenReturn(heldLease);

        final LeaderElector elector = new LeaderElector(config);

        final Thread electionThread = new Thread(elector::run);
        electionThread.start();

        Thread.sleep(500);

        assertThat(elector.isLeader()).isFalse();
        assertThat(elector.getLeader()).isEqualTo("other-holder");
        assertThat(callbacks.newLeaderIdentity.get()).isEqualTo("other-holder");

        elector.stop();
        electionThread.join(1000);
    }

    @Test
    void shouldReportNewLeader() throws Exception {
        final Instant now = Instant.now();
        final Lease heldLease = createLeaseWithTimes("leader-1", now, now);

        when(leaseManager.get(NAMESPACE, LEASE_NAME)).thenReturn(heldLease);

        final LeaderElector elector = new LeaderElector(config);

        final Thread electionThread = new Thread(elector::run);
        electionThread.start();

        assertThat(callbacks.waitForNewLeader(2, TimeUnit.SECONDS)).isTrue();
        assertThat(callbacks.newLeaderIdentity.get()).isEqualTo("leader-1");

        elector.stop();
        electionThread.join(1000);
    }

    @Test
    void shouldReturnIsLeaderFalseBeforeStart() {
        final LeaderElector elector = new LeaderElector(config);
        assertThat(elector.isLeader()).isFalse();
    }

    @Test
    void shouldReturnNullLeaderBeforeStart() {
        final LeaderElector elector = new LeaderElector(config);
        assertThat(elector.getLeader()).isNull();
    }

    private Lease createLease(final String holder) {
        return createLeaseWithTimes(holder, Instant.now(), Instant.now());
    }

    private Lease createLeaseWithTimes(final String holder, final Instant acquireTime, final Instant renewTime) {
        final Lease lease = new Lease();
        lease.setMetadata(Metadata.builder()
                .namespace(NAMESPACE)
                .name(LEASE_NAME)
                .build());
        lease.setSpec(LeaseSpec.builder()
                .holderIdentity(holder)
                .leaseDurationSeconds(5)
                .acquireTime(acquireTime)
                .renewTime(renewTime)
                .leaseTransitions(0)
                .build());
        return lease;
    }

    private static class TestCallbacks implements LeaderCallbacks {
        final CountDownLatch leadershipLatch = new CountDownLatch(1);
        final CountDownLatch newLeaderLatch = new CountDownLatch(1);
        final AtomicBoolean isLeading = new AtomicBoolean(false);
        final AtomicReference<String> newLeaderIdentity = new AtomicReference<>();

        @Override
        public void onStartLeading() {
            isLeading.set(true);
            leadershipLatch.countDown();
            while (isLeading.get()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        @Override
        public void onStopLeading() {
            isLeading.set(false);
        }

        @Override
        public void onNewLeader(final String identity) {
            newLeaderIdentity.set(identity);
            newLeaderLatch.countDown();
        }

        boolean waitForLeadership(long timeout, TimeUnit unit) throws InterruptedException {
            return leadershipLatch.await(timeout, unit);
        }

        boolean waitForNewLeader(long timeout, TimeUnit unit) throws InterruptedException {
            return newLeaderLatch.await(timeout, unit);
        }
    }
}
