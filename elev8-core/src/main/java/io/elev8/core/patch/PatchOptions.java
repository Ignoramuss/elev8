package io.elev8.core.patch;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration options for patch operations on Kubernetes resources.
 * These options control how patches are applied and validated.
 */
@Getter
@Builder
public class PatchOptions {
    /**
     * The type of patch to apply (JSON Patch, Merge Patch, or Strategic Merge Patch).
     */
    @Builder.Default
    private final PatchType patchType = PatchType.STRATEGIC_MERGE_PATCH;

    /**
     * If true, the patch will be validated but not persisted.
     * Useful for testing patches before applying them.
     */
    @Builder.Default
    private final Boolean dryRun = false;

    /**
     * The name of the field manager for tracking field ownership.
     * Used for Server-side Apply and conflict resolution.
     */
    private final String fieldManager;

    /**
     * If true, force the patch to be applied even if there are conflicts.
     * This takes ownership of conflicting fields.
     */
    @Builder.Default
    private final Boolean force = false;

    /**
     * Creates a default PatchOptions instance using Strategic Merge Patch.
     *
     * @return a new PatchOptions with default values
     */
    public static PatchOptions defaults() {
        return PatchOptions.builder().build();
    }

    /**
     * Creates a PatchOptions instance for JSON Patch (RFC 6902).
     *
     * @return a new PatchOptions configured for JSON Patch
     */
    public static PatchOptions jsonPatch() {
        return PatchOptions.builder()
                .patchType(PatchType.JSON_PATCH)
                .build();
    }

    /**
     * Creates a PatchOptions instance for JSON Merge Patch (RFC 7396).
     *
     * @return a new PatchOptions configured for Merge Patch
     */
    public static PatchOptions mergePatch() {
        return PatchOptions.builder()
                .patchType(PatchType.MERGE_PATCH)
                .build();
    }

    /**
     * Creates a PatchOptions instance for Strategic Merge Patch.
     *
     * @return a new PatchOptions configured for Strategic Merge Patch
     */
    public static PatchOptions strategicMergePatch() {
        return PatchOptions.builder()
                .patchType(PatchType.STRATEGIC_MERGE_PATCH)
                .build();
    }

    /**
     * Creates a PatchOptions instance with dry-run enabled.
     *
     * @param patchType the type of patch to apply
     * @return a new PatchOptions configured for dry-run
     */
    public static PatchOptions dryRun(final PatchType patchType) {
        return PatchOptions.builder()
                .patchType(patchType)
                .dryRun(true)
                .build();
    }

    /**
     * Creates a PatchOptions instance with a field manager.
     *
     * @param fieldManager the name of the field manager
     * @return a new PatchOptions with the specified field manager
     */
    public static PatchOptions withFieldManager(final String fieldManager) {
        return PatchOptions.builder()
                .fieldManager(fieldManager)
                .build();
    }
}
