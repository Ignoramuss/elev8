package io.elev8.resources.aggregation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceTypeTest {

    @Nested
    class Metadata {

        @Test
        void podShouldHaveCorrectMetadata() {
            assertThat(ResourceType.POD.getApiVersion()).isEqualTo("v1");
            assertThat(ResourceType.POD.getKind()).isEqualTo("Pod");
            assertThat(ResourceType.POD.getPlural()).isEqualTo("pods");
            assertThat(ResourceType.POD.getScope()).isEqualTo(ResourceType.Scope.NAMESPACED);
        }

        @Test
        void deploymentShouldHaveCorrectMetadata() {
            assertThat(ResourceType.DEPLOYMENT.getApiVersion()).isEqualTo("apps/v1");
            assertThat(ResourceType.DEPLOYMENT.getKind()).isEqualTo("Deployment");
            assertThat(ResourceType.DEPLOYMENT.getPlural()).isEqualTo("deployments");
        }

        @Test
        void namespaceShouldHaveCorrectMetadata() {
            assertThat(ResourceType.NAMESPACE.getApiVersion()).isEqualTo("v1");
            assertThat(ResourceType.NAMESPACE.getKind()).isEqualTo("Namespace");
            assertThat(ResourceType.NAMESPACE.getPlural()).isEqualTo("namespaces");
            assertThat(ResourceType.NAMESPACE.getScope()).isEqualTo(ResourceType.Scope.CLUSTER);
        }

        @Test
        void crdShouldHaveCorrectMetadata() {
            assertThat(ResourceType.CUSTOM_RESOURCE_DEFINITION.getApiVersion()).isEqualTo("apiextensions.k8s.io/v1");
            assertThat(ResourceType.CUSTOM_RESOURCE_DEFINITION.getKind()).isEqualTo("CustomResourceDefinition");
        }
    }

    @Nested
    class ScopeConvenience {

        @Test
        void namespacedTypeShouldReturnTrueForIsNamespaced() {
            assertThat(ResourceType.POD.isNamespaced()).isTrue();
            assertThat(ResourceType.POD.isClusterScoped()).isFalse();
        }

        @Test
        void clusterTypeShouldReturnTrueForIsClusterScoped() {
            assertThat(ResourceType.NAMESPACE.isClusterScoped()).isTrue();
            assertThat(ResourceType.NAMESPACE.isNamespaced()).isFalse();
        }

        @Test
        void allTypesShouldHaveExactlyOneScope() {
            for (final ResourceType type : ResourceType.values()) {
                assertThat(type.isNamespaced() ^ type.isClusterScoped())
                        .as("Type %s should be exactly one scope", type)
                        .isTrue();
            }
        }
    }

    @Nested
    class StaticGroups {

        @Test
        void commonShouldContainExpectedTypes() {
            assertThat(ResourceType.COMMON).containsExactlyInAnyOrder(
                    ResourceType.POD, ResourceType.SERVICE, ResourceType.DEPLOYMENT,
                    ResourceType.DAEMON_SET, ResourceType.STATEFUL_SET, ResourceType.REPLICA_SET,
                    ResourceType.JOB, ResourceType.CRON_JOB, ResourceType.HORIZONTAL_POD_AUTOSCALER
            );
        }

        @Test
        void commonShouldBeSubsetOfAllNamespaced() {
            assertThat(ResourceType.ALL_NAMESPACED).containsAll(ResourceType.COMMON);
        }

        @Test
        void allNamespacedShouldContain23Types() {
            assertThat(ResourceType.ALL_NAMESPACED).hasSize(23);
        }

        @Test
        void allNamespacedShouldOnlyContainNamespacedTypes() {
            for (final ResourceType type : ResourceType.ALL_NAMESPACED) {
                assertThat(type.isNamespaced())
                        .as("ALL_NAMESPACED should not contain %s", type)
                        .isTrue();
            }
        }

        @Test
        void allClusterShouldContain5Types() {
            assertThat(ResourceType.ALL_CLUSTER).hasSize(5);
        }

        @Test
        void allClusterShouldOnlyContainClusterTypes() {
            for (final ResourceType type : ResourceType.ALL_CLUSTER) {
                assertThat(type.isClusterScoped())
                        .as("ALL_CLUSTER should not contain %s", type)
                        .isTrue();
            }
        }

        @Test
        void namespacedAndClusterShouldBeDisjoint() {
            final Set<ResourceType> intersection = EnumSet.copyOf(ResourceType.ALL_NAMESPACED);
            intersection.retainAll(ResourceType.ALL_CLUSTER);
            assertThat(intersection).isEmpty();
        }

        @Test
        void namespacedAndClusterShouldCoverAllTypes() {
            final Set<ResourceType> all = EnumSet.copyOf(ResourceType.ALL_NAMESPACED);
            all.addAll(ResourceType.ALL_CLUSTER);
            assertThat(all).containsExactlyInAnyOrder(ResourceType.values());
        }
    }
}
