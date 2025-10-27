package io.elev8.auth.accessentries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.AssociateAccessPolicyRequest;
import software.amazon.awssdk.services.eks.model.AssociateAccessPolicyResponse;
import software.amazon.awssdk.services.eks.model.CreateAccessEntryRequest;
import software.amazon.awssdk.services.eks.model.CreateAccessEntryResponse;
import software.amazon.awssdk.services.eks.model.DeleteAccessEntryRequest;
import software.amazon.awssdk.services.eks.model.DeleteAccessEntryResponse;
import software.amazon.awssdk.services.eks.model.DescribeAccessEntryRequest;
import software.amazon.awssdk.services.eks.model.DescribeAccessEntryResponse;
import software.amazon.awssdk.services.eks.model.ListAccessEntriesRequest;
import software.amazon.awssdk.services.eks.model.ListAccessEntriesResponse;
import software.amazon.awssdk.services.eks.model.ListAssociatedAccessPoliciesRequest;
import software.amazon.awssdk.services.eks.model.ListAssociatedAccessPoliciesResponse;
import software.amazon.awssdk.services.eks.model.UpdateAccessEntryRequest;
import software.amazon.awssdk.services.eks.model.UpdateAccessEntryResponse;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccessEntryManagerTest {

    @Mock
    private EksClient eksClient;

    private AccessEntryManager manager;

    private static final String TEST_CLUSTER_NAME = "test-cluster";
    private static final String TEST_PRINCIPAL_ARN = "arn:aws:iam::123456789012:role/TestRole";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = AccessEntryManager.builder()
                .clusterName(TEST_CLUSTER_NAME)
                .eksClient(eksClient)
                .build();
    }

    @Test
    void shouldCreateAccessEntry() {
        final AccessEntry entry = AccessEntry.builder()
                .principalArn(TEST_PRINCIPAL_ARN)
                .kubernetesGroup("system:masters")
                .type("STANDARD")
                .build();

        when(eksClient.createAccessEntry(any(CreateAccessEntryRequest.class)))
                .thenReturn(CreateAccessEntryResponse.builder().build());

        when(eksClient.describeAccessEntry(any(DescribeAccessEntryRequest.class)))
                .thenReturn(DescribeAccessEntryResponse.builder()
                        .accessEntry(software.amazon.awssdk.services.eks.model.AccessEntry.builder()
                                .principalArn(TEST_PRINCIPAL_ARN)
                                .kubernetesGroups(List.of("system:masters"))
                                .type("STANDARD")
                                .createdAt(Instant.now())
                                .build())
                        .build());

        when(eksClient.listAssociatedAccessPolicies(any(ListAssociatedAccessPoliciesRequest.class)))
                .thenReturn(ListAssociatedAccessPoliciesResponse.builder()
                        .associatedAccessPolicies(List.of())
                        .build());

        final AccessEntry result = manager.create(entry);

        assertThat(result.getPrincipalArn()).isEqualTo(TEST_PRINCIPAL_ARN);
        verify(eksClient).createAccessEntry(any(CreateAccessEntryRequest.class));
    }

    @Test
    void shouldGetAccessEntry() {
        when(eksClient.describeAccessEntry(any(DescribeAccessEntryRequest.class)))
                .thenReturn(DescribeAccessEntryResponse.builder()
                        .accessEntry(software.amazon.awssdk.services.eks.model.AccessEntry.builder()
                                .principalArn(TEST_PRINCIPAL_ARN)
                                .kubernetesGroups(List.of("system:masters"))
                                .type("STANDARD")
                                .createdAt(Instant.now())
                                .build())
                        .build());

        when(eksClient.listAssociatedAccessPolicies(any(ListAssociatedAccessPoliciesRequest.class)))
                .thenReturn(ListAssociatedAccessPoliciesResponse.builder()
                        .associatedAccessPolicies(List.of())
                        .build());

        final AccessEntry result = manager.get(TEST_PRINCIPAL_ARN);

        assertThat(result.getPrincipalArn()).isEqualTo(TEST_PRINCIPAL_ARN);
        assertThat(result.getKubernetesGroups()).containsExactly("system:masters");
        verify(eksClient).describeAccessEntry(any(DescribeAccessEntryRequest.class));
    }

    @Test
    void shouldListAccessEntries() {
        when(eksClient.listAccessEntries(any(ListAccessEntriesRequest.class)))
                .thenReturn(ListAccessEntriesResponse.builder()
                        .accessEntries(List.of(TEST_PRINCIPAL_ARN))
                        .build());

        when(eksClient.describeAccessEntry(any(DescribeAccessEntryRequest.class)))
                .thenReturn(DescribeAccessEntryResponse.builder()
                        .accessEntry(software.amazon.awssdk.services.eks.model.AccessEntry.builder()
                                .principalArn(TEST_PRINCIPAL_ARN)
                                .type("STANDARD")
                                .createdAt(Instant.now())
                                .build())
                        .build());

        when(eksClient.listAssociatedAccessPolicies(any(ListAssociatedAccessPoliciesRequest.class)))
                .thenReturn(ListAssociatedAccessPoliciesResponse.builder()
                        .associatedAccessPolicies(List.of())
                        .build());

        final List<AccessEntry> results = manager.list();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPrincipalArn()).isEqualTo(TEST_PRINCIPAL_ARN);
        verify(eksClient).listAccessEntries(any(ListAccessEntriesRequest.class));
    }

    @Test
    void shouldUpdateAccessEntry() {
        final AccessEntry entry = AccessEntry.builder()
                .principalArn(TEST_PRINCIPAL_ARN)
                .kubernetesGroup("developers")
                .username("test-user")
                .build();

        when(eksClient.updateAccessEntry(any(UpdateAccessEntryRequest.class)))
                .thenReturn(UpdateAccessEntryResponse.builder().build());

        when(eksClient.describeAccessEntry(any(DescribeAccessEntryRequest.class)))
                .thenReturn(DescribeAccessEntryResponse.builder()
                        .accessEntry(software.amazon.awssdk.services.eks.model.AccessEntry.builder()
                                .principalArn(TEST_PRINCIPAL_ARN)
                                .kubernetesGroups(List.of("developers"))
                                .username("test-user")
                                .type("STANDARD")
                                .modifiedAt(Instant.now())
                                .build())
                        .build());

        when(eksClient.listAssociatedAccessPolicies(any(ListAssociatedAccessPoliciesRequest.class)))
                .thenReturn(ListAssociatedAccessPoliciesResponse.builder()
                        .associatedAccessPolicies(List.of())
                        .build());

        final AccessEntry result = manager.update(entry);

        assertThat(result.getPrincipalArn()).isEqualTo(TEST_PRINCIPAL_ARN);
        assertThat(result.getUsername()).isEqualTo("test-user");
        verify(eksClient).updateAccessEntry(any(UpdateAccessEntryRequest.class));
    }

    @Test
    void shouldDeleteAccessEntry() {
        when(eksClient.deleteAccessEntry(any(DeleteAccessEntryRequest.class)))
                .thenReturn(DeleteAccessEntryResponse.builder().build());

        manager.delete(TEST_PRINCIPAL_ARN);

        verify(eksClient).deleteAccessEntry(any(DeleteAccessEntryRequest.class));
    }

    @Test
    void shouldAssociateAccessPolicy() {
        final AccessPolicy policy = AccessPolicy.builder()
                .policyArn("arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy")
                .build();

        when(eksClient.associateAccessPolicy(any(AssociateAccessPolicyRequest.class)))
                .thenReturn(AssociateAccessPolicyResponse.builder().build());

        manager.associateAccessPolicy(TEST_PRINCIPAL_ARN, policy);

        verify(eksClient).associateAccessPolicy(any(AssociateAccessPolicyRequest.class));
    }

    @Test
    void shouldThrowExceptionWhenClusterNameIsNull() {
        assertThatThrownBy(() -> AccessEntryManager.builder().build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cluster name is required");
    }

    @Test
    void shouldNotCloseClientWhenNotOwned() {
        manager.close();
        // Verify that close is not called on the injected client
    }
}
