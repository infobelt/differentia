package com.infobelt.differentia;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A change builder can look at two objects of the same
 * type and based on the annotations it can build a reasonably
 * meaningful message to describe the change
 */
@Slf4j
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
        return messageBuilder.buildChangeMessage(this, newInstance, oldInstance, changes);
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

        if (classAnnotation != null) {

            for (Field field : referenceObject.getClass().getDeclaredFields()) {
                AuditMetadata[] metadataAnnotation = field.getAnnotationsByType(AuditMetadata.class);
                if (metadataAnnotation.length > 0) {

                    AuditMetadata propertyAnnotation = metadataAnnotation[0];

                    // Found a property
                    AuditChange auditChange = new AuditChange();
                    auditChange.setEventType(event);
                    auditChange.setProperty(field.getName());
                    auditChange.setDescriptiveName(propertyAnnotation.name());
                    auditChange.setDescriptive(classAnnotation.descriptiveProperty().equals(field.getName()));
                    switch (event) {
                        case ADD:
                            if (propertyAnnotation.traverse()) {
                                changes.addAll(traverse(event, propertyAnnotation, field, newInstance, oldInstance));
                            } else {
                                auditChange.setNewValue(getBeanValue(newInstance, field.getName(), propertyAnnotation));
                                changes.add(auditChange);
                            }
                            break;
                        case CHANGE:
                            if (propertyAnnotation.traverse()) {
                                changes.addAll(traverse(event, propertyAnnotation, field, newInstance, oldInstance));
                            } else {
                                String oldValue = getBeanValue(oldInstance, field.getName(), propertyAnnotation);
                                String newValue = getBeanValue(newInstance, field.getName(), propertyAnnotation);
                                if (!Objects.equals(newValue, oldValue)) {
                                    auditChange.setNewValue(newValue);
                                    auditChange.setOldValue(oldValue);
                                    changes.add(auditChange);
                                }
                            }
                            break;
                        case REMOVE:
                            if (propertyAnnotation.traverse()) {
                                changes.addAll(traverse(event, propertyAnnotation, field, newInstance, oldInstance));
                            } else {
                                auditChange.setOldValue(getBeanValue(oldInstance, field.getName(), propertyAnnotation));
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

    /**
     * Traverse will dig into a property and determine to traverse it and build more changes
     *
     * @param event
     * @param propertyAnnotation
     * @param field
     * @param newInstance
     * @param oldInstance
     * @return
     */
    private List<AuditChange> traverse(AuditEventType event, AuditMetadata propertyAnnotation, Field field, Object newInstance, Object oldInstance) {

        try {
            Object newValue = newInstance != null ? PropertyUtils.getProperty(newInstance, field.getName()) : null;
            Object oldValue = oldInstance != null ? PropertyUtils.getProperty(newInstance, field.getName()) : null;

            if (newValue instanceof Collection<?> || oldValue instanceof Collection<?>) {
                List<AuditChange> changes = new ArrayList<>();
                if (newValue == null) {
                    ((Collection)oldValue).forEach(o -> changes.addAll(buildChanges(o,null)));
                } else if (oldValue == null) {
                    ((Collection)newValue).forEach(o -> changes.addAll(buildChanges(o,null)));
                } else {
                    // So we have a list - we will need to go through the list
                    // and determine compare objects that match

                    // Starting with old - find all the stuff that is in new

                    
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
    private String getBeanValue(Object instance, String name, AuditMetadata auditMetadata) {
        try {
            if ("".equals(auditMetadata.descriptiveProperty())) {
                return String.valueOf(PropertyUtils.getProperty(instance, name));
            } else {
                return String.valueOf(PropertyUtils.getProperty(PropertyUtils.getProperty(instance, name), auditMetadata.descriptiveProperty()));
            }
        } catch (Exception e) {
            log.warn("Unable to get property " + name);
            throw new RuntimeException("Unable to get the audit value for property " + name, e);
        }
    }

    public String getName(Object instance, boolean capitalize) {
        return capitalize ? StringUtils.capitalize(getName(instance)) : StringUtils.uncapitalize(getName(instance));
    }

    public String getName(Object instance) {
        StringBuilder sb = new StringBuilder();
        AuditMetadata auditMetadata = instance.getClass().getAnnotation(AuditMetadata.class);
        if (auditMetadata != null && !"".equals(auditMetadata.name())) {
            sb.append(auditMetadata.name());

            if (!"".equals(auditMetadata.descriptiveProperty())) {
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

