package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.CourseBlockWithEntity


interface ClazzAssignmentEditView: UstadEditView<CourseBlockWithEntity> {

    companion object {

        const val VIEW_NAME = "CourseAssignmentEditView"

        const val TERMINOLOGY_ID = "clazzTerminologyId"

    }

}