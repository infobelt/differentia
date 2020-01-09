package com.infobelt.differentia;

import com.infobelt.differentia.models.*;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class StudentEnrollmentCourseTest {

    private static final AuditBuilder AUDIT_BUILDER = new AuditBuilder();

    @Test
    public void basicRelationshipTest() {

        Student student = new Student();
        student.setName("Philip");

        Course course = new Course();
        course.setName("Coding");

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setStudent(student);

        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(enrollment, null);

        // We have ignoreSelf so hopefully it is just the assoc/dissoc
        assertThat(changes.size(), equalTo(2));
        assertThat(changes.get(0).getMessage(), equalTo("Course has been disassociated from student"));
        assertThat(changes.get(1).getMessage(), equalTo("Student Philip has been disassociated from course Coding"));


    }


}
