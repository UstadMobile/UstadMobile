package com.ustadmobile.core.navigation

import kotlinx.browser.window
import web.url.URLSearchParams
import kotlin.test.Test
import kotlin.test.assertEquals

class SavedStateHandle2Test {

    @Test
    fun givenStateSavedToHistory_whenNewHandleCreated_thenValueShouldBeRestored() {
        val stateHandle = SavedStateHandle2(window.history, URLSearchParams(""))
        stateHandle["test"] = "42"

        val restoredHandle = SavedStateHandle2(window.history, URLSearchParams(""))
        assertEquals("42", restoredHandle["test"])

    }

}