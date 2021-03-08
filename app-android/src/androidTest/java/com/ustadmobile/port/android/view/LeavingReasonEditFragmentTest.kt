package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.LeavingReasonEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson

import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.UstadView
import kotlinx.coroutines.runBlocking


@AdbScreenRecord("LeavingReasonEdit screen Test")
class LeavingReasonEditFragmentTest : TestCase(){

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    private lateinit var fragmentScenario: FragmentScenario<LeavingReasonEditFragment>


    @AdbScreenRecord("given LeavingReason not present when filled then should save to database")
    @Test
    fun givenNoLeavingReasonPresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {

        init{

            fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
                LeavingReasonEditFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }

        }.run{

            LeavingReasonEditScreen{

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                LeavingReasonTitleInput{
                    isErrorEnabled()
                    edit{
                        typeText("Moved Aboard")
                    }
                }

                fragmentScenario.clickOptionMenu(R.id.menu_done)

            }


        }
    }


    @AdbScreenRecord("given LeavingReason exists when updated then should be updated on database")
    @Test
    fun givenLeavingReasonExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {

        val existingLeavingReason = LeavingReason().apply {
            leavingReasonTitle = "New LeavingReason"
            leavingReasonUid = dbRule.repo.leavingReasonDao.insert(this)
        }

        init{

            fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to
                            existingLeavingReason.leavingReasonUid)) {
                LeavingReasonEditFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }

        }.run{

            LeavingReasonEditScreen {

                flakySafely {
                    LeavingReasonTitleInput {
                        edit {
                            clearText()
                            replaceText("Leaving Reason Changed")
                            hasText("Leaving Reason Changed")
                        }
                    }
                }

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                runBlocking {
                    val reasonFromDb = dbRule.db.leavingReasonDao.findByUidAsync(
                            existingLeavingReason.leavingReasonUid)
                    Assert.assertEquals("title change matches",
                            existingLeavingReason.leavingReasonTitle,
                            reasonFromDb!!.leavingReasonTitle)
                }

            }

        }

    }
}