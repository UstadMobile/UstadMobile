package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzEnrollmentWithClazzWorkProgress
import com.ustadmobile.lib.db.entities.ClazzWorkWithMetrics


interface ClazzWorkDetailProgressListView: UstadListView<ClazzEnrollmentWithClazzWorkProgress,
        ClazzEnrollmentWithClazzWorkProgress> {

    var clazzWorkWithMetrics : DataSource.Factory<Int, ClazzWorkWithMetrics>?

    companion object {
        const val VIEW_NAME = "ClazzMemberWithClazzWorkProgressListView"
    }

}