package com.ustadmobile.lib.annotationprocessor.core

import com.ustadmobile.door.DataSourceFactoryJs
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorObserver
import db2.ExampleDatabase2
import db2.ExampleDatabase2_JsImpl
import db2.ExampleEntity2
import db2.ExampleSyncableEntity
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.promise
import kotlinx.coroutines.withTimeout
import org.w3c.dom.set
import kotlin.browser.localStorage
import kotlin.js.Date
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
    fun testDatabaseBuilder() = GlobalScope.promise {
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
        var startTime = Date().getTime().toLong()
        val entity = ExampleEntity2(name = "LiveData Test2", someNumber =  501)
        entity.uid = dbInstance.exampleDao2().insertAsyncAndGiveId(entity)

        console.log("testLiveData: Inserted entity ${entity.uid} t =${Date().getTime().toLong() - startTime}ms")
        val channel = Channel<List<ExampleEntity2>>(1)
        val liveData = dbInstance.exampleDao2().findByMinUidLive()
        liveData.observeForever(object :DoorObserver<List<ExampleEntity2>> {
            override fun onChanged(t: List<ExampleEntity2>) {
                console.log("testLiveData: got result $t ; time=${Date().getTime().toLong() - startTime}ms")
                channel.offer(t)
            }
        })
        console.log("Waiting to receive on channel")
        val receivedList = channel.receive()

        console.log("testLiveData: done - running assertion on ${receivedList.isNotEmpty()}")
        assertTrue(receivedList.isNotEmpty() && receivedList[0] != null, "LiveData loads with callback as expected")
        channel.close()
    }

    @Test
    fun testDataSourceFactory() = GlobalScope.promise {
        val syncableEntity = ExampleSyncableEntity(esNumber = 101)
        syncableEntity.esUid = dbInstance.exampleSyncableDao().insertAsync(syncableEntity)
        val dataSource = dbInstance.exampleSyncableDao().findAllDataSource()
        val channel = Channel<List<ExampleSyncableEntity>>(1)
        (dataSource as DataSourceFactoryJs<Int, ExampleSyncableEntity>).create().load(0, 50) {e: Exception?, entities: List<ExampleSyncableEntity>? ->
            if(entities != null) {
                channel.offer(entities)
            }else {
                println("Exception loading entities: $e")
            }
        }

        val listReceived = withTimeout(5000) { channel.receive() }
        channel.close()
        assertEquals(1, listReceived.size, "Got list of one entitity")
    }


    @Test
    fun givenBlankDatabase_whenListInserted_thenTotalNumItemsShouldMatch() = GlobalScope.promise {
        val entityList = listOf(ExampleEntity2(name = "Test Item 1", someNumber =  50),
                ExampleEntity2(name = "Test Item 2", someNumber =  51))
        dbInstance.exampleDao2().insertListAsync(entityList)
        assertEquals(entityList.size, dbInstance.exampleDao2().countNumEntitiesAsync(),
                "After inserting entities into blank db, num entities in db equals list length")

    }

    @Test
    fun givenQueryWithArrayParam_whenQueryCalled_thenShouldReturnMatchingValuse() = GlobalScope.promise {
        val e1 = ExampleSyncableEntity(esNumber = 42)
        var e2 = ExampleSyncableEntity(esNumber = 43)
        e1.esUid = dbInstance.exampleSyncableDao().insertAsync(e1)
        e2.esUid = dbInstance.exampleSyncableDao().insertAsync(e2)

        val entitiesFromListParam = dbInstance.exampleSyncableDao().findByListParam(
                listOf(42, 43))
        assertEquals(2, entitiesFromListParam.size, "Got expected results from list param query")
    }


    @Test
    fun givenNonAbstractFun_whenMethodCalled_thenShouldReturnValue() = GlobalScope.promise {
        var e1 = ExampleSyncableEntity(esNumber = 50)
        e1.esUid = dbInstance.exampleSyncableDao().insertAsync(e1)
        val eAdded = dbInstance.exampleSyncableDao().findByUidAndAddOneThousand(e1.esUid)
        assertEquals(1050, eAdded?.esNumber, "Got value back from server's own fn")
    }


}