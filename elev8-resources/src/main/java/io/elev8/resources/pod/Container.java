package io.elev8.resources.pod;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Container {

    @NonNull String name;
    @NonNull String image;
    List<String> command;
    List<String> args;
    String workingDir;
    @Singular List<ContainerPort> ports;
    @Singular("envVar") List<EnvVar> env;
    @Singular("resource") Map<String, Quantity> resources;
    String imagePullPolicy;

    public static class ContainerBuilder {
        public ContainerBuilder addPort(final int containerPort) {
            return port(ContainerPort.builder().containerPort(containerPort).build());
        }

        public ContainerBuilder addEnv(final String name, final String value) {
            return envVar(EnvVar.builder().name(name).value(value).build());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContainerPort {
        String name;
        @NonNull Integer containerPort;
        @Builder.Default String protocol = "TCP";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EnvVar {
        @NonNull String name;
        String value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Quantity {
        String amount;
    }
}
