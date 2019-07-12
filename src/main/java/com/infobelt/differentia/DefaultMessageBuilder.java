package com.infobelt.differentia;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A default implementation of the text message builder
 */
public class DefaultMessageBuilder implements MessageBuilder {

    @Override
    public String buildDeleteMessage(AuditBuilder builder, Object newInstance) {
        return "";
    }

    @Override
    public String buildNewMessage(AuditBuilder builder, Object newInstance) {
        return "";
    }
    @Override
    public String buildChangesMessage(AuditBuilder builder, Object newInstance, Object oldInstance, List<AuditChange> changes) {
        return builder.getName(newInstance, true, true) + " has changed";
    }

    @Override
    public String buildChangeMessage(AuditBuilder builder, ObjectMetadata om, AuditChange auditChange) {
        String template = getTemplate(auditChange);
        Map<String, String> valuesMap = new HashMap<>();
        addEntry("entity", auditChange.getEntity(), valuesMap);

        addEntry("entityDescriptiveName", auditChange.getEntityDescriptiveName(), valuesMap);
        addEntry("descriptiveName", auditChange.getDescriptiveName(), valuesMap);

        valuesMap.put("newValue", auditChange.getNewValue());
        valuesMap.put("oldValue", auditChange.getOldValue());
        addEntry("relatedEntity", auditChange.getRelatedEntity(), valuesMap);
        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        return StringUtils.normalizeSpace(sub.replace(template));
    }

    private void addEntry(String key, String value, Map<String, String> valuesMap) {
        valuesMap.put(key, value);
        valuesMap.put(key + "_caps", StringUtils.capitalize(value));
        valuesMap.put(key + "_nocaps", StringUtils.lowerCase(value));
    }

    private String getTemplate(AuditChange auditChange) {
        switch (auditChange.getEventType()) {
            case ADD:
                return "New ${entity} has ${descriptiveName_nocaps} of ${newValue}";
            case REMOVE:
                return "Removed ${entity} had ${descriptiveName_nocaps} of ${oldValue}";
            case CHANGE:
                return "${entity_caps} ${entityDescriptiveName} ${descriptiveName_nocaps} changed from ${oldValue} to ${newValue}";
            case ASSOCIATE:
                return "${relatedEntity_caps} ${newValue} has been associated with ${entity_nocaps} ${entityDescriptiveName}";
            case DISASSOCIATE:
                return "${relatedEntity_caps} ${oldValue} has been disassociated from ${entity_nocaps} ${entityDescriptiveName}";
            default:
                return "Unknown change type";
        }
    }

}
