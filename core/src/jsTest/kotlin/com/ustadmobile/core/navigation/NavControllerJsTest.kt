package com.ustadmobile.core.navigation

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.util.defaultJsonSerializer
import com.ustadmobile.core.view.UstadView
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.w3c.dom.HashChangeEvent
import org.w3c.dom.events.Event
import kotlin.test.*

class NavControllerJsTest {

    private lateinit var json: Json

    @BeforeTest
    fun init(){
        defaultJsonSerializer()
        Napier.base(DebugAntilog())
        json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }

    /**
     * Runs a given block (e.g. a block that would cause navigation to happen) and then waits for
     * the hash to change to the given viewName.
     */
    private suspend fun waitForHashChange(
        viewName: String,
        timeout: Long = 500,
        delayAfter: Long = 30,
        block: suspend () -> Unit,
    )  {
        val completableDeferred = CompletableDeferred<Boolean>()
        val evtListener: (Event) -> Unit = {
            val hashEvt = it as HashChangeEvent
            if(hashEvt.newURL.contains("#/$viewName")) {
                completableDeferred.complete(true)
            }
        }

        window.addEventListener("hashchange", evtListener)
        block()
        try {
            withTimeout(timeout) {
                completableDeferred.await()
            }
        }catch(e: Exception) {
            Napier.e("Timed out waiting to navigate to $viewName after ${timeout}ms.")
        }finally {
            window.removeEventListener("hashchange", evtListener)
            delay(delayAfter)
        }
    }


    @Test
    fun givenDataSavedInState_whenNavigationMovesForwardThenUserGoesBack_thenShouldRetrieveState() = GlobalScope.promise{
        waitForHashChange("ViewName1") {
            window.location.assign("#/ViewName1")
        }

        val navControllerJs = NavControllerJs(TEST_SEPARATOR, "umnav1", json)
        navControllerJs.currentBackStackEntry!!.savedStateHandle.set("answer", "42")

        waitForHashChange("ViewName2") {
            navControllerJs.navigate("ViewName2", mapOf())
        }

        waitForHashChange("ViewName1") {
            window.history.go(-1)
        }

        val answer = navControllerJs.currentBackStackEntry!!.savedStateHandle.get<String>("answer")
        assertEquals("42", answer, "Value saved is retrieved")

        navControllerJs.unplug()
    }

    //Make sure that a new saved state handle is used when navigating forward
    @Test
    fun givenDataSavedInState_whenNavigationMovesForward_thenDataShouldNotBeInCurrentState() = GlobalScope.promise {
        waitForHashChange("ViewName3") {
            window.location.assign("#/ViewName3")
        }

        val navControllerJs = NavControllerJs(TEST_SEPARATOR, "umnav2", json)
        navControllerJs.currentBackStackEntry!!.savedStateHandle.set("answer", "42")

        waitForHashChange("ViewName4") {
            navControllerJs.navigate("ViewName4", mapOf())
        }

        val answer = navControllerJs.currentBackStackEntry!!.savedStateHandle.get<String>("answer")
        assertNull(answer, "Value saved in previous stack entry is not in the current entry after navigation")

        navControllerJs.unplug()
    }

    @Test
    fun givenNavigateForwardThreeTimes_thenPopBackStackNotInclusiveToFirstView_shouldArriveAtFirstView() = GlobalScope.promise {
        //Add check saving value
        waitForHashChange("ViewName0") {
            window.location.assign("#/ViewName0")
        }

        val navControllerJs = NavControllerJs(TEST_SEPARATOR, "umnav3", json)
        for(i in 1 .. 3) {
            waitForHashChange("ViewName$i") {
                navControllerJs.navigate("ViewName$i", mapOf())
            }
            navControllerJs.currentBackStackEntry?.savedStateHandle?.set("pageNum", i.toString())
        }

        /**
         * Stack should go from
         * ViewName0, ViewName1, ViewName2, ViewName3, ViewName4
         * to:
         * ViewName0, ViewName1 (because inclusive = false)
         */
        waitForHashChange("ViewName1") {
            navControllerJs.popBackStack("ViewName1", inclusive = false)
        }

        assertTrue(window.location.href.endsWith("#/ViewName1"),
            "After popBackStack url ends with expected result")

        assertEquals("1",
            navControllerJs.currentBackStackEntry?.savedStateHandle?.get("pageNum"),
            "Data in SavedState from page was retrieved as expected")

        navControllerJs.unplug()
    }

    @Test
    fun givenNavigateForwardThreeTimes_thenNavigateWithPopOffToFirstViewThenGoBack_shouldArriveAtView1() = GlobalScope.promise {
        waitForHashChange("ViewName0") {
            window.location.assign("#/ViewName0")
        }

        val navControllerJs = NavControllerJs(TEST_SEPARATOR, "umnav4", json)
        for(i in 1 .. 3) {
            waitForHashChange("ViewName$i") {
                navControllerJs.navigate("ViewName$i", mapOf())
            }
            navControllerJs.currentBackStackEntry?.savedStateHandle?.set("pageNum", i.toString())
        }

        /**
         * Stack should change from:
         * ViewName0, ViewName1, ViewName2, ViewName3
         *
         * To:
         * ViewName0, ViewName1, ViewName4 (popUpTo=ViewName1, inclusive = false)
         */
        waitForHashChange("ViewName4") {
            navControllerJs.navigate("ViewName4", mapOf(),
                UstadMobileSystemCommon.UstadGoOptions(popUpToViewName = "ViewName1",
                    popUpToInclusive = false))
        }

        waitForHashChange("ViewName1") {
            window.history.go(-1)
        }

        assertTrue(window.location.href.endsWith("#/ViewName1"),
            "After popBackStack url ends with expected result")

        assertEquals("1",
            navControllerJs.currentBackStackEntry?.savedStateHandle?.get("pageNum"),
            "Value saved to page 1 savedStateHandle is available as expected")

        navControllerJs.unplug()
    }

    //Make sure the first page in the stack is handled correctly
    @Test
    fun givenOpenedOnStartPage_thenNavigateAndGoBack_dataShouldBeSaved() = GlobalScope.promise {
        waitForHashChange("ViewName0") {
            window.location.replace("#/ViewName0")
        }

        val navControllerJs = NavControllerJs(TEST_SEPARATOR, "umnav5", json)
        navControllerJs.currentBackStackEntry?.savedStateHandle?.set("pageNum", "0")

        waitForHashChange("ViewName1") {
            navControllerJs.navigate("ViewName1", mapOf())
        }

        waitForHashChange("ViewName0") {
            window.history.go(-1)
        }

        assertEquals("0",
            navControllerJs.currentBackStackEntry?.savedStateHandle?.get("pageNum"),
            "State saved by first page in stack is retrieved as expected")
        navControllerJs.unplug()
    }

    /**
     * Simulate the navigation that would be expected when the user goes from a list view
     * to create a new item. Navigate from List to Edit. Navigate from Edit to Detail (where
     * popoffto=CURRENT_DEST, inclusive=true). Then go back. Should wind up at the ListView
     */
    @Test
    fun givenNavigateWithPopUpToCurrentDest_thenGoBack_shouldGoBackToFirstDest() = GlobalScope.promise {
        waitForHashChange("ListView") {
            window.location.replace("#/ListView")
        }

        val navControllerJs = NavControllerJs(TEST_SEPARATOR, "umnav6", json)
        navControllerJs.currentBackStackEntry?.savedStateHandle?.set("pageNum", "0")

        waitForHashChange("EditView") {
            navControllerJs.navigate("Editview", mapOf())
        }

        waitForHashChange("DetailView") {
            navControllerJs.navigate("DetailView", mapOf(),
                UstadMobileSystemCommon.UstadGoOptions(popUpToViewName = UstadView.CURRENT_DEST, popUpToInclusive = true))
        }

        //Now go back - should wind up back at the list view, not the edit view
        waitForHashChange("ListView") {
            window.history.go(-1)
        }

        assertTrue(window.location.href.endsWith("#/ListView"),
            "Location has now returned to ListView")

        assertEquals("0",
            navControllerJs.currentBackStackEntry?.savedStateHandle?.get("pageNum"),
            "Got expected save state value")

        navControllerJs.unplug()
    }

    //Simulate returning a result - make sure data goes to the right place.
    @Test
    fun givenNavigatedForward_whenSaveToPrevBackStack_thenPopBackStackShouldGetSavedValue() = GlobalScope.promise {
        waitForHashChange("EditView1") {
            window.location.replace("#/EditView1")
        }

        val navControllerJs = NavControllerJs(TEST_SEPARATOR, "umnav7", json)

        //E.g. navigate to another picker list
        waitForHashChange("ListView") {
            navControllerJs.navigate("ListView", mapOf())
        }

        //E.g. navigate to creating something new
        waitForHashChange("EditView2") {
            navControllerJs.navigate("EditView2", mapOf())
        }

        val editView1Handle = navControllerJs.getBackStackEntry("EditView1")
        editView1Handle?.savedStateHandle?.set("returnedVal", "42")

        waitForHashChange("EditView1") {
            navControllerJs.popBackStack("EditView1", false)
        }

        assertEquals("42",
            navControllerJs.currentBackStackEntry?.savedStateHandle?.get("returnedVal"),
            "Returned value was found in back state handle as expected")

        navControllerJs.unplug()
    }

    @Test
    fun givenEmptyState_whenNavigateForwardThenBackwardThenForward_shouldRetainState() = GlobalScope.promise {
        waitForHashChange("EditView1") {
            window.location.replace("#/EditView1")
        }

        val navControllerJs = NavControllerJs(TEST_SEPARATOR, "umnav8", json)
        waitForHashChange("EditView2") {
            navControllerJs.navigate("EditView2", mapOf())
        }

        navControllerJs.currentBackStackEntry?.savedStateHandle?.set("pageNum", "2")

        waitForHashChange("EditView1") {
            window.history.go(-1)
        }

        waitForHashChange("EditView2") {
            window.history.go(1)
        }

        assertEquals("2",
            navControllerJs.currentBackStackEntry?.savedStateHandle?.get("pageNum"),
            "Retrieve expected back stack entry after navigating forward")

        navControllerJs.unplug()

    }



    @Test
    fun givenNavStateSaved_whenNewNavControllerCreatedWithSameStorageKey_thenShouldRetainValues() = GlobalScope.promise {
        waitForHashChange("ViewName1") {
            window.location.assign("#/ViewName1")
        }

        val navControllerJs1 = NavControllerJs(TEST_SEPARATOR, "umnav_restore_test",
            json)
        navControllerJs1.currentBackStackEntry!!.savedStateHandle.set("answer", "42")

        waitForHashChange("ViewName2") {
            navControllerJs1.navigate("ViewName2", mapOf())
        }

        navControllerJs1.unplug()

        val navControllerJs2 = NavControllerJs(TEST_SEPARATOR, "umnav_restore_test",
            json)

        waitForHashChange("ViewName1") {
            window.history.go(-1)
        }

        val answer = navControllerJs2.currentBackStackEntry!!.savedStateHandle.get<String>("answer")
        assertEquals("42", answer, "Value saved is retrieved")

        navControllerJs2.unplug()
    }

    companion object {

        /**
         * When running normally the url and endpoint are separated by /#/ . When running in tests,
         * this is actually "/context.html#/"
         */
        const val TEST_SEPARATOR = "/context.html#/"

    }
}