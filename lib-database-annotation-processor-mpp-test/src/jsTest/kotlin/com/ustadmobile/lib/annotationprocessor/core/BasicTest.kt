package com.ustadmobile.lib.annotationprocessor.core

import com.ustadmobile.door.DatabaseBuilder
import db2.ExampleDatabase2
import db2.ExampleDatabase2_JsImpl
import db2.ExampleEntity2
import db2.ExampleSyncableEntity
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.defaultSerializer
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.promise
import kotlinx.serialization.Serializable
import org.w3c.dom.set
import kotlin.browser.localStorage
import kotlinx.serialization.json.*
import kotlin.test.*


class BasicTest {

    private lateinit var dbInstance: ExampleDatabase2

    @BeforeTest
    fun register() {
        localStorage["doordb.endpoint.url"] = "http://localhost:8089/"
        ExampleDatabase2_JsImpl.register()
        dbInstance = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class,
                "ExampleDatabase2").build()

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
    fun givenDatabaseCreated_whenSelectEntityOnServerIsCalled_thenShouldReturnEntity() = GlobalScope.promise {
        val initEntity = dbInstance.exampleDao2().findByUidAsync(5000L) //inserted by ExampleDatabase2App
        assertNotNull(initEntity, "init entity not null")
        assertEquals("Initial Entry", initEntity.name, "got initial entry")
    }

    @Test
    fun testDaoPostWithoutIdFetch() = GlobalScope.promise {
        val dbInstance = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class,
                "ExampleDatabase2").build()
        val entity = ExampleEntity2(name = "JsPost", someNumber =  50)
        entity.uid = dbInstance.exampleDao2().insertAsyncAndGiveId(entity)
        assertNotEquals(0, entity.uid, "After insert non-zero UID is received")
    }

}