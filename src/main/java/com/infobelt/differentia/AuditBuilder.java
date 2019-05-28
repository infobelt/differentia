package com.infobelt.differentia;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A change builder can look at two objects of the same
 * type and based on the annotations it can build a reasonably
 * meaningful message to describe the change
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class AuditBuilder {

    private MessageBuilder messageBuilder = new DefaultMessageBuilder();

    /**
     * Build a message to describe what happened between the two objects
     *
     * @param oldInstance The old instance (or null if it is new)
     * @param newInstance The new instance (or null if it has been deleted)
     * @return A text message described what happen
     */
    public String buildMessage(Object oldInstance, Object newInstance) {
        if (oldInstance == null)
            return messageBuilder.buildNewMessage(this, newInstance);
        else if (newInstance == null)
            return messageBuilder.buildDeleteMessage(this, oldInstance);
        else
            return changeMessage(oldInstance, newInstance);
    }

    public String changeMessage(Object oldInstance, Object newInstance) {
        List<AuditChange> changes = buildChanges(oldInstance, newInstance);
        return messageBuilder.buildChangesMessage(this, newInstance, oldInstance, changes);
    }

    public List<AuditChange> buildChanges(Object oldInstance, Object newInstance) {
        List<AuditChange> changes = new ArrayList<>();

        // Grab an object as reference and then go through the properties
        Object referenceObject = oldInstance != null ? oldInstance : newInstance;
        ObjectMetadata om = new ObjectMetadata(referenceObject);

        // Whats the top level event that is going on
        AuditEventType event = AuditEventType.CHANGE;
        if (oldInstance == null) {
            event = AuditEventType.ADD;
        } else if (newInstance == null) {
            event = AuditEventType.REMOVE;
        }

        // Do we have a parent, then we need to make sure we have a change for it
        if (om.hasParent()) {


            AuditChange auditChange = new AuditChange();
            auditChange.setEntity(om.getParentObjectMetadata().getEntityName());
            auditChange.setEntityDescriptiveName(om.getParentObjectMetadata().getEntityDescriptiveName(om.getParentObject(referenceObject)));

            FieldMetadata fieldMetadata = om.getParentObjectMetadata().getField(om.getMappedBy());

            auditChange.setAffectedId(om.getParentObjectMetadata().getAffectedId(om.getParentObject(referenceObject)));
            auditChange.setEventType(fieldMetadata.getEvent(event));
            auditChange.setProperty(fieldMetadata.getFieldName());
            auditChange.setDescriptiveName(fieldMetadata.getPropertyDescriptiveName());
            auditChange.setDescriptive(fieldMetadata.isDescriptiveField());
            auditChange.setRelatedEntity(om.getEntityName());
            auditChange.setNewValue("");
            auditChange.setOldValue("");
            auditChange.setMessage(messageBuilder.buildChangeMessage(this, om.getParentObjectMetadata(), auditChange));

            if (event == AuditEventType.CHANGE) {
                try {
                    Object oldParent = om.getParentObject(oldInstance);
                    Object oldParentValue = PropertyUtils.getProperty(oldParent, fieldMetadata.getFieldName());
                    if (oldParentValue instanceof Collection) {
                        if (!((Collection) oldParentValue).contains(oldInstance)) {
                            changes.add(auditChange);
                        }
                    } else {
                        if (!oldParentValue.equals(oldInstance)) {
                            changes.add(auditChange);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Unable to get mappedBy field " + fieldMetadata.getFieldName() + " on " + oldInstance, e);
                }
            } else {
                changes.add(auditChange);
            }
        }

        if (om.isTracked()) {

            for (FieldMetadata fieldMetadata : om.getFields()) {
                if (fieldMetadata.isTracked()) {

                    // Found a property
                    AuditChange auditChange = createAuditChange(event, fieldMetadata, referenceObject);
                    switch (event) {
                        case ADD:
                            if (fieldMetadata.isTraversable()) {
                                changes.addAll(traverse(event, fieldMetadata, newInstance, oldInstance));
                            } else {
                                auditChange.setNewValue(getBeanValue(newInstance, fieldMetadata.getFieldName(), fieldMetadata));
                                auditChange.setMessage(messageBuilder.buildChangeMessage(this, om, auditChange));

                                if (auditChange.getNewValue() != null)
                                    changes.add(auditChange);
                            }
                            break;
                        case CHANGE:
                            if (fieldMetadata.isTraversable()) {
                                changes.addAll(traverse(event, fieldMetadata, newInstance, oldInstance));
                            } else {
                                String oldValue = getBeanValue(oldInstance, fieldMetadata.getFieldName(), fieldMetadata);
                                String newValue = getBeanValue(newInstance, fieldMetadata.getFieldName(), fieldMetadata);
                                if (!Objects.equals(newValue, oldValue)) {
                                    auditChange.setNewValue(newValue);
                                    auditChange.setOldValue(oldValue);
                                    auditChange.setMessage(messageBuilder.buildChangeMessage(this, om, auditChange));
                                    changes.add(auditChange);
                                }
                            }
                            break;
                        case REMOVE:
                            if (fieldMetadata.isTraversable()) {
                                changes.addAll(traverse(event, fieldMetadata, newInstance, oldInstance));
                            } else {
                                auditChange.setOldValue(getBeanValue(oldInstance, fieldMetadata.getFieldName(), fieldMetadata));
                                auditChange.setMessage(messageBuilder.buildChangeMessage(this, om, auditChange));
                                if (auditChange.getOldValue() != null)
                                    changes.add(auditChange);
                            }
                            break;
                    }
                }

            }
        } else {
            log.warn("You are sure you wanted to audit this object " + referenceObject.getClass() + ", if you do you need to add the @AuditMetadata annotation to the object");
        }

        return changes;
    }

    private AuditChange createAuditChange(AuditEventType event, FieldMetadata fieldMetadata, Object object) {
        AuditChange auditChange = new AuditChange();
        auditChange.setAffectedId(fieldMetadata.getObjectMetadata().getAffectedId(object));
        auditChange.setEntity(fieldMetadata.getObjectMetadata().getEntityName());
        auditChange.setEntityDescriptiveName(fieldMetadata.getObjectMetadata().getEntityDescriptiveName(object));
        auditChange.setEventType(fieldMetadata.getEvent(event));
        auditChange.setProperty(fieldMetadata.getFieldName());
        auditChange.setDescriptiveName(fieldMetadata.getPropertyDescriptiveName());
        auditChange.setDescriptive(fieldMetadata.isDescriptiveField());
        return auditChange;
    }

    /**
     * Traverse will dig into a property and determine to traverse it and build more changes
     *
     * @param event
     * @param fieldMetadata
     * @param newInstance
     * @param oldInstance
     * @return
     */
    private List<AuditChange> traverse(AuditEventType event, FieldMetadata fieldMetadata, Object
            newInstance, Object oldInstance) {

        try {
            Object newValue = newInstance != null ? PropertyUtils.getProperty(newInstance, fieldMetadata.getFieldName()) : null;
            Object oldValue = oldInstance != null ? PropertyUtils.getProperty(oldInstance, fieldMetadata.getFieldName()) : null;
            Object referenceObject = newInstance != null ? newInstance : oldInstance;
            if (Collection.class.isAssignableFrom(fieldMetadata.getFieldType())) {
                List<AuditChange> changes = new ArrayList<>();
                if (newValue == null) {
                    ((Collection<?>) oldValue).forEach(o -> changes.addAll(buildChanges(o, null)));
                } else if (oldValue == null) {
                    ((Collection<?>) newValue).forEach(o -> changes.addAll(buildChanges(o, null)));
                } else {
                    // So we have a list, we will need to go through the list
                    // and determine compare objects that match
                    List<Object> oldValues = new ArrayList<>((Collection<?>) oldValue);
                    List<Object> newValues = new ArrayList<>((Collection<?>) newValue);

                    // Modifications
                    oldValues.stream().filter(newValues::contains).forEach(o ->
                            changes.addAll(buildChanges(o, newValues.get(newValues.indexOf(o)))));

                    // TODO Shame we can't use Predicate.not - roll on Java11

                    // Removals
                    oldValues.stream().filter(o -> !newValues.contains(o)).forEach(o -> {
//                        ObjectMetadata newOm = new ObjectMetadata(classAnnotation, om, referenceObject.getClass(), field);
                        AuditChange auditChange = createAuditChange(AuditEventType.REMOVE, fieldMetadata, referenceObject);
                        auditChange.setRelatedEntity(getName(o));
                        auditChange.setOldValue(getBeanValue(o, fieldMetadata.getDescriptiveProperty(), fieldMetadata));
                        auditChange.setMessage(messageBuilder.buildChangeMessage(this, fieldMetadata.getObjectMetadata(), auditChange));
                        changes.add(auditChange);
                    });

                    // Additions
                    newValues.stream().filter(o -> !oldValues.contains(o)).forEach(o -> {
//                        ObjectMetadata newOm = new ObjectMetadata(classAnnotation, om, referenceObject.getClass(), field);
                        AuditChange auditChange = createAuditChange(AuditEventType.ADD, fieldMetadata, referenceObject);
                        auditChange.setRelatedEntity(getName(o));
                        auditChange.setNewValue(getBeanValue(o, fieldMetadata.getDescriptiveProperty(), fieldMetadata));
                        auditChange.setMessage(messageBuilder.buildChangeMessage(this, fieldMetadata.getObjectMetadata(), auditChange));
                        changes.add(auditChange);
                    });
                }

                return changes;
            } else {
                return buildChanges(oldValue, newValue);
            }
        } catch (Exception e) {
            log.warn("Unable to get property to traverse " + fieldMetadata.getFieldName());
            throw new RuntimeException("Unable to get the audit value for traversed property " + fieldMetadata.getFieldName(), e);
        }

    }

    // The aim of this is to help with the stringification of things
    private String getBeanValue(Object instance, String name, FieldMetadata fieldMetadata) {

        // Handle a null object
        if (instance==null)
            return null;

        try {
            Object value = null;
            if ("".equals(fieldMetadata.getDescriptiveProperty())) {
                value = PropertyUtils.getProperty(instance, name);
            } else if ("".equals(name)) {
                value = String.valueOf(instance);
            } else if (fieldMetadata.isTraversable()) {
                value = PropertyUtils.getProperty(instance, fieldMetadata.getDescriptiveProperty());
            } else {
                value = PropertyUtils.getProperty(PropertyUtils.getProperty(instance, name), fieldMetadata.getDescriptiveProperty());
            }

            return value == null ? null : String.valueOf(value);

        } catch (Exception e) {
            log.warn("Unable to get property " + name);
            throw new RuntimeException("Unable to get the audit value for property " + name, e);
        }
    }

    public String getName(Object instance, boolean includeDescriptive, boolean capitalize) {
        return capitalize ? StringUtils.capitalize(getName(instance, includeDescriptive)) : StringUtils.uncapitalize(getName(instance, includeDescriptive));
    }

    public String getName(Object instance) {
        return getName(instance, true);
    }

    public String getName(Object instance, boolean includeDescriptive) {
        StringBuilder sb = new StringBuilder();
        AuditMetadata auditMetadata = instance.getClass().getAnnotation(AuditMetadata.class);
        if (auditMetadata != null && !"".equals(auditMetadata.name())) {
            sb.append(auditMetadata.name());

            if (includeDescriptive && !"".equals(auditMetadata.descriptiveProperty())) {
                try {
                    sb.append(" ");
                    sb.append(BeanUtils.getProperty(instance, auditMetadata.descriptiveProperty()));
                } catch (Exception e) {
                    log.warn("Unable to get descriptive property [" + auditMetadata.descriptiveProperty() + "] on object " + instance);
                }
            }
        } else {
            sb.append(instance.getClass().getSimpleName());
        }
        return sb.toString();
    }

    public String buildNewMessage(Object obj) {
        return messageBuilder.buildNewMessage(this, obj);
    }

    public String buildDeleteMessage(Object obj) {
        return messageBuilder.buildDeleteMessage(this, obj);
    }
}

