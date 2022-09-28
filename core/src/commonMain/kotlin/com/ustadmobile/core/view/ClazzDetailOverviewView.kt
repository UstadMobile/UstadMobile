package com.ustadmobile.core.view

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.CourseBlockWithCompleteEntity
import com.ustadmobile.lib.db.entities.Schedule

interface ClazzDetailOverviewView: UstadDetailView<ClazzWithDisplayDetails> {

    var scheduleList: DataSourceFactory<Int, Schedule>?

    var courseBlockList: DataSourceFactory<Int, CourseBlockWithCompleteEntity>?

    var clazzCodeVisible: Boolean

    var showPermissionButton: Boolean

    companion object {

        const val VIEW_NAME = "CourseDetailOverviewView"

    }

}