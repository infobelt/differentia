package com.infobelt.differentia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.Field;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectMetadata {
    private String propertyDescriptiveName;
    private String fieldName;
    private String entityName;
    private boolean tracked;
    public AuditEventType event;
    private AuditMetadata classAnnotation;
    private AuditMetadata propertyAnnotation;
    private Class<?> clazz;
    private Field field;

    public ObjectMetadata(AuditMetadata classAnnotation, AuditMetadata propertyAnnotation, Class<?> aClass, Field field) {
        tracked = true;
        this.classAnnotation = classAnnotation;
        this.propertyAnnotation = propertyAnnotation;
        this.clazz = aClass;
        this.field = field;
    }

    public ObjectMetadata(Class<?> aClass, Field field) {
        this.tracked = true;
        this.clazz = aClass;
        this.field = field;
    }

    public ObjectMetadata(AuditMetadata classAnnotation, ObjectMetadata om, Class<?> aClass, Field field) {
        this.classAnnotation = classAnnotation;
        this.propertyAnnotation = om.propertyAnnotation;
        this.clazz = aClass;
        this.field = field;
    }

    public static ObjectMetadata notTracked() {
        ObjectMetadata om = new ObjectMetadata();
        om.setTracked(false);
        return om;
    }

    public boolean isDescriptiveField() {
        return classAnnotation != null && classAnnotation.descriptiveProperty().equals(field.getName());
    }

    public boolean isTraversable() {
        return propertyAnnotation != null && propertyAnnotation.traverse();
    }

    public String getPropertyDescriptiveName() {
        if (propertyAnnotation != null) {
            if ("".equals(propertyAnnotation.name())) {
                return field.getName();
            } else {
                return propertyAnnotation.name();
            }
        } else {
            return field.getName();
        }
    }

    public String getFieldName() {
        return field.getName();
    }

    public String getEntityName() {
        if (classAnnotation == null || "".equals(classAnnotation.name())) {
            return clazz.getSimpleName();
        } else {
            return classAnnotation.name();
        }
    }

    public AuditEventType getEvent(AuditEventType event) {
        switch (event) {
            case ADD:
                return propertyAnnotation != null ? propertyAnnotation.add() : AuditEventType.ADD;
            case REMOVE:
                return propertyAnnotation != null ? propertyAnnotation.remove() : AuditEventType.REMOVE;
            default:
                return event;
        }
    }

    public String getEntityDescriptiveName(Object entity) {

        if (classAnnotation != null && !"".equals(classAnnotation.descriptiveProperty())) {
            try {
                return String.valueOf(PropertyUtils.getProperty(entity, classAnnotation.descriptiveProperty()));
            } catch (Exception e) {
                log.warn("Unable to get descriptive property " + classAnnotation.descriptiveProperty() + " on object " + entity);
                throw new RuntimeException("Unable to get descriptive property " + classAnnotation.descriptiveProperty() + " on object " + entity, e);
            }
        }

        // Always worth seeing if we have an ID
        if (classAnnotation == null || "".equals(classAnnotation.descriptiveProperty())) {
            Object id = null;
            try {
                id = PropertyUtils.getProperty(entity, "id");
                if (id != null) {
                    return String.valueOf(id);
                }
            } catch (Exception e) {
                // Ignore and try something else
            }
        }

        return "";
    }

    public String getDescriptiveProperty() {
        if (propertyAnnotation != null) {
            return propertyAnnotation.descriptiveProperty();
        } else {
            return "";
        }
    }
}
