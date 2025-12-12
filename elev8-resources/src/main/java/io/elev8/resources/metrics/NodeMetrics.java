package io.elev8.resources.metrics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents node resource usage metrics from the Kubernetes Metrics API (metrics.k8s.io/v1beta1).
 *
 * <p>NodeMetrics contains the current aggregate resource usage for a node.
 * This is a read-only cluster-scoped resource that cannot be created, updated, or deleted.</p>
 *
 * <p>Example JSON response:</p>
 * <pre>{@code
 * {
 *   "apiVersion": "metrics.k8s.io/v1beta1",
 *   "kind": "NodeMetrics",
 *   "metadata": {
 *     "name": "ip-192-168-1-100.ec2.internal"
 *   },
 *   "timestamp": "2024-01-15T10:30:00Z",
 *   "window": "30s",
 *   "usage": {
 *     "cpu": "500m",
 *     "memory": "4Gi"
 *   }
 * }
 * }</pre>
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeMetrics extends AbstractResource {

    public static final String API_VERSION = "metrics.k8s.io/v1beta1";
    public static final String KIND = "NodeMetrics";

    private Instant timestamp;
    private String window;
    private ResourceUsage usage;

    public NodeMetrics() {
        super(API_VERSION, KIND, null);
    }

    private NodeMetrics(final Builder builder) {
        super(API_VERSION, KIND, builder.metadata);
        this.timestamp = builder.timestamp;
        this.window = builder.window;
        this.usage = builder.usage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private Instant timestamp;
        private String window;
        private ResourceUsage usage;

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

        public Builder timestamp(final Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder window(final String window) {
            this.window = window;
            return this;
        }

        public Builder usage(final ResourceUsage usage) {
            this.usage = usage;
            return this;
        }

        public NodeMetrics build() {
            return new NodeMetrics(this);
        }
    }
}
