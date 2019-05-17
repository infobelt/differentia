package com.infobelt.differentia;

import lombok.Data;

@Data
public class AuditChange {

    private String entity;

    private boolean descriptive = false;

    private AuditEventType eventType;

    private String property;

    private String descriptiveName;

    private String oldValue;

    private String newValue;

}
