package com.ustadmobile.core.viewmodel.clazzassignment

import app.cash.turbine.test
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.test.clientservertest.clientServerIntegrationTest
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
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
import com.ustadmobile.util.test.nav.TestUstadSavedStateHandle
import org.junit.Test
import org.kodein.di.direct
import org.kodein.di.instance
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class ClazzAssignmentIntegrationTest: AbstractMainDispatcherTest() {

    inline fun <T: UstadViewModel> T.use(
        block: (T) -> Unit
    ) {
        try {
            block(this)
        }finally {
            close()
        }
    }

    fun savedStateOf(vararg keys: Pair<String, String>) = TestUstadSavedStateHandle().apply {
        keys.forEach {
            set(it.first, it.second)
        }
    }

    @Test
    fun givenCourseAndAssignmentCreated_whenStudentSubmits_thenTeacherCanMarkAndStudentCanSeeMarkGiven() {
        clientServerIntegrationTest {
            //Create course and assignment
            val testCourse = Clazz().apply {
                clazzUid = serverDb.doorPrimaryKeyManager.nextId(Clazz.TABLE_ID)
            }

            val assignmentUid = serverDb.withDoorTransactionAsync {
                serverDb.createNewClazzAndGroups(testCourse, serverDi.direct.instance(), emptyMap())

                val clazzAssignmentUid = serverDb.clazzAssignmentDao.insertAsync(ClazzAssignment().apply {
                    caClazzUid = testCourse.clazzUid
                })

                serverDb.courseBlockDao.insertAsync(CourseBlock().apply {
                    cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                    cbEntityUid = clazzAssignmentUid
                })

                clazzAssignmentUid
            }

            val studentClient = clients.first()
            val teacherClient = clients[1]

            val studentPerson = studentClient.createUserAndLogin()
            val teacherPerson = teacherClient.createUserAndLogin()

            suspend fun enrolIntoCourse(personUid: Long, role: Int) {
                EnrolIntoCourseUseCase().invoke(
                    enrolment = ClazzEnrolment(
                        clazzUid = testCourse.clazzUid,
                        personUid = personUid,
                        role = role,
                    ),
                    timeZoneId = testCourse.clazzTimeZone ?: "UTC",
                    db = serverDb,
                    repo = null
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
                    UstadView.ARG_ENTITY_UID to assignmentUid.toString()
                )
            ).use { viewModel ->
                viewModel.uiState.test(timeout = 5.seconds, name = "student can submit") {
                    awaitItemWhere { it.activeUserCanSubmit && it.submissionTextFieldVisible }
                    viewModel.onChangeSubmissionText("I can has cheezburger")
                    viewModel.onClickSubmit()
                    cancelAndIgnoreRemainingEvents()
                }

                //wait for saving to finish
                viewModel.appUiState.test(timeout = 5.seconds, name = "student submit load finishes") {
                    awaitItemWhere { it.loadingState == LoadingUiState.NOT_LOADING }
                    cancelAndIgnoreRemainingEvents()
                }
            }

            //Server receives submission
            serverDb.courseAssignmentSubmissionDao.getAllSubmissionsFromSubmitterAsFlow(
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
                    ClazzAssignmentSubmitterDetailViewModel.ARG_SUBMITTER_UID to studentPerson.personUid.toString()
                )
            ).use { viewModel ->
                viewModel.uiState.test(timeout = 5.seconds) {
                    awaitItemWhere { it.submissionList.isNotEmpty() && it.submissionList.first().casText == "I can has cheezburger" }
                }
            }
        }
    }

}