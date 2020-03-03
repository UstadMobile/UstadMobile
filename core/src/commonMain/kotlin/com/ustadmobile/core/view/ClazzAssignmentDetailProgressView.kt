package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.PersonWithAssignmentMetrics

/**
 * Core View. Screen is for ClazzAssignmentDetail's View
 */
interface ClazzAssignmentDetailProgressView : UstadView {

    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param factory The factory to set to the view
     */
    fun setListProvider(factory: DataSource.Factory<Int, PersonWithAssignmentMetrics>)

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "ClazzAssignmentDetail"
    }

}

