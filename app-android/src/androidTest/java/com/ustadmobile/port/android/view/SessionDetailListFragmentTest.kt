package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.SessionListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.screen.SessionDetailListScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.insertStatementForSessions
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Session Detail List screen tests")
class SessionDetailListFragmentTest : TestCase()  {

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
            dbRule.insertPersonForActiveUser(Person().apply {
                firstNames = "Bob"
                lastName = "Jones"
                admin = true
                personUid = 42
            })
            dbRule.repo.insertStatementForSessions()
        }
    }


    @AdbScreenRecord("Given list when personWithAttempt clicked then navigate to PersonSessionList")
    @Test
    fun givenPersonsAttemptedContent_whenClickOnPerson_thenShouldNavigateToSessionListForPerson() {
        init{

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to 1000L.toString(),
                            UstadView.ARG_PERSON_UID to 1000L.toString(),
                            SessionListView.ARG_CONTEXT_REGISTRATION to "abc")) {
                StatementListViewFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }

        }.run{

            SessionDetailListScreen{

                recycler{

                    childWith<SessionDetailListScreen.PersonWithSessionDetail>{
                        withDescendant { withText("Completed") }
                    }perform {
                        verbTitle{
                            hasText("Completed")
                        }
                        objectTitle{
                            hasText("Quiz 1")
                        }
                        scoreText{
                            hasText("Score 100%")
                        }
                        scoreResults{
                            hasText("(5/5)")
                        }
                    }
                }


            }

        }
    }

}