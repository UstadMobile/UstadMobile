package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.port.android.screen.SessionListScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.insertStatementForSessions
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Session List screen tests")
class SessionListFragmentTest : TestCase()  {

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
    fun setup(){
        runBlocking {
            dbRule.insertPersonAndStartSession(Person().apply {
                firstNames = "Bob"
                lastName = "Jones"
                admin = true
                personUid = 42
            })
            dbRule.repo.insertStatementForSessions()
        }
    }


    @AdbScreenRecord("Given list when PersonWithSession clicked then navigate to SessionDetailList")
    @Test
    fun givenSessionListForPerson_whenClickOnSession_thenShouldNavigateToSessionDetailListForPerson() {
        init{

            val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_CONTENT_ENTRY_UID to 1000L.toString(),
                            UstadView.ARG_PERSON_UID to 1000L.toString())) {
                SessionListFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }

        }.run{

            SessionListScreen{

                recycler{

                    childWith<SessionListScreen.PersonWithSession>{
                        withDescendant { withText("Passed - ") }
                    }perform {
                        scoreText{
                            hasText("Score 100%")
                        }
                        scoreResults{
                            hasText("(5/5)")
                        }
                        successStatusText{
                            click()
                        }
                    }

                }

                flakySafely {
                    Assert.assertEquals("After clicking on item, it navigates to detail view",
                            R.id.content_entry_detail_session_detail_list_dest,
                            systemImplNavRule.navController.currentDestination?.id)
                }


            }

        }
    }

}