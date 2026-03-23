package io.elev8.core.selector;

import io.elev8.core.selector.LabelSelectorQuery.Operator;
import io.elev8.core.selector.LabelSelectorQuery.Requirement;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LabelSelectorQueryTest {

    @Nested
    class Equality {

        @Test
        void shouldProduceEqualsExpression() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .equals("app", "myapp")
                    .build();

            assertThat(query.toQueryString()).isEqualTo("app=myapp");
        }

        @Test
        void shouldProduceNotEqualsExpression() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .notEquals("env", "dev")
                    .build();

            assertThat(query.toQueryString()).isEqualTo("env!=dev");
        }

        @Test
        void shouldCombineMultipleEqualityRequirements() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .equals("app", "myapp")
                    .equals("env", "prod")
                    .build();

            assertThat(query.toQueryString()).isEqualTo("app=myapp,env=prod");
        }
    }

    @Nested
    class SetBased {

        @Test
        void shouldProduceInExpression() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .in("env", "staging", "prod")
                    .build();

            assertThat(query.toQueryString()).isEqualTo("env in (staging,prod)");
        }

        @Test
        void shouldProduceNotInExpression() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .notIn("tier", "backend", "database")
                    .build();

            assertThat(query.toQueryString()).isEqualTo("tier notin (backend,database)");
        }

        @Test
        void shouldHandleSingleValueIn() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .in("env", "prod")
                    .build();

            assertThat(query.toQueryString()).isEqualTo("env in (prod)");
        }

        @Test
        void shouldAcceptListOverloadForIn() {
            final List<String> values = Arrays.asList("frontend", "backend");
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .in("tier", values)
                    .build();

            assertThat(query.toQueryString()).isEqualTo("tier in (frontend,backend)");
        }

        @Test
        void shouldAcceptListOverloadForNotIn() {
            final List<String> values = Arrays.asList("alpha", "beta");
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .notIn("release", values)
                    .build();

            assertThat(query.toQueryString()).isEqualTo("release notin (alpha,beta)");
        }
    }

    @Nested
    class Existence {

        @Test
        void shouldProduceExistsExpression() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .exists("managed-by")
                    .build();

            assertThat(query.toQueryString()).isEqualTo("managed-by");
        }

        @Test
        void shouldProduceNotExistsExpression() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .notExists("deprecated")
                    .build();

            assertThat(query.toQueryString()).isEqualTo("!deprecated");
        }
    }

    @Nested
    class Combined {

        @Test
        void shouldCombineAllOperatorTypes() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .equals("app", "myapp")
                    .notEquals("env", "dev")
                    .in("tier", "frontend", "backend")
                    .notIn("version", "v1")
                    .exists("managed-by")
                    .notExists("deprecated")
                    .build();

            assertThat(query.toQueryString()).isEqualTo(
                    "app=myapp,env!=dev,tier in (frontend,backend),version notin (v1),managed-by,!deprecated"
            );
        }

        @Test
        void shouldMaintainInsertionOrder() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .exists("z-label")
                    .equals("a-label", "value")
                    .notIn("m-label", "x")
                    .build();

            assertThat(query.toQueryString()).isEqualTo("z-label,a-label=value,m-label notin (x)");
        }
    }

    @Nested
    class Validation {

        @Test
        void shouldRejectNullKeyInEquals() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().equals(null, "value"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("key");
        }

        @Test
        void shouldRejectEmptyKeyInEquals() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().equals("", "value"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("key");
        }

        @Test
        void shouldRejectNullValueInEquals() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().equals("app", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("value");
        }

        @Test
        void shouldRejectNullKeyInNotEquals() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().notEquals(null, "value"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("key");
        }

        @Test
        void shouldRejectNullValueInNotEquals() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().notEquals("app", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("value");
        }

        @Test
        void shouldRejectNullKeyInIn() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().in(null, "a"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("key");
        }

        @Test
        void shouldRejectEmptyValuesInIn() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().in("key"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Values");
        }

        @Test
        void shouldRejectNullArrayInIn() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().in("key", (String[]) null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Values");
        }

        @Test
        void shouldRejectNullListInIn() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().in("key", (List<String>) null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Values");
        }

        @Test
        void shouldRejectEmptyListInIn() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().in("key", Collections.emptyList()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Values");
        }

        @Test
        void shouldRejectNullElementInInArray() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().in("key", "a", null, "b"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        void shouldRejectNullElementInInList() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().in("key", Arrays.asList("a", null)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        void shouldRejectNullKeyInNotIn() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().notIn(null, "a"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("key");
        }

        @Test
        void shouldRejectEmptyValuesInNotIn() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().notIn("key"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Values");
        }

        @Test
        void shouldRejectNullKeyInExists() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().exists(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("key");
        }

        @Test
        void shouldRejectEmptyKeyInExists() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().exists(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("key");
        }

        @Test
        void shouldRejectNullKeyInNotExists() {
            assertThatThrownBy(() -> LabelSelectorQuery.builder().notExists(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("key");
        }
    }

    @Nested
    class StaticFactories {

        @Test
        void equalsShouldProduceCorrectQuery() {
            assertThat(LabelSelectorQuery.equals("app", "nginx").toQueryString())
                    .isEqualTo("app=nginx");
        }

        @Test
        void notEqualsShouldProduceCorrectQuery() {
            assertThat(LabelSelectorQuery.notEquals("env", "dev").toQueryString())
                    .isEqualTo("env!=dev");
        }

        @Test
        void existsShouldProduceCorrectQuery() {
            assertThat(LabelSelectorQuery.exists("managed-by").toQueryString())
                    .isEqualTo("managed-by");
        }

        @Test
        void notExistsShouldProduceCorrectQuery() {
            assertThat(LabelSelectorQuery.notExists("deprecated").toQueryString())
                    .isEqualTo("!deprecated");
        }

        @Test
        void inShouldProduceCorrectQuery() {
            assertThat(LabelSelectorQuery.in("env", "staging", "prod").toQueryString())
                    .isEqualTo("env in (staging,prod)");
        }

        @Test
        void notInShouldProduceCorrectQuery() {
            assertThat(LabelSelectorQuery.notIn("tier", "cache").toQueryString())
                    .isEqualTo("tier notin (cache)");
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void emptyBuilderShouldProduceEmptyString() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder().build();

            assertThat(query.toQueryString()).isEmpty();
        }

        @Test
        void emptyBuilderShouldBeEmpty() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder().build();

            assertThat(query.isEmpty()).isTrue();
        }

        @Test
        void nonEmptyQueryShouldNotBeEmpty() {
            final LabelSelectorQuery query = LabelSelectorQuery.exists("key");

            assertThat(query.isEmpty()).isFalse();
        }

        @Test
        void getRequirementsShouldReturnAllRequirements() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .equals("app", "myapp")
                    .exists("managed")
                    .build();

            assertThat(query.getRequirements()).hasSize(2);
            assertThat(query.getRequirements().get(0).getOperator()).isEqualTo(Operator.EQUALS);
            assertThat(query.getRequirements().get(1).getOperator()).isEqualTo(Operator.EXISTS);
        }

        @Test
        void getRequirementsShouldBeUnmodifiable() {
            final LabelSelectorQuery query = LabelSelectorQuery.equals("app", "myapp");

            assertThatThrownBy(() -> query.getRequirements().add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void toStringShouldMatchToQueryString() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .equals("app", "myapp")
                    .in("env", "staging", "prod")
                    .build();

            assertThat(query.toString()).isEqualTo(query.toQueryString());
        }

        @Test
        void emptyStringValueShouldBeAllowed() {
            final LabelSelectorQuery query = LabelSelectorQuery.builder()
                    .equals("label", "")
                    .build();

            assertThat(query.toQueryString()).isEqualTo("label=");
        }

        @Test
        void builderShouldBeReusable() {
            final LabelSelectorQuery.Builder builder = LabelSelectorQuery.builder()
                    .equals("app", "myapp");

            final LabelSelectorQuery first = builder.build();
            builder.exists("extra");
            final LabelSelectorQuery second = builder.build();

            assertThat(first.getRequirements()).hasSize(1);
            assertThat(second.getRequirements()).hasSize(2);
        }

        @Test
        void requirementShouldExposeKey() {
            final LabelSelectorQuery query = LabelSelectorQuery.equals("app", "myapp");
            final Requirement requirement = query.getRequirements().get(0);

            assertThat(requirement.getKey()).isEqualTo("app");
        }

        @Test
        void requirementShouldExposeValues() {
            final LabelSelectorQuery query = LabelSelectorQuery.in("env", "a", "b");
            final Requirement requirement = query.getRequirements().get(0);

            assertThat(requirement.getValues()).containsExactly("a", "b");
        }

        @Test
        void requirementValuesShouldBeUnmodifiable() {
            final LabelSelectorQuery query = LabelSelectorQuery.in("env", "a", "b");
            final Requirement requirement = query.getRequirements().get(0);

            assertThatThrownBy(() -> requirement.getValues().add("c"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void existsRequirementShouldHaveEmptyValues() {
            final LabelSelectorQuery query = LabelSelectorQuery.exists("key");
            final Requirement requirement = query.getRequirements().get(0);

            assertThat(requirement.getValues()).isEmpty();
        }
    }
}
