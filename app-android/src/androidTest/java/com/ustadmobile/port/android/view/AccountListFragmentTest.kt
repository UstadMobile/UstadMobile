package com.ustadmobile.port.android.view

import android.app.Application
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import com.google.gson.Gson
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ACTIVE_SESSION_PREFKEY
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.AboutView
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.port.android.screen.AccountListScreen
import com.ustadmobile.test.port.android.util.getApplicationDi
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.waitUntilWithFragmentScenario
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.*
import org.kodein.di.*


@AdbScreenRecord("Account List Tests")
class AccountListFragmentTest : TestCase() {

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    val context: Application = ApplicationProvider.getApplicationContext()

    private lateinit var mockWebServer: MockWebServer

    private lateinit var mockServerUrl: String

    private val defaultNumOfAccounts = 2

    private lateinit var di: DI

    lateinit var impl: UstadMobileSystemImpl

    lateinit var repo: UmAppDatabase

    lateinit var sessions: MutableList<UserSessionWithPersonAndEndpoint>

    @Before
    fun setup() {
        mockWebServer = MockWebServer().also {
            it.start()
        }
        mockServerUrl = mockWebServer.url("/").toString()
        di = getApplicationDi()
        impl = di.direct.instance()
        val endpoint = Endpoint(mockServerUrl)
        repo = di.on(endpoint).direct.instance(tag = DoorTag.TAG_REPO)
    }

    @After
    fun destroy() {
        mockWebServer.shutdown()
    }

    private fun launchFragment(numberOfAccounts: Int = 1, guestActive: Boolean = false):
            FragmentScenario<AccountListFragment> {

        runBlocking { addSessions(numberOfAccounts, guestActive) }


        return launchFragmentInContainer(themeResId = R.style.UmTheme_App,
            fragmentArgs = bundleOf()) {
            AccountListFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }
    }



    private fun addSessions(numberOfAccounts: Int = 1, guestActive: Boolean = false) {
        val endpoint = Endpoint(mockServerUrl)
        val di = getApplicationDi()
        val gson: Gson = di.direct.instance()

        sessions = (0 until numberOfAccounts).map {
            val person = runBlocking {
                repo.insertPersonAndGroup(Person().apply {
                    username = "Person.${it+1}"
                    firstNames = "FirstName${it+1}"
                    lastName = "Lastname${it+1}"
                })
            }

            val userSession = UserSession().apply {
                usPersonUid = person.personUid
                usStatus = UserSession.STATUS_ACTIVE
                usStartTime = systemTimeInMillis()
                usClientNodeId = (repo as DoorDatabaseRepository).config.nodeId
                usUid = runBlocking { repo.userSessionDao.insertSession(this@apply) }
            }

            UserSessionWithPersonAndEndpoint(userSession, person, endpoint)


        }.toMutableList()

        impl.setAppPref(ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY, endpoint.url,
            ApplicationProvider.getApplicationContext())
        impl.setAppPref(ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION,
            gson.toJson(listOf(endpoint.url)), ApplicationProvider.getApplicationContext())

        if(guestActive) {
            val guestPerson = runBlocking {
                repo.insertPersonAndGroup(Person().apply {
                    firstNames = "Guest"
                    lastName = "User"
                    username = null
                })
            }

            val guestSession = UserSession().apply {
                usPersonUid  = guestPerson.personUid
                usStartTime = systemTimeInMillis()
                usStatus = UserSession.STATUS_ACTIVE
                usClientNodeId = (repo as DoorDatabaseRepository).config.nodeId
                usUid = runBlocking { repo.userSessionDao.insertSession(this@apply) }
            }

            sessions += UserSessionWithPersonAndEndpoint(guestSession, guestPerson, endpoint)

            impl.setAppPref(ACCOUNTS_ACTIVE_SESSION_PREFKEY,
                safeStringify(di, UserSessionWithPersonAndEndpoint.serializer(), sessions.last()),
                ApplicationProvider.getApplicationContext())
        }else {
            impl.setAppPref(ACCOUNTS_ACTIVE_SESSION_PREFKEY,
                safeStringify(di, UserSessionWithPersonAndEndpoint.serializer(), sessions.first()),
                ApplicationProvider.getApplicationContext())
        }
    }

    @AdbScreenRecord("given stored accounts exists when app launched should be displayed")
    @Test
    fun givenStoredAccounts_whenAppLaunched_thenShouldShowAllAccounts() {
        init {
            launchFragment(defaultNumOfAccounts)
        }.run {
            AccountListScreen {
                recycler {
                    //active account was removed from the list
                    hasChildCount(sessions.size + 2)
                }
            }
        }

    }

    @AdbScreenRecord("given app launched when only guest account on the device and is " +
            "active then both profile and logout button should be hidden")
    @Test
    fun givenAppLaunched_whenOnlyGuestAccountOnTheDeviceAndIsLogged_thenShouldHideBothProfileAndLogoutButton() {
        init {
            launchFragment(numberOfAccounts = 0, guestActive = true)
        }.run {
            AccountListScreen {
                recycler {
                    firstChild<AccountListScreen.MainItem> {
                        profileButton {
                            isNotDisplayed()
                        }
                        logoutButton {
                            isDisplayed()
                        }
                    }
                }
            }
        }


    }

    @AdbScreenRecord("given stored accounts when guest account active profile button should be hidden ")
    @Test
    fun givenStoredAccounts_whenGuestAccountActive_thenShouldHideProfileButton() {
        init {
            launchFragment(defaultNumOfAccounts, true)

        }.run {

            AccountListScreen {
                recycler {
                    firstChild<AccountListScreen.MainItem> {
                        profileButton {
                            isNotDisplayed()
                        }
                    }
                }
            }
        }
    }


    @AdbScreenRecord("given active account when app launched should be displayed")
    @Test
    fun givenActiveAccountExists_whenAppLaunched_thenShouldShowIt() {
        init {
            launchFragment(defaultNumOfAccounts)
        }.run {
            AccountListScreen {
                recycler {
                    firstChild<AccountListScreen.MainItem> {
                        fullNameText {
                            hasText("FirstName1 Lastname1")
                        }
                    }
                }
            }

        }


    }

    @AdbScreenRecord("given add account button when clicked should open enter link screen")
    @Test
    fun givenAddAccountButton_whenClicked_thenShouldOpenEnterLinkScreen() {
        init {
            launchFragment()
        }.run {
            AccountListScreen {
                recycler {
                    childAt<AccountListScreen.NewLayout>(1) {
                        newLayout {
                            click()
                            assertEquals("It navigated to the get started screen",
                                    R.id.site_enterlink_dest,
                                    systemImplNavRule.navController.currentDestination?.id)
                        }
                    }
                }
            }

        }


    }


    @AdbScreenRecord("given delete button when clicked should remove account from the device")
    @Test
    fun givenDeleteAccountButton_whenClicked_thenShouldRemoveAccountFromTheDevice() {

        init {

        }.run {
            val fragmentScenario = launchFragment(defaultNumOfAccounts)
            val accountManager: UstadAccountManager by di.instance()

            AccountListScreen {
                recycler {
                    childWith<AccountListScreen.MainItem> {
                        withDescendant { withText("FirstName2 Lastname2") }
                    } perform {
                        accountDeleteButton {
                            click()
                        }
                    }
                }
            }

            val currentStoredAccounts = accountManager.activeUserSessionsLive.waitUntilWithFragmentScenario(fragmentScenario) {
                it.size == 1
            }

            val isAccountDeleted = currentStoredAccounts!!.find { it.person.firstNames == "FirstName2" }
            assertEquals("Correct account got deleted",
                    null, isAccountDeleted)
        }


    }

    @AdbScreenRecord("Given logout button clicked with no other accounts should navigate to enter link")
    @Test
    fun givenLogoutButton_whenClicked_thenShouldRemoveAccountFromTheDeviceAndNavigateToEnterLink() {
        init {
            launchFragment(1)
        }.run {

            val accountManager: UstadAccountManager by di.instance()

            AccountListScreen {
                recycler {
                    firstChild<AccountListScreen.MainItem> {
                        logoutButton {
                            click()
                        }
                    }
                }
            }

            flakySafely {
                assertNull("Active session is null", accountManager.activeSession)

                assertEquals("It navigated to the get started screen",
                    R.id.site_enterlink_dest,
                    systemImplNavRule.navController.currentDestination?.id)
            }
        }


    }


    @AdbScreenRecord("given profile button when clicked should open profile details screen")
    @Test
    fun givenProfileButton_whenClicked_thenShouldGoToProfileView() {


        init {
            launchFragment(defaultNumOfAccounts)
        }.run {

            AccountListScreen {
                recycler {
                    firstChild<AccountListScreen.MainItem> {
                        profileButton {
                            click()
                        }
                    }
                }
            }
            assertEquals("It navigated to the profile details",
                    R.id.person_detail_dest, systemImplNavRule.navController.currentDestination?.id)
        }


    }


    @AdbScreenRecord("given account list when account is clicked should be active")
    @Test
    fun givenAccountList_whenAccountIsClicked_shouldBeActiveAndNavigateToFirstDest() {

        init {
            launchFragment(defaultNumOfAccounts)
        }.run {

            AccountListScreen {
                recycler {
                    firstChild<AccountListScreen.MainItem> {
                        fullNameText {
                            hasText("FirstName1 Lastname1")
                        }
                    }

                    childWith<AccountListScreen.MainItem> {
                        withDescendant { withText("FirstName2 Lastname2") }
                    }perform {
                        click()
                    }

                    firstChild<AccountListScreen.MainItem> {
                        fullNameText {
                            hasText("FirstName2 Lastname2")
                        }
                    }
                }
            }

            assertEquals("It navigated to first expected destination",
                R.id.content_entry_list_home_dest, systemImplNavRule.navController.currentDestination?.id)
        }


    }


    @AdbScreenRecord("given about item displayed when clicked should open about screen")
    @Test
    fun givenAboutButton_whenClicked_thenShouldGoToAboutView() {

        before {
            launchFragment()
            Intents.init()
        }.after {
            Intents.release()
        }.run {

            AccountListScreen {
                recycler {
                    childAt<AccountListScreen.AboutItem>(2) {
                        aboutTextView {
                            click()
                        }
                    }
                }
            }
            intended(IntentMatchers.hasExtra("ref", "/${AboutView.VIEW_NAME}?"))
        }


    }

}
