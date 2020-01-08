package com.infobelt.differentia;

import com.infobelt.differentia.models.AssociatedBoss;
import com.infobelt.differentia.models.Employee;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class AssociatedBossEmployeeTest {

    private static final AuditBuilder AUDIT_BUILDER = new AuditBuilder();
    private Employee employee1;
    private Employee employee2;
    private Employee employee3;
    private AssociatedBoss oldBoss;
    private AssociatedBoss newBoss;


    @Before
    public void setup() {
        employee1 = new Employee(null, "1", "Thing1");
        employee2 = new Employee(null, "2", "Thing2");
        employee3 = new Employee(null, "2", "Thing3");

        oldBoss = new AssociatedBoss();
        oldBoss.setName("Phil");
        oldBoss.getEmployees().add(employee1);
        oldBoss.getEmployees().add(employee2);

        newBoss = new AssociatedBoss();
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

    }

    @Test
    public void testAddition() {

        // Thing2 will become Thing3

        oldBoss.getEmployees().remove(employee2);

        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(oldBoss, newBoss);
        assertThat(changes.size(), equalTo(1));
        assertThat(changes.get(0).getEventType(), equalTo(AuditEventType.ASSOCIATE));
        assertThat(changes.get(0).getProperty(), equalTo("employees"));
        assertThat(changes.get(0).getNewValue(), equalTo("Thing3"));
        assertThat(changes.get(0).getMessage(), equalTo("Employee Thing3 has been associated with big boss Phil"));

    }

    @Test
    public void testRemove() {

        // Thing2 will become Thing3

        newBoss.getEmployees().remove(employee3);

        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(oldBoss, newBoss);
        assertThat(changes.size(), equalTo(1));
        assertThat(changes.get(0).getEventType(), equalTo(AuditEventType.DISASSOCIATE));
        assertThat(changes.get(0).getProperty(), equalTo("employees"));
        assertThat(changes.get(0).getOldValue(), equalTo("Thing2"));
        assertThat(changes.get(0).getMessage(), equalTo("Employee has been disassociated from big boss"));

    }

    @Test
    public void testEmployeeChange() {

        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(oldBoss, newBoss);
        assertThat(changes.size(), equalTo(1));
        assertThat(changes.get(0).getEventType(), equalTo(AuditEventType.CHANGE));
        assertThat(changes.get(0).getProperty(), equalTo("name"));
        assertThat(changes.get(0).getOldValue(), equalTo("Thing2"));
        assertThat(changes.get(0).getMessage(), equalTo("Employee Thing2 name changed from Thing2 to Thing3"));

    }

    @Test
    public void testEmployeeAddToBoss() {

        AssociatedBoss b1 = new AssociatedBoss();
        b1.setName("Billy");
        Employee e1 = new Employee(b1, "1", "Thing1");
        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(null, e1);
        assertThat(changes.size(), equalTo(4));
        assertThat(changes.get(0).getEventType(), equalTo(AuditEventType.ASSOCIATE));
        assertThat(changes.get(0).getProperty(), equalTo("employees"));
        assertThat(changes.get(0).getOldValue(), equalTo(""));
        assertThat(changes.get(0).getMessage(), equalTo("Employee Thing1 has been associated with big boss Billy"));

    }

    @Test
    public void testEmployeeRemoveFromBoss() {

        AssociatedBoss b1 = new AssociatedBoss();
        b1.setName("Billy");
        Employee e1 = new Employee(b1, "1", "Thing1");
        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(e1, null);
        assertThat(changes.size(), equalTo(4));
        assertThat(changes.get(0).getEventType(), equalTo(AuditEventType.DISASSOCIATE));
        assertThat(changes.get(0).getProperty(), equalTo("employees"));
        assertThat(changes.get(0).getOldValue(), equalTo("Thing1"));
        assertThat(changes.get(0).getMessage(), equalTo("Employee has been disassociated from big boss"));

    }

}
