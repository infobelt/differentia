package com.infobelt.differentia.models;

import com.infobelt.differentia.AuditEventType;
import com.infobelt.differentia.AuditMetadata;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AuditMetadata(name = "Enrollment", left = "student", right = "course")
@Data
public class Enrollment {

    @AuditMetadata(remove = AuditEventType.DISASSOCIATE, add=AuditEventType.ASSOCIATE)
    private Course course;

    @AuditMetadata(remove = AuditEventType.DISASSOCIATE, add=AuditEventType.ASSOCIATE)
    private Student student;

}
