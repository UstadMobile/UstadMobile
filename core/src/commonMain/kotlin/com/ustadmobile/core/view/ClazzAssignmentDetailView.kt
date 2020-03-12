package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics

/**
 * Core View. Screen is for ClazzAssignmentDetail's View
 */
interface ClazzAssignmentDetailView : UstadView {


    fun setClazzAssignment(clazzAssignment: ClazzAssignmentWithMetrics)

    fun setupTabs(tabs : List<String>)

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "ClazzAssignmentDetail"
    }

}

