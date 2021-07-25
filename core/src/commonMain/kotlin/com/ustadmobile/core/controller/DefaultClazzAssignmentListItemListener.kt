package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics


class DefaultClazzAssignmentListItemListener(var view: ClazzAssignmentListView?,
                                             var listViewMode: ListViewMode,
                                             val systemImpl: UstadMobileSystemImpl,
                                             val context: Any): ClazzAssignmentListItemListener {

    override fun onClickAssignment(clazzAssignment: ClazzAssignmentWithMetrics) {
        if(listViewMode == ListViewMode.BROWSER) {
            systemImpl.go(ClazzAssignmentDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to clazzAssignment.caUid.toString()), context)
        }else {
            view?.finishWithResult(listOf(clazzAssignment))
        }
    }
}
