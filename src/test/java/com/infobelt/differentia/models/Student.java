package com.infobelt.differentia.models;

import com.infobelt.differentia.AuditMetadata;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AuditMetadata(name = "Student", descriptiveProperty = "name")
@Data
public class Student {

    @AuditMetadata
    private String name;

    @AuditMetadata
    private List<Enrollment> enrollments = new ArrayList<>();

}
