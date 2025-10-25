package io.elev8.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metadata {
    String name;
    String namespace;
    String uid;
    String resourceVersion;
    Instant creationTimestamp;

    @Singular
    Map<String, String> labels;

    @Singular
    Map<String, String> annotations;
}
