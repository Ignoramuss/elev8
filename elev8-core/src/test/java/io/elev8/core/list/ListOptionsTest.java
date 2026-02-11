package io.elev8.core.list;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListOptionsTest {

    @Nested
    class Defaults {

        @Test
        void shouldHaveNullFields() {
            final ListOptions options = ListOptions.defaults();

            assertThat(options.getLabelSelector()).isNull();
            assertThat(options.getFieldSelector()).isNull();
            assertThat(options.getLimit()).isNull();
            assertThat(options.getContinueToken()).isNull();
            assertThat(options.getResourceVersion()).isNull();
        }
    }

    @Nested
    class WithFieldSelector {

        @Test
        void shouldSetFieldSelector() {
            final ListOptions options = ListOptions.withFieldSelector("metadata.name=my-pod");

            assertThat(options.getFieldSelector()).isEqualTo("metadata.name=my-pod");
            assertThat(options.getLabelSelector()).isNull();
            assertThat(options.getLimit()).isNull();
        }

        @Test
        void shouldSupportCompositeFieldSelector() {
            final ListOptions options = ListOptions.withFieldSelector("status.phase=Running,metadata.namespace=default");

            assertThat(options.getFieldSelector()).isEqualTo("status.phase=Running,metadata.namespace=default");
        }
    }

    @Nested
    class WithLabelSelector {

        @Test
        void shouldSetLabelSelector() {
            final ListOptions options = ListOptions.withLabelSelector("app=myapp,env=prod");

            assertThat(options.getLabelSelector()).isEqualTo("app=myapp,env=prod");
            assertThat(options.getFieldSelector()).isNull();
        }
    }

    @Nested
    class WithLimit {

        @Test
        void shouldSetLimit() {
            final ListOptions options = ListOptions.withLimit(100);

            assertThat(options.getLimit()).isEqualTo(100);
            assertThat(options.getContinueToken()).isNull();
        }
    }

    @Nested
    class Builder {

        @Test
        void shouldBuildWithAllFields() {
            final ListOptions options = ListOptions.builder()
                    .labelSelector("app=myapp")
                    .fieldSelector("status.phase=Running")
                    .limit(50)
                    .continueToken("abc123")
                    .resourceVersion("12345")
                    .build();

            assertThat(options.getLabelSelector()).isEqualTo("app=myapp");
            assertThat(options.getFieldSelector()).isEqualTo("status.phase=Running");
            assertThat(options.getLimit()).isEqualTo(50);
            assertThat(options.getContinueToken()).isEqualTo("abc123");
            assertThat(options.getResourceVersion()).isEqualTo("12345");
        }

        @Test
        void shouldBuildWithLimitAndContinueToken() {
            final ListOptions options = ListOptions.builder()
                    .limit(10)
                    .continueToken("nextPageToken")
                    .build();

            assertThat(options.getLimit()).isEqualTo(10);
            assertThat(options.getContinueToken()).isEqualTo("nextPageToken");
        }
    }
}
