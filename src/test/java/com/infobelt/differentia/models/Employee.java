package com.infobelt.differentia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AuditMetadata(parent = "boss", mappedBy = "employees", id = "id", descriptiveProperty = "name")
@Data
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
public class Employee {

    @AuditMetadata(ignore = true)
    private AssociatedBoss boss;

    private String id;

    @AuditMetadata
    private String name;

}
