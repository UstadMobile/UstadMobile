package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.CourseBlockWithCompleteEntity
import com.ustadmobile.lib.db.entities.Schedule

interface ClazzDetailOverviewView: UstadDetailView<ClazzWithDisplayDetails> {

    var scheduleList: DoorDataSourceFactory<Int, Schedule>?

    var courseBlockList: DoorDataSourceFactory<Int, CourseBlockWithCompleteEntity>?

    var clazzCodeVisible: Boolean

    companion object {

        const val VIEW_NAME = "CourseDetailOverviewView"

    }

}