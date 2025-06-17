package com.ustadmobile.core.viewmodel.site.edit
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.Site

object BottomNavPermissionConstants {

    val BOTTOM_NAV_LABELS = listOf(
        MR.strings.course to Site.SHOW_COURSE,
        MR.strings.library to Site.SHOW_LIBRARY,
        MR.strings.messages to Site.SHOW_MESSAGES,
        MR.strings.people to Site.SHOW_PEOPLE,
    )

}
