package com.ustadmobile.navigation

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.redux.ReduxAppStateManager
import com.ustadmobile.redux.ReduxNavStackState
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
        ReduxAppStateManager.dispatch(ReduxNavStackState(navStack))
    }

    override fun navigate(
        viewName: String,
        args: Map<String, String>,
        goOptions: UstadMobileSystemCommon.UstadGoOptions
    ) {
        navStack.add(UstadBackStackEntryJs(viewName, args))
        ReduxAppStateManager.dispatch(ReduxNavStackState(navStack))

        val popUpToViewName = goOptions.popUpToViewName
        if(popUpToViewName != null)
            popBackStack(popUpToViewName, goOptions.popUpToInclusive)

        val params = if(args.isEmpty())  "" else "?${UMFileUtil.mapToQueryString(args)}"
        if(goOptions.popUpToViewName?.isNotEmpty() == true){
            window.location.replace("${hashType}$viewName$params")
        }else{
            window.location.assign("${hashType}$viewName$params")
        }
    }

    companion object {
        private const val hashType = "#/"
    }
}