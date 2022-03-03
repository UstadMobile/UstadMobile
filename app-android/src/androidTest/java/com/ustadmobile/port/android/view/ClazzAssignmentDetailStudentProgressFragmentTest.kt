package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
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
            caRequireFileSubmission = false
            caUid = dbRule.repo.clazzAssignmentDao.insert(this)
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

                }



            }

        }
    }

}
