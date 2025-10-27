package io.elev8.auth.accessentries;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AccessEntryTest {

    private static final String TEST_PRINCIPAL_ARN = "arn:aws:iam::123456789012:role/TestRole";

    @Test
    void shouldCreateAccessEntryWithRequiredFields() {
        final AccessEntry entry = AccessEntry.builder()
                .principalArn(TEST_PRINCIPAL_ARN)
                .build();

        assertThat(entry.getPrincipalArn()).isEqualTo(TEST_PRINCIPAL_ARN);
        assertThat(entry.getKubernetesGroups()).isNullOrEmpty();
        assertThat(entry.getType()).isEqualTo("STANDARD");
    }

    @Test
    void shouldCreateAccessEntryWithAllFields() {
        final Instant now = Instant.now();
        final AccessPolicy policy = AccessPolicy.builder()
                .policyArn("arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy")
                .build();

        final AccessEntry entry = AccessEntry.builder()
                .principalArn(TEST_PRINCIPAL_ARN)
                .kubernetesGroup("system:masters")
                .username("test-user")
                .type("STANDARD")
                .tag("Environment", "test")
                .createdAt(now)
                .modifiedAt(now)
                .accessPolicy(policy)
                .build();

        assertThat(entry.getPrincipalArn()).isEqualTo(TEST_PRINCIPAL_ARN);
        assertThat(entry.getKubernetesGroups()).containsExactly("system:masters");
        assertThat(entry.getUsername()).isEqualTo("test-user");
        assertThat(entry.getType()).isEqualTo("STANDARD");
        assertThat(entry.getTags()).containsEntry("Environment", "test");
        assertThat(entry.getCreatedAt()).isEqualTo(now);
        assertThat(entry.getModifiedAt()).isEqualTo(now);
        assertThat(entry.getAccessPolicies()).hasSize(1);
        assertThat(entry.getAccessPolicies().get(0).getPolicyArn())
                .isEqualTo("arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy");
    }

    @Test
    void shouldSetMultipleKubernetesGroups() {
        final AccessEntry entry = AccessEntry.builder()
                .principalArn(TEST_PRINCIPAL_ARN)
                .kubernetesGroup("system:masters")
                .kubernetesGroup("developers")
                .build();

        assertThat(entry.getKubernetesGroups()).containsExactly("system:masters", "developers");
    }

    @Test
    void shouldSetKubernetesGroupsFromList() {
        final List<String> groups = List.of("system:masters", "developers");
        final AccessEntry entry = AccessEntry.builder()
                .principalArn(TEST_PRINCIPAL_ARN)
                .kubernetesGroups(groups)
                .build();

        assertThat(entry.getKubernetesGroups()).containsExactlyElementsOf(groups);
    }

    @Test
    void shouldSetMultipleTags() {
        final AccessEntry entry = AccessEntry.builder()
                .principalArn(TEST_PRINCIPAL_ARN)
                .tag("Environment", "test")
                .tag("Team", "platform")
                .build();

        assertThat(entry.getTags()).containsEntry("Environment", "test");
        assertThat(entry.getTags()).containsEntry("Team", "platform");
    }

    @Test
    void shouldSetTagsFromMap() {
        final Map<String, String> tags = Map.of("Environment", "test", "Team", "platform");
        final AccessEntry entry = AccessEntry.builder()
                .principalArn(TEST_PRINCIPAL_ARN)
                .tags(tags)
                .build();

        assertThat(entry.getTags()).containsAllEntriesOf(tags);
    }

    @Test
    void shouldUseDefaultType() {
        final AccessEntry entry = AccessEntry.builder()
                .principalArn(TEST_PRINCIPAL_ARN)
                .build();

        assertThat(entry.getType()).isEqualTo("STANDARD");
    }

    @Test
    void shouldAllowNullOptionalFields() {
        final AccessEntry entry = AccessEntry.builder()
                .principalArn(TEST_PRINCIPAL_ARN)
                .username(null)
                .createdAt(null)
                .modifiedAt(null)
                .build();

        assertThat(entry.getUsername()).isNull();
        assertThat(entry.getCreatedAt()).isNull();
        assertThat(entry.getModifiedAt()).isNull();
    }

    @Test
    void shouldCreateBuilderFromExistingAccessEntry() {
        final AccessEntry original = AccessEntry.builder()
                .principalArn(TEST_PRINCIPAL_ARN)
                .kubernetesGroup("system:masters")
                .username("test-user")
                .build();

        final AccessEntry copy = original.toBuilder()
                .username("new-user")
                .build();

        assertThat(copy.getPrincipalArn()).isEqualTo(TEST_PRINCIPAL_ARN);
        assertThat(copy.getKubernetesGroups()).containsExactly("system:masters");
        assertThat(copy.getUsername()).isEqualTo("new-user");
    }
}
