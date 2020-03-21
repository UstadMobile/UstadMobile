package com.ustadmobile.core.util.ext

import android.os.Bundle

fun Bundle?.toStringMap(): Map<String, String> {
    return if(this != null) {
        keySet().map { it to this.get(it).toString() }.toMap()
    }else {
        mapOf()
    }
}

fun Bundle?.toNullableStringMap(): Map<String, String>? {
    return if(this != null) {
        keySet().map { it to this.get(it).toString() }.toMap()
    }else {
        null
    }
}

fun Map<String, String>.toBundle(): Bundle {
    return Bundle().also { bundle ->
        this.entries.forEach { entry -> bundle.putString(entry.key, entry.value) }
    }
}
