package com.ustadmobile.navigation

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.redux.ReduxAppStateManager
import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxNavStackState
import com.ustadmobile.redux.ReduxSerializationState
import kotlinx.browser.window

class NavControllerJs: UstadNavController {

    private val navStack: MutableList<UstadBackStackEntryJs> = ReduxAppStateManager.getCurrentState().navStack.stack

    override val currentBackStackEntry: UstadBackStackEntry?
        get() = navStack.lastOrNull()

    override fun getBackStackEntry(viewName: String): UstadBackStackEntry? {
        return navStack.lastOrNull { it.viewName == viewName }
    }

    override fun popBackStack(viewName: String, inclusive: Boolean) {
        var splitIndex = navStack.indexOfLast { it.viewName == viewName } + 1
        if(inclusive)
            splitIndex--

        navStack.removeAll(navStack.subList(splitIndex, navStack.size))
        dispatch(ReduxNavStackState(navStack))

        currentBackStackEntry?.arguments?.let { args ->
            navigate(viewName, args, false)
        }
    }

    override fun navigate(viewName: String,
                          args: Map<String, String>,
                          goOptions: UstadMobileSystemCommon.UstadGoOptions) {
        dispatch(ReduxSerializationState(goOptions.serializer))
        navStack.add(UstadBackStackEntryJs(viewName, args))
        dispatch(ReduxNavStackState(navStack))

        val popUpToViewName = goOptions.popUpToViewName
        if(popUpToViewName != null)
            popBackStack(popUpToViewName, goOptions.popUpToInclusive)

        navigate(viewName, args, goOptions.popUpToViewName?.isNotEmpty() == true)
    }

    private fun navigate(viewName: String, args: Map<String, String>, hasOption: Boolean = false){
        val params = when {
            args.isEmpty() -> ""
            else -> "?${UMFileUtil.mapToQueryString(args)}"
        }

        if(hasOption){
            window.location.replace("#$viewName$params")
        }else{
            window.location.assign("#$viewName$params")
        }
    }

    fun navigateUp(): Boolean {
        val destinationToPopTo = when (navStack.size) {
            1 -> currentBackStackEntry
            else -> navStack[navStack.lastIndex - 1]
        }
        if(destinationToPopTo != null){
            popBackStack(destinationToPopTo.viewName, true)
        }
        return true
    }
}