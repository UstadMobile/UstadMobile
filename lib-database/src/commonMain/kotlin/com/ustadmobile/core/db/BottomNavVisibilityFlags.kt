package com.ustadmobile.core.db

object BottomNavVisibilityFlags {

    const val SHOW_COURSE = 1L        // 2^0
    const val SHOW_LIBRARY = 2L       // 2^1
    const val SHOW_MESSAGES = 4L      // 2^2
    const val SHOW_PEOPLE = 8L        // 2^3

    const val ALL = SHOW_COURSE or SHOW_LIBRARY or SHOW_MESSAGES or SHOW_PEOPLE
}
