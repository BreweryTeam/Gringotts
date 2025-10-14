package dev.jsinco.malts.configuration;

import dev.jsinco.malts.configuration.files.Config;

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
    String dynamicFileNameFormat() default "";
    Class<? extends OkaeriFile> dynamicFileNameHolder() default Config.class;
}
