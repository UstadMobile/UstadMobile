package com.ustadmobile.core.test

import com.ustadmobile.util.test.nav.TestUstadSavedStateHandle


fun savedStateOf(vararg keys: Pair<String, String>) = TestUstadSavedStateHandle().apply {
    keys.forEach {
        set(it.first, it.second)
    }
}