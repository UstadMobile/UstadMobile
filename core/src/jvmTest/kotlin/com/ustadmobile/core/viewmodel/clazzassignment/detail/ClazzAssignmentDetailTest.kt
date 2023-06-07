package com.ustadmobile.core.viewmodel.clazzassignment.detail

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabViewModel
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.Role
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ClazzAssignmentDetailTest {

    val endpoint = Endpoint("http://test.com/")

    @Test
    fun givenUserHasClazzAssignmentProgressSubmission_whenShown_shouldShowOverviewAndStudentSubmissionTabs() {
        testViewModel<ClazzAssignmentDetailViewModel> {
            val activeUser = setActiveUser(endpoint)

            val testClazz = Clazz().apply {
                clazzUid = activeDb.clazzDao.insert(this)
            }

            val testAssignment = ClazzAssignment().apply {
                caClazzUid = testClazz.clazzUid
                caUid = activeDb.clazzAssignmentDao.insert(this)
            }

            viewModelFactory {
                savedStateHandle[ARG_ENTITY_UID] = testAssignment.caUid.toString()
                ClazzAssignmentDetailViewModel(di, savedStateHandle)
            }

            activeDb.grantScopedPermission(activeUser,
                Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT, Clazz.TABLE_ID,
                testAssignment.caClazzUid)

            viewModel.uiState.assertItemReceived(timeout = 5.seconds) {
                it.tabs.last().viewName == ClazzAssignmentDetailSubmissionsTabViewModel.DEST_NAME
            }
        }
    }

    @Test
    fun givenUserDoesNotHaveClazzAssignmentProgressSubmission_whenShown_shouldShowOnlyOverviewTabs() {
        testViewModel<ClazzAssignmentDetailViewModel> {
            val activeUser = setActiveUser(endpoint)

            val testClazz = Clazz().apply {
                clazzUid = activeDb.clazzDao.insert(this)
            }

            val testAssignment = ClazzAssignment().apply {
                caClazzUid = testClazz.clazzUid
                caUid = activeDb.clazzAssignmentDao.insert(this)
            }

            viewModelFactory {
                savedStateHandle[ARG_ENTITY_UID] = testAssignment.caUid.toString()
                ClazzAssignmentDetailViewModel(di, savedStateHandle)
            }

            viewModel.uiState.assertItemReceived(timeout = 5.seconds) {
                it.tabs.first().viewName == ClazzAssignmentDetailOverviewView.VIEW_NAME &&
                    it.tabs.size == 1
            }
        }
    }

}