package com.ustadmobile.core.navigation

import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UstadSavedStateHandleJsTest {

    private lateinit var json: Json

    @BeforeTest
    fun setup() {
        json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }

    @Test
    fun givenPreLoadedValues_whenInitRuns_thenShouldLoadPrevValues() {
        val stateHandle = UstadSavedStateHandleJs(mapOf("answer" to "42"))
        assertEquals("42", stateHandle.get("answer"),
            "Data saved in sessionstorage was loaded as expected")
    }

    @Test
    fun givenNoPreloadedValues_whenInitRuns_thenShouldBeEmpty() {
        val stateHandle = UstadSavedStateHandleJs(mapOf())
        assertNull(stateHandle.get("answer"))
    }


}