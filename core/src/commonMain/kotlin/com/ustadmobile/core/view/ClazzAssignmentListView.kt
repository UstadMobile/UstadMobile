package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics

/**
 * Core View. Screen is for ClazzAssignmentList's View
 */
interface ClazzAssignmentListView : UstadView {

    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param factory The factory to set to the view
     */
    fun setListProvider(factory: DataSource.Factory<Int, ClazzAssignmentWithMetrics>)

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "ClazzAssignmentList"
    }

}

