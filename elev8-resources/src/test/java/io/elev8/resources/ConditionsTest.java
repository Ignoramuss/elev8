package io.elev8.resources;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionsTest {

    @Nested
    class FindByType {

        @Test
        void shouldFindExistingCondition() {
            final List<TestCondition> conditions = Arrays.asList(
                    new TestCondition("Ready", "True", "2024-01-15T10:00:00Z", "AllPodsReady", "All pods are ready"),
                    new TestCondition("Available", "True", "2024-01-15T10:00:00Z", "MinimumReplicasAvailable", "Minimum replicas available")
            );

            final Optional<TestCondition> result = Conditions.findByType(conditions, "Ready");

            assertThat(result).isPresent();
            assertThat(result.get().getType()).isEqualTo("Ready");
            assertThat(result.get().getStatus()).isEqualTo("True");
        }

        @Test
        void shouldReturnEmptyForMissingCondition() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", null, null, null)
            );

            final Optional<TestCondition> result = Conditions.findByType(conditions, "Available");

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyForNullList() {
            final Optional<TestCondition> result = Conditions.findByType(null, "Ready");

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyForNullType() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", null, null, null)
            );

            final Optional<TestCondition> result = Conditions.findByType(conditions, null);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyForEmptyList() {
            final Optional<TestCondition> result = Conditions.findByType(Collections.emptyList(), "Ready");

            assertThat(result).isEmpty();
        }

        @Test
        void shouldHandleNullElementsInList() {
            final List<TestCondition> conditions = Arrays.asList(
                    null,
                    new TestCondition("Ready", "True", null, null, null),
                    null
            );

            final Optional<TestCondition> result = Conditions.findByType(conditions, "Ready");

            assertThat(result).isPresent();
            assertThat(result.get().getType()).isEqualTo("Ready");
        }
    }

    @Nested
    class HasCondition {

        @Test
        void shouldReturnTrueForExistingCondition() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", null, null, null)
            );

            assertThat(Conditions.hasCondition(conditions, "Ready")).isTrue();
        }

        @Test
        void shouldReturnFalseForMissingCondition() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", null, null, null)
            );

            assertThat(Conditions.hasCondition(conditions, "Available")).isFalse();
        }

        @Test
        void shouldReturnFalseForNullList() {
            assertThat(Conditions.hasCondition(null, "Ready")).isFalse();
        }

        @Test
        void shouldReturnFalseForEmptyList() {
            assertThat(Conditions.hasCondition(Collections.emptyList(), "Ready")).isFalse();
        }
    }

    @Nested
    class IsTrue {

        @Test
        void shouldReturnTrueWhenStatusIsTrue() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", null, null, null)
            );

            assertThat(Conditions.isTrue(conditions, "Ready")).isTrue();
        }

        @Test
        void shouldReturnFalseWhenStatusIsFalse() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "False", null, null, null)
            );

            assertThat(Conditions.isTrue(conditions, "Ready")).isFalse();
        }

        @Test
        void shouldReturnFalseWhenStatusIsUnknown() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "Unknown", null, null, null)
            );

            assertThat(Conditions.isTrue(conditions, "Ready")).isFalse();
        }

        @Test
        void shouldReturnFalseWhenConditionNotFound() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", null, null, null)
            );

            assertThat(Conditions.isTrue(conditions, "Available")).isFalse();
        }

        @Test
        void shouldReturnFalseForNullList() {
            assertThat(Conditions.isTrue(null, "Ready")).isFalse();
        }

        @Test
        void shouldReturnFalseWhenStatusIsNull() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", null, null, null, null)
            );

            assertThat(Conditions.isTrue(conditions, "Ready")).isFalse();
        }
    }

    @Nested
    class IsFalse {

        @Test
        void shouldReturnTrueWhenStatusIsFalse() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "False", null, null, null)
            );

            assertThat(Conditions.isFalse(conditions, "Ready")).isTrue();
        }

        @Test
        void shouldReturnFalseWhenStatusIsTrue() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", null, null, null)
            );

            assertThat(Conditions.isFalse(conditions, "Ready")).isFalse();
        }

        @Test
        void shouldReturnFalseWhenStatusIsUnknown() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "Unknown", null, null, null)
            );

            assertThat(Conditions.isFalse(conditions, "Ready")).isFalse();
        }

        @Test
        void shouldReturnFalseWhenConditionNotFound() {
            assertThat(Conditions.isFalse(Collections.emptyList(), "Ready")).isFalse();
        }

        @Test
        void shouldReturnFalseForNullList() {
            assertThat(Conditions.isFalse(null, "Ready")).isFalse();
        }
    }

    @Nested
    class IsUnknown {

        @Test
        void shouldReturnTrueWhenStatusIsUnknown() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "Unknown", null, null, null)
            );

            assertThat(Conditions.isUnknown(conditions, "Ready")).isTrue();
        }

        @Test
        void shouldReturnFalseWhenStatusIsTrue() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", null, null, null)
            );

            assertThat(Conditions.isUnknown(conditions, "Ready")).isFalse();
        }

        @Test
        void shouldReturnFalseWhenStatusIsFalse() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "False", null, null, null)
            );

            assertThat(Conditions.isUnknown(conditions, "Ready")).isFalse();
        }

        @Test
        void shouldReturnFalseWhenConditionNotFound() {
            assertThat(Conditions.isUnknown(Collections.emptyList(), "Ready")).isFalse();
        }

        @Test
        void shouldReturnFalseForNullList() {
            assertThat(Conditions.isUnknown(null, "Ready")).isFalse();
        }
    }

    @Nested
    class GetStatus {

        @Test
        void shouldReturnStatusWhenConditionExists() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", null, null, null)
            );

            final Optional<String> result = Conditions.getStatus(conditions, "Ready");

            assertThat(result).contains("True");
        }

        @Test
        void shouldReturnEmptyWhenConditionNotFound() {
            final Optional<String> result = Conditions.getStatus(Collections.emptyList(), "Ready");

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyForNullList() {
            assertThat(Conditions.getStatus(null, "Ready")).isEmpty();
        }
    }

    @Nested
    class GetReason {

        @Test
        void shouldReturnReasonWhenConditionExists() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", null, "AllPodsReady", null)
            );

            final Optional<String> result = Conditions.getReason(conditions, "Ready");

            assertThat(result).contains("AllPodsReady");
        }

        @Test
        void shouldReturnEmptyWhenReasonIsNull() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", null, null, null)
            );

            final Optional<String> result = Conditions.getReason(conditions, "Ready");

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyWhenConditionNotFound() {
            final Optional<String> result = Conditions.getReason(Collections.emptyList(), "Ready");

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyForNullList() {
            assertThat(Conditions.getReason(null, "Ready")).isEmpty();
        }
    }

    @Nested
    class GetMessage {

        @Test
        void shouldReturnMessageWhenConditionExists() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", null, null, "All pods are ready")
            );

            final Optional<String> result = Conditions.getMessage(conditions, "Ready");

            assertThat(result).contains("All pods are ready");
        }

        @Test
        void shouldReturnEmptyWhenMessageIsNull() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", null, null, null)
            );

            final Optional<String> result = Conditions.getMessage(conditions, "Ready");

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyWhenConditionNotFound() {
            final Optional<String> result = Conditions.getMessage(Collections.emptyList(), "Ready");

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyForNullList() {
            assertThat(Conditions.getMessage(null, "Ready")).isEmpty();
        }
    }

    @Nested
    class GetLastTransitionTime {

        @Test
        void shouldReturnLastTransitionTimeWhenConditionExists() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", "2024-01-15T10:00:00Z", null, null)
            );

            final Optional<String> result = Conditions.getLastTransitionTime(conditions, "Ready");

            assertThat(result).contains("2024-01-15T10:00:00Z");
        }

        @Test
        void shouldReturnEmptyWhenLastTransitionTimeIsNull() {
            final List<TestCondition> conditions = Collections.singletonList(
                    new TestCondition("Ready", "True", null, null, null)
            );

            final Optional<String> result = Conditions.getLastTransitionTime(conditions, "Ready");

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyWhenConditionNotFound() {
            final Optional<String> result = Conditions.getLastTransitionTime(Collections.emptyList(), "Ready");

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyForNullList() {
            assertThat(Conditions.getLastTransitionTime(null, "Ready")).isEmpty();
        }
    }

    @Nested
    class StatusConstants {

        @Test
        void shouldHaveCorrectStatusConstants() {
            assertThat(Conditions.STATUS_TRUE).isEqualTo("True");
            assertThat(Conditions.STATUS_FALSE).isEqualTo("False");
            assertThat(Conditions.STATUS_UNKNOWN).isEqualTo("Unknown");
        }
    }

    private static class TestCondition implements Condition {
        private final String type;
        private final String status;
        private final String lastTransitionTime;
        private final String reason;
        private final String message;

        TestCondition(final String type, final String status, final String lastTransitionTime,
                final String reason, final String message) {
            this.type = type;
            this.status = status;
            this.lastTransitionTime = lastTransitionTime;
            this.reason = reason;
            this.message = message;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getStatus() {
            return status;
        }

        @Override
        public String getLastTransitionTime() {
            return lastTransitionTime;
        }

        @Override
        public String getReason() {
            return reason;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
