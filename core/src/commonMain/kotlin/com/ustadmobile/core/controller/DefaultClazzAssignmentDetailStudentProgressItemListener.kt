package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntryWithAttemptsSummary


class DefaultClazzAssignmentDetailStudentProgressItemListener(var view: ClazzAssignmentDetailStudentProgressView?,
                                   var listViewMode: ListViewMode,
                                   val systemImpl: UstadMobileSystemImpl,
                                   val context: Any): ClazzAssignmentDetailStudentProgressItemListener {

    override fun onClickClazzAssignment(clazzAssignment: ContentEntryWithAttemptsSummary) {
        if(listViewMode == ListViewMode.BROWSER) {
            systemImpl.go(ClazzAssignmentDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to clazzAssignment.contentEntryUid.toString()), context)
        }else {
           // view?.finishWithResult(listOf(clazzAssignment))
        }
    }
}
