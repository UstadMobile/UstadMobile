package com.ustadmobile.lib.annotationprocessor.core

import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.door.DatabaseBuilder
import db2.ExampleDatabase2
import db2.ExampleDatabase2_JsImpl
import db2.ExampleEntity2
import db2.ExampleSyncableEntity
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import org.w3c.dom.set
import kotlin.browser.localStorage
import kotlin.test.*


class BasicTest {

    private lateinit var dbInstance: ExampleDatabase2

    private val httpClient = HttpClient()

    private val testServerUrl = "http://localhost:8089/"

    @BeforeTest
    fun setup() = GlobalScope.promise {
        localStorage["doordb.endpoint.url"] = testServerUrl
        ExampleDatabase2_JsImpl.register()
        dbInstance = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class,
                "ExampleDatabase2").build()
        httpClient.get<Unit>("${testServerUrl}ExampleDatabase2/clearAllTables")
    }


    @Test
    fun testDatabaseBuilder() {
        println("Attempt test database builder")
        assertNotNull(dbInstance, "Constructed db instance object")
    }

    @Test
    fun givenEntityInserted_whenSelectIsCalled_thenShouldReturnSameEntity() = GlobalScope.promise {
        val exampleEntity = ExampleEntity2(name = "Js Entity", someNumber = 60)
        exampleEntity.uid = dbInstance.exampleDao2().insertAsyncAndGiveId(exampleEntity)
        val entityFromDao = dbInstance.exampleDao2().findByUidAsync(exampleEntity.uid)
        assertEquals(exampleEntity, entityFromDao, "Entity retrieved by UID is the same as inserted")
    }

    @Test
    fun testDaoPostWithoutIdFetch() = GlobalScope.promise {
        val entity = ExampleEntity2(name = "JsPost", someNumber =  50)
        entity.uid = dbInstance.exampleDao2().insertAsyncAndGiveId(entity)
        assertNotEquals(0, entity.uid, "After insert non-zero UID is received")
    }

    @Test
    fun getList()  = GlobalScope.promise {
        val aList = dbInstance.exampleDao2().findAllAsync()
        assertNotNull(aList)
        println("dah")
    }

    @Test
    fun testLiveData() = GlobalScope.promise {
        val entity = ExampleEntity2(name = "LiveData Test", someNumber =  50)
        entity.uid = dbInstance.exampleDao2().insertAsyncAndGiveId(entity)
        val listRef = mutableListOf<List<ExampleEntity2>>()
        waitForLiveData(dbInstance.exampleDao2().findByMinUidLive(), 5000) {
            listRef[0] = it
            it.isNotEmpty()
        }

        assertTrue(listRef.isNotEmpty() && listRef[0] != null, "LiveData loads with callback as expected")
    }

    @Test
    fun givenBlankDatabase_whenListInserted_thenTotalNumItemsShouldMatch() = GlobalScope.promise {
        val entityList = listOf(ExampleEntity2(name = "Test Item 1", someNumber =  50),
                ExampleEntity2(name = "Test Item 2", someNumber =  51))
        dbInstance.exampleDao2().insertListAsync(entityList)
        assertEquals(entityList.size, dbInstance.exampleDao2().countNumEntitiesAsync(),
                "After inserting entities into blank db, num entities in db equals list length")

    }


}