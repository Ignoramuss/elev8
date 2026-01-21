package io.elev8.resources.workqueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultRateLimitingWorkQueueTest {

    private DefaultRateLimitingWorkQueue<String> queue;

    @BeforeEach
    void setUp() {
        queue = new DefaultRateLimitingWorkQueue<>(
                new ExponentialBackoffRateLimiter<>(Duration.ofMillis(10), Duration.ofMillis(100))
        );
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
    void shouldAddItemWithRateLimitedDelay() throws InterruptedException {
        queue.addRateLimited("item");

        assertThat(queue.poll(Duration.ofMillis(5))).isNull();

        final String result = queue.poll(Duration.ofMillis(50));

        assertThat(result).isEqualTo("item");
    }

    @Test
    void shouldTrackRequeueCount() {
        queue.addRateLimited("item");
        assertThat(queue.numRequeues("item")).isEqualTo(1);

        queue.addRateLimited("item");
        assertThat(queue.numRequeues("item")).isEqualTo(2);

        queue.addRateLimited("item");
        assertThat(queue.numRequeues("item")).isEqualTo(3);
    }

    @Test
    void shouldResetRequeueCountOnForget() {
        queue.addRateLimited("item");
        queue.addRateLimited("item");
        assertThat(queue.numRequeues("item")).isEqualTo(2);

        queue.forget("item");

        assertThat(queue.numRequeues("item")).isEqualTo(0);
    }

    @Test
    void shouldSupportAddAfter() throws InterruptedException {
        queue.addAfter("item", Duration.ofMillis(50));

        assertThat(queue.poll(Duration.ofMillis(10))).isNull();

        final String result = queue.poll(Duration.ofMillis(100));
        assertThat(result).isEqualTo("item");
    }

    @Test
    void shouldDelegateToUnderlyingQueue() throws InterruptedException {
        queue.add("item");
        assertThat(queue.length()).isEqualTo(1);

        final String item = queue.get();
        assertThat(item).isEqualTo("item");

        queue.done(item);
        assertThat(queue.length()).isEqualTo(0);
    }

    @Test
    void shouldRejectNullInAddRateLimited() {
        assertThatThrownBy(() -> queue.addRateLimited(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    void shouldRejectNullDelegate() {
        assertThatThrownBy(() -> new DefaultRateLimitingWorkQueue<>(null, new ExponentialBackoffRateLimiter<>()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullRateLimiter() {
        assertThatThrownBy(() -> new DefaultRateLimitingWorkQueue<>(new DefaultDelayingWorkQueue<>(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCreateWithDefaultSettings() throws InterruptedException {
        final DefaultRateLimitingWorkQueue<String> defaultQueue = new DefaultRateLimitingWorkQueue<>();

        try {
            defaultQueue.add("item");
            final String result = defaultQueue.poll(Duration.ofSeconds(1));
            assertThat(result).isEqualTo("item");
        } finally {
            defaultQueue.shutdown();
        }
    }

    @Test
    void shouldHandleTypicalControllerPattern() throws InterruptedException {
        final AtomicInteger processCount = new AtomicInteger(0);
        final AtomicInteger successCount = new AtomicInteger(0);
        final CountDownLatch done = new CountDownLatch(1);

        queue.add("item");

        final Thread worker = new Thread(() -> {
            try {
                while (!queue.isShuttingDown()) {
                    final String key = queue.poll(Duration.ofMillis(50));
                    if (key == null) {
                        if (successCount.get() > 0) {
                            break;
                        }
                        continue;
                    }
                    try {
                        final int count = processCount.incrementAndGet();
                        if (count < 3) {
                            throw new RuntimeException("Simulated failure");
                        }
                        successCount.incrementAndGet();
                        queue.forget(key);
                    } catch (final RuntimeException e) {
                        queue.addRateLimited(key);
                    } finally {
                        queue.done(key);
                    }
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });

        worker.start();
        done.await(5, TimeUnit.SECONDS);
        queue.shutdown();

        assertThat(processCount.get()).isEqualTo(3);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(queue.numRequeues("item")).isEqualTo(0);
    }

    @Test
    void shouldSupportShutdown() {
        queue.add("item");
        assertThat(queue.isShuttingDown()).isFalse();

        queue.shutdown();

        assertThat(queue.isShuttingDown()).isTrue();
    }

    @Test
    void shouldCloseViaAutoCloseable() throws Exception {
        try (final DefaultRateLimitingWorkQueue<String> autoCloseQueue = new DefaultRateLimitingWorkQueue<>()) {
            autoCloseQueue.add("item");
            assertThat(autoCloseQueue.isShuttingDown()).isFalse();
        }
    }
}
