package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.ClazzAssignmentListView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI
import org.kodein.di.DIAware


class DefaultClazzAssignmentListItemListener(var view: ClazzAssignmentListView?,
                                             var listViewMode: ListViewMode,
                                             val systemImpl: UstadMobileSystemImpl,
                                             val context: Any,
                                             override val di: DI,
                                             var presenter: ClazzAssignmentListPresenter? = null): ClazzAssignmentListItemListener, DIAware {

    override fun onClickAssignment(clazzAssignment: ClazzAssignmentWithMetrics) {
        if(listViewMode == ListViewMode.BROWSER) {
            systemImpl.go(ClazzAssignmentDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to clazzAssignment.caUid.toString()), context)
        }else {
            presenter?.finishWithResult(safeStringify(di, ListSerializer(ClazzAssignmentWithMetrics.serializer()), listOf(clazzAssignment)))
        }
    }
}
