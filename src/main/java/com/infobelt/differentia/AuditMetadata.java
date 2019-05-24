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

    /**
     * The event type to use for an add
     * <p>
     * This is useful is you want to handle the traverse and have it register as association
     * and disassociation
     *
     * @return The audit event type to use for an add
     */
    AuditEventType add() default AuditEventType.ADD;

    /**
     * The event type to use for an add
     * <p>
     * This is useful is you want to handle the traverse and have it register as association
     * and disassociation
     *
     * @return The audit event type to use for a remove
     */
    AuditEventType remove() default AuditEventType.REMOVE;

    /**
     * If set to true that we will only monitor the fields that are annotated
     *
     * @return false by default
     */
    boolean onlyAnnotated() default false;

    /**
     * Allows you to ignore a field or a class
     */
    boolean ignore() default false;

    /**
     * Define who the parent of a change is - this is useful in an association and disassocition
     * model, this refers to the property that is the parent
     */
    String parent() default "";

    /**
     * Define the property on the parent that holds this relationship
     * so that we understand how to get it
     */
    String mappedBy() default "";
}
