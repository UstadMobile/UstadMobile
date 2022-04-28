package com.ustadmobile.util.test.nav

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadNavController
import org.kodein.di.DI
import org.kodein.di.DIAware
import java.lang.Integer.max
import java.util.concurrent.CopyOnWriteArrayList

class TestUstadNavController(override val di: DI) : UstadNavController, DIAware{

    private val navStack: MutableList<TestUstadBackStackEntry> = CopyOnWriteArrayList()

    override val currentBackStackEntry: UstadBackStackEntry?
        get() = navStack.lastOrNull()

    override fun getBackStackEntry(viewName: String): UstadBackStackEntry? {
        return navStack.lastOrNull { it.viewName == viewName }
    }

    override fun popBackStack(viewName: String, inclusive: Boolean) {
        var splitIndex = navStack.indexOfLast { it.viewName == viewName } + 1
        if(inclusive)
            splitIndex--

        navStack.removeAll(navStack.subList(max(0, splitIndex), navStack.size))
    }

    override fun navigate(
        viewName: String,
        args: Map<String, String>,
        goOptions: UstadMobileSystemCommon.UstadGoOptions
    ) {
        val popUpToViewName = goOptions.popUpToViewName
        if(popUpToViewName != null)
            popBackStack(popUpToViewName, goOptions.popUpToInclusive)

        navStack.add(TestUstadBackStackEntry(viewName, args))
    }
}