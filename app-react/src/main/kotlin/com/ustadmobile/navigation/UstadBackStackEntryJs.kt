package com.ustadmobile.navigation

import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle

class UstadBackStackEntryJs(
    override val viewName: String,
    override val arguments: Map<String, String>
    ) : UstadBackStackEntry{

    override val savedStateHandle: UstadSavedStateHandle = UstadSavedStateHandleJs()

}