package io.elev8.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an API element as beta-level stability.
 *
 * <p>Beta APIs are nearing stability but may still change in minor releases.
 * They are suitable for production use with awareness of potential changes.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE, ElementType.CONSTRUCTOR})
public @interface Beta {

    String since() default "";

    String description() default "";
}
