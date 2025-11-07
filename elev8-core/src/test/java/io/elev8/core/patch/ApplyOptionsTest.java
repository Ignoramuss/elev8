package io.elev8.core.patch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplyOptionsTest {

    @Test
    void testOf() {
        final ApplyOptions options = ApplyOptions.of("elev8-client");

        assertEquals("elev8-client", options.getFieldManager());
        assertFalse(options.getDryRun());
        assertFalse(options.getForce());
    }

    @Test
    void testOfWithNullFieldManagerThrowsException() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ApplyOptions.of(null)
        );
        assertTrue(exception.getMessage().contains("fieldManager is required"));
    }

    @Test
    void testOfWithEmptyFieldManagerThrowsException() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ApplyOptions.of("")
        );
        assertTrue(exception.getMessage().contains("fieldManager is required"));
    }

    @Test
    void testBuilder() {
        final ApplyOptions options = ApplyOptions.builder()
                .fieldManager("my-controller")
                .dryRun(true)
                .force(true)
                .build();

        assertEquals("my-controller", options.getFieldManager());
        assertTrue(options.getDryRun());
        assertTrue(options.getForce());
    }

    @Test
    void testDryRun() {
        final ApplyOptions options = ApplyOptions.dryRun("elev8-client");

        assertEquals("elev8-client", options.getFieldManager());
        assertTrue(options.getDryRun());
        assertFalse(options.getForce());
    }

    @Test
    void testDryRunWithNullFieldManagerThrowsException() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ApplyOptions.dryRun(null)
        );
        assertTrue(exception.getMessage().contains("fieldManager is required"));
    }

    @Test
    void testForce() {
        final ApplyOptions options = ApplyOptions.force("elev8-client");

        assertEquals("elev8-client", options.getFieldManager());
        assertFalse(options.getDryRun());
        assertTrue(options.getForce());
    }

    @Test
    void testForceWithNullFieldManagerThrowsException() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ApplyOptions.force(null)
        );
        assertTrue(exception.getMessage().contains("fieldManager is required"));
    }

    @Test
    void testValidate() {
        final ApplyOptions validOptions = ApplyOptions.of("elev8");
        assertDoesNotThrow(validOptions::validate);
    }

    @Test
    void testValidateWithNullFieldManagerThrowsException() {
        final ApplyOptions options = ApplyOptions.builder()
                .fieldManager(null)
                .build();

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                options::validate
        );
        assertTrue(exception.getMessage().contains("fieldManager is required"));
    }

    @Test
    void testValidateWithEmptyFieldManagerThrowsException() {
        final ApplyOptions options = ApplyOptions.builder()
                .fieldManager("   ")
                .build();

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                options::validate
        );
        assertTrue(exception.getMessage().contains("fieldManager is required"));
    }

    @Test
    void testToPatchOptions() {
        final ApplyOptions applyOptions = ApplyOptions.builder()
                .fieldManager("elev8-client")
                .dryRun(true)
                .force(true)
                .build();

        final PatchOptions patchOptions = applyOptions.toPatchOptions();

        assertEquals(PatchType.APPLY_PATCH, patchOptions.getPatchType());
        assertEquals("elev8-client", patchOptions.getFieldManager());
        assertTrue(patchOptions.getDryRun());
        assertTrue(patchOptions.getForce());
    }

    @Test
    void testToPatchOptionsWithInvalidFieldManagerThrowsException() {
        final ApplyOptions options = ApplyOptions.builder()
                .fieldManager(null)
                .build();

        assertThrows(IllegalStateException.class, options::toPatchOptions);
    }

    @Test
    void testCombinedOptions() {
        final ApplyOptions options = ApplyOptions.builder()
                .fieldManager("my-operator")
                .dryRun(true)
                .force(false)
                .build();

        assertEquals("my-operator", options.getFieldManager());
        assertTrue(options.getDryRun());
        assertFalse(options.getForce());
    }
}
