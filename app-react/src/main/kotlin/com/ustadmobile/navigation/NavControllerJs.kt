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
import kotlinx.browser.window
import kotlin.js.Date

class NavControllerJs : UstadNavController {

    private val navStack: MutableList<UstadBackStackEntryJs> = ReduxAppStateManager.getCurrentState().navStack.stack

    override val currentBackStackEntry: UstadBackStackEntry?
        get() = navStack.lastOrNull()

    init {
        window.onpopstate = {
            console.log("JS-LOG", it)
        }
    }

    override fun getBackStackEntry(viewName: String): UstadBackStackEntry? {
        return navStack.lastOrNull { it.viewName == viewName }
    }

    override fun popBackStack(viewName: String, inclusive: Boolean) {

        if(viewName.isEmpty()){
            navigateUp()
            return
        }

        val indexToPopTo = navStack.indexOfLast { it.viewName == viewName }
        var splitIndex = indexToPopTo + 1

        if(inclusive)
            splitIndex--

        if(splitIndex >= 0) {
            navStack.removeAll(navStack.subList(splitIndex, navStack.size))
            dispatch(ReduxNavStackState(navStack))
        }
        currentBackStackEntry?.let { entry ->
            navigateInternal(entry.viewName, entry.arguments.toMutableMap(),(navStack.size - indexToPopTo) * -1)
        }
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
        val backIdArg = mapOf("backstackId" to Date().getTime().toString())
        val params = when {
            args.isEmpty() -> "?${backIdArg.toUrlQueryString()}"
            else -> {
                args.putAll(backIdArg)
                "?${args.toUrlQueryString()}"
            }
        }

        if(stepsToGoBackInHistory != 0){
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
}