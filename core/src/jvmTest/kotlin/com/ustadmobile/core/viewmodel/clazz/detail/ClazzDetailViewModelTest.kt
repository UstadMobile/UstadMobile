package com.ustadmobile.core.viewmodel.clazz.detail

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceViewModel
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CoursePermission
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ClazzDetailViewModelTest : AbstractMainDispatcherTest()  {

    val endpoint = Endpoint("http://test.com/")

    @Test
    fun givenUserHasAttendancePermissions_whenOnCreateCalled_thenShouldMakeAttendanceTabVisible() {
        testViewModel<ClazzDetailViewModel> {
            val activeUser = setActiveUser(endpoint)

            val testClazz = Clazz().apply {
                clazzName = "Test Course"
                clazzUid = activeDb.clazzDao().insertAsync(this)
            }

            activeDb.coursePermissionDao().upsertAsync(
                CoursePermission(
                    cpToPersonUid = activeUser.personUid,
                    cpPermissionsFlag = CoursePermission.TEACHER_DEFAULT_PERMISSIONS,
                    cpClazzUid = testClazz.clazzUid,
                )
            )

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testClazz.clazzUid.toString()
                ClazzDetailViewModel(di, savedStateHandle)
            }

            viewModel.uiState.assertItemReceived(timeout = 500.seconds) {state ->
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
                clazzUid = activeDb.clazzDao().insertAsync(this)
            }

            activeDb.coursePermissionDao().upsertAsync(
                CoursePermission(
                    cpToPersonUid = activeUser.personUid,
                    cpPermissionsFlag = CoursePermission.STUDENT_DEFAULT_PERMISSIONS,
                    cpClazzUid = testClazz.clazzUid,
                )
            )

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