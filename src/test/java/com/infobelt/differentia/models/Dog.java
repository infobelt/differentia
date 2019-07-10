package com.infobelt.differentia.models;

import com.infobelt.differentia.AuditMetadata;
import lombok.Data;

@Data
@AuditMetadata
public class Dog {

    @AuditMetadata
    private String name;

    @AuditMetadata(descriptiveProperty = "name")
    private Owner owner;

}
