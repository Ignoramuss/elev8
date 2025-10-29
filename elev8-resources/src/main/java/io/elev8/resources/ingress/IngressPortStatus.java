package io.elev8.resources.ingress;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

/**
 * IngressPortStatus represents the error condition of a service port.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IngressPortStatus {
    /**
     * Port is the port number of the ingress port.
     */
    @NonNull
    private Integer port;

    /**
     * Protocol is the protocol of the ingress port.
     * The supported values are: "TCP", "UDP", "SCTP".
     */
    @NonNull
    private String protocol;

    /**
     * Error is to record the problem with the service port
     * The format of the error shall comply with the following rules:
     * - built-in error values shall be specified in this file and those shall use
     *   CamelCase names
     * - cloud provider specific error values must have names that comply with the
     *   format foo.example.com/CamelCase.
     */
    private String error;
}
