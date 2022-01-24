package com.ustadmobile.util.test.nav

import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle

class TestUstadBackStackEntry(override val viewName: String,
                              override val arguments: Map<String, String>) : UstadBackStackEntry{

    override val savedStateHandle: UstadSavedStateHandle = TestUstadSavedStateHandle()

}