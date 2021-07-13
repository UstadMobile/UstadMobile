package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.schedule.toLocalMidnight
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.screen.ClazzEnrolmentEditScreen
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("ClazzEnrolment Edit screen tests")
class ClazzEnrolmentEditFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    lateinit var fragmentScenario: FragmentScenario<ClazzEnrolmentEditFragment>


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


    @AdbScreenRecord("with no enrolment present, fill all the fields and navigate to list screen")
    @Test
    fun givenNoEnrolmentPresentYet_whenFilledInAndSaveClicked_thenShouldNavigateToEnrolmentListScreen() {

        init {

            val person = Person().apply {
                firstNames = "Test"
                lastName = "User"
                personUid = dbRule.repo.personDao.insert(this)
            }

            val clazz = Clazz().apply {
                clazzName = "new Clazz"
                clazzStartTime = DateTime(2020, 10, 10).toLocalMidnight("utc").unixMillisLong
                clazzUid = dbRule.repo.clazzDao.insert(this)
            }

            val bundle = bundleOf(UstadView.ARG_PERSON_UID to person.personUid.toString(),
                    UstadView.ARG_CLAZZUID to clazz.clazzUid.toString(),
                    UstadView.ARG_SAVE_TO_DB to true.toString())

            fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundle) {
                ClazzEnrolmentEditFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }

        }.run {

            ClazzEnrolmentEditScreen {


                roleTextView {
                    setMessageIdOption(systemImplNavRule.impl.getString(MessageID.student,
                            ApplicationProvider.getApplicationContext()))
                }

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                startDateLayout {
                    isErrorDisabled()
                }

                endDateLayout {
                    isErrorDisabled()
                }


            }


        }


    }

    @AdbScreenRecord("with existing enrolment when enrolment is ending then update db")
    @Test
    fun givenExistingEnrolment_whenEnrolmentEndsWithReason_thenShouldUpdateDb() {
        var enrolment: ClazzEnrolment? = null
        init {

            val person = Person().apply {
                firstNames = "Test"
                lastName = "User"
                personUid = dbRule.repo.personDao.insert(this)
            }

            val clazz = Clazz().apply {
                clazzName = "new Clazz"
                clazzStartTime = DateTime(2020, 10, 10).toLocalMidnight("utc").unixMillisLong
                clazzUid = dbRule.repo.clazzDao.insert(this)
            }

            enrolment = ClazzEnrolment().apply {
                clazzEnrolmentDateJoined = DateTime(2021, 2, 21).unixMillisLong
                clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT
                clazzEnrolmentClazzUid = clazz.clazzUid
                clazzEnrolmentPersonUid = person.personUid
                clazzEnrolmentUid = dbRule.repo.clazzEnrolmentDao.insert(this)
            }

            val bundle = bundleOf(UstadView.ARG_PERSON_UID to person.personUid.toString(),
                    UstadView.ARG_CLAZZUID to clazz.clazzUid.toString(),
                    UstadView.ARG_SAVE_TO_DB to true.toString(),
                    UstadView.ARG_ENTITY_UID to enrolment!!.clazzEnrolmentUid.toString())

            fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundle) {
                ClazzEnrolmentEditFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }

        }.run {

            ClazzEnrolmentEditScreen {

                flakySafely {
                    endDateLayout {
                        edit {
                            setDateWithDialog(DateTime(2021, 3, 1).unixMillisLong)
                        }
                    }
                }

                outcomeText {
                    setMessageIdOption(systemImplNavRule.impl.getString(MessageID.graduated,
                            ApplicationProvider.getApplicationContext()))
                }

                fragmentScenario.onFragment { fragment ->
                    fragment.findNavController().currentBackStackEntry?.savedStateHandle
                            ?.set("LeavingReason", defaultGson().toJson(listOf(LeavingReason().apply {
                                leavingReasonTitle = LeavingReason.PASSED_TITLE
                                leavingReasonUid = LeavingReason.PASSED_UID
                            })))
                }


                fragmentScenario.clickOptionMenu(R.id.menu_done)

                val updatedDb = dbRule.db.clazzEnrolmentDao.findByUidLive(
                        enrolment!!.clazzEnrolmentUid).waitUntilWithFragmentScenario(
                        fragmentScenario) { it?.clazzEnrolmentLeavingReasonUid == LeavingReason.PASSED_UID }

                Assert.assertEquals("outcome matches", ClazzEnrolment.OUTCOME_GRADUATED,
                updatedDb!!.clazzEnrolmentOutcome)

                Assert.assertEquals("reason matches", LeavingReason.PASSED_UID,
                        updatedDb.clazzEnrolmentLeavingReasonUid)

            }


        }


    }


}