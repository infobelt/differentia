package com.infobelt.differentia;

import lombok.Data;

@Data
@AuditMetadata
public class Owner {

    @AuditMetadata
    private String name;

}
