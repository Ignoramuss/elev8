package io.elev8.examples;

import io.elev8.eks.EksClient;
import io.elev8.resources.deployment.Deployment;
import io.elev8.resources.deployment.DeploymentSpec;
import io.elev8.resources.pod.Container;
import io.elev8.resources.pod.Pod;
import io.elev8.resources.pod.PodSpec;
import io.elev8.resources.service.Service;
import io.elev8.resources.service.ServiceSpec;
import io.elev8.resources.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Example demonstrating CRUD operations for Pod, Service, and Deployment resources.
 */
public class ResourceCrudExample {

    private static final Logger log = LoggerFactory.getLogger(ResourceCrudExample.class);

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: ResourceCrudExample <cluster-name> <region>");
            System.err.println("Example: ResourceCrudExample my-cluster us-east-1");
            System.exit(1);
        }

        final String clusterName = args[0];
        final String region = args[1];
        final String namespace = "default";

        try (EksClient client = EksClient.builder()
                .cluster(clusterName)
                .region(region)
                .iamAuth()
                .build()) {

            log.info("Connected to EKS cluster: {}", clusterName);

            demonstratePodOperations(client, namespace);
            demonstrateServiceOperations(client, namespace);
            demonstrateDeploymentOperations(client, namespace);

        } catch (Exception e) {
            log.error("Error executing example", e);
            System.exit(1);
        }
    }

    private static void demonstratePodOperations(EksClient client, String namespace) throws Exception {
        log.info("=== Pod Operations ===");

        final Container container = Container.builder()
                .name("nginx")
                .image("nginx:latest")
                .addPort(80)
                .addEnv("ENV_VAR", "value")
                .build();

        final Pod pod = Pod.builder()
                .name("example-pod")
                .namespace(namespace)
                .label("app", "example")
                .spec(PodSpec.builder()
                        .container(container)
                        .build())
                .build();

        log.info("Creating pod: {}", pod.getName());
        final Pod created = client.pods().create(pod);
        log.info("Pod created: {}/{}", created.getNamespace(), created.getName());

        log.info("Listing pods in namespace: {}", namespace);
        final List<Pod> pods = client.pods().list(namespace);
        log.info("Found {} pods", pods.size());

        log.info("Getting pod: {}", pod.getName());
        final Pod retrieved = client.pods().get(namespace, pod.getName());
        log.info("Retrieved pod: {}", retrieved.getName());

        log.info("Deleting pod: {}", pod.getName());
        client.pods().delete(namespace, pod.getName());
        log.info("Pod deleted");
    }

    private static void demonstrateServiceOperations(EksClient client, String namespace) throws Exception {
        log.info("=== Service Operations ===");

        final Service service = Service.builder()
                .name("example-service")
                .namespace(namespace)
                .label("app", "example")
                .spec(ServiceSpec.builder()
                        .selector("app", "example")
                        .addPort(80, 80)
                        .type("ClusterIP")
                        .build())
                .build();

        log.info("Creating service: {}", service.getName());
        final Service created = client.services().create(service);
        log.info("Service created: {}/{}", created.getNamespace(), created.getName());

        log.info("Listing services in namespace: {}", namespace);
        final List<Service> services = client.services().list(namespace);
        log.info("Found {} services", services.size());

        log.info("Deleting service: {}", service.getName());
        client.services().delete(namespace, service.getName());
        log.info("Service deleted");
    }

    private static void demonstrateDeploymentOperations(EksClient client, String namespace) throws Exception {
        log.info("=== Deployment Operations ===");

        final Container container = Container.builder()
                .name("nginx")
                .image("nginx:1.21")
                .addPort(80)
                .build();

        final Deployment deployment = Deployment.builder()
                .name("example-deployment")
                .namespace(namespace)
                .label("app", "example")
                .spec(DeploymentSpec.builder()
                        .replicas(3)
                        .selector("app", "example")
                        .template(DeploymentSpec.PodTemplateSpec.builder()
                                .label("app", "example")
                                .spec(PodSpec.builder()
                                        .container(container)
                                        .build())
                                .build())
                        .build())
                .build();

        log.info("Creating deployment: {}", deployment.getName());
        final Deployment created = client.deployments().create(deployment);
        log.info("Deployment created: {}/{} with {} replicas",
                created.getNamespace(), created.getName(), created.getSpec().getReplicas());

        log.info("Listing deployments in namespace: {}", namespace);
        final List<Deployment> deployments = client.deployments().list(namespace);
        log.info("Found {} deployments", deployments.size());

        final Deployment retrieved = client.deployments().get(namespace, deployment.getName());
        retrieved.getSpec().setReplicas(5);

        log.info("Updating deployment to 5 replicas");
        final Deployment updated = client.deployments().update(retrieved);
        log.info("Deployment updated: {} replicas", updated.getSpec().getReplicas());

        log.info("Deleting deployment: {}", deployment.getName());
        client.deployments().delete(namespace, deployment.getName());
        log.info("Deployment deleted");
    }
}
