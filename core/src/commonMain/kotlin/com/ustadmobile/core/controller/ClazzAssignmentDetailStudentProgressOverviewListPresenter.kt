package com.ustadmobile.core.controller

import com.ustadmobile.core.controller.ContentEntryDetailAttemptsListPresenter.Companion.SORT_OPTIONS
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.launch
import org.kodein.di.DI

class ClazzAssignmentDetailStudentProgressOverviewListPresenter(context: Any, arguments: Map<String, String>,
                                                                view: ClazzAssignmentDetailStudentProgressOverviewListView,
                                                                di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ClazzAssignmentDetailStudentProgressOverviewListView,
        PersonWithAttemptsSummary>(context, arguments, view, di, lifecycleOwner), AttemptListListener,
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
        presenterScope.launch {
            val entity = repo.clazzAssignmentDao.findByUidAsync(clazzAssignmentUid)
            view.showMarked = entity?.caRequireFileSubmission ?: false
            repo.clazzAssignmentRollUpDao.cacheBestStatements(
                    clazzUid, clazzAssignmentUid,
                    0)
        }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    private fun updateListOnView() {
        view.progressSummary = repo.clazzAssignmentDao.getStudentsProgressOnAssignment(
                clazzUid,
                mLoggedInPersonUid, clazzAssignmentUid,
                Role.PERMISSION_ASSIGNMENT_VIEWSTUDENTPROGRESS)

        view.list = repo.clazzAssignmentDao.getAttemptSummaryForStudentsInAssignment(
                clazzAssignmentUid, clazzUid,
                mLoggedInPersonUid, searchText.toQueryLikeParam(),
                selectedSortOption?.flag ?: StatementDao.SORT_FIRST_NAME_ASC)

    }

    override fun handleClickCreateNewFab() {

    }

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        updateListOnView()
    }

    override fun onSearchSubmitted(text: String?) {
        searchText = text
        updateListOnView()
    }

    override fun onClickPersonWithStatementDisplay(personWithAttemptsSummary: PersonWithAttemptsSummary) {
        systemImpl.go(ClazzAssignmentDetailStudentProgressView.VIEW_NAME,
                mapOf(UstadView.ARG_PERSON_UID to personWithAttemptsSummary.personUid.toString(),
                UstadView.ARG_CLAZZ_ASSIGNMENT_UID to clazzAssignmentUid.toString(),
                UstadView.ARG_CLAZZUID to clazzUid.toString()), context)
    }


}