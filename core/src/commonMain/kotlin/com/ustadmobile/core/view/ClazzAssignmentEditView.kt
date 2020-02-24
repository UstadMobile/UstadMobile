package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithMetrics

/**
 * Core View. Screen is for ClazzAssignmentEdit's View
 */
interface ClazzAssignmentEditView : UstadView {


    fun finish()

    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param factory The factory to set to the view
     */
    fun setListProvider(factory: DataSource.Factory<Int, ContentEntryWithMetrics>)

    fun setClazzAssignment(clazzAssignment: ClazzAssignment)

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "ClazzAssignmentEdit"

        const val GRADING_NONE = 0
        const val GRADING_NUMERICAL = 1
        const val GRADING_LETTERS = 2
    }

}

