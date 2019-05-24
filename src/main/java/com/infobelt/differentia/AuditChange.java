package com.infobelt.differentia;

import lombok.Data;

@Data
public class AuditChange {

    private String entity;

    private String relatedEntity;

    private String entityDescriptiveName;

    private boolean descriptive = false;

    private AuditEventType eventType;

    private String property;

    private String descriptiveName;

    private String oldValue;

    private String newValue;

    private String message;

    private Object affectedId;

}
