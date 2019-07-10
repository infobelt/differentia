package com.infobelt.differentia.models;

import com.infobelt.differentia.AuditMetadata;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AuditMetadata(name = "Bossing", descriptiveProperty = "name")
@Data
public class Enrollment {

    @AuditMetadata
    private String name;

    @AuditMetadata(traverse = true, descriptiveProperty = "name")
    private List<Employee> employees = new ArrayList<>();

}
