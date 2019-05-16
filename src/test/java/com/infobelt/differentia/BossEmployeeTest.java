package com.infobelt.differentia;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class BossEmployeeTest {

    private static final AuditBuilder AUDIT_BUILDER = new AuditBuilder();

    @Test
    public void testEmployees() {

        // Thing2 will become Thing3

        Employee employee1 = new Employee("1","Thing1");
        Employee employee2 = new Employee("2","Thing2");
        Employee employee3 = new Employee("2","Thing3");
        Boss oldBoss = new Boss();
        oldBoss.setName("Phil");

        oldBoss.getEmployees().add(employee1);
        oldBoss.getEmployees().add(employee2);
        Boss newBoss = new Boss();
        newBoss.setName("Phil");
        newBoss.getEmployees().add(employee1);
        newBoss.getEmployees().add(employee3);

        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(oldBoss, newBoss);
        assertThat(changes.size(), equalTo(1));
        assertThat(changes.get(0).getEventType(), equalTo(AuditEventType.CHANGE));
        assertThat(changes.get(0).getOldValue(), equalTo("Bob"));
        assertThat(changes.get(0).getNewValue(), equalTo("Sally"));
    }

}
