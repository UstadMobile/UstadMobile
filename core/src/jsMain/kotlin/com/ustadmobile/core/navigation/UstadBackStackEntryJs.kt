package com.ustadmobile.core.navigation

import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle

class UstadBackStackEntryJs(
    override val viewName: String,
    override val arguments: Map<String, String>,
    //jsViewUri is stored as a string so NavControllerJs can find it without worrying about the order of the arguments map
    internal val jsViewUri: String,
) : UstadBackStackEntry{

    override val savedStateHandle: UstadSavedStateHandle = UstadSavedStateHandleJs()

}