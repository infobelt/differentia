package com.infobelt.differentia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AuditMetadata
@Data
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
public class Employee {

    private String id;

    @AuditMetadata
    private String name;

}
