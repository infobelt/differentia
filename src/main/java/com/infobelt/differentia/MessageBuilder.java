package com.infobelt.differentia;

import java.util.List;

/**
 * Message builder is the interface that is used to construct the text
 * representation
 */
public interface MessageBuilder {

    String buildDeleteMessage(AuditBuilder auditBuilder, Object oldInstance);

    String buildNewMessage(AuditBuilder builder, Object newInstance);

    String buildChangeMessage(AuditBuilder builder, Object newInstance, Object oldInstance, List<AuditChange> changes);

}
