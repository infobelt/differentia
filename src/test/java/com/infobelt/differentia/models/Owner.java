package com.infobelt.differentia.models;

import com.infobelt.differentia.AuditMetadata;
import lombok.Data;

@Data
@AuditMetadata
public class Owner {

    @AuditMetadata
    private String name;

}
