package com.infobelt.differentia;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class DogOwnerTest {

    private static final AuditBuilder AUDIT_BUILDER = new AuditBuilder();

    @Test
    public void basicRelationshipTest() {
        Dog dog = new Dog();
        dog.setName("Fluffy");

        Dog dog2 = new Dog();
        dog2.setName("Fluffy");

        Owner owner1 = new Owner();
        owner1.setName("Bob");

        dog.setOwner(owner1);

        Owner owner2 = new Owner();
        owner2.setName("Sally");
        dog2.setOwner(owner2);

        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(dog, dog2);
        assertThat(changes.size(), equalTo(1));
        assertThat(changes.get(0).getEventType(), equalTo(AuditEventType.CHANGE));
        assertThat(changes.get(0).getOldValue(), equalTo("Bob"));
        assertThat(changes.get(0).getNewValue(), equalTo("Sally"));

    }


}
