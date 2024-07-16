package com.ustadmobile.core.viewmodel.clazz.detailoverview

import app.cash.turbine.test
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CoursePermission
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ClazzDetailOverviewViewModelTest : AbstractMainDispatcherTest() {

    @Test
    fun givenClazzExists_whenOnCreateCalled_thenClazzIsSetOnView() {
        testViewModel<ClazzDetailOverviewViewModel> {
            val user = setActiveUser(activeEndpoint)
            val testClazz = Clazz().apply {
                clazzName = "Test"
                clazzUid = activeDb.clazzDao().insert(this)
            }

            activeDb.coursePermissionDao().upsertAsync(
                CoursePermission(
                    cpToPersonUid = user.personUid,
                    cpPermissionsFlag = CoursePermission.TEACHER_DEFAULT_PERMISSIONS,
                    cpClazzUid = testClazz.clazzUid
                )
            )

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testClazz.clazzUid.toString()
                ClazzDetailOverviewViewModel(di, savedStateHandle, "Course")
            }

            viewModel.uiState.assertItemReceived(timeout = 5.seconds) {
                it.clazz?.clazzUid == testClazz.clazzUid
            }
        }
    }

    @Test
    fun givenClazzExists_whenOnClickEditcalled_thenShouldGoToEdit() {
        testViewModel<ClazzDetailOverviewViewModel> {
            val user = setActiveUser(activeEndpoint)

            val testClazz = Clazz().apply {
                clazzName = "Test"
                clazzUid = activeDb.clazzDao().insert(this)
            }

            activeDb.coursePermissionDao().upsertAsync(
                CoursePermission(
                    cpToPersonUid = user.personUid,
                    cpPermissionsFlag = CoursePermission.TEACHER_DEFAULT_PERMISSIONS,
                    cpClazzUid = testClazz.clazzUid
                )
            )

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testClazz.clazzUid.toString()
                ClazzDetailOverviewViewModel(di, savedStateHandle, "Course")
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                viewModel.appUiState.test(timeout = 5000.seconds) {
                    val editButtonClickableState = awaitItemWhere { it.fabState.visible }
                    editButtonClickableState.fabState.onClick()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.navCommandFlow.assertItemReceived(timeout = 5.seconds) {
                    val cmd = it as NavigateNavCommand
                    cmd.viewName == ClazzEditViewModel.DEST_NAME &&
                        cmd.args[UstadView.ARG_ENTITY_UID] == testClazz.clazzUid.toString()
                }

                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}