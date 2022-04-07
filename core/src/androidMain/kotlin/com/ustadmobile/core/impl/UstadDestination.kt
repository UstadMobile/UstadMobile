package com.ustadmobile.core.impl

import com.google.android.material.appbar.AppBarLayout

data class UstadDestination(
    val destinationId: Int,
    val actionBarScrollBehavior: Int = AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL,
    val hideBottomNavigation: Boolean = false,
    val hideAccountIcon: Boolean = false
)
