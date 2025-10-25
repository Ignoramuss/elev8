package io.elev8.resources.pod;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PodSpec {

    private List<Container> containers;
    private List<Container> initContainers;
    private String restartPolicy;
    private String serviceAccountName;
    private String nodeName;
    private Long terminationGracePeriodSeconds;

    public PodSpec() {
    }

    private PodSpec(Builder builder) {
        this.containers = builder.containers;
        this.initContainers = builder.initContainers;
        this.restartPolicy = builder.restartPolicy;
        this.serviceAccountName = builder.serviceAccountName;
        this.nodeName = builder.nodeName;
        this.terminationGracePeriodSeconds = builder.terminationGracePeriodSeconds;
    }

    public List<Container> getContainers() {
        return containers;
    }

    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }

    public List<Container> getInitContainers() {
        return initContainers;
    }

    public void setInitContainers(List<Container> initContainers) {
        this.initContainers = initContainers;
    }

    public String getRestartPolicy() {
        return restartPolicy;
    }

    public void setRestartPolicy(String restartPolicy) {
        this.restartPolicy = restartPolicy;
    }

    public String getServiceAccountName() {
        return serviceAccountName;
    }

    public void setServiceAccountName(String serviceAccountName) {
        this.serviceAccountName = serviceAccountName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Long getTerminationGracePeriodSeconds() {
        return terminationGracePeriodSeconds;
    }

    public void setTerminationGracePeriodSeconds(Long terminationGracePeriodSeconds) {
        this.terminationGracePeriodSeconds = terminationGracePeriodSeconds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<Container> containers;
        private List<Container> initContainers;
        private String restartPolicy = "Always";
        private String serviceAccountName;
        private String nodeName;
        private Long terminationGracePeriodSeconds;

        public Builder containers(List<Container> containers) {
            this.containers = containers;
            return this;
        }

        public Builder addContainer(Container container) {
            if (this.containers == null) {
                this.containers = new ArrayList<>();
            }
            this.containers.add(container);
            return this;
        }

        public Builder initContainers(List<Container> initContainers) {
            this.initContainers = initContainers;
            return this;
        }

        public Builder addInitContainer(Container container) {
            if (this.initContainers == null) {
                this.initContainers = new ArrayList<>();
            }
            this.initContainers.add(container);
            return this;
        }

        public Builder restartPolicy(String restartPolicy) {
            this.restartPolicy = restartPolicy;
            return this;
        }

        public Builder serviceAccountName(String serviceAccountName) {
            this.serviceAccountName = serviceAccountName;
            return this;
        }

        public Builder nodeName(String nodeName) {
            this.nodeName = nodeName;
            return this;
        }

        public Builder terminationGracePeriodSeconds(long seconds) {
            this.terminationGracePeriodSeconds = seconds;
            return this;
        }

        public PodSpec build() {
            if (containers == null || containers.isEmpty()) {
                throw new IllegalArgumentException("At least one container is required");
            }
            return new PodSpec(this);
        }
    }
}
