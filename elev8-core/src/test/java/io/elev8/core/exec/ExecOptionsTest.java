package io.elev8.core.exec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecOptionsTest {

    @Test
    void testOf() {
        final String[] command = new String[]{"/bin/bash", "-c", "ls"};
        final ExecOptions options = ExecOptions.of(command);

        assertArrayEquals(command, options.getCommand());
        assertFalse(options.getStdin());
        assertTrue(options.getStdout());
        assertTrue(options.getStderr());
        assertFalse(options.getTty());
        assertNull(options.getContainer());
    }

    @Test
    void testOfWithNullCommandThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> ExecOptions.of(null));
    }

    @Test
    void testOfWithEmptyCommandThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> ExecOptions.of(new String[]{}));
    }

    @Test
    void testBuilder() {
        final String[] command = new String[]{"/bin/sh"};
        final ExecOptions options = ExecOptions.builder()
                .command(command)
                .stdin(true)
                .stdout(true)
                .stderr(false)
                .tty(true)
                .container("nginx")
                .build();

        assertArrayEquals(command, options.getCommand());
        assertTrue(options.getStdin());
        assertTrue(options.getStdout());
        assertFalse(options.getStderr());
        assertTrue(options.getTty());
        assertEquals("nginx", options.getContainer());
    }

    @Test
    void testInteractive() {
        final String[] command = new String[]{"/bin/bash"};
        final ExecOptions options = ExecOptions.interactive(command);

        assertArrayEquals(command, options.getCommand());
        assertTrue(options.getStdin());
        assertTrue(options.getStdout());
        assertTrue(options.getStderr());
        assertTrue(options.getTty());
    }

    @Test
    void testInContainer() {
        final String[] command = new String[]{"ls", "-la"};
        final ExecOptions options = ExecOptions.inContainer(command, "app");

        assertArrayEquals(command, options.getCommand());
        assertEquals("app", options.getContainer());
        assertFalse(options.getStdin());
        assertTrue(options.getStdout());
    }

    @Test
    void testValidate() {
        final ExecOptions valid = ExecOptions.of(new String[]{"ls"});
        assertDoesNotThrow(valid::validate);
    }

    @Test
    void testValidateWithNullCommandThrowsException() {
        final ExecOptions invalid = ExecOptions.builder().command(null).build();
        assertThrows(IllegalStateException.class, invalid::validate);
    }

    @Test
    void testValidateWithEmptyCommandThrowsException() {
        final ExecOptions invalid = ExecOptions.builder().command(new String[]{}).build();
        assertThrows(IllegalStateException.class, invalid::validate);
    }
}
