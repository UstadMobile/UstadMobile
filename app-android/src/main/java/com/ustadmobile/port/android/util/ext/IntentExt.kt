package com.ustadmobile.port.android.util.ext

import android.content.Intent
import com.ustadmobile.core.networkmanager.defaultGson

fun Intent.putExtraResultAsJson(key: String, value: Any) {
    putExtra(key, defaultGson().toJson(value))
}