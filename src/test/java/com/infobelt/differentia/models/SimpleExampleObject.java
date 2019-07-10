package com.infobelt.differentia;

import lombok.Data;

@Data
@AuditMetadata(name = "example", descriptiveProperty = "name", onlyAnnotated = true)
public class SimpleExampleObject {

    @AuditMetadata(name="First name")
    private String name;

    @AuditMetadata(name="Description")
    private String description;

    private int amount;

}
