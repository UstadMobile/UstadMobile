package com.ustadmobile.core.controller

import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI

class ClazzAssignmentDetailStudentProgressOverviewListPresenter(context: Any, arguments: Map<String, String>,
                                                                view: ClazzAssignmentDetailStudentProgressOverviewListView,
                                                                di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ClazzAssignmentDetailStudentProgressOverviewListView,
        PersonWithAttemptsSummary>(context, arguments, view, di, lifecycleOwner), AttemptListListener {

    private var filterByClazzAssignmentUid: Long = -1
    private var clazzAssignment: ClazzAssignment? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        filterByClazzAssignmentUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: -1
        GlobalScope.launch(doorMainDispatcher()) {
            clazzAssignment = withTimeoutOrNull(2000) {
                db.clazzAssignmentDao.findByUidAsync(filterByClazzAssignmentUid)
            }
            mLoggedInPersonUid = accountManager.activeAccount.personUid
            updateListOnView()
        }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    private fun updateListOnView() {
        view.studentProgress = repo.clazzAssignmentDao.getStudentsProgressOnAssignment(
                clazzAssignment?.caClazzUid?: 0,
                mLoggedInPersonUid, clazzAssignment?.caUid ?: filterByClazzAssignmentUid,
                Role.PERMISSION_ASSIGNMENT_VIEWSTUDENTPROGRESS)


    }

    override fun handleClickCreateNewFab() {

    }

    override fun onClickPersonWithStatementDisplay(personWithAttemptsSummary: PersonWithAttemptsSummary) {
        systemImpl.go(ClazzAssignmentDetailStudentProgressView.VIEW_NAME,
                mapOf(UstadView.ARG_PERSON_UID to personWithAttemptsSummary.personUid.toString(),
                UstadView.ARG_CLAZZ_ASSIGNMENT_UID to filterByClazzAssignmentUid.toString()), context)
    }


}