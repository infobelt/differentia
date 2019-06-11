package com.infobelt.differentia;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class NoAnnotationObjectTest {

    private static final AuditBuilder AUDIT_BUILDER = new AuditBuilder();

    @Test
    public void basicNew() {
        NoAnnotationObject obj1 = new NoAnnotationObject();
        obj1.setName("Cheese");
        String message = AUDIT_BUILDER.buildNewMessage(obj1);
        assertThat(message, equalTo("New NoAnnotationObject"));
    }

    @Test
    public void basicNew2() {
        NoAnnotationObject obj1 = new NoAnnotationObject();
        obj1.setName("Cheese");
        String message = AUDIT_BUILDER.buildMessage(null, obj1);
        assertThat(message, equalTo("New NoAnnotationObject"));
    }

    @Test
    public void basicDelete() {
        NoAnnotationObject obj1 = new NoAnnotationObject();
        obj1.setName("Cheese");
        String message = AUDIT_BUILDER.buildDeleteMessage(obj1);
        assertThat(message, equalTo("Deleted NoAnnotationObject"));
    }

    @Test
    public void basicDelete2() {
        NoAnnotationObject obj1 = new NoAnnotationObject();
        obj1.setName("Cheese");
        String message = AUDIT_BUILDER.buildMessage(obj1, null);
        assertThat(message, equalTo("Deleted NoAnnotationObject"));
    }

    @Test
    public void basicChange() {
        NoAnnotationObject obj1 = new NoAnnotationObject();
        obj1.setName("Cheese");
        NoAnnotationObject obj2 = new NoAnnotationObject();
        obj2.setName("Toasty");
        String message = AUDIT_BUILDER.changeMessage(obj1, obj2);
        assertThat(message, equalTo("NoAnnotationObject has changed"));

        String message2 = AUDIT_BUILDER.buildMessage(obj1, obj2);
        assertThat(message2, equalTo("NoAnnotationObject has changed"));
    }

    @Test
    public void changeDetails() {
        NoAnnotationObject obj1 = new NoAnnotationObject();
        obj1.setName("Cheese");
        NoAnnotationObject obj2 = new NoAnnotationObject();
        obj2.setName("Toasty");
        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(obj1, obj2);
        assertThat(changes.size(), equalTo(1));
        assertThat(changes.get(0).getEventType(), equalTo(AuditEventType.CHANGE));
        assertThat(changes.get(0).getProperty(), equalTo("name"));
        assertThat(changes.get(0).getNewValue(), equalTo("Toasty"));
        assertThat(changes.get(0).getOldValue(), equalTo("Cheese"));
        assertThat(changes.get(0).getDescriptiveName(), equalTo("name"));
        assertThat(changes.get(0).isDescriptive(), equalTo(false));

        assertThat(changes.get(0).getMessage(), equalTo("NoAnnotationObject name changed from Cheese to Toasty"));
    }

    @Test
    public void newDetails() {
        NoAnnotationObject obj1 = new NoAnnotationObject();
        obj1.setName("Cheese");
        NoAnnotationObject obj2 = new NoAnnotationObject();
        obj2.setName("Toasty");
        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(null, obj2);
        assertThat(changes.size(), equalTo(2));
        assertThat(changes.get(1).getEventType(), equalTo(AuditEventType.ADD));
        assertThat(changes.get(1).getProperty(), equalTo("name"));
        assertThat(changes.get(1).getNewValue(), equalTo("Toasty"));
        assertThat(changes.get(1).getOldValue(), equalTo(null));
        assertThat(changes.get(1).getDescriptiveName(), equalTo("name"));
        assertThat(changes.get(1).isDescriptive(), equalTo(false));
        assertThat(changes.get(1).getMessage(), equalTo("New NoAnnotationObject has name of Toasty"));
    }

    @Test
    public void removeDetails() {
        NoAnnotationObject obj1 = new NoAnnotationObject();
        obj1.setName("Cheese");
        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(obj1, null);
        assertThat(changes.size(), equalTo(2));
        assertThat(changes.get(1).getEventType(), equalTo(AuditEventType.REMOVE));
        assertThat(changes.get(1).getProperty(), equalTo("name"));
        assertThat(changes.get(1).getNewValue(), equalTo(null));
        assertThat(changes.get(1).getOldValue(), equalTo("Cheese"));
        assertThat(changes.get(1).getDescriptiveName(), equalTo("name"));
        assertThat(changes.get(1).isDescriptive(), equalTo(false));

        assertThat(changes.get(1).getMessage(), equalTo("Removed NoAnnotationObject had name of Cheese"));


    }
}
