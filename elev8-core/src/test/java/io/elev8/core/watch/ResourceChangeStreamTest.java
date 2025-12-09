package io.elev8.core.watch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceChangeStreamTest {

    private ResourceChangeStream<String> stream;
    private AtomicBoolean closeCalled;

    @BeforeEach
    void setUp() {
        closeCalled = new AtomicBoolean(false);
        stream = new ResourceChangeStream<>(() -> closeCalled.set(true));
    }

    @AfterEach
    void tearDown() {
        if (stream != null && !stream.isClosed()) {
            stream.close();
        }
    }

    @Nested
    class Construction {

        @Test
        void shouldCreateWithDefaultCapacity() {
            final ResourceChangeStream<String> s = new ResourceChangeStream<>(null);
            assertThat(s.isClosed()).isFalse();
            assertThat(s.getError()).isNull();
            s.close();
        }

        @Test
        void shouldCreateWithCustomCapacity() {
            final ResourceChangeStream<String> s = new ResourceChangeStream<>(100, null);
            assertThat(s.isClosed()).isFalse();
            s.close();
        }

        @Test
        void shouldHandleZeroCapacity() {
            final ResourceChangeStream<String> s = new ResourceChangeStream<>(0, null);
            assertThat(s.isClosed()).isFalse();
            s.close();
        }

        @Test
        void shouldHandleNegativeCapacity() {
            final ResourceChangeStream<String> s = new ResourceChangeStream<>(-1, null);
            assertThat(s.isClosed()).isFalse();
            s.close();
        }
    }

    @Nested
    class Enqueue {

        @Test
        void shouldEnqueueEvent() {
            final ResourceChangeEvent<String> event = createEvent(ResourceChangeType.CREATED, "test");

            stream.enqueue(event);

            assertThat(stream.getQueueSize()).isEqualTo(1);
        }

        @Test
        void shouldIgnoreNullEvent() {
            stream.enqueue(null);

            assertThat(stream.getQueueSize()).isEqualTo(0);
        }

        @Test
        void shouldIgnoreEventWhenClosed() {
            stream.close();
            stream.enqueue(createEvent(ResourceChangeType.CREATED, "test"));

            assertThat(stream.getQueueSize()).isEqualTo(0);
        }
    }

    @Nested
    class HasNext {

        @Test
        void shouldReturnTrueWhenEventsAvailable() {
            stream.enqueue(createEvent(ResourceChangeType.CREATED, "test"));

            assertThat(stream.hasNext()).isTrue();
        }

        @Test
        void shouldReturnTrueWhenOpenWithNoEvents() {
            assertThat(stream.hasNext()).isTrue();
        }

        @Test
        void shouldReturnFalseWhenClosedWithNoEvents() {
            stream.signalClose();

            assertThat(stream.hasNext()).isFalse();
        }

        @Test
        void shouldReturnTrueWhenClosedWithEvents() {
            stream.enqueue(createEvent(ResourceChangeType.CREATED, "test"));
            stream.signalClose();

            assertThat(stream.hasNext()).isTrue();
        }

        @Test
        void shouldThrowWhenErrorOccurred() {
            stream.setError(new RuntimeException("test error"));

            assertThatThrownBy(() -> stream.hasNext())
                    .isInstanceOf(WatchStreamException.class)
                    .hasMessageContaining("Watch stream error");
        }
    }

    @Nested
    class Next {

        @Test
        void shouldReturnEnqueuedEvent() {
            final ResourceChangeEvent<String> event = createEvent(ResourceChangeType.CREATED, "test");
            stream.enqueue(event);

            final ResourceChangeEvent<String> result = stream.next();

            assertThat(result).isSameAs(event);
        }

        @Test
        void shouldThrowWhenClosedWithNoEvents() {
            stream.signalClose();

            assertThatThrownBy(() -> stream.next())
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        void shouldDrainQueueAfterClose() {
            stream.enqueue(createEvent(ResourceChangeType.CREATED, "first"));
            stream.enqueue(createEvent(ResourceChangeType.UPDATED, "second"));
            stream.signalClose();

            assertThat(stream.next().getResource()).isEqualTo("first");
            assertThat(stream.next().getResource()).isEqualTo("second");
            assertThat(stream.hasNext()).isFalse();
        }
    }

    @Nested
    class Poll {

        @Test
        void shouldReturnEventImmediately() throws InterruptedException {
            final ResourceChangeEvent<String> event = createEvent(ResourceChangeType.CREATED, "test");
            stream.enqueue(event);

            final ResourceChangeEvent<String> result = stream.poll(1, TimeUnit.SECONDS);

            assertThat(result).isSameAs(event);
        }

        @Test
        void shouldReturnNullOnTimeout() throws InterruptedException {
            final ResourceChangeEvent<String> result = stream.poll(50, TimeUnit.MILLISECONDS);

            assertThat(result).isNull();
        }

        @Test
        void shouldThrowWhenErrorOccurred() {
            stream.setError(new RuntimeException("test error"));

            assertThatThrownBy(() -> stream.poll(1, TimeUnit.SECONDS))
                    .isInstanceOf(WatchStreamException.class);
        }
    }

    @Nested
    class StreamConversion {

        @Test
        void shouldConvertToJavaStream() {
            stream.enqueue(createEvent(ResourceChangeType.CREATED, "first"));
            stream.enqueue(createEvent(ResourceChangeType.UPDATED, "second"));
            stream.signalClose();

            final List<String> resources = new ArrayList<>();
            stream.stream().forEach(e -> resources.add(e.getResource()));

            assertThat(resources).containsExactly("first", "second");
        }

        @Test
        void shouldFilterByType() {
            stream.enqueue(createEvent(ResourceChangeType.CREATED, "created"));
            stream.enqueue(createEvent(ResourceChangeType.UPDATED, "updated"));
            stream.enqueue(createEvent(ResourceChangeType.DELETED, "deleted"));
            stream.signalClose();

            final List<String> resources = new ArrayList<>();
            stream.stream(ResourceChangeType.CREATED, ResourceChangeType.DELETED)
                    .forEach(e -> resources.add(e.getResource()));

            assertThat(resources).containsExactly("created", "deleted");
        }

        @Test
        void shouldFilterByPredicate() {
            stream.enqueue(createEvent(ResourceChangeType.CREATED, "match"));
            stream.enqueue(createEvent(ResourceChangeType.CREATED, "no-match"));
            stream.enqueue(createEvent(ResourceChangeType.CREATED, "match-again"));
            stream.signalClose();

            final List<String> resources = new ArrayList<>();
            stream.stream(e -> e.getResource().startsWith("match"))
                    .forEach(e -> resources.add(e.getResource()));

            assertThat(resources).containsExactly("match", "match-again");
        }

        @Test
        void shouldHandleNullTypesArray() {
            stream.enqueue(createEvent(ResourceChangeType.CREATED, "test"));
            stream.signalClose();

            final Stream<ResourceChangeEvent<String>> result = stream.stream((ResourceChangeType[]) null);

            assertThat(result.count()).isEqualTo(1);
        }

        @Test
        void shouldHandleEmptyTypesArray() {
            stream.enqueue(createEvent(ResourceChangeType.CREATED, "test"));
            stream.signalClose();

            final Stream<ResourceChangeEvent<String>> result = stream.stream();

            assertThat(result.count()).isEqualTo(1);
        }
    }

    @Nested
    class Close {

        @Test
        void shouldInvokeCloseCallback() {
            stream.close();

            assertThat(closeCalled.get()).isTrue();
            assertThat(stream.isClosed()).isTrue();
        }

        @Test
        void shouldBeIdempotent() {
            stream.close();
            stream.close();

            assertThat(stream.isClosed()).isTrue();
        }

        @Test
        void shouldCloseFromStreamClose() {
            stream.enqueue(createEvent(ResourceChangeType.CREATED, "test"));
            stream.signalClose();

            stream.stream().close();

            assertThat(stream.isClosed()).isTrue();
        }
    }

    @Nested
    class ErrorHandling {

        @Test
        void shouldSetErrorState() {
            final Exception error = new RuntimeException("test error");

            stream.setError(error);

            assertThat(stream.isClosed()).isTrue();
            assertThat(stream.getError()).isSameAs(error);
        }

        @Test
        void shouldPropagateErrorOnHasNext() {
            stream.setError(new RuntimeException("error"));

            assertThatThrownBy(() -> stream.hasNext())
                    .isInstanceOf(WatchStreamException.class)
                    .hasCauseInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    class Iteration {

        @Test
        void shouldIterateWithForEach() {
            stream.enqueue(createEvent(ResourceChangeType.CREATED, "a"));
            stream.enqueue(createEvent(ResourceChangeType.UPDATED, "b"));
            stream.enqueue(createEvent(ResourceChangeType.DELETED, "c"));
            stream.signalClose();

            final List<String> resources = new ArrayList<>();
            for (final ResourceChangeEvent<String> event : stream) {
                resources.add(event.getResource());
            }

            assertThat(resources).containsExactly("a", "b", "c");
        }

        @Test
        void shouldReturnSelfAsIterator() {
            assertThat(stream.iterator()).isSameAs(stream);
        }
    }

    @Nested
    class QueueSize {

        @Test
        void shouldReportCorrectSize() {
            assertThat(stream.getQueueSize()).isEqualTo(0);

            stream.enqueue(createEvent(ResourceChangeType.CREATED, "a"));
            assertThat(stream.getQueueSize()).isEqualTo(1);

            stream.enqueue(createEvent(ResourceChangeType.CREATED, "b"));
            assertThat(stream.getQueueSize()).isEqualTo(2);
        }
    }

    @Nested
    class Concurrency {

        @Test
        void shouldHandleConcurrentEnqueueAndConsume() throws InterruptedException {
            final CountDownLatch latch = new CountDownLatch(1);
            final List<String> consumed = new ArrayList<>();

            final Thread producer = new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    stream.enqueue(createEvent(ResourceChangeType.CREATED, "event-" + i));
                }
                stream.signalClose();
            });

            final Thread consumer = new Thread(() -> {
                try {
                    for (final ResourceChangeEvent<String> event : stream) {
                        consumed.add(event.getResource());
                    }
                } finally {
                    latch.countDown();
                }
            });

            producer.start();
            consumer.start();

            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(consumed).hasSize(100);
        }
    }

    private ResourceChangeEvent<String> createEvent(final ResourceChangeType type, final String resource) {
        return ResourceChangeEvent.<String>builder()
                .type(type)
                .resource(resource)
                .build();
    }
}
