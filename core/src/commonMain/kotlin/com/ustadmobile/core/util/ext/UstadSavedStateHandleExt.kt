package com.ustadmobile.core.util.ext

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle

fun UstadSavedStateHandle.setIfNoValueSetYet(key: String, value: String) {
    if(get(key) == null) {
        set(key, value)
    }
}
