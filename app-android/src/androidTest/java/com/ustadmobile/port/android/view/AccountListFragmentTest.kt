package com.ustadmobile.port.android.view

import android.accounts.Account
import android.app.Application
import android.content.Context
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.account.UstadAccounts
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.initPicasso
import com.ustadmobile.core.view.AboutView
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.android.screen.AccountListScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.UmViewActions.atPosition
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.waitUntilWithFragmentScenario
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import it.xabaras.android.espresso.recyclerviewchildactions.RecyclerViewChildActions.Companion.actionOnChild
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance


@AdbScreenRecord("Account List Tests")
@ExperimentalStdlibApi
class AccountListFragmentTest : TestCase() {

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()

    lateinit var mockWebServer: MockWebServer

    lateinit var mockServerUrl: String

    private val defaultNumOfAccounts = 2

    private lateinit var di: DI

    @Before
    fun setup() {
        mockWebServer = MockWebServer().also {
            it.start()
        }
        mockServerUrl = mockWebServer.url("/").toString()
        di = (ApplicationProvider.getApplicationContext<Context>() as DIAware).di
    }

    @After
    fun destroy() {
        mockWebServer.shutdown()
    }

    @AdbScreenRecord("given stored accounts exists when app launched should be displayed")
    @Test
    fun givenStoredAccounts_whenAppLaunched_thenShouldShowAllAccounts() {



        init{
            launchFragment(true, defaultNumOfAccounts)
        }.run {
            val accountManager: UstadAccountManager by di.instance()
            AccountListScreen {
                recycler {
                    //active account was removed from the list
                    hasChildCount(accountManager.storedAccounts.size + 2)
                }
            }
        }



    }

    @AdbScreenRecord("given app launched when only guest account on the device and is " +
            "active then both profile and logout button should be hidden")
    @Test
    fun givenAppLaunched_whenOnlyGuestAccountOnTheDeviceAndIsLogged_thenShouldHideBothProfileAndLogoutButton() {


        init{
            launchFragment()

        }.run {
            AccountListScreen {
                recycler {
                    firstChild<AccountListScreen.MainItem> {
                        profileButton {
                            isNotDisplayed()
                        }
                        logoutButton {
                            isNotDisplayed()
                        }
                    }
                }
            }
        }


    }

    @AdbScreenRecord("given stored accounts when guest account active profile button should be hidden ")
    @Test
    fun givenStoredAccounts_whenGuestAccountActive_thenShouldHideProfileButton() {


        init{
            launchFragment(true, defaultNumOfAccounts, true)

        }.run{

            AccountListScreen {
                recycler {
                    firstChild<AccountListScreen.MainItem> {
                        profileButton{
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
        init{
            launchFragment(true, defaultNumOfAccounts)
        }.run {

            AccountListScreen {
                recycler {
                    firstChild<AccountListScreen.MainItem> {
                        fullNameText{
                            hasText("FirstName1 Lastname1")
                        }
                    }
                }
            }

        }


    }

    @AdbScreenRecord("given add account button when clicked should open get started screen")
    @Test
    fun givenAddAccountButton_whenClicked_thenShouldOpenGetStarted() {



        init{
            launchFragment()

        }.run {

            AccountListScreen{
                recycler{
                    childAt<AccountListScreen.NewLayout>(1){
                        newLayout {
                            click()
                            assertEquals("It navigated to the get started screen",
                                    R.id.account_get_started_dest,
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

        init{

        }.run {
            val fragmentScenario = launchFragment(true, defaultNumOfAccounts)
            val accountManager: UstadAccountManager by di.instance()

            val storedAccounts = accountManager.storedAccounts

            AccountListScreen{
                recycler{
                    childAt<AccountListScreen.MainItem>(2){
                        accountDeleteButton{
                            click()
                        }
                    }
                }
            }

            val currentStoredAccounts = accountManager.storedAccountsLive.waitUntilWithFragmentScenario(fragmentScenario) {
                it.isNotEmpty()
            }

            assertNotEquals("Current account was removed from the device",
                    storedAccounts, currentStoredAccounts)

        }


    }

    @AdbScreenRecord("given logout button when clicked should logout current active account")
    @Test
    fun givenLogoutButton_whenClicked_thenShouldRemoveAccountFromTheDevice() {

        init{

        }.run {

            val fragmentScenario = launchFragment(true, defaultNumOfAccounts)
            val accountManager: UstadAccountManager by di.instance()

            val activeAccount = accountManager.activeAccount

            AccountListScreen{
                recycler{
                    firstChild<AccountListScreen.MainItem> {
                        logoutButton{
                            click()
                        }
                    }
                }
            }

            val currentActiveAccount = accountManager.activeAccountLive.waitUntilWithFragmentScenario(fragmentScenario) {
                !accountManager.storedAccounts.contains(activeAccount)
            }

            assertTrue("Active account was logged out successfully",
                    currentActiveAccount != activeAccount)

        }


    }


    @AdbScreenRecord("given profile button when clicked should open profile details screen")
    @Test
    fun givenProfileButton_whenClicked_thenShouldGoToProfileView() {


        init{

            launchFragment(true, defaultNumOfAccounts)
        }.run {

            AccountListScreen{
                recycler{
                    firstChild<AccountListScreen.MainItem> {
                        profileButton{
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
    fun givenAccountList_whenAccountIsClicked_shouldBeActive() {

        init{

            launchFragment(true, defaultNumOfAccounts)
        }.run {

            AccountListScreen{
                recycler{
                    firstChild<AccountListScreen.MainItem> {
                        fullNameText{
                            hasText("FirstName1 Lastname1")
                        }
                    }
                    childAt<AccountListScreen.MainItem>(2){
                        click()
                    }
                    firstChild<AccountListScreen.MainItem> {
                        fullNameText{
                            hasText("FirstName3 Lastname3")
                        }
                    }
                }
            }
        }



    }


    @AdbScreenRecord("given about item displayed when clicked should open about screen")
    @Test
    fun givenAboutButton_whenClicked_thenShouldGoToAboutView() {

        before{
            launchFragment()
            Intents.init()
        }.after {
            Intents.release()
        }.run {

            AccountListScreen {
                recycler {
                    childAt<AccountListScreen.AboutItem>(2){
                        aboutTextView{
                            click()
                        }
                    }
                }
            }
            intended(IntentMatchers.hasExtra("ref", "/${AboutView.VIEW_NAME}?"))
        }


    }


    private fun addAccounts(numberOfAccounts: Int = 1, guestActive: Boolean = false) {
        var usageMap = mapOf<String, Long>()
        val accounts: MutableList<UmAccount> = (1..(numberOfAccounts + 1)).map {
            val umAccount = UmAccount(it.toLong()).apply {
                username = "Person.$it"
                firstName = "FirstName$it"
                lastName = "Lastname$it"
                endpointUrl = mockServerUrl
            }
            usageMap = usageMap.plus(Pair(umAccount.username!!, System.currentTimeMillis() - it.toLong()))
            umAccount
        }.toMutableList()

        if (guestActive) {
            val guest = UmAccount(0L, "guest", "", mockServerUrl,
                    "Guest", "User")
            accounts.add(0, guest)
        }
        val storedAccounts = UstadAccounts("${accounts[0].username}@$mockServerUrl", accounts,
                usageMap)
        val impl = UstadMobileSystemImpl.instance
        impl.setAppPref(UstadAccountManager.ACCOUNTS_PREFKEY, null, context)

        impl.setAppPref(UstadAccountManager.ACCOUNTS_PREFKEY,
                Json.stringify(UstadAccounts.serializer(), storedAccounts), context)
    }

    private fun launchFragment(createExtraAccounts: Boolean = false, numberOfAccounts: Int = 1, guestActive: Boolean = false):
            FragmentScenario<AccountListFragment> {
        if (createExtraAccounts) {
            runBlocking { addAccounts(numberOfAccounts, guestActive) }
        }

        return launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            AccountListFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }
    }

}