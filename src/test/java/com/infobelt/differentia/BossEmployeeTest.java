package com.infobelt.differentia;

import com.infobelt.differentia.models.Boss;
import com.infobelt.differentia.models.Employee;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class BossEmployeeTest {

    private static final AuditBuilder AUDIT_BUILDER = new AuditBuilder();
    private Employee employee1;
    private Employee employee2;
    private Employee employee3;
    private Boss oldBoss;
    private Boss newBoss;


    @Before
    public void setup() {
        employee1 = new Employee(null, "1", "Thing1");
        employee2 = new Employee(null, "2", "Thing2");
        employee3 = new Employee(null, "2", "Thing3");

        oldBoss = new Boss();
        oldBoss.setName("Phil");
        oldBoss.getEmployees().add(employee1);
        oldBoss.getEmployees().add(employee2);

        newBoss = new Boss();
        newBoss.setName("Phil");
        newBoss.getEmployees().add(employee1);
        newBoss.getEmployees().add(employee3);
    }

    @Test
    public void testModification() {

        // Thing2 will become Thing3

        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(oldBoss, newBoss);
        assertThat(changes.size(), equalTo(1));
        assertThat(changes.get(0).getEventType(), equalTo(AuditEventType.CHANGE));
        assertThat(changes.get(0).getOldValue(), equalTo("Thing2"));
        assertThat(changes.get(0).getNewValue(), equalTo("Thing3"));
        assertThat(changes.get(0).getMessage(), equalTo("Employee Thing2 changed"));
        assertThat(changes.get(0).getAffectedId(),equalTo("2"));

    }

    @Test
    public void testAddition() {

        // Thing2 will become Thing3

        oldBoss.getEmployees().remove(employee2);

        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(oldBoss, newBoss);
        assertThat(changes.size(), equalTo(1));
        assertThat(changes.get(0).getEventType(), equalTo(AuditEventType.ADD));
        assertThat(changes.get(0).getProperty(), equalTo("employees"));
        assertThat(changes.get(0).getNewValue(), equalTo("Thing3"));
        assertThat(changes.get(0).getMessage(), equalTo("New Bossing has been added"));

    }

    @Test
    public void testRemove() {

        // Thing2 will become Thing3

        newBoss.getEmployees().remove(employee3);

        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(oldBoss, newBoss);
        assertThat(changes.size(), equalTo(1));
        assertThat(changes.get(0).getEventType(), equalTo(AuditEventType.REMOVE));
        assertThat(changes.get(0).getProperty(), equalTo("employees"));
        assertThat(changes.get(0).getOldValue(), equalTo("Thing2"));
        assertThat(changes.get(0).getMessage(), equalTo("Removed Bossing has been deleted"));

    }

}
