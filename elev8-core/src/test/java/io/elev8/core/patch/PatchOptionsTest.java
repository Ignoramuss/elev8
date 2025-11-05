package io.elev8.core.patch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatchOptionsTest {

    @Test
    void testDefaults() {
        final PatchOptions options = PatchOptions.defaults();

        assertEquals(PatchType.STRATEGIC_MERGE_PATCH, options.getPatchType());
        assertFalse(options.getDryRun());
        assertNull(options.getFieldManager());
        assertFalse(options.getForce());
    }

    @Test
    void testBuilder() {
        final PatchOptions options = PatchOptions.builder()
                .patchType(PatchType.JSON_PATCH)
                .dryRun(true)
                .fieldManager("elev8-client")
                .force(true)
                .build();

        assertEquals(PatchType.JSON_PATCH, options.getPatchType());
        assertTrue(options.getDryRun());
        assertEquals("elev8-client", options.getFieldManager());
        assertTrue(options.getForce());
    }

    @Test
    void testJsonPatch() {
        final PatchOptions options = PatchOptions.jsonPatch();

        assertEquals(PatchType.JSON_PATCH, options.getPatchType());
        assertFalse(options.getDryRun());
        assertFalse(options.getForce());
    }

    @Test
    void testMergePatch() {
        final PatchOptions options = PatchOptions.mergePatch();

        assertEquals(PatchType.MERGE_PATCH, options.getPatchType());
        assertFalse(options.getDryRun());
        assertFalse(options.getForce());
    }

    @Test
    void testStrategicMergePatch() {
        final PatchOptions options = PatchOptions.strategicMergePatch();

        assertEquals(PatchType.STRATEGIC_MERGE_PATCH, options.getPatchType());
        assertFalse(options.getDryRun());
        assertFalse(options.getForce());
    }

    @Test
    void testDryRun() {
        final PatchOptions options = PatchOptions.dryRun(PatchType.MERGE_PATCH);

        assertEquals(PatchType.MERGE_PATCH, options.getPatchType());
        assertTrue(options.getDryRun());
        assertFalse(options.getForce());
    }

    @Test
    void testWithFieldManager() {
        final PatchOptions options = PatchOptions.withFieldManager("my-controller");

        assertEquals("my-controller", options.getFieldManager());
        assertEquals(PatchType.STRATEGIC_MERGE_PATCH, options.getPatchType());
        assertFalse(options.getDryRun());
    }

    @Test
    void testBuilderWithNullValues() {
        final PatchOptions options = PatchOptions.builder()
                .patchType(null)
                .fieldManager(null)
                .build();

        assertNull(options.getPatchType());
        assertNull(options.getFieldManager());
        assertFalse(options.getDryRun());
        assertFalse(options.getForce());
    }

    @Test
    void testCombinedOptions() {
        final PatchOptions options = PatchOptions.builder()
                .patchType(PatchType.JSON_PATCH)
                .dryRun(true)
                .fieldManager("elev8")
                .force(true)
                .build();

        assertEquals(PatchType.JSON_PATCH, options.getPatchType());
        assertTrue(options.getDryRun());
        assertEquals("elev8", options.getFieldManager());
        assertTrue(options.getForce());
    }
}
