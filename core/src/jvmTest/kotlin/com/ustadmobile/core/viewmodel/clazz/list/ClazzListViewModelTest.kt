package com.ustadmobile.core.viewmodel.clazz.list

import app.cash.turbine.test
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.Clazz
import kotlinx.coroutines.flow.filter
import org.mockito.kotlin.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ClazzListViewModelTest : AbstractMainDispatcherTest()  {

    @Test
    fun givenViewModelNotYetCreated_whenInitialized_thenShouldQueryDatabase() {
        testViewModel<ClazzListViewModel> {
            viewModelFactory {
                ClazzListViewModel(di, savedStateHandle)
            }

            val clazzRepo = spy(activeRepoWithFallback.clazzDao())
            activeRepoWithFallback.stub {
                on { clazzDao() }.thenReturn(clazzRepo)
            }

            val accountPersonUid = accountManager.currentAccount.personUid

            viewModel.uiState
                .filter { it.clazzList() !is EmptyPagingSource }
                .test {
                    awaitItem()
                    verify(
                        clazzRepo, timeout(5000).atLeastOnce()
                    ).findClazzesWithPermission(
                        searchQuery = eq("%"),
                        accountPersonUid = eq(accountPersonUid),
                        excludeSelectedClazzList = eq(listOf()),
                        sortOrder = any(),
                        filter = any(),
                        currentTime = any(),
                        permission = any(),
                    )

                    cancelAndIgnoreRemainingEvents()
                }
        }
    }

    @Test
    fun givenViewModelInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {
        testViewModel<ClazzListViewModel> {
            viewModelFactory {
                ClazzListViewModel(di, savedStateHandle)
            }

            val testEntity = Clazz().apply {
                clazzUid = activeDb.clazzDao().insert(this)
            }

            val activeUser = setActiveUser(activeEndpoint)
            activeRepoWithFallback.grantScopedPermission(activeUser, Long.MAX_VALUE,
                Clazz.TABLE_ID, testEntity.clazzUid)


            viewModel.uiState.filter { it.clazzList() !is EmptyPagingSource }
                .test {
                    viewModel.onClickEntry(testEntity)
                    cancelAndIgnoreRemainingEvents()
                }

            viewModel.navCommandFlow.test {
                val navCommand = awaitItem() as NavigateNavCommand
                assertEquals( ClazzDetailViewModel.DEST_NAME, navCommand.viewName)
                assertEquals(testEntity.clazzUid.toString(),
                    navCommand.args[UstadView.ARG_ENTITY_UID])
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

}