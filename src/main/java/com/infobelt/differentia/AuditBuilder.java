package com.infobelt.differentia;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
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

        // Whats the top level event that is going on
        AuditEventType event = AuditEventType.CHANGE;
        if (oldInstance == null) {
            event = AuditEventType.ADD;
        } else if (newInstance == null) {
            event = AuditEventType.REMOVE;
        }

        AuditMetadata classAnnotation = referenceObject.getClass().getAnnotation(AuditMetadata.class);

        if (classAnnotation != null && !classAnnotation.ignore()) {

            for (Field field : referenceObject.getClass().getDeclaredFields()) {
                ObjectMetadata om = getObjectMetadata(field, classAnnotation, referenceObject);

                if (om.isTracked()) {

                    // Found a property
                    AuditChange auditChange = createAuditChange(event, om, referenceObject);
                    switch (event) {
                        case ADD:
                            if (om.isTraversable()) {
                                changes.addAll(traverse(event, om, classAnnotation, field, newInstance, oldInstance));
                            } else {
                                auditChange.setNewValue(getBeanValue(newInstance, field.getName(), om));
                                auditChange.setMessage(messageBuilder.buildChangeMessage(this, om, auditChange));

                                if (auditChange.getNewValue() != null)
                                    changes.add(auditChange);
                            }
                            break;
                        case CHANGE:
                            if (om.isTraversable()) {
                                changes.addAll(traverse(event, om, classAnnotation, field, newInstance, oldInstance));
                            } else {
                                String oldValue = getBeanValue(oldInstance, field.getName(), om);
                                String newValue = getBeanValue(newInstance, field.getName(), om);
                                if (!Objects.equals(newValue, oldValue)) {
                                    auditChange.setNewValue(newValue);
                                    auditChange.setOldValue(oldValue);
                                    auditChange.setMessage(messageBuilder.buildChangeMessage(this, om, auditChange));
                                    changes.add(auditChange);
                                }
                            }
                            break;
                        case REMOVE:
                            if (om.isTraversable()) {
                                changes.addAll(traverse(event, om, classAnnotation, field, newInstance, oldInstance));
                            } else {
                                auditChange.setOldValue(getBeanValue(oldInstance, field.getName(), om));
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

    private ObjectMetadata getObjectMetadata(Field field, AuditMetadata classAnnotation, Object referenceObject) {

        AuditMetadata[] metadataAnnotation = field.getAnnotationsByType(AuditMetadata.class);
        if (metadataAnnotation.length > 0) {
            AuditMetadata propertyAnnotation = metadataAnnotation[0];
            if (propertyAnnotation.ignore()) {
                return ObjectMetadata.notTracked();
            }
            return new ObjectMetadata(classAnnotation, propertyAnnotation, referenceObject.getClass(), field);
        } else {
            if (!classAnnotation.onlyAnnotated()) {
                return new ObjectMetadata(referenceObject.getClass(), field);
            } else {
                return ObjectMetadata.notTracked();

            }
        }
    }

    private AuditChange createAuditChange(AuditEventType event, ObjectMetadata om, Object object) {
        AuditChange auditChange = new AuditChange();
        auditChange.setEntity(om.getEntityName());
        auditChange.setEntityDescriptiveName(om.getEntityDescriptiveName(object));
        auditChange.setEventType(om.getEvent(event));
        auditChange.setProperty(om.getFieldName());
        auditChange.setDescriptiveName(om.getPropertyDescriptiveName());
        auditChange.setDescriptive(om.isDescriptiveField());
        return auditChange;
    }

    /**
     * Traverse will dig into a property and determine to traverse it and build more changes
     *
     * @param event
     * @param om
     * @param classAnnotation
     * @param field
     * @param newInstance
     * @param oldInstance
     * @return
     */
    private List<AuditChange> traverse(AuditEventType event, ObjectMetadata om, AuditMetadata classAnnotation, Field field, Object newInstance, Object oldInstance) {

        try {
            Object newValue = newInstance != null ? PropertyUtils.getProperty(newInstance, field.getName()) : null;
            Object oldValue = oldInstance != null ? PropertyUtils.getProperty(oldInstance, field.getName()) : null;
            Object referenceObject = newInstance != null ? newInstance : oldInstance;
            if (Collection.class.isAssignableFrom(field.getType())) {
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
                        ObjectMetadata newOm = new ObjectMetadata(classAnnotation, om, referenceObject.getClass(), field);
                        AuditChange auditChange = createAuditChange(AuditEventType.REMOVE, newOm, referenceObject);
                        auditChange.setRelatedEntity(getName(o));
                        auditChange.setOldValue(getBeanValue(o, newOm.getDescriptiveProperty(), newOm));
                        auditChange.setMessage(messageBuilder.buildChangeMessage(this, newOm, auditChange));
                        changes.add(auditChange);
                    });

                    // Additions
                    newValues.stream().filter(o -> !oldValues.contains(o)).forEach(o -> {
                        ObjectMetadata newOm = new ObjectMetadata(classAnnotation, om, referenceObject.getClass(), field);
                        AuditChange auditChange = createAuditChange(AuditEventType.ADD, newOm, referenceObject);
                        auditChange.setRelatedEntity(getName(o));
                        auditChange.setNewValue(getBeanValue(o, newOm.getDescriptiveProperty(), newOm));
                        auditChange.setMessage(messageBuilder.buildChangeMessage(this, newOm, auditChange));
                        changes.add(auditChange);
                    });
                }

                return changes;
            } else {
                return buildChanges(oldValue, newValue);
            }
        } catch (Exception e) {
            log.warn("Unable to get property to traverse " + field.getName());
            throw new RuntimeException("Unable to get the audit value for traversed property " + field.getName(), e);
        }

    }

    // The aim of this is to help with the stringification of things
    private String getBeanValue(Object instance, String name, ObjectMetadata om) {
        try {
            Object value = null;
            if ("".equals(om.getDescriptiveProperty())) {
                value = PropertyUtils.getProperty(instance, name);
            } else if ("".equals(name)) {
                value = String.valueOf(instance);
            } else if (om.isTraversable()) {
                value = PropertyUtils.getProperty(instance, om.getDescriptiveProperty());
            } else {
                value = PropertyUtils.getProperty(PropertyUtils.getProperty(instance, name), om.getDescriptiveProperty());
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

