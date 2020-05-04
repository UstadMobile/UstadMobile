package com.ustadmobile.port.android.util.ext

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

//As per https://github.com/android/android-ktx/issues/363

fun Context.getActivityContext(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> this.baseContext.getActivityContext()
    else -> throw IllegalArgumentException("Not an activity context")
}
