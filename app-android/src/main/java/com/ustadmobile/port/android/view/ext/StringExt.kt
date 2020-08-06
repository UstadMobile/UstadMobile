package com.ustadmobile.port.android.view.ext

import android.view.View

fun String?.visibleIfNotNullOrEmpty() = if(!this.isNullOrEmpty()) View.VISIBLE else View.GONE

fun String?.visibleIfNullOrEmpty() = if(!this.isNullOrEmpty()) View.GONE else View.INVISIBLE
