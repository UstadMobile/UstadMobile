package com.ustadmobile.core.util.ext

fun Int.alternative(alternative: Int) = if(this == 0) alternative else this


fun Long.alternative(alternative: Long) = if(this == 0L) alternative else this