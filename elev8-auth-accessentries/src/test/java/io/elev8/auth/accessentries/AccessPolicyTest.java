package io.elev8.auth.accessentries;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccessPolicyTest {

    private static final String TEST_POLICY_ARN = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy";

    @Test
    void shouldCreateAccessPolicyWithRequiredFields() {
        final AccessPolicy policy = AccessPolicy.builder()
                .policyArn(TEST_POLICY_ARN)
                .build();

        assertThat(policy.getPolicyArn()).isEqualTo(TEST_POLICY_ARN);
        assertThat(policy.getAccessScopes()).isEmpty();
    }

    @Test
    void shouldCreateAccessPolicyWithAllFields() {
        final Instant now = Instant.now();
        final AccessScope scope = AccessScope.builder()
                .type("namespace")
                .namespaces(List.of("default"))
                .build();

        final AccessPolicy policy = AccessPolicy.builder()
                .policyArn(TEST_POLICY_ARN)
                .accessScopes(List.of(scope))
                .associatedAt(now)
                .modifiedAt(now)
                .build();

        assertThat(policy.getPolicyArn()).isEqualTo(TEST_POLICY_ARN);
        assertThat(policy.getAccessScopes()).hasSize(1);
        assertThat(policy.getAssociatedAt()).isEqualTo(now);
        assertThat(policy.getModifiedAt()).isEqualTo(now);
    }

    @Test
    void shouldSetMultipleAccessScopes() {
        final AccessScope scope1 = AccessScope.builder()
                .type("namespace")
                .namespaces(List.of("default"))
                .build();

        final AccessScope scope2 = AccessScope.builder()
                .type("namespace")
                .namespaces(List.of("kube-system"))
                .build();

        final AccessPolicy policy = AccessPolicy.builder()
                .policyArn(TEST_POLICY_ARN)
                .accessScopes(List.of(scope1, scope2))
                .build();

        assertThat(policy.getAccessScopes()).hasSize(2);
    }

    @Test
    void shouldSetAccessScopesFromList() {
        final AccessScope scope = AccessScope.builder()
                .type("cluster")
                .build();

        final List<AccessScope> scopes = List.of(scope);
        final AccessPolicy policy = AccessPolicy.builder()
                .policyArn(TEST_POLICY_ARN)
                .accessScopes(scopes)
                .build();

        assertThat(policy.getAccessScopes()).containsExactlyElementsOf(scopes);
    }

    @Test
    void shouldAllowNullOptionalFields() {
        final AccessPolicy policy = AccessPolicy.builder()
                .policyArn(TEST_POLICY_ARN)
                .associatedAt(null)
                .modifiedAt(null)
                .build();

        assertThat(policy.getAssociatedAt()).isNull();
        assertThat(policy.getModifiedAt()).isNull();
    }
}
