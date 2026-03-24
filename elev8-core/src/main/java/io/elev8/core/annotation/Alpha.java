package io.elev8.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an API element as alpha-level stability.
 *
 * <p>Alpha APIs may change or be removed without notice in any release.
 * They are not recommended for production use.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE, ElementType.CONSTRUCTOR})
public @interface Alpha {

    String since() default "";

    String description() default "";
}
