package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentWithMetricsDetailView
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics


class DefaultClazzAssignmentDetailStudentProgressOverviewListItemListener(var view: ClazzAssignmentDetailStudentProgressOverviewListView?,
                                                                          var listViewMode: ListViewMode,
                                                                          val systemImpl: UstadMobileSystemImpl,
                                                                          val context: Any): ClazzAssignmentDetailStudentProgressListItemListener {

    override fun onClickClazzAssignmentWithMetrics(clazzAssignmentWithMetrics: ClazzAssignmentWithMetrics) {
        if(listViewMode == ListViewMode.BROWSER) {
            systemImpl.go(ClazzAssignmentWithMetricsDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to clazzAssignmentWithMetrics.clazzAssignmentWithMetricsUid.toString()), context)
        }else {
            view?.finishWithResult(listOf(clazzAssignmentWithMetrics))
        }
    }
}
