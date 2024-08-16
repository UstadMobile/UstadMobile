package com.ustadmobile.libuicompose.view.app

import androidx.compose.runtime.compositionLocalOf

//As per https://developer.android.com/guide/topics/large-screens/support-different-screen-sizes#window_size_classes
enum class SizeClass {
    COMPACT, MEDIUM, EXPANDED
}

val LocalWidthClass = compositionLocalOf { SizeClass.MEDIUM }
