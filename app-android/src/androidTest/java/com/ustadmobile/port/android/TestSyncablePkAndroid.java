package com.ustadmobile.port.android;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * These tests ensure that the syncable primary key mechanism is working as expected on Android.
 * The syncable primary keys are handled by generated SQLite database triggers.
 */
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

    @Test
    public void givenEmptyDatabase_whenSyncableEntitiesAreInsertedConcurrently_thenAllShouldBeInsertedWithNoDuplicates() {
        int numItemsToAdd = 100;
        UmAppDatabase db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        db.clearAllTables();

        PersonDao dao = db.getPersonDao();
        CountDownLatch latch = new CountDownLatch(numItemsToAdd);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        for(int i = 0; i < numItemsToAdd; i++) {
            final int insertNum = i;
            dao.insertAsync(new Person("newperson" + insertNum, "bob", "jones"),
                    new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    Person createdPerson = dao.findByUid(result);
                    if(createdPerson != null && createdPerson.getUsername().equals(
                            "newperson" + insertNum))
                        successCount.incrementAndGet();
                    else
                        failCount.incrementAndGet();

                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable exception) {
                    failCount.incrementAndGet();
                    latch.countDown();
                }
            });
        }

        try { latch.await(10, TimeUnit.SECONDS); }
        catch(InterruptedException e) {
            //should not happen
        }

        Assert.assertEquals("Success count = number of items added", numItemsToAdd,
                successCount.get());
        Assert.assertEquals("Number of items matches insert count", numItemsToAdd,
                dao.countAll());
    }

}
