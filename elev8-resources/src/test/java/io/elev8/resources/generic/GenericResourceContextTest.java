package io.elev8.resources.generic;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenericResourceContextTest {

    @Test
    void shouldBuildContextWithAllFields() {
        final GenericResourceContext context = GenericResourceContext.builder()
                .group("stable.example.com")
                .version("v1")
                .kind("CronTab")
                .plural("crontabs")
                .scope(GenericResourceContext.ResourceScope.NAMESPACED)
                .build();

        assertThat(context.getGroup()).isEqualTo("stable.example.com");
        assertThat(context.getVersion()).isEqualTo("v1");
        assertThat(context.getKind()).isEqualTo("CronTab");
        assertThat(context.getPlural()).isEqualTo("crontabs");
        assertThat(context.getScope()).isEqualTo(GenericResourceContext.ResourceScope.NAMESPACED);
    }

    @Test
    void shouldReturnApiVersionWithGroup() {
        final GenericResourceContext context = GenericResourceContext.builder()
                .group("stable.example.com")
                .version("v1")
                .kind("CronTab")
                .plural("crontabs")
                .scope(GenericResourceContext.ResourceScope.NAMESPACED)
                .build();

        assertThat(context.getApiVersion()).isEqualTo("stable.example.com/v1");
    }

    @Test
    void shouldReturnApiVersionWithoutGroupForCoreResources() {
        final GenericResourceContext context = GenericResourceContext.builder()
                .group(null)
                .version("v1")
                .kind("ConfigMap")
                .plural("configmaps")
                .scope(GenericResourceContext.ResourceScope.NAMESPACED)
                .build();

        assertThat(context.getApiVersion()).isEqualTo("v1");
    }

    @Test
    void shouldReturnApiVersionWithEmptyGroup() {
        final GenericResourceContext context = GenericResourceContext.builder()
                .group("")
                .version("v1")
                .kind("Pod")
                .plural("pods")
                .scope(GenericResourceContext.ResourceScope.NAMESPACED)
                .build();

        assertThat(context.getApiVersion()).isEqualTo("v1");
    }

    @Test
    void shouldReturnApiPathForCustomResource() {
        final GenericResourceContext context = GenericResourceContext.builder()
                .group("stable.example.com")
                .version("v1")
                .kind("CronTab")
                .plural("crontabs")
                .scope(GenericResourceContext.ResourceScope.NAMESPACED)
                .build();

        assertThat(context.getApiPath()).isEqualTo("/apis/stable.example.com/v1");
    }

    @Test
    void shouldReturnApiPathForCoreResource() {
        final GenericResourceContext context = GenericResourceContext.builder()
                .group(null)
                .version("v1")
                .kind("ConfigMap")
                .plural("configmaps")
                .scope(GenericResourceContext.ResourceScope.NAMESPACED)
                .build();

        assertThat(context.getApiPath()).isEqualTo("/api/v1");
    }

    @Test
    void shouldReturnApiPathWithEmptyGroup() {
        final GenericResourceContext context = GenericResourceContext.builder()
                .group("")
                .version("v1")
                .kind("Pod")
                .plural("pods")
                .scope(GenericResourceContext.ResourceScope.NAMESPACED)
                .build();

        assertThat(context.getApiPath()).isEqualTo("/api/v1");
    }

    @Test
    void shouldIdentifyNamespacedResource() {
        final GenericResourceContext context = GenericResourceContext.builder()
                .group("stable.example.com")
                .version("v1")
                .kind("CronTab")
                .plural("crontabs")
                .scope(GenericResourceContext.ResourceScope.NAMESPACED)
                .build();

        assertThat(context.isNamespaced()).isTrue();
        assertThat(context.isClusterScoped()).isFalse();
    }

    @Test
    void shouldIdentifyClusterScopedResource() {
        final GenericResourceContext context = GenericResourceContext.builder()
                .group("example.com")
                .version("v1")
                .kind("ClusterPolicy")
                .plural("clusterpolicies")
                .scope(GenericResourceContext.ResourceScope.CLUSTER)
                .build();

        assertThat(context.isNamespaced()).isFalse();
        assertThat(context.isClusterScoped()).isTrue();
    }

    @Test
    void shouldCreateContextForCoreResource() {
        final GenericResourceContext context = GenericResourceContext.forCoreResource(
                "v1", "ConfigMap", "configmaps", GenericResourceContext.ResourceScope.NAMESPACED);

        assertThat(context.getGroup()).isNull();
        assertThat(context.getVersion()).isEqualTo("v1");
        assertThat(context.getKind()).isEqualTo("ConfigMap");
        assertThat(context.getPlural()).isEqualTo("configmaps");
        assertThat(context.getScope()).isEqualTo(GenericResourceContext.ResourceScope.NAMESPACED);
        assertThat(context.getApiPath()).isEqualTo("/api/v1");
        assertThat(context.getApiVersion()).isEqualTo("v1");
    }

    @Test
    void shouldCreateContextForCustomResource() {
        final GenericResourceContext context = GenericResourceContext.forCustomResource(
                "stable.example.com", "v1", "CronTab", "crontabs",
                GenericResourceContext.ResourceScope.NAMESPACED);

        assertThat(context.getGroup()).isEqualTo("stable.example.com");
        assertThat(context.getVersion()).isEqualTo("v1");
        assertThat(context.getKind()).isEqualTo("CronTab");
        assertThat(context.getPlural()).isEqualTo("crontabs");
        assertThat(context.getScope()).isEqualTo(GenericResourceContext.ResourceScope.NAMESPACED);
        assertThat(context.getApiPath()).isEqualTo("/apis/stable.example.com/v1");
        assertThat(context.getApiVersion()).isEqualTo("stable.example.com/v1");
    }

    @Test
    void shouldCreateContextForNamespacedResource() {
        final GenericResourceContext context = GenericResourceContext.forNamespacedResource(
                "stable.example.com", "v1", "CronTab", "crontabs");

        assertThat(context.getScope()).isEqualTo(GenericResourceContext.ResourceScope.NAMESPACED);
        assertThat(context.isNamespaced()).isTrue();
    }

    @Test
    void shouldCreateContextForClusterResource() {
        final GenericResourceContext context = GenericResourceContext.forClusterResource(
                "example.com", "v1", "ClusterPolicy", "clusterpolicies");

        assertThat(context.getScope()).isEqualTo(GenericResourceContext.ResourceScope.CLUSTER);
        assertThat(context.isClusterScoped()).isTrue();
    }

    @Test
    void shouldHandleAppsApiGroup() {
        final GenericResourceContext context = GenericResourceContext.builder()
                .group("apps")
                .version("v1")
                .kind("Deployment")
                .plural("deployments")
                .scope(GenericResourceContext.ResourceScope.NAMESPACED)
                .build();

        assertThat(context.getApiPath()).isEqualTo("/apis/apps/v1");
        assertThat(context.getApiVersion()).isEqualTo("apps/v1");
    }

    @Test
    void shouldHandleBetaApiVersion() {
        final GenericResourceContext context = GenericResourceContext.builder()
                .group("policy")
                .version("v1beta1")
                .kind("PodSecurityPolicy")
                .plural("podsecuritypolicies")
                .scope(GenericResourceContext.ResourceScope.CLUSTER)
                .build();

        assertThat(context.getApiPath()).isEqualTo("/apis/policy/v1beta1");
        assertThat(context.getApiVersion()).isEqualTo("policy/v1beta1");
    }
}
