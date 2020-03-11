package com.ustadmobile.core.util.ext

import android.os.Bundle

fun Bundle?.toStringMap(): Map<String, String> {
    return if(this != null) {
        keySet().map { it to this.get(it).toString() }.toMap()
    }else {
        mapOf()
    }
}