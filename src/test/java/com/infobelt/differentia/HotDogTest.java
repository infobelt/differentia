package com.infobelt.differentia;

import com.infobelt.differentia.models.Bun;
import com.infobelt.differentia.models.HotDog;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class HotDogTest {

    private static final AuditBuilder AUDIT_BUILDER = new AuditBuilder();

    private Bun bun;
    private HotDog hotDog1;
    private HotDog hotDog2;
    private Bun bun2;


    @Before
    public void setup() {
        bun = new Bun();

        bun2 = new Bun();

        hotDog1 = new HotDog();
        hotDog1.setId(1L);
        hotDog1.setBun(bun);
        bun.getHotDogs().add(hotDog1);

        hotDog2 = new HotDog();
        hotDog2.setId(1L);
        hotDog2.setBun(bun);
    }

    @Test
    public void testNoChange() {

        List<AuditChange> changes = AUDIT_BUILDER.buildChanges(hotDog1, hotDog1);
        assertThat(changes.size(), equalTo(0));
//        assertThat(changes.get(0).getEventType(), equalTo(AuditEventType.CHANGE));
//        assertThat(changes.get(0).getOldValue(), equalTo("Thing2"));
//        assertThat(changes.get(0).getNewValue(), equalTo("Thing3"));
//        assertThat(changes.get(0).getMessage(), equalTo("Employee 2 name changed from Thing2 to Thing3"));

    }

}
