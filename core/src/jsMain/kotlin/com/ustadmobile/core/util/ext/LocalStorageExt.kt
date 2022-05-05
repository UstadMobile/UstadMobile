package com.ustadmobile.core.util.ext

import org.w3c.dom.Storage

fun Storage.getOrPut(key: String, block: () -> String) : String {
    return getItem(key) ?: block().also { newVal ->
        setItem(key, newVal)
    }
}
