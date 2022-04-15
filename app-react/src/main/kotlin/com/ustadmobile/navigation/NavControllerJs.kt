package com.ustadmobile.navigation

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.door.ext.toUrlQueryString
import com.ustadmobile.navigation.RouteManager.firstDestination
import com.ustadmobile.redux.ReduxAppStateManager
import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxNavStackState
import com.ustadmobile.util.getViewNameFromUrl
import kotlinx.browser.sessionStorage
import kotlinx.browser.window

/**
 * Handles all navigation within the web application, it is also responsible to synchronize
 * between our internal stack and browser history.
 *
 * We only pop from the history only when the pop state is triggered from our internal navigation,
 * Otherwise i.e Browser back navigation will only change the currentBackStackEntry position
 * from the stack.
 *
 * This  is due to the reason that, browser can use forward button to navigate and if our
 * internal stack entry was popped that means we won't be able to sync histories any more
 * and the state will be destroyed. So, all saved data will be lost.
 */
class NavControllerJs : UstadNavController {

    private val navStack: MutableList<UstadBackStackEntryJs> = ReduxAppStateManager.getCurrentState().navStack.stack

    //Internal current navStack entry tracker
    private var currentNavStackEntry: UstadBackStackEntry? = navStack.lastOrNull()

    override val currentBackStackEntry: UstadBackStackEntry?
        get() = currentNavStackEntry

    private val trackAndSyncHistories: (dynamic) -> Unit =  {
        val lastShownPosition = (sessionStorage.getItem(KEY_LAST_SHOWN_POSITION) ?: "0").toInt()
        val state = window.history.state
        var position = when (state) {
            null -> 0
            is Int -> state
            else -> state.asDynamic().idx
        }
        if(state == null){
            position = lastShownPosition + 1
            window.history.replaceState(position, "")
        }
        sessionStorage.setItem(KEY_LAST_SHOWN_POSITION, position.toString())
        val backButtonEvent = (position - lastShownPosition) < 0
        val currentViewName = getViewNameFromUrl() ?:""
        val historyOutOfSync = currentViewName != navStack.lastOrNull()?.viewName

        if(backButtonEvent && historyOutOfSync){
           syncBrowserAndInternalHistories(currentViewName, false, true)
        }
    }

    init {
        window.onpopstate = trackAndSyncHistories
        window.onpageshow = trackAndSyncHistories
    }

    override fun getBackStackEntry(viewName: String): UstadBackStackEntry? {
        return navStack.lastOrNull { it.viewName == viewName }
    }

    override fun popBackStack(viewName: String, inclusive: Boolean) {

        if(viewName.isEmpty()){
            navigateUp()
            return
        }

        val historyIndexToPopBackTo = syncBrowserAndInternalHistories(viewName, inclusive, false)

        currentBackStackEntry?.let { entry ->
            navigateInternal(entry.viewName, entry.arguments.toMutableMap(),historyIndexToPopBackTo)
        }
    }

    private val syncBrowserAndInternalHistories: (String, Boolean, Boolean) -> Int = {
            viewName, inclusive, fromBackEvent ->
        val indexToPopTo = navStack.indexOfLast { it.viewName == viewName }
        var splitIndex = indexToPopTo + 1

        if(inclusive)
            splitIndex--

        if(splitIndex >= 0) {
            if(fromBackEvent)
                currentNavStackEntry = navStack[splitIndex]
            else
                navStack.removeAll(navStack.subList(splitIndex, navStack.size))

            dispatch(ReduxNavStackState(navStack))
        }
        (navStack.size - indexToPopTo) * -1 //Steps to navigate back on browser history
    }

    override fun navigate(
        viewName: String,
        args: Map<String, String>,
        goOptions: UstadMobileSystemCommon.UstadGoOptions
    ) {
        if(viewName.isEmpty()){
            navigateUp()
            return
        }

        navStack.add(UstadBackStackEntryJs(viewName, args))
        dispatch(ReduxNavStackState(navStack))

        val popUpToViewName = goOptions.popUpToViewName
        if(popUpToViewName != null)
            popBackStack(popUpToViewName, goOptions.popUpToInclusive)

        navigateInternal(viewName, args.toMutableMap(),  0)
    }

    private fun navigateInternal(
        viewName: String,
        args: MutableMap<String, String>,
        stepsToGoBackInHistory: Int = 0
    ){
        val params = when {
            args.isEmpty() -> ""
            else -> "?${args.toUrlQueryString()}"
        }

        if(stepsToGoBackInHistory < 0){
            window.history.go(stepsToGoBackInHistory)
        }else{
            window.location.assign("#/$viewName$params")
        }
    }

    fun navigateUp(): Boolean {
        val destinationToPopTo = when (navStack.size) {
            in 0..1 -> UstadBackStackEntryJs(viewName = firstDestination.view, mapOf())
            else -> navStack.lastOrNull { it.viewName != getViewNameFromUrl() }
        }

        if(destinationToPopTo != null){
            navigate(destinationToPopTo.viewName, destinationToPopTo.arguments,
                UstadMobileSystemCommon.UstadGoOptions())
        }
        return true
    }

    companion object {
        const val KEY_LAST_SHOWN_POSITION = "last_shown_position"
    }
}