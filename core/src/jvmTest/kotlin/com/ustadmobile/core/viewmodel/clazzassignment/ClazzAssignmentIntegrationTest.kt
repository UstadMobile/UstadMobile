package com.ustadmobile.core.viewmodel.clazzassignment

import app.cash.turbine.test
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.test.clientservertest.clientServerIntegrationTest
import com.ustadmobile.core.test.savedStateOf
import com.ustadmobile.core.test.use
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.util.test.initNapierLog
import io.github.aakira.napier.Napier
import org.junit.Test
import org.kodein.di.direct
import org.kodein.di.instance
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ClazzAssignmentIntegrationTest: AbstractMainDispatcherTest() {

    @Test
    fun givenCourseAndAssignmentCreated_whenStudentSubmits_thenTeacherCanMarkAndStudentCanSeeMarkGiven() {
        val teacherMarkIssued = 5.0f
        initNapierLog()
        clientServerIntegrationTest {
            //Create course and assignment
            val testCourse = Clazz().apply {
                clazzUid = serverDb.doorPrimaryKeyManager.nextId(Clazz.TABLE_ID)
            }

            val assignmentUid = serverDb.withDoorTransactionAsync {
                CreateNewClazzUseCase(serverDb).invoke(testCourse)

                val clazzAssignmentUid = serverDb.clazzAssignmentDao().insertAsync(ClazzAssignment().apply {
                    caClazzUid = testCourse.clazzUid
                })

                serverDb.courseBlockDao().insertAsync(CourseBlock().apply {
                    cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                    cbEntityUid = clazzAssignmentUid
                    cbMinPoints = 0f
                    cbMaxPoints = 10f
                })

                clazzAssignmentUid
            }

            val studentClient = clients.first()
            val teacherClient = clients[1]

            val studentPerson = studentClient.createUserAndLogin()
            val teacherPerson = teacherClient.createUserAndLogin()

            suspend fun enrolIntoCourse(personUid: Long, role: Int) {
                EnrolIntoCourseUseCase(db = serverDb, repo = null).invoke(
                    enrolment = ClazzEnrolment(
                        clazzUid = testCourse.clazzUid,
                        personUid = personUid,
                        role = role,
                    ),
                    timeZoneId = testCourse.clazzTimeZone ?: "UTC",
                )
            }

            val client1AccountManager: UstadAccountManager = studentClient.di.direct.instance()
            assertEquals(studentPerson.personUid, client1AccountManager.currentUserSession.person.personUid)

            enrolIntoCourse(studentPerson.personUid, ClazzEnrolment.ROLE_STUDENT)

            enrolIntoCourse(teacherPerson.personUid, ClazzEnrolment.ROLE_TEACHER)

            //Student makes submission
            ClazzAssignmentDetailOverviewViewModel(
                studentClient.di,
                savedStateHandle = savedStateOf(
                    UstadView.ARG_ENTITY_UID to assignmentUid.toString(),
                    UstadViewModel.ARG_CLAZZUID to testCourse.clazzUid.toString(),
                )
            ).use { viewModel ->
                viewModel.uiState.test(timeout = 10.seconds, name = "student can submit") {
                    awaitItemWhere {
                        it.activeUserCanSubmit && it.submissionTextFieldVisible &&
                            it.fieldsEnabled
                    }

                    viewModel.editableSubmissionUiState.assertItemReceived(
                        timeout = 10.seconds, name = "editable submission received"
                    ) { it.editableSubmission != null }

                    viewModel.onChangeSubmissionText("I can has cheezburger")
                    viewModel.onClickSubmit()

                    val uiStateAfterSubmit = awaitItemWhere {
                        it.fieldsEnabled && it.submissions.firstOrNull()?.submission?.casText == "I can has cheezburger"
                                && !it.activeUserCanSubmit
                    }
                    assertFalse(uiStateAfterSubmit.activeUserCanSubmit,
                        message = "After submission, using default policy of must submit all at once, user must not be able to submit again")

                    cancelAndIgnoreRemainingEvents()
                }
            }

            //Server receives submission
            serverDb.courseAssignmentSubmissionDao().getAllSubmissionsFromSubmitterAsFlow(
                submitterUid = studentPerson.personUid,
                assignmentUid = assignmentUid
            ).assertItemReceived(timeout = 5.seconds, name = "submission received by server") {
                it.isNotEmpty() && it.first().casText == "I can has cheezburger"
            }

            //Teacher marks submission
            ClazzAssignmentSubmitterDetailViewModel(
                teacherClient.di,
                savedStateHandle = savedStateOf(
                    ClazzAssignmentSubmitterDetailViewModel.ARG_ASSIGNMENT_UID to assignmentUid.toString(),
                    ClazzAssignmentSubmitterDetailViewModel.ARG_SUBMITTER_UID to studentPerson.personUid.toString(),
                    UstadViewModel.ARG_CLAZZUID to testCourse.clazzUid.toString(),

                )
            ).use { viewModel ->
                Napier.d("===TEST teacher can mark student===")
                viewModel.uiState.test(timeout = 5.seconds, name = "teacher can mark student") {
                    //this is too long... should be on the uistate itself.
                    val uiState = awaitItemWhere {
                        it.draftMark != null &&
                        it.block != null &&
                        it.submissionList.isNotEmpty() &&
                        it.submissionList.first().submission.casText == "I can has cheezburger" &&
                        it.markFieldsEnabled
                    }
                    viewModel.onChangeDraftMark(uiState.draftMark?.shallowCopy {
                        this.camMark = teacherMarkIssued
                    })

                    Napier.d("===SUBMIT MARK===")
                    viewModel.onClickSubmitMark()

                    //Wait for saving mark to finish
                    awaitItemWhere {
                        it.marks.isNotEmpty()
                    }

                    Napier.d("===DONE/CANCEL===")

                    cancelAndIgnoreRemainingEvents()
                }
            }

            //Wait for the mark to reach the server
            serverDb.courseAssignmentMarkDao().getAllMarksForUserAsFlow(
                studentPerson.personUid, assignmentUid
            ).assertItemReceived(timeout = 5.seconds, name = "wait for mark from teacher to reach server") {
                it.isNotEmpty()
            }

            //Student should now be able to view mark
            ClazzAssignmentDetailOverviewViewModel(
                studentClient.di,
                savedStateHandle = savedStateOf(
                    UstadView.ARG_ENTITY_UID to assignmentUid.toString(),
                    UstadViewModel.ARG_CLAZZUID to testCourse.clazzUid.toString(),
                )
            ).use { viewModel ->
                viewModel.uiState.test(timeout = 5.seconds, name = "student can see grade") {
                    val uiStateWithGrades = awaitItemWhere { it.markList.isNotEmpty() }
                    assertEquals(teacherMarkIssued,
                        uiStateWithGrades.markList.first().courseAssignmentMark?.camMark,
                        "Mark displayed for student matches mark issued by teacher")
                    assertEquals(teacherPerson.personUid,
                        uiStateWithGrades.markList.first().courseAssignmentMark?.camMarkerPersonUid,
                        "Mark displayed for student shows marker as the teacher")
                    assertTrue(uiStateWithGrades.activeUserIsSubmitter)
                    assertFalse(uiStateWithGrades.activeUserCanSubmit)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

}