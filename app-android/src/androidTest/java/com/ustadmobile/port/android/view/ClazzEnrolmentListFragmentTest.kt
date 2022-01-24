package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.screen.ClazzEnrolmentListScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("ClazzEnrolment screen tests")
class ClazzEnrolmentListFragmentTest : TestCase()  {

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
            })
        }
    }

    @AdbScreenRecord("Given list when ClazzEnrolment clicked then navigate to ClazzEnrolmentEdit")
    @Test
    fun givenClazzEnrolmentListPresent_whenClickOnClazzEnrolment_thenShouldNavigateToClazzEnrolmentEdit() {
        init{

            val person = Person().apply {
                firstNames = "Test"
                lastName = "User"
                personUid = dbRule.repo.personDao.insert(this)
            }

            val clazz = Clazz().apply{
                clazzName = "new Clazz"
                clazzUid = dbRule.repo.clazzDao.insert(this)
            }

            ClazzEnrolment().apply {
                clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT
                clazzEnrolmentDateJoined = DateTime(2020, 10, 10).unixMillisLong
                clazzEnrolmentDateLeft = Long.MAX_VALUE
                clazzEnrolmentOutcome = ClazzEnrolment.OUTCOME_IN_PROGRESS
                clazzEnrolmentPersonUid = person.personUid
                clazzEnrolmentClazzUid = clazz.clazzUid
                clazzEnrolmentUid = dbRule.repo.clazzEnrolmentDao.insert(this)
            }

            val bundle = bundleOf(ARG_PERSON_UID to person.personUid.toString(),
            ARG_CLAZZUID to clazz.clazzUid.toString())

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundle) {
                ClazzEnrolmentListFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }


        }.run{

            ClazzEnrolmentListScreen{

                recycler{

                    childWith<ClazzEnrolmentListScreen.ClazzEnrolment> {
                        withDescendant { withText("Student - In progress") }
                    } perform {
                        editButton {
                            click()
                        }
                    }

                }

                flakySafely {
                    Assert.assertEquals("After clicking on item, it navigates to detail view",
                            R.id.clazz_enrolment_edit, systemImplNavRule.navController.currentDestination?.id)
                }


            }

        }
    }

}