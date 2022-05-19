package com.ustadmobile.core.navigation

import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UstadBackStackEntryJsTest {

    private lateinit var json: Json

    @BeforeTest
    fun setup() {
        json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }

    @Test
    fun givenEntryCreated_whenStoredInSession_thenLoaded_valuesShouldBeEqual() {
        val stackEntry = UstadBackStackEntryJs("ViewName", mapOf("arg" to "value"),
            "ViewName?arg=value", "key1", json)
        stackEntry.savedStateHandle["answer"] = "42"
        stackEntry.onCommit()

        val stackEntryLoaded = UstadBackStackEntryJs.loadFromSessionStorage("key1",
            json)

        assertEquals(stackEntry.jsViewUri, stackEntryLoaded.jsViewUri,
            "Loaded expected view uri")
        assertEquals("42", stackEntryLoaded.savedStateHandle["answer"],
            "Saved State key value was loaded")
    }

    @Test
    fun givenStorageEmpty_whenLoadFromSessionCalled_thenShouldThrowException(){
        var exception: Exception? = null

        try {
            UstadBackStackEntryJs.loadFromSessionStorage("keyDoesNotExist", json)
        }catch(e: Exception) {
            exception = e
        }

        assertNotNull(exception)
    }

}