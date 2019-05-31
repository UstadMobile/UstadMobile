package com.ustadmobile.lib.annotationprocessor.core

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorObserver
import db2.ExampleDatabase2
import db2.ExampleEntity2
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


/*
 * Note: this can be adapted for multiplatform async testing using the following techniques:
 *   https://blog.kotlin-academy.com/testing-common-modules-66b39d641617
 */
class TestDbBuilderKtKt {

    lateinit var exampleDb2: ExampleDatabase2

    @Before
    fun setup() {
        exampleDb2 = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "db1").build()
        exampleDb2.clearAllTables()
    }

    @Test
    fun givenDataInserted_whenUpdateMade_thenLiveDataShouldBeUpdated() {
        runBlocking {
            val liveData = exampleDb2.exampleDao2().findByMinUidLive()
            val channel = Channel<List<ExampleEntity2>?>(1)
            val observerFn = object : DoorObserver<List<ExampleEntity2>?> {
                override fun onChanged(t: List<ExampleEntity2>?) {
                    if(t?.size == 1) {
                        channel.offer(t)
                    }
                }
            }

            liveData.observeForever(observerFn)

            delay(1000)
            val entity = ExampleEntity2(name = "Bob", someNumber = 50)
            entity.uid = exampleDb2.exampleDao2().insertAndReturnId(entity)

            val list = withTimeout(10000) { channel.receive() }
            assertNotNull(list, message = "List is not null")
            assertEquals(1, list.size, "List size is 1")

            liveData.removeObserver(observerFn)
        }
    }

}