package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzAssignment

/**
 * Core View. Screen is for ClazzAssignmentDetail's View
 */
interface ClazzAssignmentDetailView : UstadView {

    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param factory The factory to set to the view
     */
    fun setListProvider(factory: DataSource.Factory<Int, ClazzAssignment>)

    fun setClazzAssignment(clazzAssignment: ClazzAssignment)

    fun setupTabs(tabs : List<String>)

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "ClazzAssignmentDetail"
    }

}

