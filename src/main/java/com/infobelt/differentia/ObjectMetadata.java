package com.infobelt.differentia;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;

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
}
