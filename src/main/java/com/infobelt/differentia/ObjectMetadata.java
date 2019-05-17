package com.infobelt.differentia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.Field;

@Slf4j
@Data
@AllArgsConstructor
public class ObjectMetadata {
    private AuditMetadata classAnnotation;
    private AuditMetadata propertyAnnotation;
    private Class<?> clazz;
    private Field field;

    public boolean isDescriptiveField() {
        return classAnnotation.descriptiveProperty().equals(field.getName());
    }

    public String getPropertyDescriptiveName() {
        if ("".equals(propertyAnnotation.name())) {
            return field.getName();
        } else {
            return propertyAnnotation.name();
        }
    }

    public String getFieldName() {
        return field.getName();
    }

    public String getEntityName() {
        if ("".equals(classAnnotation.name())) {
            return clazz.getSimpleName();
        } else {
            return classAnnotation.name();
        }
    }

    public AuditEventType getEvent(AuditEventType event) {
        switch (event) {
            case ADD:
                return propertyAnnotation.add();
            case REMOVE:
                return propertyAnnotation.remove();
            default:
                return event;
        }
    }

    public String getEntityDescriptiveName(Object entity) {
        if (!"".equals(classAnnotation.descriptiveProperty())) {
            try {
                return String.valueOf(PropertyUtils.getProperty(entity, classAnnotation.descriptiveProperty()));
            } catch (Exception e) {
                log.warn("Unable to get descriptive property " + classAnnotation.descriptiveProperty() + " on object " + entity);
                throw new RuntimeException("Unable to get descriptive property " + classAnnotation.descriptiveProperty() + " on object " + entity, e);
            }
        }

        // Always worth seeing if we have an ID
        if ("".equals(classAnnotation.descriptiveProperty())) {
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

        return entity.toString();
    }
}
