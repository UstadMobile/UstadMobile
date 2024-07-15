package com.ustadmobile.core.viewmodel.clazzassignment.edit

import app.cash.turbine.test
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.serialization.encodeToString
import org.kodein.di.direct
import org.kodein.di.instance
import org.mockito.kotlin.*
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ClazzAssignmentEditViewModelTest  : AbstractMainDispatcherTest() {

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldReturnResult() {
        testViewModel<ClazzAssignmentEditViewModel> {
            viewModelFactory {
                savedStateHandle[UstadView.ARG_RESULT_DEST_KEY] = "key"
                savedStateHandle[UstadView.ARG_RESULT_DEST_VIEWNAME] = ClazzEditViewModel.DEST_NAME

                ClazzAssignmentEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere { it.fieldsEnabled }
                viewModel.onAssignmentChanged(readyState.entity?.assignment?.shallowCopy {
                    caRequireTextSubmission = true
                    caClassCommentEnabled = true
                    caPrivateCommentsEnabled = false
                })

                viewModel.onCourseBlockChanged(
                    readyState.entity?.courseBlock?.copy(
                        cbTitle = "Assignment",
                        cbMaxPoints = 10f,
                    )
                )

                viewModel.onClickSave()
                cancelAndIgnoreRemainingEvents()
            }

            verify(navResultReturner, timeout(5000)).sendResult(argWhere {
                val result = it.result
                result is CourseBlockAndEditEntities &&
                    result.assignment?.caClassCommentEnabled == true &&
                    result.courseBlock.cbTitle == "Assignment"
            })
        }
    }

    @Test
    fun givenAssignmentEditedWithTextAndFileDisabled_whenClickedSave_showErrorMessage() {
        testViewModel<ClazzAssignmentEditViewModel> {
            viewModelFactory {
                ClazzAssignmentEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere { it.fieldsEnabled }

                viewModel.onAssignmentChanged(readyState.entity?.assignment?.shallowCopy {
                    caRequireTextSubmission = false
                    caRequireFileSubmission = false
                })

                viewModel.onClickSave()

                cancelAndIgnoreRemainingEvents()
            }

            viewModel.uiState.assertItemReceived { it.submissionRequiredError != null }
            verify(navResultReturner, times(0)).sendResult(any())
        }
    }

    @Test
    fun givenAssignmentEditedWithPointsSetToZero_whenClickedSave_showErrorMessage(){
        testViewModel<ClazzAssignmentEditViewModel> {
            viewModelFactory {
                ClazzAssignmentEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere { it.fieldsEnabled }
                viewModel.onCourseBlockChanged(
                    readyState.entity?.courseBlock?.copy(
                        cbMaxPoints = 0f
                    )
                )
                viewModel.onClickSave()
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.uiState.assertItemReceived(timeout = 5.seconds) {
                it.courseBlockEditUiState.caMaxPointsError == systemImpl.getString(MR.strings.field_required_prompt)
            }
            verify(navResultReturner, times(0)).sendResult(any())
        }
    }

    @Test
    fun givenAssignmentEditedWithDeadlineBeforeStartDate_whenClickedSave_showErrorMessage() {
        testViewModel<ClazzAssignmentEditViewModel> {
            viewModelFactory {
                ClazzAssignmentEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere { it.fieldsEnabled }
                viewModel.onCourseBlockChanged(
                    readyState.entity?.courseBlock?.copy(
                        cbHideUntilDate = systemTimeInMillis(),
                        cbDeadlineDate = systemTimeInMillis() - 100000
                    )
                )
                viewModel.onClickSave()

                cancelAndIgnoreRemainingEvents()
            }

            viewModel.uiState.assertItemReceived {
                it.courseBlockEditUiState.caDeadlineError == systemImpl.getString(MR.strings.end_is_before_start_error)
            }
        }
    }

    @Test
    fun givenAssignmentEditedWithGracePeriodBeforeDeadlineDate_whenClickedSave_showErrorMessage(){
        testViewModel<ClazzAssignmentEditViewModel> {
            viewModelFactory {
                ClazzAssignmentEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere { it.fieldsEnabled }

                viewModel.onCourseBlockChanged(
                    readyState.entity?.courseBlock?.copy(
                        cbDeadlineDate = systemTimeInMillis() + 100000,
                        cbGracePeriodDate = systemTimeInMillis()
                    )
                )

                viewModel.onClickSave()
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.uiState.assertItemReceived {
                it.courseBlockEditUiState.caGracePeriodError == systemImpl.getString(MR.strings.after_deadline_date_error)
            }
        }
    }

    @Test
    fun givenExistingAssignmentGroupUidWasChanged_whenSubmissionIsMadeBeforeSave_thenShowError() {

        testViewModel<ClazzAssignmentEditViewModel> {
            val testAssignment = ClazzAssignment().apply {
                caGroupUid = 0//individual submission
            }
            val clazzAssignmentUid = activeDb.clazzAssignmentDao().insert(testAssignment)
            testAssignment.caUid = clazzAssignmentUid
            val testBlock = CourseBlockAndEditEntities(
                courseBlock = CourseBlock().apply {
                    cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                    cbEntityUid = clazzAssignmentUid
                },
                assignment = testAssignment
            )

            viewModelFactory {
                savedStateHandle[ARG_ENTITY_JSON] = json.encodeToString(testBlock)

                ClazzAssignmentEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere { it.fieldsEnabled }

                activeDb.courseAssignmentSubmissionDao().insert(CourseAssignmentSubmission().apply {
                    casAssignmentUid = clazzAssignmentUid
                    casSubmitterUid = 1
                })

                viewModel.onAssignmentChanged(readyState.entity?.assignment?.shallowCopy {
                    caGroupUid = 5
                })

                viewModel.onClickSave()
                cancelAndIgnoreRemainingEvents()
            }

            val mockSnackBarDispatcher = di.direct.instance<SnackBarDispatcher>()
            verify(mockSnackBarDispatcher, timeout(5000)).showSnackBar(argWhere {
                it.message.startsWith(systemImpl.getString(MR.strings.error))
            })
        }
    }

    @Test
    fun givenExistingAssignmentMarkingTypeWasChanged_whenSubmissionMarkedBeforeSave_thenShowError() {
        testViewModel<ClazzAssignmentEditViewModel> {
            val testAssignment = ClazzAssignment().apply {
                caMarkingType = ClazzAssignment.MARKED_BY_COURSE_LEADER
                caGroupUid = 0//individual submission
            }
            val clazzAssignmentUid = activeDb.clazzAssignmentDao().insert(testAssignment)
            testAssignment.caUid = clazzAssignmentUid
            val testBlock = CourseBlockAndEditEntities(
                courseBlock = CourseBlock().apply {
                    cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                    cbEntityUid = clazzAssignmentUid
                },
                assignment = testAssignment
            )

            viewModelFactory {
                savedStateHandle[ARG_ENTITY_JSON] = json.encodeToString(testBlock)
                ClazzAssignmentEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere { it.fieldsEnabled }

                activeDb.courseAssignmentSubmissionDao().insert(CourseAssignmentSubmission().apply {
                    casAssignmentUid = clazzAssignmentUid
                    casSubmitterUid = 1
                })

                viewModel.onAssignmentChanged(readyState.entity?.assignment?.shallowCopy {
                    caMarkingType = ClazzAssignment.MARKED_BY_PEERS
                })

                viewModel.onClickSave()
                cancelAndIgnoreRemainingEvents()
            }

            val mockSnackBarDispatcher = di.direct.instance<SnackBarDispatcher>()
            verify(mockSnackBarDispatcher, timeout(5000)).showSnackBar(argWhere {
                it.message.startsWith(systemImpl.getString(MR.strings.error))
            })
        }
    }


}