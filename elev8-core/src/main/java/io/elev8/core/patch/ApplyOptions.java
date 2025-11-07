package io.elev8.core.patch;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration options for Server-side Apply operations on Kubernetes resources.
 * Server-side Apply (SSA) provides declarative resource management with field-level
 * ownership tracking. It's the recommended approach for GitOps and declarative workflows.
 *
 * <p>Server-side Apply became GA in Kubernetes 1.22 and solves the "last write wins"
 * problem by tracking which fields are owned by which field managers.</p>
 *
 * @see <a href="https://kubernetes.io/docs/reference/using-api/server-side-apply/">Server-side Apply Documentation</a>
 */
@Getter
@Builder
public class ApplyOptions {
    /**
     * The name of the field manager for tracking field ownership.
     * REQUIRED for Server-side Apply operations.
     * This identifies who is managing which fields in the resource.
     */
    private final String fieldManager;

    /**
     * If true, the apply will be validated but not persisted.
     * Useful for testing manifests before applying them.
     */
    @Builder.Default
    private final Boolean dryRun = false;

    /**
     * If true, force the apply to take ownership of conflicting fields.
     * Use with caution as it takes ownership from other field managers.
     */
    @Builder.Default
    private final Boolean force = false;

    /**
     * Creates an ApplyOptions instance with a field manager.
     * This is the minimum required configuration for Server-side Apply.
     *
     * @param fieldManager the name of the field manager (required)
     * @return a new ApplyOptions with the specified field manager
     * @throws IllegalArgumentException if fieldManager is null or empty
     */
    public static ApplyOptions of(final String fieldManager) {
        if (fieldManager == null || fieldManager.trim().isEmpty()) {
            throw new IllegalArgumentException("fieldManager is required for Server-side Apply");
        }
        return ApplyOptions.builder()
                .fieldManager(fieldManager)
                .build();
    }

    /**
     * Creates an ApplyOptions instance with dry-run enabled.
     *
     * @param fieldManager the name of the field manager (required)
     * @return a new ApplyOptions configured for dry-run
     * @throws IllegalArgumentException if fieldManager is null or empty
     */
    public static ApplyOptions dryRun(final String fieldManager) {
        if (fieldManager == null || fieldManager.trim().isEmpty()) {
            throw new IllegalArgumentException("fieldManager is required for Server-side Apply");
        }
        return ApplyOptions.builder()
                .fieldManager(fieldManager)
                .dryRun(true)
                .build();
    }

    /**
     * Creates an ApplyOptions instance with force enabled.
     * Use with caution as this takes ownership of conflicting fields.
     *
     * @param fieldManager the name of the field manager (required)
     * @return a new ApplyOptions configured to force apply
     * @throws IllegalArgumentException if fieldManager is null or empty
     */
    public static ApplyOptions force(final String fieldManager) {
        if (fieldManager == null || fieldManager.trim().isEmpty()) {
            throw new IllegalArgumentException("fieldManager is required for Server-side Apply");
        }
        return ApplyOptions.builder()
                .fieldManager(fieldManager)
                .force(true)
                .build();
    }

    /**
     * Validates that the ApplyOptions has a valid field manager.
     *
     * @throws IllegalStateException if fieldManager is null or empty
     */
    public void validate() {
        if (fieldManager == null || fieldManager.trim().isEmpty()) {
            throw new IllegalStateException("fieldManager is required for Server-side Apply operations");
        }
    }

    /**
     * Converts ApplyOptions to PatchOptions for use with the patch endpoint.
     *
     * @return a PatchOptions instance configured for Server-side Apply
     */
    public PatchOptions toPatchOptions() {
        validate();
        return PatchOptions.builder()
                .patchType(PatchType.APPLY_PATCH)
                .fieldManager(fieldManager)
                .force(force)
                .dryRun(dryRun)
                .build();
    }
}
