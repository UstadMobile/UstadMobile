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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.*


class BasicTest {

    @Serializable
    data class NameCls(val first: String = "", val second: String = "")

    @Test
    fun basicTest() {
        assertEquals(4, 2+2, "2+2=4")
    }

    @Test
    fun testHttp() = GlobalScope.promise{
        val httpClient = HttpClient()
        val localContent = httpClient.get<String>("http://localhost/")
        assertNotNull(localContent, "Got HTML from localhost")
        httpClient.close()
    }

    @Test
    fun testDatabaseBuilder() {
        println("Attempt test database builder")
        println(ExampleDatabase2_JsImpl::class)
        val dbInstance = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2_JsImpl::class,
                "ExampleDatabase2").build()
        assertNotNull(dbInstance, "Constructed db instance object")
    }

    @Test
    fun testDaoGetSingularResult() = GlobalScope.promise {
        localStorage["doordb.endpoint.url"] = "http://localhost:8087/"
        val dbInstance = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2_JsImpl::class,
                "ExampleDatabase2").build()

        val initEntity = dbInstance.exampleDao2().findByUidAsync(42)
        assertNotNull(initEntity, "init entity not null")
        assertEquals("BobJs", initEntity.name, "got empty entries list")
    }

//    @Test
//    fun testDaoPostWithoutIdFetch() = GlobalScope.promise {
//        localStorage["doordb.endpoint.url"] = "http://localhost:8087/"
//        val dbInstance = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2_JsImpl::class,
//                "ExampleDatabase2").build()
//        dbInstance.exampleDao2().insertAsync(ExampleEntity2(name = "JsPost", someNumber =  50))
//    }

    @Test
    fun testJson() = GlobalScope.promise {
        val kotlinxSerializerClass = KotlinxSerializer::class
        println(kotlinxSerializerClass)

        val httpClient = HttpClient() {
            install(JsonFeature)
        }
        val nameFromServer = httpClient.get<NameCls>("http://localhost/name.json")
        assertEquals("Bob", nameFromServer.first)
        httpClient.close()
    }


    @Test
    fun testPost() = GlobalScope.promise {
        js("debugger")
        delay(1*1000)
        val kotlinxSerializerClass = KotlinxSerializer::class
        println(kotlinxSerializerClass)

        val json = Json(JsonConfiguration.Stable)
        val jsonData = json.stringify(NameCls.serializer(), NameCls("Hello", "Cruel World2"))

        val httpClient = HttpClient() {
            install(JsonFeature)
        }

        val localContent = httpClient.post<String>("http://localhost/") {
            body = defaultSerializer().write(NameCls("Hello", "Cruel World23"))
        }
        assertNotNull(localContent, "Got HTML from localhost via POST")
        httpClient.close()
    }

}