package com.ustadmobile.lib.annotationprocessor.core

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
        assertNotNull(Any(), "Got HTML from localhost")
    }

    @Test
    fun testJson() = GlobalScope.promise {
        val kotlinxSerializerClass = KotlinxSerializer::class
        println(kotlinxSerializerClass)

        val httpClient = HttpClient() {
            install(JsonFeature)
        }
        val nameFromServer = httpClient.get<NameCls>("http://localhost/name.json")
        assertEquals("Bob", nameFromServer.first)
    }




}