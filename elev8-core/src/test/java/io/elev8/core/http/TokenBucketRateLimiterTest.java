package io.elev8.core.http;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenBucketRateLimiterTest {

    @Test
    void shouldThrowOnInvalidConfig() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(-1.0)
                .build();

        assertThatThrownBy(() -> new TokenBucketRateLimiter(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldInitializeWithBurstCapacity() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(10.0)
                .burstCapacity(5)
                .build();

        final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(config);

        assertThat(limiter.getAvailableTokens()).isEqualTo(5.0);
    }

    @Test
    void shouldAcquireTokensImmediatelyWhenAvailable() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(10.0)
                .burstCapacity(5)
                .build();

        final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(config);

        final long startTime = System.currentTimeMillis();
        limiter.acquire();
        final long elapsed = System.currentTimeMillis() - startTime;

        assertThat(elapsed).isLessThan(100);
        assertThat(limiter.getAvailableTokens()).isCloseTo(4.0, org.assertj.core.data.Offset.offset(0.5));
    }

    @Test
    void shouldConsumeTokensOnAcquire() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(10.0)
                .burstCapacity(3)
                .build();

        final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(config);

        limiter.acquire();
        limiter.acquire();
        limiter.acquire();

        assertThat(limiter.getAvailableTokens()).isCloseTo(0.0, org.assertj.core.data.Offset.offset(0.5));
    }

    @Test
    void shouldBlockWhenNoTokensAvailable() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(10.0)
                .burstCapacity(1)
                .build();

        final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(config);

        limiter.acquire();

        final long startTime = System.currentTimeMillis();
        limiter.acquire();
        final long elapsed = System.currentTimeMillis() - startTime;

        assertThat(elapsed).isGreaterThanOrEqualTo(50);
    }

    @Test
    void shouldTryAcquireReturnTrueWhenTokenAvailable() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(10.0)
                .burstCapacity(5)
                .build();

        final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(config);

        final boolean acquired = limiter.tryAcquire(Duration.ofMillis(100));

        assertThat(acquired).isTrue();
    }

    @Test
    void shouldTryAcquireReturnFalseOnTimeout() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(1.0)
                .burstCapacity(1)
                .build();

        final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(config);

        limiter.acquire();

        final long startTime = System.currentTimeMillis();
        final boolean acquired = limiter.tryAcquire(Duration.ofMillis(50));
        final long elapsed = System.currentTimeMillis() - startTime;

        assertThat(acquired).isFalse();
        assertThat(elapsed).isGreaterThanOrEqualTo(40).isLessThan(200);
    }

    @Test
    void shouldTryAcquireSucceedWithSufficientTimeout() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(100.0)
                .burstCapacity(1)
                .build();

        final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(config);

        limiter.acquire();

        final boolean acquired = limiter.tryAcquire(Duration.ofMillis(500));

        assertThat(acquired).isTrue();
    }

    @Test
    void shouldRefillTokensOverTime() throws InterruptedException {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(100.0)
                .burstCapacity(10)
                .build();

        final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(config);

        for (int i = 0; i < 10; i++) {
            limiter.acquire();
        }

        assertThat(limiter.getAvailableTokens()).isCloseTo(0.0, org.assertj.core.data.Offset.offset(0.5));

        Thread.sleep(50);

        assertThat(limiter.getAvailableTokens()).isGreaterThan(3.0);
    }

    @Test
    void shouldNotExceedBurstCapacity() throws InterruptedException {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(1000.0)
                .burstCapacity(10)
                .build();

        final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(config);

        Thread.sleep(200);

        assertThat(limiter.getAvailableTokens()).isEqualTo(10.0);
    }

    @Test
    void shouldHandleConcurrentAccess() throws InterruptedException {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(1000.0)
                .burstCapacity(100)
                .build();

        final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(config);
        final int threadCount = 10;
        final int acquiresPerThread = 5;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(threadCount);
        final AtomicInteger successCount = new AtomicInteger(0);

        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < acquiresPerThread; j++) {
                        limiter.acquire();
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        final boolean completed = doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(threadCount * acquiresPerThread);
    }

    @Test
    void shouldHandleHighThroughput() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(10000.0)
                .burstCapacity(100)
                .build();

        final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(config);

        final long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            limiter.acquire();
        }

        final long elapsed = System.currentTimeMillis() - startTime;

        assertThat(elapsed).isLessThan(500);
    }

    @Test
    void shouldRespectRateLimit() {
        final RateLimiterConfig config = RateLimiterConfig.builder()
                .requestsPerSecond(50.0)
                .burstCapacity(5)
                .build();

        final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(config);

        final long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            limiter.acquire();
        }

        final long elapsed = System.currentTimeMillis() - startTime;

        assertThat(elapsed).isGreaterThanOrEqualTo(50);
    }
}
