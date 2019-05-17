package com.infobelt.differentia;

import java.util.List;

/**
 * Message builder is the interface that is used to construct the text
 * representation
 */
public interface MessageBuilder {

    String buildDeleteMessage(AuditBuilder auditBuilder, Object oldInstance);

    String buildNewMessage(AuditBuilder builder, Object newInstance);

    String buildChangesMessage(AuditBuilder builder, Object newInstance, Object oldInstance, List<AuditChange> changes);

    String buildChangeMessage(AuditBuilder builder, ObjectMetadata om, AuditChange auditChange);
}
