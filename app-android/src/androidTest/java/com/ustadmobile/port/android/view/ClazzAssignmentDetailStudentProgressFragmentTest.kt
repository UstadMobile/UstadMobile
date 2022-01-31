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
import com.ustadmobile.port.android.screen.ClazzAssignmentDetailStudentProgressScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("ClazzAssignment screen tests")
class ClazzAssignmentDetailStudentProgressFragmentTest : TestCase()  {

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
            dbRule.insertPersonAndStartSession(Person().apply {
                firstNames = "Bob"
                lastName = "Jones"
                admin = true
                personUid = 42
            }, true)
        }
    }


    @AdbScreenRecord("Given list when ClazzAssignment clicked then navigate to ClazzAssignmentDetail")
    @Test
    fun givenClazzAssignmentListPresent_whenClickOnClazzAssignment_thenShouldNavigateToClazzAssignmentDetail() {
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
            caDeadlineDate = DateTime(2021, 5, 5).unixMillisLong
            caClazzUid = testClazz.clazzUid
            caUid = dbRule.repo.clazzAssignmentDao.insert(this)
        }

        ClazzAssignmentContentJoin().apply {
            cacjContentUid = contentEntry.contentEntryUid
            cacjAssignmentUid = clazzAssignment.caUid
            cacjUid = dbRule.repo.clazzAssignmentContentJoinDao.insert(this)
        }

        ClazzAssignmentRollUp().apply {
            this.cacheClazzAssignmentUid = clazzAssignment.caUid
            this.cacheContentEntryUid = contentEntry.contentEntryUid
            this.cacheContentComplete = true
            this.cacheMaxScore = 15
            this.cachePersonUid = student.personUid
            this.cacheStudentScore = 5
            this.cacheProgress = 100
            this.cacheUid = dbRule.repo.clazzAssignmentRollUpDao.insert(this)
        }

        StatementEntity().apply {
            statementContentEntryUid = contentEntry.contentEntryUid
            contentEntryRoot = true
            statementPersonUid = student.personUid
            statementVerbUid = VerbEntity.VERB_COMPLETED_UID
            contextRegistration = randomUuid().toString()
            statementUid = dbRule.repo.statementDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_CLAZZ_ASSIGNMENT_UID to clazzAssignment.caUid.toString(),
                        UstadView.ARG_PERSON_UID to student.personUid)) {
            ClazzAssignmentDetailStudentProgressFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            ClazzAssignmentDetailStudentProgressScreen{

                recycler{

                    childWith<ClazzAssignmentDetailStudentProgressScreen.Content> {
                        withDescendant { withText(contentEntry.title!!) }
                    } perform {

                        this.contentTitle{
                            hasText(contentEntry.title!!)
                        }

                        this.score{
                            hasText("33%")
                        }
                        this.scoreResults{
                            hasText("(5/15)")
                        }

                    }

                    childWith<ClazzAssignmentDetailStudentProgressScreen.TotalScore> {
                        withDescendant { withText("Total score") }
                    } perform {

                        this.score{
                            hasText("33%")
                        }

                    }


                }



            }

        }
    }

}
