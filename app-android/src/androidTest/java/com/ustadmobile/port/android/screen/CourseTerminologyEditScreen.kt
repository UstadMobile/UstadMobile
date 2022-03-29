package com.ustadmobile.port.android.screen

import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.CourseTerminologyEditFragment

object CourseTerminologyEditScreen : KScreen<CourseTerminologyEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_report_edit
    override val viewClass: Class<*>?
        get() = CourseTerminologyEditFragment::class.java


}