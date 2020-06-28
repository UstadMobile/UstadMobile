package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazzWorkProgress
import com.ustadmobile.lib.db.entities.ClazzWorkWithMetrics


interface ClazzWorkDetailProgressListView: UstadListView<ClazzMemberWithClazzWorkProgress,
        ClazzMemberWithClazzWorkProgress> {

    var clazzWorkWithMetrics: DoorLiveData<ClazzWorkWithMetrics>?

    var clazzWorkWithMetricsFlat : ClazzWorkWithMetrics?

    var hasContent: Boolean?

    companion object {
        const val VIEW_NAME = "ClazzMemberWithClazzWorkProgressListView"
    }

}