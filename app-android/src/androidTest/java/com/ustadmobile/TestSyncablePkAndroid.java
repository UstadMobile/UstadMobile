package com.ustadmobile;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestSyncablePkAndroid {


    @Test
    public void givenSyncableEntity_whenInserted_canBeLookedUp() {
        UmAppDatabase db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        db.clearAllTables();
        String newUsername = "bob" + System.currentTimeMillis();
        Person newPerson = new Person(newUsername, "bob", "jones");
        long insertedPk = db.getPersonDao().insert(newPerson);
        Assert.assertEquals("Inserted pk was inserted under matching key",
                newUsername, db.getPersonDao().findByUid(insertedPk).getUsername());
    }

    @Test
    public void givenSyncableEntityList_whenInserted_canAllBeLookedUp() {
        UmAppDatabase db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        db.clearAllTables();
        int numInsertsToRun = 100;
        long timestamp = System.currentTimeMillis();
        List<Person> personList = new ArrayList<>();
        for(int i = 0; i < numInsertsToRun; i++) {
            Person newPerson = new Person("newperson" + i, "bob" + timestamp, "jones");
            personList.add(newPerson);
        }
        List<Long> insertedIds = db.getPersonDao().insertListAndGetIds(personList);

        for(int i = 0; i < numInsertsToRun; i++) {
            Assert.assertEquals("newperson" + i,
                    db.getPersonDao().findByUid(insertedIds.get(i)).getUsername());
        }
    }

}
