package com.ustadmobile.port.android

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.test.core.impl.PlatformTestUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Test
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * These tests ensure that the syncable primary key mechanism is working as expected on Android.
 * The syncable primary keys are handled by generated SQLite umDatabase triggers.
 */
class TestSyncablePkAndroid {


    @Test
    fun givenSyncableEntity_whenInserted_canBeLookedUp() {
        val db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
        db.clearAllTables()
        val newUsername = "bob" + System.currentTimeMillis()
        val newPerson = Person(newUsername, "bob", "jones")
        val insertedPk = db.personDao.insert(newPerson)
        Assert.assertEquals("Inserted pk was inserted under matching key",
                newUsername, db.personDao.findByUid(insertedPk)!!.username)
    }

    @Test
    fun givenSyncableEntityList_whenInserted_canAllBeLookedUp() {
        val db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
        db.clearAllTables()
        val numInsertsToRun = 100
        val timestamp = System.currentTimeMillis()
        val personList = ArrayList<Person>()
        for (i in 0 until numInsertsToRun) {
            val newPerson = Person("newperson$i", "bob$timestamp", "jones")
            personList.add(newPerson)
        }
        val insertedIds = db.personDao.insertListAndGetIds(personList)

        for (i in 0 until numInsertsToRun) {
            Assert.assertEquals("newperson$i",
                    db.personDao.findByUid(insertedIds[i])!!.username)
        }
    }

    @Test
    fun givenEmptyDatabase_whenSyncableEntitiesAreInsertedConcurrently_thenAllShouldBeInsertedWithNoDuplicates() {
        val numItemsToAdd = 100
        val db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
        db.clearAllTables()

        val dao = db.personDao
        val latch = CountDownLatch(numItemsToAdd)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        GlobalScope.launch {
            for (i in 0 until numItemsToAdd) {
                try {
                    val result = dao.insertAsync(Person("newperson$i", "bob", "jones"))
                    val createdPerson = dao.findByUid(result)
                    if (createdPerson != null && createdPerson.username == "newperson$i")
                        successCount.incrementAndGet()
                    else
                        failCount.incrementAndGet()

                    latch.countDown()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                    latch.countDown()
                }
            }

            Assert.assertEquals("Success count = number of items added", numItemsToAdd.toLong(),
                    successCount.get().toLong())
            Assert.assertEquals("Number of items matches insert count", numItemsToAdd.toLong(),
                    dao.countAll())
        }


        try {
            latch.await(10, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            //should not happen
        }
    }

}
