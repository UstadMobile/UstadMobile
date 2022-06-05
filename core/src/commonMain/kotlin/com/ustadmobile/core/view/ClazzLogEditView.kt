package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzLog


interface ClazzLogEditView: UstadEditView<ClazzLog> {

    //The date (ms since epoch to midnight of the given date as per the timezone of the class)
    var date: Long

    //The time (ms since midnight)
    var time: Long

    var timeZone : String?

    var dateError: String?

    var timeError: String?

    companion object {

        const val VIEW_NAME = "CourseLogEditView"

    }

}