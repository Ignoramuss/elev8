package io.elev8.core.exec;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration options for pod exec operations.
 * These options control how commands are executed inside containers.
 */
@Getter
@Builder(toBuilder = true)
public class ExecOptions {
    /**
     * The command to execute in the container.
     * Required. Example: new String[]{"/bin/bash", "-c", "ls -la"}
     */
    private final String[] command;

    /**
     * Enable STDIN channel for sending input to the command.
     * If true, you can send input via ExecWatch.writeStdin().
     */
    @Builder.Default
    private final Boolean stdin = false;

    /**
     * Enable STDOUT channel for receiving command output.
     */
    @Builder.Default
    private final Boolean stdout = true;

    /**
     * Enable STDERR channel for receiving error output.
     */
    @Builder.Default
    private final Boolean stderr = true;

    /**
     * Allocate a TTY (pseudo-terminal) for the command.
     * When true, STDERR is merged into STDOUT.
     */
    @Builder.Default
    private final Boolean tty = false;

    /**
     * The container name for multi-container pods.
     * Optional for single-container pods.
     */
    private final String container;

    /**
     * Creates default ExecOptions with stdout and stderr enabled.
     *
     * @param command the command to execute
     * @return a new ExecOptions instance
     */
    public static ExecOptions of(final String[] command) {
        if (command == null || command.length == 0) {
            throw new IllegalArgumentException("Command is required for exec");
        }
        return ExecOptions.builder()
                .command(command)
                .build();
    }

    /**
     * Creates ExecOptions for an interactive shell with TTY.
     *
     * @param command the shell command (e.g., new String[]{"/bin/bash"})
     * @return a new ExecOptions configured for interactive shell
     */
    public static ExecOptions interactive(final String[] command) {
        if (command == null || command.length == 0) {
            throw new IllegalArgumentException("Command is required for exec");
        }
        return ExecOptions.builder()
                .command(command)
                .stdin(true)
                .stdout(true)
                .stderr(true)
                .tty(true)
                .build();
    }

    /**
     * Creates ExecOptions for executing a command in a specific container.
     *
     * @param command the command to execute
     * @param container the container name
     * @return a new ExecOptions for the specified container
     */
    public static ExecOptions inContainer(final String[] command, final String container) {
        if (command == null || command.length == 0) {
            throw new IllegalArgumentException("Command is required for exec");
        }
        return ExecOptions.builder()
                .command(command)
                .container(container)
                .build();
    }

    /**
     * Validates the ExecOptions.
     *
     * @throws IllegalStateException if options are invalid
     */
    public void validate() {
        if (command == null || command.length == 0) {
            throw new IllegalStateException("Command is required for exec operations");
        }
    }
}
