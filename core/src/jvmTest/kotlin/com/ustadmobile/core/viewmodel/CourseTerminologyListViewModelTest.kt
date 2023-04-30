package com.ustadmobile.core.viewmodel

import app.cash.turbine.test
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.paging.LoadParams
import com.ustadmobile.door.paging.LoadResult
import com.ustadmobile.lib.db.entities.CourseTerminology
import kotlinx.coroutines.flow.filter
import org.mockito.kotlin.*
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class CourseTerminologyListViewModelTest {

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        testViewModel<CourseTerminologyListViewModel> {
            viewModelFactory {
                CourseTerminologyListViewModel(di, savedStateHandle)
            }

            val terminologyRepo = spy(activeRepo.courseTerminologyDao)
            activeRepo.stub {
                on { courseTerminologyDao }.thenReturn(terminologyRepo)
            }

            viewModel.uiState
                .filter { it.terminologyList() !is EmptyPagingSource }
                .test {
                    awaitItem()
                    verify(terminologyRepo, timeout(5000)).findAllCourseTerminologyPagingSource()
                    cancelAndIgnoreRemainingEvents()
                }
        }
    }


    @Test
    fun givenPresenterCreatedInPickMode_whenOnClickEntryCalled_thenShouldReturnResult() {
        testViewModel<CourseTerminologyListViewModel> {

            val testEntity = CourseTerminology().apply {
                ctTitle = "Title"
                ctUid = activeDb.courseTerminologyDao.insertAsync(this)
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_RESULT_DEST_VIEWNAME] = "ClazzEdit"
                savedStateHandle[UstadView.ARG_RESULT_DEST_KEY] = "terminology"
                CourseTerminologyListViewModel(di, savedStateHandle)
            }

            viewModel.uiState.filter { it.terminologyList() !is EmptyPagingSource }
                .test(timeout = 5.seconds) {
                    val item = awaitItem()
                    val loadResult = item.terminologyList().load(
                        LoadParams.Refresh(0, 50, true))
                    val firstEntry = (loadResult as LoadResult.Page).data.first()
                    viewModel.onClickEntry(firstEntry)
                    cancelAndIgnoreRemainingEvents()
                }

            verify(navResultReturner, timeout(5000 * 1000)).sendResult(argWhere {
                (it.result as? CourseTerminology)?.ctTitle == testEntity.ctTitle
            })

        }
    }
}