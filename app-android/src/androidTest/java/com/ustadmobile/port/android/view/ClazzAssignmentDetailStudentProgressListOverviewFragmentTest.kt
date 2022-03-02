package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.screen.ClazzAssignmentDetailStudentProgressListScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("ClazzAssignmentWithMetrics screen tests")
class ClazzAssignmentDetailStudentProgressListOverviewFragmentTest : TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @Before
    fun setup() {
        runBlocking {
            val admin = Person().apply {
                firstNames = "Bob"
                lastName = "Jones"
                admin = true
                personUid = 42
            }
            dbRule.insertPersonAndStartSession(admin, true)
        }
    }

    @AdbScreenRecord("Given list when ClazzAssignmentWithMetrics clicked then navigate to ClazzAssignmentWithMetricsDetail")
    @Test
    fun givenClazzAssignmentWithMetricsListPresent_whenClickOnClazzAssignmentWithMetrics_thenShouldNavigateToClazzAssignmentWithMetricsDetail() {
        val testClazz = Clazz().apply {
            clazzUid = dbRule.repo.clazzDao.insert(this)
        }

        val student = Person().apply{
            firstNames = "Student"
            lastName = "A"
            personUid = dbRule.repo.personDao.insert(this)
        }

        val contentEntry = ContentEntry().apply {
            title = "Quiz 1"
            this.contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
            this.description = "Math Quiz"
            leaf = true
            contentEntryUid = dbRule.repo.contentEntryDao.insert(this)
        }

        val clazzEnrolment = ClazzEnrolment().apply {
            clazzEnrolmentClazzUid = testClazz.clazzUid
            clazzEnrolmentDateJoined = DateTime(2021, 5, 1).unixMillisLong
            clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT
            clazzEnrolmentPersonUid = student.personUid
            clazzEnrolmentOutcome = ClazzEnrolment.OUTCOME_IN_PROGRESS
            clazzEnrolmentUid = dbRule.repo.clazzEnrolmentDao.insert(this)
        }

        val clazzAssignment = ClazzAssignment().apply {
            caTitle = "New Clazz Assignment"
            caDescription = "complete quiz"
            caClazzUid = testClazz.clazzUid
            caRequireFileSubmission = false
            caUid = dbRule.repo.clazzAssignmentDao.insert(this)
        }

        StatementEntity().apply {
            statementContentEntryUid = contentEntry.contentEntryUid
            contentEntryRoot = true
            resultCompletion = true
            extensionProgress = 100
            resultScoreRaw = 5
            resultScoreMax = 15
            statementPersonUid = student.personUid
            statementVerbUid = VerbEntity.VERB_COMPLETED_UID
            contextRegistration = randomUuid().toString()
            statementUid = dbRule.repo.statementDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(
                        UstadView.ARG_ENTITY_UID to clazzAssignment.caUid.toString(),
                        UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString())) {
            ClazzAssignmentDetailStudentProgressListOverviewFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            ClazzAssignmentDetailStudentProgressListScreen{

                recycler{

                    childWith<ClazzAssignmentDetailStudentProgressListScreen.ClazzAssignmentWithMetrics> {
                        withDescendant {  withText("Marked") }
                    } perform {


                        notStartedText{
                            hasText("1")
                        }

                        startedText{
                            hasText("0")
                        }

                    }

                    childWith<ClazzAssignmentDetailStudentProgressListScreen.StudentAttempt> {
                        withTag(student.personUid)
                    } perform {

                        this.personName{
                            hasText("Student A")
                        }

                        click()

                    }

                    flakySafely {
                        Assert.assertEquals("After clicking on item, it navigates to detail view",
                                R.id.clazz_assignment_detail_student_progress, systemImplNavRule.navController.currentDestination?.id)
                    }


                }

            }

        }
    }

}
