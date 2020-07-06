package com.ustadmobile.core.util.ext

import android.os.Bundle
import com.ustadmobile.core.networkmanager.defaultGson

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

fun Bundle.putEntityAsJson(key: String, value: Any) {
    putString(key, defaultGson().toJson(value))
}

fun Map<String, String>.toBundle(): Bundle {
    return Bundle().also { bundle ->
        this.entries.forEach { entry -> bundle.putString(entry.key, entry.value) }
    }
}

fun Map<String, String?>.toBundleWithNullableValues(): Bundle {
    return Bundle().also { bundle ->
        this.entries.forEach { entry -> bundle.putString(entry.key, entry.value) }
    }
}