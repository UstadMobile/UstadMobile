package com.ustadmobile.core.viewmodel.site.edit
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.BottomNavVisibilityFlags

object BottomNavPermissionConstants {

    val BOTTOM_NAV_LABELS = listOf(
        MR.strings.course to BottomNavVisibilityFlags.SHOW_COURSE,
        MR.strings.library to BottomNavVisibilityFlags.SHOW_LIBRARY,
        MR.strings.messages to BottomNavVisibilityFlags.SHOW_MESSAGES,
        MR.strings.people to BottomNavVisibilityFlags.SHOW_PEOPLE,
    )

}
