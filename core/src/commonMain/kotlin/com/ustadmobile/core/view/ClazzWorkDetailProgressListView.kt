package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzWorkProgress
import com.ustadmobile.lib.db.entities.ClazzWorkWithMetrics


interface ClazzWorkDetailProgressListView: UstadListView<ClazzEnrolmentWithClazzWorkProgress,
        ClazzEnrolmentWithClazzWorkProgress> {

    var clazzWorkWithMetrics : DataSource.Factory<Int, ClazzWorkWithMetrics>?

    companion object {
        const val VIEW_NAME = "ClazzMemberWithClazzWorkProgressListView"
    }

}