package com.infobelt.differentia;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class ObjectMetadata {
    private boolean ignoreSelf = false;
    private List<ObjectMetadata> parentsObjectMetadata = new ArrayList<>();
    private String propertyDescriptiveName;
    private String entityName;
    private boolean tracked;
    private AuditMetadata classAnnotation;
    private Class<?> clazz;
    private List<FieldMetadata> fields = new ArrayList<>();
    private Map<String, FieldMetadata> fieldMap = new HashMap<>();

    private String parent;

    private boolean join = false;

    ObjectMetadata(Object object) {
        this.classAnnotation = object.getClass().getAnnotation(AuditMetadata.class);
        this.clazz = object.getClass();
        if (classAnnotation != null && !classAnnotation.ignore()) {
            setTracked(true);
            this.ignoreSelf = classAnnotation.ignoreSelf();

            for (Field field : object.getClass().getDeclaredFields()) {
                FieldMetadata newFieldMetadata = new FieldMetadata(this, field);
                fields.add(newFieldMetadata);
                fieldMap.put(newFieldMetadata.getFieldName(), newFieldMetadata);
            }

            if (!"".equals(classAnnotation.parent())) {
                addParent(object, classAnnotation.parent());
            }

            if (classAnnotation.parents().length > 0) {
                for (String parent : classAnnotation.parents()) {
                    addParent(object, parent);
                }
            }

            if (!"".equals(classAnnotation.left()) && !"".equals(classAnnotation.right())) {
                // We have a join table - lets add some logic to track it
                join = true;
            }
        } else {
            setTracked(false);
        }
    }

    public ObjectMetadata getLeft(Object object) {
        Object leftObject = getLeftObject(object);
        return leftObject != null ? new ObjectMetadata(leftObject) : null;
    }

    public Object getLeftObject(Object object) {
        try {
            return PropertyUtils.getProperty(object, classAnnotation.left());
        } catch (Exception e) {
            throw new RuntimeException("Unable to access left " + classAnnotation.left() + " on object " + object, e);
        }
    }

    public Object getRightObject(Object object) {
        try {
            return PropertyUtils.getProperty(object, classAnnotation.right());
        } catch (Exception e) {
            throw new RuntimeException("Unable to access left " + classAnnotation.right() + " on object " + object, e);
        }
    }

    public ObjectMetadata getRight(Object object) {
        Object rightObject = getRightObject(object);
        return rightObject != null ? new ObjectMetadata(rightObject) : null;
    }

    private void addParent(Object object, String parent) {
        try {
            Object parentObject = PropertyUtils.getProperty(object, parent);
            ObjectMetadata parentObjectMetadata;
            if (parentObject != null) {
                parentObjectMetadata = new ObjectMetadata(parentObject);
                parentObjectMetadata.setParent(parent);
                this.parentsObjectMetadata.add(parentObjectMetadata);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to access parent " + classAnnotation.parent() + " on object " + object, e);
        }
    }

    public String getEntityName() {
        if (classAnnotation == null || "".equals(classAnnotation.name())) {
            return clazz.getSimpleName();
        } else {
            return classAnnotation.name();
        }
    }

    public String getEntityDescriptiveName(Object entity) {
        if (classAnnotation != null && !"".equals(classAnnotation.descriptiveProperty())) {
            try {
                if (classAnnotation.descriptiveProperty().contains("|")) {
                    String[] props = classAnnotation.descriptiveProperty().split("\\|");
                    String descriptiveProp = "";
                    for (int i = 0; i < props.length; i++) {
                        String tempVal = String.valueOf(PropertyUtils.getProperty(entity, props[i].trim()));
                        descriptiveProp += tempVal + " ";
                    }
                    return descriptiveProp;
                } else {
                    return String.valueOf(PropertyUtils.getProperty(entity, classAnnotation.descriptiveProperty()));
                }
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


    public boolean hasParent() {
        return !parentsObjectMetadata.isEmpty();
    }

    public String getMappedBy() {
        return classAnnotation.mappedBy();
    }

    public FieldMetadata getField(String name) {
        return fieldMap.get(name);
    }

    public Object getParentObject(ObjectMetadata parentOm, Object o) {
        try {
            if (parentOm.parent != null) {
                return PropertyUtils.getProperty(o, parentOm.parent);
            }
            return "Unknown parent";
        } catch (Exception e) {
            throw new RuntimeException("Unable to get parent " + classAnnotation.parent() + " on object " + o, e);
        }
    }

    public Object getAffectedId(Object o) {
        try {
            if (!"".equals(classAnnotation.id()))
                return PropertyUtils.getProperty(o, classAnnotation.id());
            else if (PropertyUtils.isReadable(o, "id")) {
                return PropertyUtils.getProperty(o, "id");
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to get ID " + classAnnotation.id() + " on object " + o, e);
        }
    }
}
