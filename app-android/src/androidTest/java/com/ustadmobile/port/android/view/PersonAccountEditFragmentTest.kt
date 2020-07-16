package com.ustadmobile.port.android.view

import android.app.Application
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.nhaarman.mockitokotlin2.*
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.controller.PersonAccountEditPresenter
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.UmViewActions.hasInputLayoutError
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.setDateField
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import junit.framework.Assert
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("PersonAccountEdit screen Test")
class PersonAccountEditFragmentTest {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

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
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private lateinit var mockWebServer: MockWebServer

    private lateinit var serverUrl: String

    private val mPersonUid: Long = 121212

    @Before
    fun setUp(){
        mockWebServer = MockWebServer()
        mockWebServer.start()
        serverUrl = mockWebServer.url("/").toString()

        mockWebServer.enqueue(MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(Buffer().write(Json.stringify(UmAccount.serializer(),
                        UmAccount(0L)).toByteArray())))
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun createPerson(withUsername: Boolean = false, isAdmin: Boolean = false): Person {
        return Person().apply {
            fatherName = "Doe"
            firstNames = "Jane"
            lastName = "Doe"
            if(withUsername){
                username = "dummyUserName"
            }
            admin = isAdmin
            personUid = mPersonUid
            dbRule.repo.personDao.insert(this)
        }
    }


    @AdbScreenRecord("given person account edit launched when username is null should show username field")
    @Test
    fun givenPersonAccountEditLaunched_whenUsernameIsNull_themUsernameFieldShouldBeVisible(){
        val person = createPerson(false)
        launchFragment(person)
        onView(withId(R.id.username_textinputlayout)).check(matches(isDisplayed()))
    }

    @AdbScreenRecord("given person account edit launched when username is null should hide username field")
    @Test
    fun givenPersonAccountEditLaunched_whenUsernameIsNotNull_themUsernameFieldShouldBeHidden(){
        val person = createPerson(true)
        launchFragment(person)
        onView(withId(R.id.username_textinputlayout)).check(matches(not(isDisplayed())))
    }

    @AdbScreenRecord("given person account edit launched when username is null and not filled on save clicked should show error")
    @Test
    fun givenPersonAccountEditLaunched_whenUsernameIsNullAndSaveClicked_shouldShowErrors(){
        val person = createPerson(false)
        launchFragment(person, true, leftOutUsername = true)
        onView(withId(R.id.username_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))

    }

    @AdbScreenRecord("given person account edit launched when username is null and person not admin when save clicked should show error")
    @Test
    fun givenPersonAccountEditLaunched_whenUserIsNotAdminAndSaveClicked_shouldShowErrors() {
        val person = createPerson(true)
        launchFragment(person, true, leftOutUsername = true)

        onView(withId(R.id.current_password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))

        onView(withId(R.id.new_password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))
    }

    @AdbScreenRecord("given person account edit launched and person is admin when new password not filled on save clicked should show error")
    @Test
    fun givenPersonAccountEditLaunchedAndPersonIsAdmin_whenNewPasswordIsNotFilledAndSaveClicked_thenShouldShowError(){
        val person = createPerson(false, isAdmin = true)
        launchFragment(person, true, leftOutUsername = true, fillCurrentPassword = false)

        onView(withId(R.id.new_password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))

    }

    @AdbScreenRecord("given person account edit launched when all fields are filled on save clicked should show no error")
    @Test
    fun givenPersonAccount_whenAllFieldsAreFilledAndSaveClicked_thenShouldShowNoErrors(){
        val person = createPerson(false, isAdmin = false)
        launchFragment(person, true, leftOutUsername = false, fillCurrentPassword = true,
                fillNewPassword = true)

        onView(withId(R.id.current_password_textinputlayout)).check(matches(
                not(hasInputLayoutError(context.getString(R.string.field_required_prompt)))))

        onView(withId(R.id.new_password_textinputlayout)).check(matches(
                not(hasInputLayoutError(context.getString(R.string.field_required_prompt)))))
    }


    private fun launchFragment(person: Person, fillForm: Boolean = false, leftOutUsername: Boolean = false,
                               fillCurrentPassword: Boolean = false, fillNewPassword: Boolean = false){

        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString()).toBundle()

        val scenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = args) {
            PersonAccountEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        if(fillForm){
            if(!leftOutUsername){
                onView(withId(R.id.account_username_text)).perform(click(),typeText("person.username"))
            }

            if(fillCurrentPassword){
                onView(withId(R.id.account_current_password_text)).perform(click(),typeText("password"))
            }

            if(fillNewPassword){
                onView(withId(R.id.account_new_password_text)).perform(click(),typeText("password"))
            }
            scenario.clickOptionMenu(R.id.menu_done)
        }
    }

}