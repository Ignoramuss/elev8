package io.elev8.resources.metrics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/**
 * Represents pod resource usage metrics from the Kubernetes Metrics API (metrics.k8s.io/v1beta1).
 *
 * <p>PodMetrics contains the current resource usage for all containers in a pod.
 * This is a read-only resource that cannot be created, updated, or deleted.</p>
 *
 * <p>Example JSON response:</p>
 * <pre>{@code
 * {
 *   "apiVersion": "metrics.k8s.io/v1beta1",
 *   "kind": "PodMetrics",
 *   "metadata": {
 *     "name": "nginx-deployment-abc123",
 *     "namespace": "default"
 *   },
 *   "timestamp": "2024-01-15T10:30:00Z",
 *   "window": "30s",
 *   "containers": [
 *     {
 *       "name": "nginx",
 *       "usage": {
 *         "cpu": "100m",
 *         "memory": "256Mi"
 *       }
 *     }
 *   ]
 * }
 * }</pre>
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PodMetrics extends AbstractResource {

    public static final String API_VERSION = "metrics.k8s.io/v1beta1";
    public static final String KIND = "PodMetrics";

    private Instant timestamp;
    private String window;
    private List<ContainerMetrics> containers;

    public PodMetrics() {
        super(API_VERSION, KIND, null);
    }

    private PodMetrics(final Builder builder) {
        super(API_VERSION, KIND, builder.metadata);
        this.timestamp = builder.timestamp;
        this.window = builder.window;
        this.containers = builder.containers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private Instant timestamp;
        private String window;
        private List<ContainerMetrics> containers;

        private Builder() {
        }

        public Builder metadata(final Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder name(final String name) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata.setName(name);
            return this;
        }

        public Builder namespace(final String namespace) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata.setNamespace(namespace);
            return this;
        }

        public Builder timestamp(final Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder window(final String window) {
            this.window = window;
            return this;
        }

        public Builder containers(final List<ContainerMetrics> containers) {
            this.containers = containers;
            return this;
        }

        public PodMetrics build() {
            return new PodMetrics(this);
        }
    }
}
