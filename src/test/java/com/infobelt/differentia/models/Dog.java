package com.infobelt.differentia;

import lombok.Data;

@Data
@AuditMetadata
public class Dog {

    @AuditMetadata
    private String name;

    @AuditMetadata(descriptiveProperty = "name")
    private Owner owner;

}
