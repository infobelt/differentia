package com.infobelt.differentia.models;

import com.infobelt.differentia.AuditMetadata;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AuditMetadata(name = "Course", descriptiveProperty = "name")
@Data
public class Course {

    @AuditMetadata
    private String name;

}
