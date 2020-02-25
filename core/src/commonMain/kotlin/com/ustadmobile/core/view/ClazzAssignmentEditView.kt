package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzAssignmentContentEntryJoinWithContentEntry

/**
 * Core View. Screen is for ClazzAssignmentEdit's View
 */
interface ClazzAssignmentEditView : UstadView {

    var contentEntryList : DoorMutableLiveData<List<ClazzAssignmentContentEntryJoinWithContentEntry>>?

    fun finish()

    fun setClazzAssignment(clazzAssignment: ClazzAssignment)

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "ClazzAssignmentEdit"

        const val GRADING_NONE = 0
        const val GRADING_NUMERICAL = 1
        const val GRADING_LETTERS = 2
    }

}

