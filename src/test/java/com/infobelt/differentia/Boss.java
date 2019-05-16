package com.infobelt.differentia;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AuditMetadata
@Data
public class Boss {

    @AuditMetadata
    private String name;

    @AuditMetadata(traverse = true)
    private List<Employee> employees = new ArrayList<>();

}
