package io.elev8.core.patch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatchTypeTest {

    @Test
    void testJsonPatchContentType() {
        assertEquals("application/json-patch+json", PatchType.JSON_PATCH.getContentType());
    }

    @Test
    void testMergePatchContentType() {
        assertEquals("application/merge-patch+json", PatchType.MERGE_PATCH.getContentType());
    }

    @Test
    void testStrategicMergePatchContentType() {
        assertEquals("application/strategic-merge-patch+json", PatchType.STRATEGIC_MERGE_PATCH.getContentType());
    }

    @Test
    void testApplyPatchContentType() {
        assertEquals("application/apply-patch+yaml", PatchType.APPLY_PATCH.getContentType());
    }

    @Test
    void testAllPatchTypesHaveUniqueContentTypes() {
        final String jsonPatch = PatchType.JSON_PATCH.getContentType();
        final String mergePatch = PatchType.MERGE_PATCH.getContentType();
        final String strategicMergePatch = PatchType.STRATEGIC_MERGE_PATCH.getContentType();
        final String applyPatch = PatchType.APPLY_PATCH.getContentType();

        assertNotEquals(jsonPatch, mergePatch);
        assertNotEquals(jsonPatch, strategicMergePatch);
        assertNotEquals(jsonPatch, applyPatch);
        assertNotEquals(mergePatch, strategicMergePatch);
        assertNotEquals(mergePatch, applyPatch);
        assertNotEquals(strategicMergePatch, applyPatch);
    }

    @Test
    void testEnumValues() {
        final PatchType[] types = PatchType.values();

        assertEquals(4, types.length);
        assertEquals(PatchType.JSON_PATCH, types[0]);
        assertEquals(PatchType.MERGE_PATCH, types[1]);
        assertEquals(PatchType.STRATEGIC_MERGE_PATCH, types[2]);
        assertEquals(PatchType.APPLY_PATCH, types[3]);
    }
}
