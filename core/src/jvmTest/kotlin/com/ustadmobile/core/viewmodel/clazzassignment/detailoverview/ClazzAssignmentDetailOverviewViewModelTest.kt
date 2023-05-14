package com.ustadmobile.core.viewmodel.clazzassignment.detailoverview

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.assignment.submitassignment.AssignmentDeadlinePassedException
import com.ustadmobile.core.domain.assignment.submitassignment.SubmitAssignmentUseCase
import com.ustadmobile.core.test.viewmodeltest.ViewModelTestBuilder
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import org.mockito.kotlin.any
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verifyBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ClazzAssignmentDetailOverviewViewModelTest {

    val endpoint = Endpoint("http://test.com/")

    class AssignmentDetailOverviewTestContext(
        val clazz: Clazz,
        val assignment: ClazzAssignment,
        val courseBlock: CourseBlock,
        val person: Person
    )

    private fun testClazzAssignmentDetailOverviewViewModel(
        activeUserRole: Int,
        assignment: ClazzAssignment = ClazzAssignment(),
        courseBlock: CourseBlock = CourseBlock(),
        block: suspend ViewModelTestBuilder<ClazzAssignmentDetailOverviewViewModel>.(AssignmentDetailOverviewTestContext) -> Unit
    ) {
        testViewModel {
            val activePerson = setActiveUser(endpoint)
            val context = activeDb.withDoorTransactionAsync {
                val clazzUid = activeDb.doorPrimaryKeyManager.nextId(Clazz.TABLE_ID)
                val clazz = Clazz().apply {
                    this.clazzUid = clazzUid
                    clazzName = "Test Course"
                }
                activeDb.createNewClazzAndGroups(clazz, systemImpl, emptyMap())


                activeDb.takeIf { activeUserRole != 0 }?.enrolPersonIntoClazzAtLocalTimezone(
                    activePerson, clazzUid, activeUserRole
                )

                assignment.caClazzUid = clazzUid
                assignment.caUid = activeDb.clazzAssignmentDao.insertAsync(assignment)

                courseBlock.cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                courseBlock.cbEntityUid = assignment.caUid
                courseBlock.cbEntityUid = activeDb.courseBlockDao.insertAsync(courseBlock)

                AssignmentDetailOverviewTestContext(
                    clazz, assignment, courseBlock,
                    activePerson
                )
            }

            block(context)
        }
    }

    @Test
    fun givenStudentWithNoSubmissionGivenYet_whenShown_thenShowNoSubmissionStatusAndAddFileTextWithComments() {
        testClazzAssignmentDetailOverviewViewModel(
            activeUserRole = ClazzEnrolment.ROLE_STUDENT,
            assignment = ClazzAssignment().apply {
                caRequireFileSubmission = true
                caRequireTextSubmission = true
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
            }
        ) { testContext ->
            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.assignment != null && it.courseBlock != null && it.latestSubmission != null
                }
                assertTrue(readyState.activeUserIsSubmitter)
                assertTrue(readyState.activeUserCanSubmit)
                assertTrue(readyState.addFileSubmissionVisible)
                assertTrue(readyState.canEditSubmissionText)
                assertEquals(0L, readyState.latestSubmission?.casTimestamp)
                assertEquals(0, readyState.latestSubmissionAttachments?.size)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }


    @Test
    fun givenStudentWithSubmissionNotMarkedAndNoMultipleSubmission_whenShown_thenDontShowAddFileTextWithSubmittedStatus() {
        testClazzAssignmentDetailOverviewViewModel(
            activeUserRole = ClazzEnrolment.ROLE_STUDENT,
            assignment = ClazzAssignment().apply {
                caRequireFileSubmission = true
                caRequireTextSubmission = true
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
            }
        ) { testContext ->

            //insert a submission for this student
            activeDb.courseAssignmentSubmissionDao.insert(CourseAssignmentSubmission().apply {
                casSubmitterPersonUid = testContext.person.personUid
                casSubmitterUid = testContext.person.personUid
                casText = "I can has cheezburger"
            })

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.assignment != null && it.courseBlock != null && it.latestSubmissionAttachments != null
                }
                assertTrue(readyState.activeUserIsSubmitter)
                assertFalse(readyState.addFileSubmissionVisible)
                assertFalse(readyState.canEditSubmissionText)
                assertTrue((readyState.latestSubmission?.casTimestamp ?: 0) > 0)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenStudentWithSubmissionNotMarkedAndMultipleSubmissionPolicy_whenShown_thenShowAddFileTextWithSubmittedStatus() {
        testClazzAssignmentDetailOverviewViewModel(
            activeUserRole = ClazzEnrolment.ROLE_STUDENT,
            assignment = ClazzAssignment().apply {
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_MULTIPLE_ALLOWED
                caRequireTextSubmission = true
                caRequireTextSubmission = true
            }
        ) { testContext ->
            activeDb.courseAssignmentSubmissionDao.insertAsync(CourseAssignmentSubmission().apply {
                casSubmitterUid = testContext.person.personUid
                casSubmitterPersonUid = testContext.person.personUid
                casText = "Test text"
            })

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.assignment != null && it.courseBlock != null && it.latestSubmissionAttachments != null
                }
                assertTrue(readyState.activeUserIsSubmitter)
                assertTrue(readyState.addFileSubmissionVisible)
                assertTrue(readyState.canEditSubmissionText)
                assertTrue((readyState.latestSubmission?.casTimestamp ?: 0) > 0)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }


    @Test
    fun givenValidAssignment_whenSubmitAssignmentUseCaseThrowsException_thenShouldShowErrorMessage() {
        testClazzAssignmentDetailOverviewViewModel(
            activeUserRole = ClazzEnrolment.ROLE_STUDENT,
            assignment = ClazzAssignment().apply {
                caRequireFileSubmission = true
                caRequireTextSubmission = true
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
            }
        ) { testContext ->
            val exceptionMessage = "Deadline passed"
            val mockSubmissionUseCase = mock<SubmitAssignmentUseCase> {
                onBlocking { invoke(any(), any(), any(), any(), any()) }
                    .thenThrow(AssignmentDeadlinePassedException(exceptionMessage))
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                ClazzAssignmentDetailOverviewViewModel(
                    di, savedStateHandle,
                    submitAssignmentUseCase = mockSubmissionUseCase
                )
            }

            viewModel.uiState.test(timeout = 500.seconds) {
                awaitItemWhere {
                    it.assignment != null && it.courseBlock != null && it.latestSubmission != null
                }

                viewModel.onChangeSubmissionText("I can has cheezburger")
                viewModel.onClickSubmit()
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.uiState.assertItemReceived(timeout = 500.seconds) {
                it.submissionError == exceptionMessage
            }
        }
    }

    @Test
    fun givenStudentWithSubmissionMarkedAndSingleSubmissionPolicy_whenShown_thenShowMarkedStatusWithNoAddTextFileButtons() {
        testClazzAssignmentDetailOverviewViewModel(
            activeUserRole = ClazzEnrolment.ROLE_STUDENT,
            assignment = ClazzAssignment().apply {
                caRequireFileSubmission = true
                caRequireTextSubmission = true
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
            }
        ) { testContext ->
            //insert submission
            activeDb.courseAssignmentSubmissionDao.insert(CourseAssignmentSubmission().apply {
                casSubmitterPersonUid = testContext.person.personUid
                casSubmitterUid = testContext.person.personUid
                casText = "I can has cheezburger"
            })

            //insert mark
            activeDb.courseAssignmentMarkDao.insert(CourseAssignmentMark().apply {
                camMark = 5f
                camSubmitterUid = testContext.person.personUid
                camAssignmentUid = testContext.assignment.caUid
            })

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val stateWithMark = awaitItemWhere {
                    it.submissionMark != null && it.assignment != null && it.courseBlock != null
                }
                assertEquals(5f, stateWithMark.submissionMark?.averageScore)
                assertFalse(stateWithMark.activeUserCanSubmit)
                assertEquals(CourseAssignmentSubmission.MARKED, stateWithMark.submissionStatus)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenStudentWithSubmissionMarkedAndMultipleSubmissionPolicy_whenShown_thenShowMarkedStatusAndAddTextFileButtons() {
        testClazzAssignmentDetailOverviewViewModel(
            activeUserRole = ClazzEnrolment.ROLE_STUDENT,
            assignment = ClazzAssignment().apply {
                caRequireFileSubmission = true
                caRequireTextSubmission = true
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_MULTIPLE_ALLOWED
            }
        ) { testContext ->
            //insert submission
            activeDb.courseAssignmentSubmissionDao.insert(CourseAssignmentSubmission().apply {
                casSubmitterPersonUid = testContext.person.personUid
                casSubmitterUid = testContext.person.personUid
                casText = "I can has cheezburger"
            })

            //insert mark
            activeDb.courseAssignmentMarkDao.insert(CourseAssignmentMark().apply {
                camMark = 5f
                camSubmitterUid = testContext.person.personUid
                camAssignmentUid = testContext.assignment.caUid
            })

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val stateWithMark = awaitItemWhere {
                    it.submissionMark != null && it.assignment != null && it.courseBlock != null
                }
                assertEquals(5f, stateWithMark.submissionMark?.averageScore)
                assertTrue(stateWithMark.activeUserCanSubmit)
                assertEquals(CourseAssignmentSubmission.MARKED, stateWithMark.submissionStatus)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenStudentWithNoSubmissionAndSingleSubmitPolicy_whenClickSubmitSubmission_thenSubmitAndHideAddTextFile() {
        testClazzAssignmentDetailOverviewViewModel(
            activeUserRole = ClazzEnrolment.ROLE_STUDENT,
            assignment = ClazzAssignment().apply {
                caRequireFileSubmission = true
                caRequireTextSubmission = true
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
            }
        ) { testContext ->
            val submissionText = "I can haz cheezburger"

            val mockSubmitterUseCase = mock<SubmitAssignmentUseCase> {
                onBlocking { invoke(any(), any(), any(), any(), any()) }.thenAnswer {
                    val submission = it.arguments[4] as CourseAssignmentSubmission
                    SubmitAssignmentUseCase.SubmitAssignmentResult(submission.shallowCopy {
                        casTimestamp = systemTimeInMillis()
                    })
                }
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle, mockSubmitterUseCase)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                awaitItemWhere {
                    it.assignment != null && it.courseBlock != null && it.latestSubmission != null
                }

                viewModel.onChangeSubmissionText(submissionText)
                viewModel.onClickSubmit()
                cancelAndIgnoreRemainingEvents()
            }

            verifyBlocking(mockSubmitterUseCase, timeout(5000)) {
                invoke(any(), any(), any(), any(), argWhere {
                    it.casText == submissionText
                })
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val submittedDoneState = awaitItemWhere {
                    (it.latestSubmission?.casTimestamp ?: 0) > 0
                }

                assertFalse(submittedDoneState.activeUserCanSubmit)
                assertEquals(
                    CourseAssignmentSubmission.SUBMITTED,
                    submittedDoneState.submissionStatus
                )
                assertEquals(submissionText, submittedDoneState.latestSubmission?.casText)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenStudentWithPrivateCommentsDisabled_whenShown_thenDoNotShowSubmitPrivateCommentButton() {
        testClazzAssignmentDetailOverviewViewModel(
            activeUserRole = ClazzEnrolment.ROLE_STUDENT,
            assignment = ClazzAssignment().apply {
                caPrivateCommentsEnabled = false
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
            }
        ) { testContext ->
            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.assignment != null && it.courseBlock != null
                }

                assertTrue(readyState.activeUserIsSubmitter)
                assertTrue(readyState.privateCommentSectionVisible)
                assertFalse(readyState.submitPrivateCommentVisible)
            }
        }
    }

    @Test
    fun givenStudentWithPrivateCommentsEnabled_whenShown_thenShowPrivateComments() {
        testClazzAssignmentDetailOverviewViewModel(
            activeUserRole = ClazzEnrolment.ROLE_STUDENT,
            assignment = ClazzAssignment().apply {
                caPrivateCommentsEnabled = true
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
            }
        ) { testContext ->
            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.assignment != null && it.courseBlock != null
                }

                assertTrue(readyState.activeUserIsSubmitter)
                assertTrue(readyState.privateCommentSectionVisible)
                assertTrue(readyState.submitPrivateCommentVisible)
            }
        }
    }







}