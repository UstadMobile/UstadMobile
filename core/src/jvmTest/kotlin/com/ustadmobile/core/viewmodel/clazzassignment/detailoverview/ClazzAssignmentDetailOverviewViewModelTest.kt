package com.ustadmobile.core.viewmodel.clazzassignment.detailoverview

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.assignment.submitassignment.AssignmentDeadlinePassedException
import com.ustadmobile.core.domain.assignment.submitassignment.SubmitAssignmentUseCase
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.test.viewmodeltest.ViewModelTestBuilder
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.loadFirstList
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseGroupSet
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

//USELESS_IS_CHECK: IDE does not understand multiplatform hierarchy the checks are not useless
@Suppress("USELESS_IS_CHECK")
class ClazzAssignmentDetailOverviewViewModelTest : AbstractMainDispatcherTest()  {

    val endpoint = Endpoint("http://test.com/")

    class AssignmentDetailOverviewTestContext(
        val clazz: Clazz,
        val assignment: ClazzAssignment,
        val courseBlock: CourseBlock,
        val person: Person,
        val groupSet: CourseGroupSet? = null,
    )

    private fun testClazzAssignmentDetailOverviewViewModel(
        activeUserRole: Int,
        assignment: ClazzAssignment = ClazzAssignment(),
        courseBlock: CourseBlock = CourseBlock(),
        groupSet: CourseGroupSet? = null,
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
                CreateNewClazzUseCase(activeDb).invoke(clazz)

                EnrolIntoCourseUseCase(
                    db = activeDb, repo =  null
                ).takeIf { activeUserRole != 0 }?.invoke(
                    ClazzEnrolment(
                        clazzUid = clazzUid,
                        personUid = activePerson.personUid,
                    ).also {
                        it.clazzEnrolmentRole = activeUserRole
                    },
                    "UTC"
                )

                if(groupSet != null) {
                    groupSet.cgsUid = activeDb.courseGroupSetDao().insertAsync(groupSet)
                    assignment.caGroupUid = groupSet.cgsUid
                }

                assignment.caClazzUid = clazzUid
                assignment.caUid = activeDb.clazzAssignmentDao().insertAsync(assignment)

                courseBlock.cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                courseBlock.cbEntityUid = assignment.caUid
                courseBlock.cbEntityUid = activeDb.courseBlockDao().insertAsync(courseBlock)

                AssignmentDetailOverviewTestContext(
                    clazz, assignment, courseBlock,
                    activePerson, groupSet,
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
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.assignment != null && it.courseBlock != null
                }
                assertTrue(readyState.activeUserIsSubmitter)
                assertTrue(readyState.activeUserCanSubmit)
                assertTrue(readyState.addFileSubmissionVisible)
                assertTrue(readyState.canEditSubmissionText)
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.editableSubmissionUiState.assertItemReceived {
                it.editableSubmission?.casTimestamp == 0L
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
            activeDb.courseAssignmentSubmissionDao().insert(CourseAssignmentSubmission().apply {
                casSubmitterPersonUid = testContext.person.personUid
                casSubmitterUid = testContext.person.personUid
                casText = "I can has cheezburger"
                casAssignmentUid = testContext.assignment.caUid
            })

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.assignment != null && it.courseBlock != null && it.submissions.isNotEmpty()
                }
                assertTrue(readyState.activeUserIsSubmitter)
                assertFalse(readyState.addFileSubmissionVisible)
                assertFalse(readyState.canEditSubmissionText)
                assertTrue(readyState.submissions.isNotEmpty())
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
            activeDb.courseAssignmentSubmissionDao().insertAsync(CourseAssignmentSubmission().apply {
                casSubmitterUid = testContext.person.personUid
                casSubmitterPersonUid = testContext.person.personUid
                casText = "Test text"
                casAssignmentUid = testContext.assignment.caUid
            })

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.assignment != null && it.courseBlock != null && it.submissions.isNotEmpty()
                }
                assertTrue(readyState.activeUserIsSubmitter)
                assertTrue(readyState.addFileSubmissionVisible)
                assertTrue(readyState.canEditSubmissionText)
                assertTrue(readyState.submissions.isNotEmpty())
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
            val exceptionMessage = "Deadline has passed"
            val mockSubmissionUseCase = mock<SubmitAssignmentUseCase> {
                onBlocking { invoke(any(), any(), any(), any(), any()) }
                    .thenThrow(AssignmentDeadlinePassedException(exceptionMessage))
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzAssignmentDetailOverviewViewModel(
                    di, savedStateHandle,
                    submitAssignmentUseCase = mockSubmissionUseCase
                )
            }

            viewModel.uiState.test(timeout = 10.seconds) {
                awaitItemWhere {
                    it.assignment != null && it.courseBlock != null
                }

                viewModel.editableSubmissionUiState.assertItemReceived(
                    timeout = 10.seconds, name = "editable submission not null"
                ) {
                    it.editableSubmission != null
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
            activeDb.courseAssignmentSubmissionDao().insert(CourseAssignmentSubmission().apply {
                casSubmitterPersonUid = testContext.person.personUid
                casSubmitterUid = testContext.person.personUid
                casText = "I can has cheezburger"
                casAssignmentUid = testContext.assignment.caUid
            })

            //insert mark
            activeDb.courseAssignmentMarkDao().insert(CourseAssignmentMark().apply {
                camMark = 5f
                camSubmitterUid = testContext.person.personUid
                camAssignmentUid = testContext.assignment.caUid
            })

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
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
            activeDb.courseAssignmentSubmissionDao().insert(CourseAssignmentSubmission().apply {
                casSubmitterPersonUid = testContext.person.personUid
                casSubmitterUid = testContext.person.personUid
                casText = "I can has cheezburger"
            })

            //insert mark
            activeDb.courseAssignmentMarkDao().insert(CourseAssignmentMark().apply {
                camMark = 5f
                camSubmitterUid = testContext.person.personUid
                camAssignmentUid = testContext.assignment.caUid
            })

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val stateWithMark = awaitItemWhere {
                    it.submissionMark?.averageScore == 5f && it.activeUserCanSubmit && it.submissionStatus == CourseAssignmentSubmission.MARKED
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
                    val submission = it.arguments.last() as CourseAssignmentSubmission
                    activeDb.courseAssignmentSubmissionDao().insert(
                        submission.shallowCopy {
                            casAssignmentUid = testContext.assignment.caUid
                            casSubmitterUid = testContext.person.personUid
                            casSubmitterPersonUid = testContext.person.personUid
                            casTimestamp = systemTimeInMillis()
                            casClazzUid = testContext.clazz.clazzUid
                        }
                    )
                    SubmitAssignmentUseCase.SubmitAssignmentResult(submission.shallowCopy {
                        casTimestamp = systemTimeInMillis()
                    })
                }
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle, mockSubmitterUseCase)
            }

            viewModel.uiState.test(timeout = 5.seconds, name = "Wait for loading") {
                awaitItemWhere {
                    it.assignment != null && it.courseBlock != null && it.submitterUid != 0L
                }

                viewModel.editableSubmissionUiState.assertItemReceived (
                    timeout = 5.seconds, name = "editable submission not null"
                ) {
                    it.editableSubmission != null
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

            viewModel.uiState.test(timeout = 5.seconds, name = "wait for submission done") {
                val submittedDoneState = awaitItemWhere {
                    it.submissions.firstOrNull()?.submission?.casText == submissionText
                }

                assertFalse(submittedDoneState.activeUserCanSubmit)
                assertEquals(
                    CourseAssignmentSubmission.SUBMITTED,
                    submittedDoneState.submissionStatus
                )
                assertEquals(submissionText,
                    submittedDoneState.submissions.firstOrNull()?.submission?.casText)
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
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.assignment != null && it.courseBlock != null
                }

                assertTrue(readyState.activeUserIsSubmitter)
                assertTrue(readyState.privateCommentSectionVisible)
                assertFalse(readyState.submitPrivateCommentVisible)
                cancelAndIgnoreRemainingEvents()
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
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.assignment != null && it.courseBlock != null
                }

                assertTrue(readyState.activeUserIsSubmitter)
                assertTrue(readyState.privateCommentSectionVisible)
                assertTrue(readyState.submitPrivateCommentVisible)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }


    @Test
    fun givenUserNotAssignedInGroup_whenShown_displayErrorAndDontShowSubmitAndAddTextFileButtonsAndPrivateOff() {
        testClazzAssignmentDetailOverviewViewModel(
            activeUserRole = ClazzEnrolment.ROLE_STUDENT,
            assignment = ClazzAssignment().apply {
                caPrivateCommentsEnabled = true
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
            },
            groupSet = CourseGroupSet().apply {
                cgsName = "Test groups"
            }
        ) {testContext ->
            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.assignment != null && it.courseBlock != null && it.unassignedError != null
                }

                assertNotNull(readyState.unassignedError)
                assertFalse(readyState.activeUserCanSubmit)
                assertFalse(readyState.activeUserIsSubmitter)
                assertFalse(readyState.privateCommentSectionVisible)
                cancelAndIgnoreRemainingEvents()
            }

        }
    }


    @Test
    fun givenUserIsNotStudent_whenShown_dontShowPrivateCommentsSubmissionStatusScoreAndAddFileText() {
        testClazzAssignmentDetailOverviewViewModel(
            activeUserRole = ClazzEnrolment.ROLE_TEACHER,
            assignment = ClazzAssignment().apply {
                caPrivateCommentsEnabled = true
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
            },
        ) { testContext ->
            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.assignment != null && it.courseBlock != null
                }

                assertFalse(readyState.privateCommentSectionVisible)
                assertFalse(readyState.submitPrivateCommentVisible)
                assertFalse(readyState.activeUserIsSubmitter)
                assertFalse(readyState.activeUserCanSubmit)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenStudentLoggedIn_whenShown_willShowExistingPrivateCommentsAndAllowSubmissionOfNewPrivateComment() {
        testClazzAssignmentDetailOverviewViewModel(
            activeUserRole = ClazzEnrolment.ROLE_STUDENT,
            assignment = ClazzAssignment().apply {
                caPrivateCommentsEnabled = true
            },
        ) { testContext ->
            val teacherComment = "You want burger?"
            val replyComment = "I can has cheezburger"
            activeDb.commentsDao().insertAsync(Comments().apply {
                commentsText = teacherComment
                commentsEntityUid = testContext.assignment.caUid
                commentsForSubmitterUid = testContext.person.personUid
                commentsDateTimeAdded = systemTimeInMillis()
            })

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val commentReadyState = awaitItemWhere {
                    it.privateComments() !is EmptyPagingSource<*, *> && it.privateCommentSectionVisible
                }
                val commentLoadResult: List<CommentsAndName> = commentReadyState.privateComments().loadFirstList()
                assertEquals(teacherComment, commentLoadResult.first().comment.commentsText)
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.onChangePrivateCommentText(replyComment)
            viewModel.onClickSubmitPrivateComment()
            viewModel.uiState.test(timeout = 5.seconds) {
                val commentReadyState = awaitItemWhere {
                    it.privateComments() !is EmptyPagingSource<*, *>
                }
                val commentsAfterReply = commentReadyState.privateComments().loadFirstList()
                assertEquals(replyComment, commentsAfterReply.first().comment.commentsText)
                assertEquals(teacherComment, commentsAfterReply[1].comment.commentsText)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenCourseCommentsEnabled_whenShown_willShowExistingCourseCommentsAndAllowSubmissionOfNewCourseComment() {
        testClazzAssignmentDetailOverviewViewModel(
            activeUserRole = ClazzEnrolment.ROLE_STUDENT,
            assignment = ClazzAssignment().apply {
                caPrivateCommentsEnabled = true
                caClassCommentEnabled = true
            },
        ) { testContext ->
            val startComment = "I can has cheezburger"
            val replyComment = "Yes you kan"

            activeDb.commentsDao().insertAsync(Comments().apply {
                commentsText = startComment
                commentsEntityUid = testContext.assignment.caUid
                commentsForSubmitterUid = 0
            })

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = testContext.assignment.caUid.toString()
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                @Suppress("unused") val commentReadyState = awaitItemWhere {
                    it.courseComments() !is EmptyPagingSource<*, *>
                }
                val commentsOnLoad = commentReadyState.courseComments().loadFirstList()
                assertEquals(startComment, commentsOnLoad.first().comment.commentsText)
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.onChangeCourseCommentText(replyComment)
            viewModel.onClickSubmitCourseComment()

            viewModel.uiState.test(timeout = 5.seconds) {
                val commentReadyState = awaitItemWhere {
                    it.courseComments() !is EmptyPagingSource<*, *>
                }
                val commentsAfterReply = commentReadyState.courseComments().loadFirstList()
                assertEquals(replyComment, commentsAfterReply.first().comment.commentsText)
                assertEquals(startComment, commentsAfterReply[1].comment.commentsText)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }


}