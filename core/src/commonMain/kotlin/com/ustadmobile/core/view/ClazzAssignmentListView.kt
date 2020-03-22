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

    /**
     * Edit visibility is the ability to add a new assignment, edit an assignment.
     * We show/hide the + Assignment floating action button on the list view.
     * We also show/hide the ability to edit a story if such an option exists in the
     * Assignment list recycler view.
     */
    fun setEditVisibility(visible: Boolean)

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "ClazzAssignmentList"
    }

}

