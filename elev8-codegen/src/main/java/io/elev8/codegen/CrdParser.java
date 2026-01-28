package io.elev8.codegen;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.elev8.resources.crd.CRDVersion;
import io.elev8.resources.crd.CustomResourceDefinition;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parser for CustomResourceDefinition YAML/JSON files.
 */
@Slf4j
public class CrdParser {

    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    public CrdParser() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.jsonMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Parse a CRD from a file (YAML or JSON).
     *
     * @param file the CRD file
     * @return the parsed CustomResourceDefinition
     * @throws CrdParseException if parsing fails
     */
    public CustomResourceDefinition parse(final File file) throws CrdParseException {
        if (file == null) {
            throw new CrdParseException("File cannot be null");
        }
        if (!file.exists()) {
            throw new CrdParseException("File does not exist: " + file.getAbsolutePath());
        }

        try (final InputStream is = new FileInputStream(file)) {
            return parse(is, getFileType(file));
        } catch (IOException e) {
            throw new CrdParseException("Failed to read file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Parse a CRD from an input stream.
     *
     * @param inputStream the input stream
     * @param fileType    the file type (yaml or json)
     * @return the parsed CustomResourceDefinition
     * @throws CrdParseException if parsing fails
     */
    public CustomResourceDefinition parse(final InputStream inputStream, final FileType fileType)
            throws CrdParseException {
        if (inputStream == null) {
            throw new CrdParseException("Input stream cannot be null");
        }
        if (fileType == null) {
            throw new CrdParseException("File type cannot be null");
        }

        try {
            final ObjectMapper mapper = fileType == FileType.YAML ? yamlMapper : jsonMapper;
            final CustomResourceDefinition crd = mapper.readValue(inputStream, CustomResourceDefinition.class);
            validateCrd(crd);
            return crd;
        } catch (IOException e) {
            throw new CrdParseException("Failed to parse CRD", e);
        }
    }

    /**
     * Parse all CRD files in a directory.
     *
     * @param directory the directory containing CRD files
     * @return list of parsed CustomResourceDefinitions
     * @throws CrdParseException if parsing fails
     */
    public List<CustomResourceDefinition> parseDirectory(final File directory) throws CrdParseException {
        if (directory == null) {
            throw new CrdParseException("Directory cannot be null");
        }
        if (!directory.exists()) {
            throw new CrdParseException("Directory does not exist: " + directory.getAbsolutePath());
        }
        if (!directory.isDirectory()) {
            throw new CrdParseException("Path is not a directory: " + directory.getAbsolutePath());
        }

        final List<CustomResourceDefinition> crds = new ArrayList<>();
        final File[] files = directory.listFiles((dir, name) ->
                name.endsWith(".yaml") || name.endsWith(".yml") || name.endsWith(".json"));

        if (files == null) {
            return crds;
        }

        for (final File file : files) {
            try {
                log.info("Parsing CRD file: {}", file.getName());
                crds.add(parse(file));
            } catch (CrdParseException e) {
                log.warn("Failed to parse CRD file {}: {}", file.getName(), e.getMessage());
                throw e;
            }
        }

        return crds;
    }

    /**
     * Get the storage version from a CRD.
     * The storage version is the version with storage=true.
     *
     * @param crd the CustomResourceDefinition
     * @return the storage version, or the first version if none is marked as storage
     */
    public CRDVersion getStorageVersion(final CustomResourceDefinition crd) {
        if (crd.getSpec() == null || crd.getSpec().getVersions() == null
                || crd.getSpec().getVersions().isEmpty()) {
            return null;
        }

        final Optional<CRDVersion> storageVersion = crd.getSpec().getVersions().stream()
                .filter(v -> Boolean.TRUE.equals(v.getStorage()))
                .findFirst();

        return storageVersion.orElse(crd.getSpec().getVersions().get(0));
    }

    private void validateCrd(final CustomResourceDefinition crd) throws CrdParseException {
        if (crd == null) {
            throw new CrdParseException("Parsed CRD is null");
        }
        if (!"CustomResourceDefinition".equals(crd.getKind())) {
            throw new CrdParseException("Invalid kind: expected CustomResourceDefinition, got " + crd.getKind());
        }
        if (crd.getSpec() == null) {
            throw new CrdParseException("CRD spec is missing");
        }
        if (crd.getSpec().getGroup() == null || crd.getSpec().getGroup().isEmpty()) {
            throw new CrdParseException("CRD spec.group is required");
        }
        if (crd.getSpec().getNames() == null) {
            throw new CrdParseException("CRD spec.names is required");
        }
        if (crd.getSpec().getNames().getKind() == null || crd.getSpec().getNames().getKind().isEmpty()) {
            throw new CrdParseException("CRD spec.names.kind is required");
        }
        if (crd.getSpec().getNames().getPlural() == null || crd.getSpec().getNames().getPlural().isEmpty()) {
            throw new CrdParseException("CRD spec.names.plural is required");
        }
        if (crd.getSpec().getVersions() == null || crd.getSpec().getVersions().isEmpty()) {
            throw new CrdParseException("CRD spec.versions is required");
        }
    }

    private FileType getFileType(final File file) {
        final String name = file.getName().toLowerCase();
        if (name.endsWith(".json")) {
            return FileType.JSON;
        }
        return FileType.YAML;
    }

    /**
     * File type enum for parsing.
     */
    public enum FileType {
        YAML,
        JSON
    }
}
