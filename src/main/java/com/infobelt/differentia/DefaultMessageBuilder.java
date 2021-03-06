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
        return "Deleted " + builder.getName(newInstance);
    }

    @Override
    public String buildNewMessage(AuditBuilder builder, Object newInstance) {
        return "New " + builder.getName(newInstance);
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
                if ((auditChange.getEntity().equals("Field") && auditChange.getProperty().equals("fieldLevelInterpretation")) || (auditChange.getEntity().equals("DocumentText") && auditChange.getProperty().equals("docTextInterpretation")))
                {
                    return "New ${entity} has ${descriptiveName_nocaps}";
                }
                else {
                    return "New ${entity} has been added";
                }
            case REMOVE:
                return "${entity} has been deleted";
            case CHANGE:
                return "${entity_caps} ${entityDescriptiveName} changed";
            case ASSOCIATE:
                return "${relatedEntity_caps} has been associated with ${entity_nocaps}";
            case DISASSOCIATE:
                return "${relatedEntity_caps} has been disassociated from ${entity_nocaps}";
            default:
                return "Unknown change type";
        }
    }

}
