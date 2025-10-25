package io.elev8.resources.deployment;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.Metadata;
import io.elev8.resources.pod.PodSpec;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeploymentSpec {

    private Integer replicas;
    private Map<String, String> selector;
    private PodTemplateSpec template;
    private String strategy;
    private Integer revisionHistoryLimit;

    public DeploymentSpec() {
    }

    private DeploymentSpec(Builder builder) {
        this.replicas = builder.replicas;
        this.selector = builder.selector;
        this.template = builder.template;
        this.strategy = builder.strategy;
        this.revisionHistoryLimit = builder.revisionHistoryLimit;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public Map<String, String> getSelector() {
        return selector;
    }

    public void setSelector(Map<String, String> selector) {
        this.selector = selector;
    }

    public PodTemplateSpec getTemplate() {
        return template;
    }

    public void setTemplate(PodTemplateSpec template) {
        this.template = template;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public Integer getRevisionHistoryLimit() {
        return revisionHistoryLimit;
    }

    public void setRevisionHistoryLimit(Integer revisionHistoryLimit) {
        this.revisionHistoryLimit = revisionHistoryLimit;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer replicas = 1;
        private Map<String, String> selector;
        private PodTemplateSpec template;
        private String strategy = "RollingUpdate";
        private Integer revisionHistoryLimit = 10;

        public Builder replicas(int replicas) {
            this.replicas = replicas;
            return this;
        }

        public Builder selector(Map<String, String> selector) {
            this.selector = selector;
            return this;
        }

        public Builder addSelector(String key, String value) {
            if (this.selector == null) {
                this.selector = new HashMap<>();
            }
            this.selector.put(key, value);
            return this;
        }

        public Builder template(PodTemplateSpec template) {
            this.template = template;
            return this;
        }

        public Builder strategy(String strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder revisionHistoryLimit(int limit) {
            this.revisionHistoryLimit = limit;
            return this;
        }

        public DeploymentSpec build() {
            if (selector == null || selector.isEmpty()) {
                throw new IllegalArgumentException("Selector is required");
            }
            if (template == null) {
                throw new IllegalArgumentException("Pod template is required");
            }
            return new DeploymentSpec(this);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PodTemplateSpec {
        private Metadata metadata;
        private PodSpec spec;

        public PodTemplateSpec() {
        }

        private PodTemplateSpec(Builder builder) {
            this.metadata = builder.metadata;
            this.spec = builder.spec;
        }

        public Metadata getMetadata() {
            return metadata;
        }

        public void setMetadata(Metadata metadata) {
            this.metadata = metadata;
        }

        public PodSpec getSpec() {
            return spec;
        }

        public void setSpec(PodSpec spec) {
            this.spec = spec;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Metadata metadata;
            private PodSpec spec;

            public Builder metadata(Metadata metadata) {
                this.metadata = metadata;
                return this;
            }

            public Builder label(String key, String value) {
                if (this.metadata == null) {
                    this.metadata = Metadata.builder().build();
                }
                this.metadata = Metadata.builder()
                        .labels(this.metadata.getLabels())
                        .label(key, value)
                        .build();
                return this;
            }

            public Builder spec(PodSpec spec) {
                this.spec = spec;
                return this;
            }

            public PodTemplateSpec build() {
                if (spec == null) {
                    throw new IllegalArgumentException("Pod spec is required");
                }
                return new PodTemplateSpec(this);
            }
        }
    }
}
