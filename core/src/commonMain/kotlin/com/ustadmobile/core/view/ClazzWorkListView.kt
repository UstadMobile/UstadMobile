package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.ClazzWorkWithMetrics

interface ClazzWorkListView: UstadListView<ClazzWork, ClazzWorkWithMetrics> {

    val canSeeResult: Boolean
    companion object {
        const val VIEW_NAME = "ClazzWorkListView"
    }

}