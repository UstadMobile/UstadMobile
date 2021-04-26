package com.ustadmobile.core.controller

import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.SessionListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class ClazzAssignmentDetailStudentProgressOverviewListPresenter(context: Any, arguments: Map<String, String>, view: ClazzAssignmentDetailStudentProgressOverviewListView,
                                                                di: DI, lifecycleOwner: DoorLifecycleOwner,
                                                                private val clazzAssignmentWithMetricsItemListener: DefaultClazzAssignmentDetailStudentProgressOverviewListItemListener = DefaultClazzAssignmentDetailStudentProgressOverviewListItemListener(view, ListViewMode.BROWSER, di.direct.instance(), context))
    : UstadListPresenter<ClazzAssignmentDetailStudentProgressOverviewListView, PersonWithAttemptsSummary>(context, arguments, view, di, lifecycleOwner), ClazzAssignmentDetailStudentProgressListItemListener by clazzAssignmentWithMetricsItemListener {

    private var filterByClazzAssignmentUid: Long = -1

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        filterByClazzAssignmentUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: -1
        clazzAssignmentWithMetricsItemListener.listViewMode = mListMode
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    private fun updateListOnView() {

        // TODO get clazz assignment metrics

        // TODO get all attempts for each person for all entries in assignment

    }

    override fun handleClickCreateNewFab() {

    }

    fun onClickPersonWithAttemptsSummary(personWithAttemptsSummary: PersonWithAttemptsSummary) {
        // TODO go to studentProgress with person and clazzAssignment selected
    }



}