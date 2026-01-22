package io.elev8.resources.workqueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultDelayingWorkQueueTest {

    private DefaultDelayingWorkQueue<String> queue;

    @BeforeEach
    void setUp() {
        queue = new DefaultDelayingWorkQueue<>();
    }

    @AfterEach
    void tearDown() {
        queue.shutdown();
    }

    @Test
    void shouldAddItemImmediately() throws InterruptedException {
        queue.add("item");

        final String result = queue.poll(Duration.ofSeconds(1));

        assertThat(result).isEqualTo("item");
    }

    @Test
    void shouldAddItemAfterDelay() throws InterruptedException {
        queue.addAfter("delayed-item", Duration.ofMillis(100));

        assertThat(queue.poll(Duration.ofMillis(50))).isNull();

        final String result = queue.poll(Duration.ofMillis(200));
        assertThat(result).isEqualTo("delayed-item");
    }

    @Test
    void shouldAddImmediatelyForZeroDelay() throws InterruptedException {
        queue.addAfter("item", Duration.ZERO);

        final String result = queue.poll(Duration.ofSeconds(1));

        assertThat(result).isEqualTo("item");
    }

    @Test
    void shouldAddImmediatelyForNegativeDelay() throws InterruptedException {
        queue.addAfter("item", Duration.ofMillis(-100));

        final String result = queue.poll(Duration.ofSeconds(1));

        assertThat(result).isEqualTo("item");
    }

    @Test
    void shouldCancelPreviousDelayWhenAddingWithNewDelay() throws InterruptedException {
        queue.addAfter("item", Duration.ofMillis(500));
        assertThat(queue.pendingDelayCount()).isEqualTo(1);

        Thread.sleep(50);
        queue.addAfter("item", Duration.ofMillis(100));

        final long start = System.currentTimeMillis();
        final String result = queue.poll(Duration.ofSeconds(2));
        final long elapsed = System.currentTimeMillis() - start;

        assertThat(result).isEqualTo("item");
        assertThat(elapsed).isLessThan(400);
    }

    @Test
    void shouldCancelDelayWhenAddingImmediately() throws InterruptedException {
        queue.addAfter("item", Duration.ofMillis(500));
        assertThat(queue.pendingDelayCount()).isEqualTo(1);

        Thread.sleep(50);
        queue.add("item");

        final String result = queue.poll(Duration.ofMillis(100));

        assertThat(result).isEqualTo("item");
        assertThat(queue.pendingDelayCount()).isEqualTo(0);
    }

    @Test
    void shouldHandleMultipleDelayedItems() throws InterruptedException {
        queue.addAfter("first", Duration.ofMillis(50));
        queue.addAfter("second", Duration.ofMillis(100));
        queue.addAfter("third", Duration.ofMillis(150));

        assertThat(queue.pendingDelayCount()).isEqualTo(3);

        final String first = queue.poll(Duration.ofMillis(200));
        queue.done(first);
        assertThat(first).isEqualTo("first");

        final String second = queue.poll(Duration.ofMillis(200));
        queue.done(second);
        assertThat(second).isEqualTo("second");

        final String third = queue.poll(Duration.ofMillis(200));
        queue.done(third);
        assertThat(third).isEqualTo("third");
    }

    @Test
    void shouldCancelAllPendingOnShutdown() throws InterruptedException {
        queue.addAfter("item1", Duration.ofSeconds(10));
        queue.addAfter("item2", Duration.ofSeconds(10));
        queue.addAfter("item3", Duration.ofSeconds(10));

        assertThat(queue.pendingDelayCount()).isEqualTo(3);

        queue.shutdown();

        assertThat(queue.pendingDelayCount()).isEqualTo(0);
        assertThat(queue.isShuttingDown()).isTrue();
    }

    @Test
    void shouldRejectAddAfterShutdown() throws InterruptedException {
        queue.shutdown();

        queue.add("item");
        queue.addAfter("delayed", Duration.ofMillis(100));

        assertThat(queue.length()).isEqualTo(0);
        assertThat(queue.pendingDelayCount()).isEqualTo(0);
    }

    @Test
    void shouldRejectNullItemInAddAfter() {
        assertThatThrownBy(() -> queue.addAfter(null, Duration.ofMillis(100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    void shouldRejectNullDuration() {
        assertThatThrownBy(() -> queue.addAfter("item", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    void shouldSupportConcurrentDelayedAdds() throws InterruptedException {
        final int numThreads = 10;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    queue.addAfter("item-" + index, Duration.ofMillis(50));
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);

        Thread.sleep(150);

        int count = 0;
        while (queue.poll(Duration.ofMillis(50)) != null) {
            count++;
        }

        assertThat(count).isEqualTo(numThreads);
    }

    @Test
    void shouldDelegateToUnderlyingQueue() throws InterruptedException {
        final DefaultWorkQueue<String> underlyingQueue = new DefaultWorkQueue<>();
        final DefaultDelayingWorkQueue<String> delayingQueue = new DefaultDelayingWorkQueue<>(underlyingQueue);

        try {
            delayingQueue.add("item");
            assertThat(underlyingQueue.length()).isEqualTo(1);

            final String item = delayingQueue.poll(Duration.ofSeconds(1));
            assertThat(item).isEqualTo("item");

            delayingQueue.done(item);
        } finally {
            delayingQueue.shutdown();
        }
    }

    @Test
    void shouldCloseViaAutoCloseable() throws Exception {
        try (final DefaultDelayingWorkQueue<String> autoCloseQueue = new DefaultDelayingWorkQueue<>()) {
            autoCloseQueue.add("item");
            assertThat(autoCloseQueue.isShuttingDown()).isFalse();
        }
    }

    @Test
    void shouldDeduplicatePendingDelayedItems() throws InterruptedException {
        queue.addAfter("same-item", Duration.ofMillis(100));
        queue.addAfter("same-item", Duration.ofMillis(100));
        queue.addAfter("same-item", Duration.ofMillis(100));

        assertThat(queue.pendingDelayCount()).isEqualTo(1);

        final String item = queue.poll(Duration.ofMillis(500));
        assertThat(item).isEqualTo("same-item");

        assertThat(queue.poll(Duration.ofMillis(100))).isNull();
    }
}
