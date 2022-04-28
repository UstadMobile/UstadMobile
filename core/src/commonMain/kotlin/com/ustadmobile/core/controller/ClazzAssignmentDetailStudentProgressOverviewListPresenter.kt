package com.ustadmobile.core.controller

import com.ustadmobile.core.controller.ContentEntryDetailAttemptsListPresenter.Companion.SORT_OPTIONS
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.PersonGroupAssignmentSummary
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class ClazzAssignmentDetailStudentProgressOverviewListPresenter(context: Any, arguments: Map<String, String>,
                                                                view: ClazzAssignmentDetailStudentProgressOverviewListView,
                                                                di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ClazzAssignmentDetailStudentProgressOverviewListView,
        PersonGroupAssignmentSummary>(context, arguments, view, di, lifecycleOwner), SubmissionSummaryListener,
        OnSortOptionSelected, OnSearchSubmitted{

    private var clazzUid: Long = -1
    private var clazzAssignmentUid: Long = -1
    var searchText: String? = null

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        clazzAssignmentUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: -1
        clazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: -1
        selectedSortOption = SORT_OPTIONS[0]
        mLoggedInPersonUid = accountManager.activeAccount.personUid
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    private fun updateListOnView() {
        view.progressSummary = repo.clazzAssignmentDao.getProgressSummaryForAssignment(
                clazzAssignmentUid, clazzUid, "")

        view.list = repo.clazzAssignmentDao.getSubmitterListForAssignment(
            clazzAssignmentUid, clazzUid,
            systemImpl.getString(MessageID.group_number, context).replace("%1\$s",""),
            searchText.toQueryLikeParam())

    }

    override fun handleClickCreateNewFab() {}

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {}

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        updateListOnView()
    }

    override fun onSearchSubmitted(text: String?) {
        searchText = text
        updateListOnView()
    }

    override fun onClickPerson(personWithAttemptsSummary: PersonGroupAssignmentSummary) {
        systemImpl.go(ClazzAssignmentDetailStudentProgressView.VIEW_NAME,
                mapOf(UstadView.ARG_SUBMITER_UID to personWithAttemptsSummary.submitterUid.toString(),
                UstadView.ARG_CLAZZ_ASSIGNMENT_UID to clazzAssignmentUid.toString(),
                UstadView.ARG_CLAZZUID to clazzUid.toString()), context)
    }


}