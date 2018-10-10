package com.ustadmobile.core.db;

import com.ustadmobile.lib.db.entities.Person;

import org.junit.Assert;
import org.junit.Test;

public class TestUmAppDatabaseJdbc {



    @Test
    public void givenDataSource_whenLookedUp_shouldCreateDb(){
        UmAppDatabase db = UmAppDatabase.getInstance(null);
        Assert.assertNotNull(db);
    }

    @Test
    public void givenDatabase_whenEntityCreated_shouldBeFoundByUid() {
        UmAppDatabase db = UmAppDatabase.getInstance(null);
        Person somePerson = new Person();
        somePerson.setUsername("bob");
        long personUid = db.getPersonDao().insert(somePerson);

        Person fromDb = db.getPersonDao().findByUid(personUid);

        Assert.assertEquals("Person from DB has same username", somePerson.getUsername(),
                fromDb.getUsername());
    }


}
