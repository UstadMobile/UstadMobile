package com.ustadmobile.core.viewmodel.clazz.detailoverview

import app.cash.turbine.test
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ClazzDetailOverviewViewModelTest {

    @Test
    fun givenClazzExists_whenOnCreateCalled_thenClazzIsSetOnView() {
        testViewModel<ClazzDetailOverviewViewModel> {
            val testClazz = Clazz().apply {
                clazzName = "Test"
                clazzUid = activeDb.clazzDao.insert(this)
            }

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
                clazzUid = activeDb.clazzDao.insert(this)
            }

            activeDb.grantScopedPermission(user, Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT,
                Clazz.TABLE_ID, testClazz.clazzUid)

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testClazz.clazzUid.toString()
                ClazzDetailOverviewViewModel(di, savedStateHandle, "Course")
            }

            viewModel.uiState.test(timeout = 30.seconds) {
                viewModel.appUiState.test(timeout = 5000.seconds) {
                    val editButtonClickableState = awaitItemWhere { it.fabState.visible }
                    editButtonClickableState.fabState.onClick()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.navCommandFlow.assertItemReceived(timeout = 5.seconds) {
                    val cmd = it as NavigateNavCommand
                    cmd.viewName == ClazzEdit2View.VIEW_NAME &&
                        cmd.args[UstadView.ARG_ENTITY_UID] == testClazz.clazzUid.toString()
                }

                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}