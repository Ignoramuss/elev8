package io.elev8.resources.pod;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Container {

    private String name;
    private String image;
    private List<String> command;
    private List<String> args;
    private String workingDir;
    private List<ContainerPort> ports;
    private List<EnvVar> env;
    private Map<String, Quantity> resources;
    private String imagePullPolicy;

    public Container() {
    }

    private Container(Builder builder) {
        this.name = builder.name;
        this.image = builder.image;
        this.command = builder.command;
        this.args = builder.args;
        this.workingDir = builder.workingDir;
        this.ports = builder.ports;
        this.env = builder.env;
        this.resources = builder.resources;
        this.imagePullPolicy = builder.imagePullPolicy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getCommand() {
        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public List<ContainerPort> getPorts() {
        return ports;
    }

    public void setPorts(List<ContainerPort> ports) {
        this.ports = ports;
    }

    public List<EnvVar> getEnv() {
        return env;
    }

    public void setEnv(List<EnvVar> env) {
        this.env = env;
    }

    public Map<String, Quantity> getResources() {
        return resources;
    }

    public void setResources(Map<String, Quantity> resources) {
        this.resources = resources;
    }

    public String getImagePullPolicy() {
        return imagePullPolicy;
    }

    public void setImagePullPolicy(String imagePullPolicy) {
        this.imagePullPolicy = imagePullPolicy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String image;
        private List<String> command;
        private List<String> args;
        private String workingDir;
        private List<ContainerPort> ports;
        private List<EnvVar> env;
        private Map<String, Quantity> resources;
        private String imagePullPolicy;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder image(String image) {
            this.image = image;
            return this;
        }

        public Builder command(List<String> command) {
            this.command = command;
            return this;
        }

        public Builder command(String... command) {
            this.command = List.of(command);
            return this;
        }

        public Builder args(List<String> args) {
            this.args = args;
            return this;
        }

        public Builder args(String... args) {
            this.args = List.of(args);
            return this;
        }

        public Builder workingDir(String workingDir) {
            this.workingDir = workingDir;
            return this;
        }

        public Builder ports(List<ContainerPort> ports) {
            this.ports = ports;
            return this;
        }

        public Builder addPort(ContainerPort port) {
            if (this.ports == null) {
                this.ports = new ArrayList<>();
            }
            this.ports.add(port);
            return this;
        }

        public Builder addPort(int containerPort) {
            return addPort(ContainerPort.builder().containerPort(containerPort).build());
        }

        public Builder env(List<EnvVar> env) {
            this.env = env;
            return this;
        }

        public Builder addEnv(EnvVar envVar) {
            if (this.env == null) {
                this.env = new ArrayList<>();
            }
            this.env.add(envVar);
            return this;
        }

        public Builder addEnv(String name, String value) {
            return addEnv(EnvVar.builder().name(name).value(value).build());
        }

        public Builder resources(Map<String, Quantity> resources) {
            this.resources = resources;
            return this;
        }

        public Builder imagePullPolicy(String imagePullPolicy) {
            this.imagePullPolicy = imagePullPolicy;
            return this;
        }

        public Container build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Container name is required");
            }
            if (image == null || image.isEmpty()) {
                throw new IllegalArgumentException("Container image is required");
            }
            return new Container(this);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContainerPort {
        private String name;
        private Integer containerPort;
        private String protocol;

        public ContainerPort() {
        }

        private ContainerPort(Builder builder) {
            this.name = builder.name;
            this.containerPort = builder.containerPort;
            this.protocol = builder.protocol;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getContainerPort() {
            return containerPort;
        }

        public void setContainerPort(Integer containerPort) {
            this.containerPort = containerPort;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String name;
            private Integer containerPort;
            private String protocol = "TCP";

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder containerPort(int containerPort) {
                this.containerPort = containerPort;
                return this;
            }

            public Builder protocol(String protocol) {
                this.protocol = protocol;
                return this;
            }

            public ContainerPort build() {
                if (containerPort == null) {
                    throw new IllegalArgumentException("Container port is required");
                }
                return new ContainerPort(this);
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EnvVar {
        private String name;
        private String value;

        public EnvVar() {
        }

        private EnvVar(Builder builder) {
            this.name = builder.name;
            this.value = builder.value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String name;
            private String value;

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder value(String value) {
                this.value = value;
                return this;
            }

            public EnvVar build() {
                if (name == null || name.isEmpty()) {
                    throw new IllegalArgumentException("Environment variable name is required");
                }
                return new EnvVar(this);
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Quantity {
        private String amount;

        public Quantity() {
        }

        public Quantity(String amount) {
            this.amount = amount;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }
    }
}
