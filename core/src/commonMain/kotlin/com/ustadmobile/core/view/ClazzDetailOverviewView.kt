package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.Schedule

interface ClazzDetailOverviewView: UstadDetailView<ClazzWithDisplayDetails> {

    var scheduleList: DataSource.Factory<Int, Schedule>?

    var clazzCodeVisible: Boolean

    companion object {

        const val VIEW_NAME = "ClazzDetailOverviewView"

    }

}