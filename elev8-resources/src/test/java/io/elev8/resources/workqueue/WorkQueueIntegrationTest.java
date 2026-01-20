package io.elev8.resources.workqueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class WorkQueueIntegrationTest {

    private RateLimitingWorkQueue<String> queue;
    private ExecutorService executor;

    @AfterEach
    void tearDown() {
        if (queue != null) {
            queue.shutdown();
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    void shouldHandleTypicalControllerPattern() throws InterruptedException {
        queue = WorkQueues.newDefaultRateLimitingQueue();
        final List<String> processedKeys = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch done = new CountDownLatch(3);

        queue.add("pod/default/nginx");
        queue.add("pod/kube-system/coredns");
        queue.add("pod/default/redis");

        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            while (!queue.isShuttingDown()) {
                try {
                    final String key = queue.poll(Duration.ofMillis(100));
                    if (key == null) {
                        continue;
                    }
                    try {
                        processedKeys.add(key);
                        queue.forget(key);
                    } finally {
                        queue.done(key);
                        done.countDown();
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        done.await(5, TimeUnit.SECONDS);
        queue.shutdown();

        assertThat(processedKeys).containsExactlyInAnyOrder(
                "pod/default/nginx",
                "pod/kube-system/coredns",
                "pod/default/redis"
        );
    }

    @Test
    void shouldRetryFailingItems() throws InterruptedException {
        queue = WorkQueues.newExponentialBackoffQueue(Duration.ofMillis(10), Duration.ofMillis(100));
        final AtomicInteger attemptCount = new AtomicInteger(0);
        final CountDownLatch success = new CountDownLatch(1);

        queue.add("failing-item");

        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            while (!queue.isShuttingDown()) {
                try {
                    final String key = queue.poll(Duration.ofMillis(50));
                    if (key == null) {
                        continue;
                    }
                    try {
                        final int attempt = attemptCount.incrementAndGet();
                        if (attempt < 3) {
                            throw new RuntimeException("Simulated failure #" + attempt);
                        }
                        queue.forget(key);
                        success.countDown();
                    } catch (final RuntimeException e) {
                        queue.addRateLimited(key);
                    } finally {
                        queue.done(key);
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        assertThat(success.await(5, TimeUnit.SECONDS)).isTrue();
        queue.shutdown();

        assertThat(attemptCount.get()).isEqualTo(3);
    }

    @Test
    void shouldDeduplicateRapidUpdates() throws InterruptedException {
        queue = WorkQueues.newDefaultRateLimitingQueue();
        final AtomicInteger processCount = new AtomicInteger(0);

        for (int i = 0; i < 100; i++) {
            queue.add("same-key");
        }

        assertThat(queue.length()).isEqualTo(1);

        final String key = queue.poll(Duration.ofSeconds(1));
        processCount.incrementAndGet();
        queue.done(key);

        assertThat(processCount.get()).isEqualTo(1);
        assertThat(queue.poll(Duration.ofMillis(50))).isNull();
    }

    @Test
    void shouldHandleMultipleWorkers() throws InterruptedException {
        queue = WorkQueues.newDefaultRateLimitingQueue();
        final int numItems = 100;
        final int numWorkers = 4;
        final Set<String> processedItems = ConcurrentHashMap.newKeySet();
        final CountDownLatch allProcessed = new CountDownLatch(numItems);

        for (int i = 0; i < numItems; i++) {
            queue.add("item-" + i);
        }

        executor = Executors.newFixedThreadPool(numWorkers);
        for (int w = 0; w < numWorkers; w++) {
            executor.submit(() -> {
                while (!queue.isShuttingDown()) {
                    try {
                        final String key = queue.poll(Duration.ofMillis(50));
                        if (key == null) {
                            if (allProcessed.getCount() == 0) {
                                break;
                            }
                            continue;
                        }
                        try {
                            processedItems.add(key);
                            queue.forget(key);
                        } finally {
                            queue.done(key);
                            allProcessed.countDown();
                        }
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        assertThat(allProcessed.await(10, TimeUnit.SECONDS)).isTrue();
        queue.shutdown();

        assertThat(processedItems).hasSize(numItems);
    }

    @Test
    void shouldHandleRequeueWhileProcessing() throws InterruptedException {
        queue = WorkQueues.newDefaultRateLimitingQueue();
        final AtomicInteger version = new AtomicInteger(0);
        final AtomicInteger processCount = new AtomicInteger(0);
        final CountDownLatch done = new CountDownLatch(1);

        queue.add("resource-key");

        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            while (!queue.isShuttingDown()) {
                try {
                    final String key = queue.poll(Duration.ofMillis(50));
                    if (key == null) {
                        continue;
                    }
                    try {
                        processCount.incrementAndGet();

                        final int v = version.incrementAndGet();
                        if (v < 3) {
                            queue.add(key);
                        } else {
                            queue.forget(key);
                            done.countDown();
                        }
                    } finally {
                        queue.done(key);
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        queue.shutdown();

        assertThat(processCount.get()).isEqualTo(3);
    }

    @Test
    void shouldSimulateInformerEvents() throws InterruptedException {
        queue = WorkQueues.newDefaultRateLimitingQueue();
        final List<String> events = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch latch = new CountDownLatch(6);

        simulateInformerEvent("ADD", "pod/ns1/pod1", queue);
        simulateInformerEvent("ADD", "pod/ns1/pod2", queue);
        simulateInformerEvent("UPDATE", "pod/ns1/pod1", queue);
        simulateInformerEvent("ADD", "pod/ns2/pod1", queue);
        simulateInformerEvent("UPDATE", "pod/ns1/pod1", queue);
        simulateInformerEvent("DELETE", "pod/ns1/pod2", queue);

        assertThat(queue.length()).isLessThanOrEqualTo(4);

        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            while (!queue.isShuttingDown()) {
                try {
                    final String key = queue.poll(Duration.ofMillis(50));
                    if (key == null) {
                        continue;
                    }
                    try {
                        events.add("processed:" + key);
                        queue.forget(key);
                    } finally {
                        queue.done(key);
                        latch.countDown();
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        latch.await(5, TimeUnit.SECONDS);
        queue.shutdown();

        assertThat(events).containsExactlyInAnyOrder(
                "processed:pod/ns1/pod1",
                "processed:pod/ns1/pod2",
                "processed:pod/ns2/pod1"
        );
    }

    private void simulateInformerEvent(final String eventType,
                                       final String resourceKey,
                                       final RateLimitingWorkQueue<String> workQueue) {
        workQueue.add(resourceKey);
    }

    @Test
    void shouldHandleGracefulShutdown() throws InterruptedException {
        queue = WorkQueues.newDefaultRateLimitingQueue();
        final AtomicInteger itemsProcessed = new AtomicInteger(0);
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch workerDone = new CountDownLatch(1);

        for (int i = 0; i < 10; i++) {
            queue.add("item-" + i);
        }

        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            started.countDown();
            while (!queue.isShuttingDown()) {
                try {
                    final String key = queue.poll(Duration.ofMillis(10));
                    if (key != null) {
                        itemsProcessed.incrementAndGet();
                        queue.done(key);
                        Thread.sleep(20);
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            workerDone.countDown();
        });

        started.await();
        Thread.sleep(50);

        queue.shutdown();

        assertThat(workerDone.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(itemsProcessed.get()).isGreaterThan(0);
        assertThat(itemsProcessed.get()).isLessThanOrEqualTo(10);
    }

    @Test
    void shouldWorkWithFactoryMethods() throws InterruptedException {
        final WorkQueue<String> basicQueue = WorkQueues.newDefaultQueue();
        final DelayingWorkQueue<String> delayingQueue = WorkQueues.newDelayingQueue();
        final RateLimitingWorkQueue<String> rateLimitingQueue = WorkQueues.newDefaultRateLimitingQueue();

        try {
            basicQueue.add("basic");
            assertThat(basicQueue.poll(Duration.ofSeconds(1))).isEqualTo("basic");

            delayingQueue.addAfter("delayed", Duration.ofMillis(10));
            assertThat(delayingQueue.poll(Duration.ofMillis(100))).isEqualTo("delayed");

            rateLimitingQueue.addRateLimited("limited");
            assertThat(rateLimitingQueue.poll(Duration.ofMillis(100))).isEqualTo("limited");
        } finally {
            basicQueue.shutdown();
            delayingQueue.shutdown();
            rateLimitingQueue.shutdown();
        }
    }
}
