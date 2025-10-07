package dev.jsinco.gringotts.configuration;

import dev.jsinco.gringotts.configuration.files.Config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OkaeriFileName {
    String value() default "";
    boolean dynamicFileName() default false;
    String dynamicFileNameKey() default "";
    String dynamicFileNamePrefix() default "";
    Class<? extends OkaeriFile> dynamicFileNameHolder() default Config.class;
}
