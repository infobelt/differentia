package com.infobelt.differentia;

import java.util.List;

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
    public String buildChangeMessage(AuditBuilder builder, Object newInstance, Object oldInstance, List<AuditChange> changes) {
        return builder.getName(newInstance, true) + " has changed";
    }

}
