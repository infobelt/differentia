package com.infobelt.differentia;

import com.infobelt.differentia.models.SimpleExampleObject;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SimpleExampleObjectTest {

    private static final AuditBuilder AUDIT_BUILDER = new AuditBuilder();

    @Test
    public void basicNew() {
        SimpleExampleObject obj1 = new SimpleExampleObject();
        obj1.setName("Cheese");
        String message = AUDIT_BUILDER.buildNewMessage(obj1);
        assertThat(message, equalTo("New example Cheese"));
    }

    @Test
    public void basicNew2() {
        SimpleExampleObject obj1 = new SimpleExampleObject();
        obj1.setName("Cheese");
        String message = AUDIT_BUILDER.buildMessage(null, obj1);
        assertThat(message, equalTo("New example Cheese"));
    }

    @Test
    public void basicDelete() {
        SimpleExampleObject obj1 = new SimpleExampleObject();
        obj1.setName("Cheese");
        String message = AUDIT_BUILDER.buildDeleteMessage(obj1);
        assertThat(message, equalTo("Deleted example Cheese"));
    }

    @Test
    public void basicDelete2() {
        SimpleExampleObject obj1 = new SimpleExampleObject();
        obj1.setName("Cheese");
        String message = AUDIT_BUILDER.buildMessage(obj1, null);
        assertThat(message, equalTo("Deleted example Cheese"));
    }

    @Test
    public void basicChange() {
        SimpleExampleObject obj1 = new SimpleExampleObject();
        obj1.setName("Cheese");
        SimpleExampleObject obj2 = new SimpleExampleObject();
        obj2.setName("Toasty");
        String message = AUDIT_BUILDER.changeMessage(obj1, obj2);
        assertThat(message, equalTo("Example Toasty has changed"));

        String message2 = AUDIT_BUILDER.buildMessage(obj1, obj2);
        assertThat(message2, equalTo("Example Toasty has changed"));
    }

    @Test
    public void changeDetails() {
        SimpleExampleObject obj1 = new SimpleExampleObject();
        obj1.setName("Cheese");
        SimpleExampleObject obj2 = new SimpleExampleObject();
        obj2.setName("Toasty");
        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(obj1, obj2);
        assertThat(changes.size(), equalTo(1));
        assertThat(changes.get(0).getEventType(), equalTo(AuditEventType.CHANGE));
        assertThat(changes.get(0).getProperty(), equalTo("name"));
        assertThat(changes.get(0).getNewValue(), equalTo("Toasty"));
        assertThat(changes.get(0).getOldValue(), equalTo("Cheese"));
        assertThat(changes.get(0).getDescriptiveName(), equalTo("First name"));
        assertThat(changes.get(0).isDescriptive(), equalTo(true));

        assertThat(changes.get(0).getMessage(), equalTo("Example Cheese changed"));
    }

    @Test
    public void newDetails() {
        SimpleExampleObject obj1 = new SimpleExampleObject();
        obj1.setName("Cheese");
        SimpleExampleObject obj2 = new SimpleExampleObject();
        obj2.setName("Toasty");
        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(null, obj2);
        assertThat(changes.size(), equalTo(2));
        assertThat(changes.get(1).getEventType(), equalTo(AuditEventType.ADD));
        assertThat(changes.get(1).getProperty(), equalTo("name"));
        assertThat(changes.get(1).getNewValue(), equalTo("Toasty"));
        assertThat(changes.get(1).getOldValue(), equalTo(null));
        assertThat(changes.get(1).getDescriptiveName(), equalTo("First name"));
        assertThat(changes.get(1).isDescriptive(), equalTo(true));
        assertThat(changes.get(1).getMessage(), equalTo("New example has been added"));
    }

    @Test
    public void removeDetails() {
        SimpleExampleObject obj1 = new SimpleExampleObject();
        obj1.setName("Cheese");
        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(obj1, null);
        assertThat(changes.size(), equalTo(2));
        assertThat(changes.get(1).getEventType(), equalTo(AuditEventType.REMOVE));
        assertThat(changes.get(1).getProperty(), equalTo("name"));
        assertThat(changes.get(1).getNewValue(), equalTo(null));
        assertThat(changes.get(1).getOldValue(), equalTo("Cheese"));
        assertThat(changes.get(1).getDescriptiveName(), equalTo("First name"));
        assertThat(changes.get(1).isDescriptive(), equalTo(true));

        assertThat(changes.get(1).getMessage(), equalTo("example has been deleted"));


    }
}
