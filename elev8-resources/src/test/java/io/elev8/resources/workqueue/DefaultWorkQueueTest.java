package io.elev8.resources.workqueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultWorkQueueTest {

    private DefaultWorkQueue<String> queue;

    @BeforeEach
    void setUp() {
        queue = new DefaultWorkQueue<>();
    }

    @AfterEach
    void tearDown() {
        queue.shutdown();
    }

    @Test
    void shouldAddAndGetItem() throws InterruptedException {
        queue.add("item1");

        final String item = queue.poll(Duration.ofSeconds(1));

        assertThat(item).isEqualTo("item1");
    }

    @Test
    void shouldMaintainFifoOrder() throws InterruptedException {
        queue.add("first");
        queue.add("second");
        queue.add("third");

        assertThat(queue.poll(Duration.ofSeconds(1))).isEqualTo("first");
        queue.done("first");

        assertThat(queue.poll(Duration.ofSeconds(1))).isEqualTo("second");
        queue.done("second");

        assertThat(queue.poll(Duration.ofSeconds(1))).isEqualTo("third");
        queue.done("third");
    }

    @Test
    void shouldDeduplicatePendingItems() throws InterruptedException {
        queue.add("item");
        queue.add("item");
        queue.add("item");

        assertThat(queue.length()).isEqualTo(1);

        final String item = queue.poll(Duration.ofSeconds(1));
        assertThat(item).isEqualTo("item");
        queue.done(item);

        assertThat(queue.poll(Duration.ofMillis(100))).isNull();
    }

    @Test
    void shouldRequeueItemAddedDuringProcessing() throws InterruptedException {
        queue.add("item");

        final String first = queue.poll(Duration.ofSeconds(1));
        assertThat(first).isEqualTo("item");

        queue.add("item");
        queue.done(first);

        final String second = queue.poll(Duration.ofSeconds(1));
        assertThat(second).isEqualTo("item");
        queue.done(second);

        assertThat(queue.poll(Duration.ofMillis(100))).isNull();
    }

    @Test
    void shouldNotRequeueIfNotAddedDuringProcessing() throws InterruptedException {
        queue.add("item");

        final String item = queue.poll(Duration.ofSeconds(1));
        queue.done(item);

        assertThat(queue.poll(Duration.ofMillis(100))).isNull();
    }

    @Test
    void shouldReturnQueueLength() {
        assertThat(queue.length()).isEqualTo(0);

        queue.add("item1");
        assertThat(queue.length()).isEqualTo(1);

        queue.add("item2");
        assertThat(queue.length()).isEqualTo(2);

        queue.add("item1");
        assertThat(queue.length()).isEqualTo(2);
    }

    @Test
    void shouldHandleShutdown() throws InterruptedException {
        queue.add("item");
        queue.shutdown();

        assertThat(queue.isShuttingDown()).isTrue();

        queue.add("new-item");
        assertThat(queue.length()).isEqualTo(1);
    }

    @Test
    void shouldReturnNullOnPollTimeout() throws InterruptedException {
        final String result = queue.poll(Duration.ofMillis(50));

        assertThat(result).isNull();
    }

    @Test
    void shouldRejectNullItem() {
        assertThatThrownBy(() -> queue.add(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    void shouldHandleDoneWithNullSafely() {
        queue.done(null);
    }

    @Test
    void shouldHandleConcurrentAdds() throws InterruptedException {
        final int numProducers = 10;
        final int itemsPerProducer = 100;
        final ExecutorService executor = Executors.newFixedThreadPool(numProducers);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(numProducers);

        for (int p = 0; p < numProducers; p++) {
            final int producerId = p;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < itemsPerProducer; i++) {
                        queue.add("producer-" + producerId + "-item-" + i);
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(queue.length()).isEqualTo(numProducers * itemsPerProducer);
    }

    @Test
    void shouldHandleConcurrentProducersAndConsumers() throws InterruptedException {
        final int numProducers = 5;
        final int numConsumers = 3;
        final int itemsPerProducer = 50;
        final ExecutorService executor = Executors.newFixedThreadPool(numProducers + numConsumers);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch producersDone = new CountDownLatch(numProducers);
        final AtomicInteger processedCount = new AtomicInteger(0);
        final List<String> processedItems = Collections.synchronizedList(new ArrayList<>());

        for (int p = 0; p < numProducers; p++) {
            final int producerId = p;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < itemsPerProducer; i++) {
                        queue.add("producer-" + producerId + "-item-" + i);
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    producersDone.countDown();
                }
            });
        }

        for (int c = 0; c < numConsumers; c++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    while (true) {
                        final String item = queue.poll(Duration.ofMillis(100));
                        if (item != null) {
                            processedItems.add(item);
                            processedCount.incrementAndGet();
                            queue.done(item);
                        } else if (producersDone.await(0, TimeUnit.MILLISECONDS) && queue.length() == 0) {
                            break;
                        }
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        startLatch.countDown();
        producersDone.await(10, TimeUnit.SECONDS);
        Thread.sleep(500);
        queue.shutdown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(processedCount.get()).isEqualTo(numProducers * itemsPerProducer);
    }

    @Test
    void shouldInterruptBlockingGetOnShutdown() throws InterruptedException {
        final CountDownLatch waitingLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final AtomicInteger exceptionCount = new AtomicInteger(0);

        final Thread consumer = new Thread(() -> {
            waitingLatch.countDown();
            try {
                queue.get();
            } catch (final InterruptedException e) {
                exceptionCount.incrementAndGet();
            }
            completedLatch.countDown();
        });

        consumer.start();
        waitingLatch.await();
        Thread.sleep(50);

        queue.shutdown();
        completedLatch.await(2, TimeUnit.SECONDS);

        assertThat(exceptionCount.get()).isEqualTo(1);
    }

    @Test
    void shouldCloseViaAutoCloseable() throws Exception {
        try (final DefaultWorkQueue<String> autoCloseQueue = new DefaultWorkQueue<>()) {
            autoCloseQueue.add("item");
            assertThat(autoCloseQueue.isShuttingDown()).isFalse();
        }
    }

    @Test
    void shouldDeduplicateMultipleConcurrentAddsOfSameItem() throws InterruptedException {
        final int numThreads = 20;
        final ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    queue.add("same-item");
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(queue.length()).isEqualTo(1);

        final String item = queue.poll(Duration.ofSeconds(1));
        assertThat(item).isEqualTo("same-item");
        queue.done(item);

        assertThat(queue.poll(Duration.ofMillis(100))).isNull();
    }
}
