package io.elev8.examples;

import io.elev8.eks.EksClient;
import io.elev8.core.client.KubernetesClientException;
import io.elev8.core.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;

/**
 * Simple example demonstrating EKS client usage with IAM authentication.
 *
 * Prerequisites:
 * - AWS credentials configured (via env vars, credentials file, or instance profile)
 * - EKS cluster exists in the specified region
 * - IAM permissions to describe EKS cluster and call Kubernetes API
 */
public class SimpleExample {

    private static final Logger log = LoggerFactory.getLogger(SimpleExample.class);

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: SimpleExample <cluster-name> <region>");
            System.err.println("Example: SimpleExample my-cluster us-east-1");
            System.exit(1);
        }

        String clusterName = args[0];
        String region = args[1];

        // Create EKS client with IAM authentication
        try (EksClient client = EksClient.builder()
                .clusterName(clusterName)
                .region(Region.of(region))
                .build()) {

            log.info("Successfully created EKS client for cluster: {}", clusterName);

            // Example: List namespaces
            HttpResponse response = client.getKubernetesClient().get("/api/v1/namespaces");

            if (response.isSuccessful()) {
                log.info("Successfully retrieved namespaces");
                log.info("Response: {}", response.getBody());
            } else {
                log.error("Failed to retrieve namespaces: {} - {}",
                        response.getStatusCode(), response.getBody());
            }

        } catch (KubernetesClientException e) {
            log.error("Kubernetes API error", e);
            System.exit(1);
        } catch (Exception e) {
            log.error("Unexpected error", e);
            System.exit(1);
        }
    }
}
