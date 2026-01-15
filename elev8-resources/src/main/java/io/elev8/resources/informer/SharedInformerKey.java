package io.elev8.resources.informer;

import java.util.Objects;

/**
 * Key for identifying unique SharedInformers in a factory.
 * Two informers with the same key share the same underlying watch and cache.
 */
public final class SharedInformerKey {

    private final Class<?> resourceClass;
    private final String namespace;
    private final String labelSelector;
    private final String fieldSelector;

    public SharedInformerKey(
            final Class<?> resourceClass,
            final String namespace,
            final String labelSelector,
            final String fieldSelector) {
        this.resourceClass = Objects.requireNonNull(resourceClass, "resourceClass cannot be null");
        this.namespace = namespace;
        this.labelSelector = labelSelector;
        this.fieldSelector = fieldSelector;
    }

    public Class<?> getResourceClass() {
        return resourceClass;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getLabelSelector() {
        return labelSelector;
    }

    public String getFieldSelector() {
        return fieldSelector;
    }

    public boolean isAllNamespaces() {
        return namespace == null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SharedInformerKey that = (SharedInformerKey) o;
        return Objects.equals(resourceClass, that.resourceClass)
                && Objects.equals(namespace, that.namespace)
                && Objects.equals(labelSelector, that.labelSelector)
                && Objects.equals(fieldSelector, that.fieldSelector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceClass, namespace, labelSelector, fieldSelector);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(resourceClass.getSimpleName());
        if (namespace != null) {
            sb.append("[namespace=").append(namespace).append("]");
        } else {
            sb.append("[all-namespaces]");
        }
        if (labelSelector != null) {
            sb.append("[labels=").append(labelSelector).append("]");
        }
        if (fieldSelector != null) {
            sb.append("[fields=").append(fieldSelector).append("]");
        }
        return sb.toString();
    }
}
