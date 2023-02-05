package com.ustadmobile.core.navigation

import kotlinx.browser.window
import kotlin.test.Test
import kotlin.test.assertEquals

class SavedStateHandle2Test {

    @Test
    fun givenStateSavedToHistory_whenNewHandleCreated_thenValueShouldBeRestored() {
        val stateHandle = SavedStateHandle2(window.history)
        stateHandle["test"] = "42"

        val restoredHandle = SavedStateHandle2(window.history)
        assertEquals("42", restoredHandle["test"])

    }

}