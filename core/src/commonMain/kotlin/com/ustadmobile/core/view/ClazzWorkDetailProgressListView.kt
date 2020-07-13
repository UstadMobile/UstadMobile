package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazzWorkProgress
import com.ustadmobile.lib.db.entities.ClazzWorkWithMetrics


interface ClazzWorkDetailProgressListView: UstadListView<ClazzMemberWithClazzWorkProgress,
        ClazzMemberWithClazzWorkProgress> {

    var clazzWorkWithMetrics : DataSource.Factory<Int, ClazzWorkWithMetrics>?

    companion object {
        const val VIEW_NAME = "ClazzMemberWithClazzWorkProgressListView"
    }

}