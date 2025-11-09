package io.elev8.core.portforward;

import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;

/**
 * Configuration options for pod port forwarding operations.
 * Port forwarding enables tunneling of network traffic from local ports to pod ports.
 */
@Getter
@Builder(toBuilder = true)
public class PortForwardOptions {
    /**
     * The ports to forward.
     * Required. Each port will have a corresponding data and error channel pair.
     */
    private final int[] ports;

    /**
     * The container name for multi-container pods.
     * Optional for single-container pods.
     */
    private final String container;

    /**
     * Creates default PortForwardOptions for forwarding specified ports.
     *
     * @param ports the ports to forward
     * @return a new PortForwardOptions instance
     * @throws IllegalArgumentException if no ports are specified or if any port is invalid
     */
    public static PortForwardOptions of(final int... ports) {
        if (ports == null || ports.length == 0) {
            throw new IllegalArgumentException("At least one port is required for port forwarding");
        }
        validatePorts(ports);
        return PortForwardOptions.builder()
                .ports(ports)
                .build();
    }

    /**
     * Creates PortForwardOptions for forwarding a single port.
     *
     * @param port the port to forward
     * @return a new PortForwardOptions configured for single port
     * @throws IllegalArgumentException if port is invalid
     */
    public static PortForwardOptions single(final int port) {
        validatePort(port);
        return PortForwardOptions.builder()
                .ports(new int[]{port})
                .build();
    }

    /**
     * Creates PortForwardOptions for forwarding ports in a specific container.
     *
     * @param ports the ports to forward
     * @param container the container name
     * @return a new PortForwardOptions for the specified container
     * @throws IllegalArgumentException if no ports are specified, ports are invalid, or container is null
     */
    public static PortForwardOptions withContainer(final int[] ports, final String container) {
        if (ports == null || ports.length == 0) {
            throw new IllegalArgumentException("At least one port is required for port forwarding");
        }
        if (container == null || container.trim().isEmpty()) {
            throw new IllegalArgumentException("Container name cannot be null or empty");
        }
        validatePorts(ports);
        return PortForwardOptions.builder()
                .ports(ports)
                .container(container)
                .build();
    }

    /**
     * Validates the PortForwardOptions.
     *
     * @throws IllegalStateException if options are invalid
     */
    public void validate() {
        if (ports == null || ports.length == 0) {
            throw new IllegalStateException("At least one port is required for port forwarding operations");
        }
        validatePorts(ports);
    }

    /**
     * Gets the number of ports being forwarded.
     *
     * @return the port count
     */
    public int getPortCount() {
        return ports != null ? ports.length : 0;
    }

    /**
     * Checks if this port forward includes the specified port.
     *
     * @param port the port to check
     * @return true if the port is included
     */
    public boolean hasPort(final int port) {
        if (ports == null) {
            return false;
        }
        for (final int p : ports) {
            if (p == port) {
                return true;
            }
        }
        return false;
    }

    private static void validatePorts(final int[] ports) {
        for (final int port : ports) {
            validatePort(port);
        }
    }

    private static void validatePort(final int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                    "Port must be between 1 and 65535, got: " + port);
        }
    }

    @Override
    public String toString() {
        return "PortForwardOptions{" +
                "ports=" + Arrays.toString(ports) +
                ", container='" + container + '\'' +
                '}';
    }
}
