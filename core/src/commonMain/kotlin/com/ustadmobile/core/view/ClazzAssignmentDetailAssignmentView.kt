package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntryWithMetrics

/**
 * Core View. Screen is for ClazzAssignmentDetailAssignment's View
 */
interface ClazzAssignmentDetailAssignmentView : UstadView {

    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param factory The factory to set to the view
     */
    fun setListProvider(factory: DataSource.Factory<Int, ContentEntryWithMetrics>)

    fun setEditVisibility(visible: Boolean)

    fun setClazzAssignment(clazzAssignment: ClazzAssignment)

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "ClazzAssignmentDetailAssignment"
    }

}

