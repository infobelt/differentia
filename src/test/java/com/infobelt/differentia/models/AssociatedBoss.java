package com.infobelt.differentia.models;

import com.infobelt.differentia.AuditEventType;
import com.infobelt.differentia.AuditMetadata;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AuditMetadata(name = "Big Boss", descriptiveProperty = "name")
@Data
public class AssociatedBoss {

    @AuditMetadata
    private String name;

    @AuditMetadata(traverse = true, descriptiveProperty = "name", add = AuditEventType.ASSOCIATE, remove = AuditEventType.DISASSOCIATE)
    private List<Employee> employees = new ArrayList<>();

}
