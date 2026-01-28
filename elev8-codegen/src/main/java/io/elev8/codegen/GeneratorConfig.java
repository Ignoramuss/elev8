package io.elev8.codegen;

import lombok.Builder;
import lombok.Getter;

import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 * Configuration options for the CRD code generator.
 */
@Getter
@Builder
public class GeneratorConfig {

    private final File crdDirectory;
    private final File outputDirectory;
    private final String targetPackage;

    @Builder.Default
    private final boolean generateManagers = true;

    @Builder.Default
    private final boolean generateBuilders = true;

    @Builder.Default
    private final boolean useLombok = true;

    @Builder.Default
    private final Set<String> excludedCrds = Collections.emptySet();

    @Builder.Default
    private final String storageVersionOnly = null;

    public void validate() {
        if (crdDirectory == null) {
            throw new IllegalArgumentException("CRD directory is required");
        }
        if (!crdDirectory.exists()) {
            throw new IllegalArgumentException("CRD directory does not exist: " + crdDirectory);
        }
        if (!crdDirectory.isDirectory()) {
            throw new IllegalArgumentException("CRD path is not a directory: " + crdDirectory);
        }
        if (outputDirectory == null) {
            throw new IllegalArgumentException("Output directory is required");
        }
        if (targetPackage == null || targetPackage.isEmpty()) {
            throw new IllegalArgumentException("Target package is required");
        }
        if (!isValidPackageName(targetPackage)) {
            throw new IllegalArgumentException("Invalid target package name: " + targetPackage);
        }
    }

    private boolean isValidPackageName(final String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        final String[] parts = packageName.split("\\.");
        for (final String part : parts) {
            if (part.isEmpty() || !isValidJavaIdentifier(part)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidJavaIdentifier(final String identifier) {
        if (identifier.isEmpty()) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(identifier.charAt(0))) {
            return false;
        }
        for (int i = 1; i < identifier.length(); i++) {
            if (!Character.isJavaIdentifierPart(identifier.charAt(i))) {
                return false;
            }
        }
        return !isJavaKeyword(identifier);
    }

    private boolean isJavaKeyword(final String word) {
        return Set.of(
                "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
                "class", "const", "continue", "default", "do", "double", "else", "enum",
                "extends", "final", "finally", "float", "for", "goto", "if", "implements",
                "import", "instanceof", "int", "interface", "long", "native", "new", "package",
                "private", "protected", "public", "return", "short", "static", "strictfp",
                "super", "switch", "synchronized", "this", "throw", "throws", "transient",
                "try", "void", "volatile", "while", "true", "false", "null"
        ).contains(word);
    }
}
