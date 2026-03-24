package io.elev8.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an API element as stable.
 *
 * <p>Stable APIs follow semantic versioning guarantees. Breaking changes
 * are only introduced in major version releases, with at least one minor
 * release of deprecation notice before removal.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE, ElementType.CONSTRUCTOR})
public @interface Stable {

    String since() default "";
}
