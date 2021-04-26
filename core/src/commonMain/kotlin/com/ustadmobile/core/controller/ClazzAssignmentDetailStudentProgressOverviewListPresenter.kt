package com.ustadmobile.core.controller

import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class ClazzAssignmentDetailStudentProgressOverviewListPresenter(context: Any, arguments: Map<String, String>, view: ClazzAssignmentDetailStudentProgressOverviewListView,
                                                                di: DI, lifecycleOwner: DoorLifecycleOwner,
                                                                private val clazzAssignmentWithMetricsItemListener: DefaultClazzAssignmentDetailStudentProgressOverviewListItemListener = DefaultClazzAssignmentDetailStudentProgressOverviewListItemListener(view, ListViewMode.BROWSER, di.direct.instance(), context))
    : UstadListPresenter<ClazzAssignmentDetailStudentProgressOverviewListView, ClazzAssignmentWithMetrics>(context, arguments, view, di, lifecycleOwner), ClazzAssignmentDetailStudentProgressListItemListener by clazzAssignmentWithMetricsItemListener {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        clazzAssignmentWithMetricsItemListener.listViewMode = mListMode
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    private fun updateListOnView() {
        /* TODO: Update the list on the view from the appropriate DAO query, e.g.
        view.list = when(sortOrder) {
            SortOrder.ORDER_NAME_ASC -> repo.daoName.findAllActiveClazzesSortByNameAsc(
                    searchQuery, loggedInPersonUid)
            SortOrder.ORDER_NAME_DSC -> repo.daoName.findAllActiveClazzesSortByNameDesc(
                    searchQuery, loggedInPersonUid)
        }
        */
    }

    override fun handleClickCreateNewFab() {

    }


}