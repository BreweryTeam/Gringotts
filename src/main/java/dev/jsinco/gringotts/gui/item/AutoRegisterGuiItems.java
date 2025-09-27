package dev.jsinco.gringotts.gui.item;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Lets {@link dev.jsinco.gringotts.gui.GringottsGui} know that it should automatically
 * register all {@link AbstractGuiItem} fields in the class. Items are registered immediately after
 * the child constructor is called.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoRegisterGuiItems {
    /**
     * If true, walks the class hierchy to find all {@link AbstractGuiItem}.
     * @return true if walk is enabled
     */
    boolean walk() default false;
}
