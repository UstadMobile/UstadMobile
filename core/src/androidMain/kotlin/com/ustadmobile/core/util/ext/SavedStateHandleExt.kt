package com.ustadmobile.core.util.ext

import androidx.lifecycle.SavedStateHandle

fun SavedStateHandle.setAllFromMap(stringMap: Map<String, String>) {
    stringMap.forEach {
        set(it.key, it.value)
    }
}

/**
 * Convert a SavedStateHandle to a StringMap for use with core
 */
fun SavedStateHandle.toStringMap() : Map<String, String> {
    return mutableMapOf<String, String>().also {
        this.keys().forEach {key ->
            val strVal = get<String>(key)
            if(strVal != null)
                it[key] = strVal
        }
    }
}


