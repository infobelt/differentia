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

    /**
     * If a property is annotated with traverse as true then when you
     * build the changes we will go into this property and see if we can build changes for
     * either this object in it - or if it is a list the objects in the list
     *
     * @return true if you want to traverse into this property
     */
    boolean traverse() default false;
}
