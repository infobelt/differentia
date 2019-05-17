package com.infobelt.differentia;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AuditMetadata
@Data
public class AssociatedBoss {

    @AuditMetadata
    private String name;

    @AuditMetadata(traverse = true, descriptiveProperty = "name", add = AuditEventType.ASSOCIATE, remove = AuditEventType.DISASSOCIATE)
    private List<Employee> employees = new ArrayList<>();

}
