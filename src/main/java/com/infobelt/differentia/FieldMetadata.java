package com.infobelt.differentia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldMetadata {
    private ObjectMetadata objectMetadata;
    private boolean tracked;
    private AuditMetadata propertyAnnotation;
    private Field field;

    public FieldMetadata(ObjectMetadata objectMetadata, Field field) {
        this.objectMetadata = objectMetadata;
        AuditMetadata[] annotations = field.getAnnotationsByType(AuditMetadata.class);
        this.propertyAnnotation = annotations.length > 0 ? annotations[0] : null;
        this.field = field;

        this.tracked = true;

        if (objectMetadata.getClassAnnotation().onlyAnnotated() && propertyAnnotation == null) {
            tracked = false;
        } else if (propertyAnnotation != null) {
            tracked = !propertyAnnotation.ignore();
        }
    }

    public boolean isDescriptiveField() {
        return objectMetadata.getClassAnnotation() != null && objectMetadata.getClassAnnotation().descriptiveProperty().equals(field.getName());
    }

    public boolean isTraversable() {
        return propertyAnnotation != null && propertyAnnotation.traverse();
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

    public String getDescriptiveProperty() {
        if (propertyAnnotation != null) {
            return propertyAnnotation.descriptiveProperty();
        } else {
            return "";
        }
    }

    public Class<?> getFieldType() {
        return field.getType();
    }
}
