package com.ustadmobile.port.android.view

import android.app.Application
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.account.UstadAccounts
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.UmViewActions.atPosition
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.waitUntilWithFragmentScenario
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import it.xabaras.android.espresso.recyclerviewchildactions.RecyclerViewChildActions.Companion.actionOnChild
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("Account List Tests")
@ExperimentalStdlibApi
class AccountListFragmentTest {

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())

    private val context = ApplicationProvider.getApplicationContext<Application>()

    lateinit var mockWebServer: MockWebServer

    lateinit var mockServerUrl: String

    private val defaultNumOfAccounts = 2

    private fun MockWebServer.enqueueAccountResponse(umAccount: UmAccount =
                                                             UmAccount(1L, "dummy1", "", mockServerUrl)) {
        enqueue(MockResponse()
                .setBody(Json.stringify(UmAccount.serializer(), umAccount))
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"))
    }

    @Before
    fun setup(){
        mockWebServer = MockWebServer().also {
            it.start()
        }
        mockServerUrl = mockWebServer.url("/").toString()
    }

    @After
    fun destroy(){
        mockWebServer.shutdown()
    }

    @AdbScreenRecord("given stored accounts exists when app launched should be displayed")
    @Test
    fun givenStoreAccounts_whenAppLaunched_thenShouldShowAllAccounts(){
        launchFragment(true, defaultNumOfAccounts)
        val accountManager = UstadAccountManager.getInstance(UstadMobileSystemImpl.instance, context)
        //3 - account for active account, add account and about view items
        onView(withId(R.id.account_list_recycler)).check(
                matches(hasChildCount(accountManager.storedAccounts.size + 3)))
    }

    @AdbScreenRecord("given active account when app launched should be displayed")
    @Test
    fun givenActiveAccountExists_whenAppLaunched_thenShouldShowIt(){
        launchFragment(true, defaultNumOfAccounts)

        onView(withId(R.id.account_list_recycler)).check(
                matches(atPosition(0, hasDescendant(withText("FirstName1 Lastname1")))))
    }

    @AdbScreenRecord("given add account button when clicked should open get started screen")
    @Test
    fun givenAddAccountButton_whenClicked_thenShouldOpenGetStarted(){
        launchFragment()
        onView(withId(R.id.account_list_recycler)).perform(
                actionOnItemAtPosition<RecyclerView.ViewHolder>(1,
                        actionOnChild(click(), R.id.item_createnew_layout)))

        assertEquals("It navigated to the get started screen",
                R.id.account_get_started_dest, systemImplNavRule.navController.currentDestination?.id)
    }


    @AdbScreenRecord("given delete button when clicked should remove account from the device")
    @Test
    fun givenDeleteAccountButton_whenClicked_thenShouldRemoveAccountFromTheDevice(){
        val fragmentScenario = launchFragment(true, defaultNumOfAccounts)
        val accountManager = UstadAccountManager.getInstance(UstadMobileSystemImpl.instance, context)

        val storedAccounts = accountManager.storedAccounts

        onView(withId(R.id.account_list_recycler)).perform(
                actionOnItemAtPosition<RecyclerView.ViewHolder>(2,
                        actionOnChild(click(), R.id.account_delete_icon)))

        val currentStoredAccounts = accountManager.storedAccountsLive.waitUntilWithFragmentScenario(fragmentScenario) {
            it.isNotEmpty()
        }

        assertNotEquals("Current account was removed from the device",
                storedAccounts, currentStoredAccounts)
    }

    @AdbScreenRecord("given logout button when clicked should logout current active account")
    @Test
    fun givenLogoutButton_whenClicked_thenShouldRemoveAccountFromTheDevice(){
        val fragmentScenario = launchFragment(true, defaultNumOfAccounts)
        val accountManager = UstadAccountManager.getInstance(UstadMobileSystemImpl.instance, context)

        val activeAccount = accountManager.activeAccount

        onView(withId(R.id.account_list_recycler)).perform(
                actionOnItemAtPosition<RecyclerView.ViewHolder>(0,
                        actionOnChild(click(), R.id.account_logout)))

        val currentActiveAccount = accountManager.activeAccountLive.waitUntilWithFragmentScenario(fragmentScenario) {
            !accountManager.storedAccounts.contains(activeAccount)
        }

        assertTrue("Active account was logged out successfully",
                currentActiveAccount != activeAccount)
    }


    @AdbScreenRecord("given profile button when clicked should open profile details screen")
    @Test
    fun givenProfileButton_whenClicked_thenShouldGoToProfileView(){
        launchFragment()
        onView(withId(R.id.account_list_recycler)).perform(
                actionOnItemAtPosition<RecyclerView.ViewHolder>(0,
                       actionOnChild(click(), R.id.account_profile)))

        assertEquals("It navigated to the profile details",
                R.id.person_detail_dest, systemImplNavRule.navController.currentDestination?.id)
    }



    @AdbScreenRecord("given about item displayed when clicked should open about screen")
    @Test
    fun givenAboutButton_whenClicked_thenShouldGoToAboutView(){
        launchFragment()
        Intents.init()

        onView(withId(R.id.account_list_recycler)).perform(
                actionOnItemAtPosition<RecyclerView.ViewHolder>(2,
                        actionOnChild(click(), R.id.account_about)))
        intended(IntentMatchers.hasExtra("ref","/${AboutView.VIEW_NAME}?"))
    }


    private suspend fun addAccounts(numberOfAccounts: Int = 1){
        var usageMap = mapOf<String, Long>()
        val accounts:MutableList<UmAccount> = (1 .. (numberOfAccounts + 1)).map {
            val person = Person("Person.$it","FirstName$it",
                    "Lastname$it").apply {
                emailAddr = "$username@$mockServerUrl"
                personUid = dbRule.db.personDao.insertAsync(this)
            }
            usageMap = usageMap.plus(Pair(person.username!!, System.currentTimeMillis() - it.toLong()))
            UmAccount(person.personUid,person.username, null,mockServerUrl)
        }.toMutableList()

        val storedAccounts = UstadAccounts("${accounts[0].username}@$mockServerUrl",accounts,
                usageMap)
        val impl = UstadMobileSystemImpl.instance
        impl.setAppPref(UstadAccountManager.ACCOUNTS_PREFKEY, null,context)

        impl.setAppPref(UstadAccountManager.ACCOUNTS_PREFKEY,
                Json.stringify(UstadAccounts.serializer(), storedAccounts), context)
    }

    private fun launchFragment(createExtraAccounts: Boolean = false, numberOfAccounts: Int = 1):
            FragmentScenario<AccountListFragment>{
        if(createExtraAccounts){
            runBlocking { addAccounts(numberOfAccounts) }
        }

        return launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            AccountListFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)
    }

}