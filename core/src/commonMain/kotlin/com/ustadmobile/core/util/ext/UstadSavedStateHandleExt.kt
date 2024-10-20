package com.ustadmobile.core.util.ext

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle

fun UstadSavedStateHandle.setIfNoValueSetYet(key: String, value: String) {
    if(get(key) == null) {
        set(key, value)
    }
}

fun UstadSavedStateHandle.require(key: String): String {
    return get(key) ?: throw IllegalArgumentException("SavedStateHandle: required key not found: $key")
}
