package com.ustadmobile.core.viewmodel.clazz.detail

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceViewModel
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ClazzDetailViewModelTest {

    val endpoint = Endpoint("http://test.com/")

    @Test
    fun givenUserHasAttendancePermissions_whenOnCreateCalled_thenShouldMakeAttendanceTabVisible() {
        testViewModel<ClazzDetailViewModel> {
            val activeUser = setActiveUser(endpoint)

            val testClazz = Clazz().apply {
                clazzName = "Test Course"
                clazzUid = activeDb.clazzDao.insertAsync(this)
            }

            activeDb.grantScopedPermission(activeUser,
                Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT, Clazz.TABLE_ID, testClazz.clazzUid)

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testClazz.clazzUid.toString()
                ClazzDetailViewModel(di, savedStateHandle)
            }

            viewModel.uiState.assertItemReceived(timeout = 5.seconds) {state ->
                state.tabs.any {
                    it.viewName == ClazzLogListAttendanceViewModel.DEST_NAME &&
                        it.args[UstadView.ARG_CLAZZUID] == testClazz.clazzUid.toString()
                }
            }
        }
    }

    @Test
    fun givenUserDoesnotHaveAttendancePermission_whenOnCreateCalled_thenAttendanceTabShouldNotBeVisible() {
        testViewModel<ClazzDetailViewModel> {
            val activeUser = setActiveUser(endpoint)

            val testClazz = Clazz().apply {
                clazzName = "Test Course"
                clazzUid = activeDb.clazzDao.insertAsync(this)
            }

            activeDb.grantScopedPermission(activeUser,
                Role.ROLE_CLAZZ_STUDENT_PERMISSIONS_DEFAULT, Clazz.TABLE_ID, testClazz.clazzUid)

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testClazz.clazzUid.toString()
                ClazzDetailViewModel(di, savedStateHandle)
            }

            viewModel.uiState.assertItemReceived(timeout = 5.seconds) {state ->
                state.tabs.isNotEmpty() &&
                !state.tabs.any {
                    it.viewName == ClazzLogListAttendanceViewModel.DEST_NAME &&
                        it.args[UstadView.ARG_CLAZZUID] == testClazz.clazzUid.toString()
                }
            }
        }
    }


}