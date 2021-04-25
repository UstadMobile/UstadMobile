package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AssignmentDetailView
import com.ustadmobile.core.view.ClazzAssignmentListView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzAssignment


class DefaultClazzAssignmentListItemListener(var view: ClazzAssignmentListView?,
                                             var listViewMode: ListViewMode,
                                             val systemImpl: UstadMobileSystemImpl,
                                             val context: Any): ClazzAssignmentListItemListener {

    override fun onClickAssignment(clazzAssignment: ClazzAssignment) {
        if(listViewMode == ListViewMode.BROWSER) {
            systemImpl.go(AssignmentDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to clazzAssignment.assignmentUid.toString()), context)
        }else {
            view?.finishWithResult(listOf(clazzAssignment))
        }
    }
}
