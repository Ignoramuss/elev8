package io.elev8.auth.accessentries;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.AccessScopeType;
import software.amazon.awssdk.services.eks.model.AssociateAccessPolicyRequest;
import software.amazon.awssdk.services.eks.model.CreateAccessEntryRequest;
import software.amazon.awssdk.services.eks.model.DeleteAccessEntryRequest;
import software.amazon.awssdk.services.eks.model.DescribeAccessEntryRequest;
import software.amazon.awssdk.services.eks.model.DescribeAccessEntryResponse;
import software.amazon.awssdk.services.eks.model.ListAccessEntriesRequest;
import software.amazon.awssdk.services.eks.model.ListAccessEntriesResponse;
import software.amazon.awssdk.services.eks.model.ListAssociatedAccessPoliciesRequest;
import software.amazon.awssdk.services.eks.model.ListAssociatedAccessPoliciesResponse;
import software.amazon.awssdk.services.eks.model.UpdateAccessEntryRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager for EKS Access Entries API operations.
 * Provides methods to create, read, update, and delete access entries.
 */
@Slf4j
public final class AccessEntryManager implements AutoCloseable {

    private final String clusterName;
    private final EksClient eksClient;
    private final boolean ownsClient;

    private AccessEntryManager(final Builder builder) {
        this.clusterName = builder.clusterName;

        if (builder.eksClient != null) {
            this.eksClient = builder.eksClient;
            this.ownsClient = false;
        } else if (builder.region != null) {
            this.eksClient = EksClient.builder().region(builder.region).build();
            this.ownsClient = true;
        } else {
            throw new IllegalArgumentException("Either region or eksClient is required");
        }
    }

    public AccessEntry create(final AccessEntry accessEntry) {
        log.debug("Creating access entry for principal: {}", accessEntry.getPrincipalArn());

        final CreateAccessEntryRequest.Builder requestBuilder = CreateAccessEntryRequest.builder()
                .clusterName(clusterName)
                .principalArn(accessEntry.getPrincipalArn())
                .type(accessEntry.getType());

        if (accessEntry.getKubernetesGroups() != null && !accessEntry.getKubernetesGroups().isEmpty()) {
            requestBuilder.kubernetesGroups(accessEntry.getKubernetesGroups());
        }

        if (accessEntry.getUsername() != null) {
            requestBuilder.username(accessEntry.getUsername());
        }

        if (accessEntry.getTags() != null && !accessEntry.getTags().isEmpty()) {
            requestBuilder.tags(accessEntry.getTags());
        }

        eksClient.createAccessEntry(requestBuilder.build());

        if (accessEntry.getAccessPolicies() != null && !accessEntry.getAccessPolicies().isEmpty()) {
            for (AccessPolicy policy : accessEntry.getAccessPolicies()) {
                associateAccessPolicy(accessEntry.getPrincipalArn(), policy);
            }
        }

        log.debug("Successfully created access entry for principal: {}", accessEntry.getPrincipalArn());

        return get(accessEntry.getPrincipalArn());
    }

    public AccessEntry get(final String principalArn) {
        log.debug("Getting access entry for principal: {}", principalArn);

        final DescribeAccessEntryRequest request = DescribeAccessEntryRequest.builder()
                .clusterName(clusterName)
                .principalArn(principalArn)
                .build();

        final DescribeAccessEntryResponse response = eksClient.describeAccessEntry(request);
        final software.amazon.awssdk.services.eks.model.AccessEntry eksEntry = response.accessEntry();

        final List<AccessPolicy> policies = listAssociatedAccessPolicies(principalArn);

        return AccessEntry.builder()
                .principalArn(eksEntry.principalArn())
                .kubernetesGroups(eksEntry.kubernetesGroups() != null ? eksEntry.kubernetesGroups() : new ArrayList<>())
                .username(eksEntry.username())
                .type(eksEntry.type())
                .tags(eksEntry.tags())
                .createdAt(eksEntry.createdAt())
                .modifiedAt(eksEntry.modifiedAt())
                .accessPolicies(policies)
                .build();
    }

    public List<AccessEntry> list() {
        log.debug("Listing access entries for cluster: {}", clusterName);

        final ListAccessEntriesRequest request = ListAccessEntriesRequest.builder()
                .clusterName(clusterName)
                .build();

        final ListAccessEntriesResponse response = eksClient.listAccessEntries(request);

        return response.accessEntries().stream()
                .map(this::get)
                .collect(Collectors.toList());
    }

    public AccessEntry update(final AccessEntry accessEntry) {
        log.debug("Updating access entry for principal: {}", accessEntry.getPrincipalArn());

        final UpdateAccessEntryRequest.Builder requestBuilder = UpdateAccessEntryRequest.builder()
                .clusterName(clusterName)
                .principalArn(accessEntry.getPrincipalArn());

        if (accessEntry.getKubernetesGroups() != null && !accessEntry.getKubernetesGroups().isEmpty()) {
            requestBuilder.kubernetesGroups(accessEntry.getKubernetesGroups());
        }

        if (accessEntry.getUsername() != null) {
            requestBuilder.username(accessEntry.getUsername());
        }

        eksClient.updateAccessEntry(requestBuilder.build());

        log.debug("Successfully updated access entry for principal: {}", accessEntry.getPrincipalArn());

        return get(accessEntry.getPrincipalArn());
    }

    public void delete(final String principalArn) {
        log.debug("Deleting access entry for principal: {}", principalArn);

        final DeleteAccessEntryRequest request = DeleteAccessEntryRequest.builder()
                .clusterName(clusterName)
                .principalArn(principalArn)
                .build();

        eksClient.deleteAccessEntry(request);

        log.debug("Successfully deleted access entry for principal: {}", principalArn);
    }

    public void associateAccessPolicy(final String principalArn, final AccessPolicy policy) {
        log.debug("Associating access policy {} to principal: {}", policy.getPolicyArn(), principalArn);

        final AssociateAccessPolicyRequest.Builder requestBuilder = AssociateAccessPolicyRequest.builder()
                .clusterName(clusterName)
                .principalArn(principalArn)
                .policyArn(policy.getPolicyArn());

        if (policy.getAccessScopes() != null && !policy.getAccessScopes().isEmpty()) {
            final List<software.amazon.awssdk.services.eks.model.AccessScope> scopes =
                    policy.getAccessScopes().stream()
                            .map(scope -> software.amazon.awssdk.services.eks.model.AccessScope.builder()
                                    .type(AccessScopeType.fromValue(scope.getType()))
                                    .namespaces(scope.getNamespaces())
                                    .build())
                            .collect(Collectors.toList());
            requestBuilder.accessScope(scopes.get(0));
        }

        eksClient.associateAccessPolicy(requestBuilder.build());

        log.debug("Successfully associated access policy {} to principal: {}", policy.getPolicyArn(), principalArn);
    }

    public List<AccessPolicy> listAssociatedAccessPolicies(final String principalArn) {
        log.debug("Listing associated access policies for principal: {}", principalArn);

        final ListAssociatedAccessPoliciesRequest request = ListAssociatedAccessPoliciesRequest.builder()
                .clusterName(clusterName)
                .principalArn(principalArn)
                .build();

        final ListAssociatedAccessPoliciesResponse response = eksClient.listAssociatedAccessPolicies(request);

        return response.associatedAccessPolicies().stream()
                .map(policy -> AccessPolicy.builder()
                        .policyArn(policy.policyArn())
                        .accessScopes(policy.associatedAt() != null ?
                                List.of(AccessScope.builder()
                                        .type(policy.accessScope() != null ? policy.accessScope().typeAsString() : "cluster")
                                        .namespaces(policy.accessScope() != null && policy.accessScope().namespaces() != null ?
                                                policy.accessScope().namespaces() : new ArrayList<>())
                                        .build()) : new ArrayList<>())
                        .associatedAt(policy.associatedAt())
                        .modifiedAt(policy.modifiedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void close() {
        if (ownsClient && eksClient != null) {
            eksClient.close();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String clusterName;
        private Region region;
        private EksClient eksClient;

        public Builder clusterName(final String clusterName) {
            this.clusterName = clusterName;
            return this;
        }

        public Builder region(final Region region) {
            this.region = region;
            return this;
        }

        public Builder region(final String region) {
            this.region = Region.of(region);
            return this;
        }

        public Builder eksClient(final EksClient eksClient) {
            this.eksClient = eksClient;
            return this;
        }

        public AccessEntryManager build() {
            if (clusterName == null || clusterName.isEmpty()) {
                throw new IllegalArgumentException("Cluster name is required");
            }
            return new AccessEntryManager(this);
        }
    }
}
