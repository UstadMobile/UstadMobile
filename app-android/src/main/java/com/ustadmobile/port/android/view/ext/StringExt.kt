package com.ustadmobile.port.android.view.ext

import android.view.View

fun String?.visibleIfNotNullOrEmpty() = if(!this.isNullOrEmpty()) View.VISIBLE else View.GONE
