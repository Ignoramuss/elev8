package io.elev8.core.patch;

import lombok.Getter;

/**
 * Represents the type of patch operation to apply to a Kubernetes resource.
 * Each patch type has a different merge strategy and Content-Type header.
 */
@Getter
public enum PatchType {
    /**
     * JSON Patch (RFC 6902) - Most precise patch format.
     * Uses a sequence of operations (add, remove, replace, move, copy, test).
     * Example: [{"op": "replace", "path": "/spec/replicas", "value": 3}]
     * Content-Type: application/json-patch+json
     */
    JSON_PATCH("application/json-patch+json"),

    /**
     * JSON Merge Patch (RFC 7396) - Simplest patch format.
     * Sends a partial JSON object with fields to update.
     * Null values delete fields.
     * Example: {"spec": {"replicas": 3}}
     * Content-Type: application/merge-patch+json
     */
    MERGE_PATCH("application/merge-patch+json"),

    /**
     * Strategic Merge Patch - Kubernetes-specific patch format.
     * Uses Kubernetes API resource struct tags to determine merge strategy.
     * Understands array merge semantics (merge vs replace).
     * Example: {"spec": {"replicas": 3}}
     * Content-Type: application/strategic-merge-patch+json
     * Note: Not supported for custom resources.
     */
    STRATEGIC_MERGE_PATCH("application/strategic-merge-patch+json");

    private final String contentType;

    PatchType(final String contentType) {
        this.contentType = contentType;
    }
}
