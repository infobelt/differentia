package com.infobelt.differentia;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use this annotation to determine which classes and
 * properties should be used for the audit message generation
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditMetadata {

    /**
     * The descriptive name of the property or class
     *
     * @return descriptive name
     */
    String name() default "";

    /**
     * The name of the property to use when we are building
     * a reference to this class
     *
     * @return the name of the property which best identifies the class
     */
    String descriptiveProperty() default "";
}
