/*
package com.ustadmobile.port.android.view

import android.app.Application
import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.UmViewActions.hasInputLayoutError
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.waitUntilWithFragmentScenario
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import junit.framework.Assert.assertEquals
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIAware
import java.lang.Thread.sleep


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

    private lateinit var di: DI

    @Before
    fun setUp(){
        mockWebServer = MockWebServer()
        mockWebServer.start()
        serverUrl = mockWebServer.url("/").toString()
        di = (ApplicationProvider.getApplicationContext<Context>() as DIAware).di
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun enqueueResponse(success:Boolean = true, responseCode: Int = 200){
        if(success){
            mockWebServer.enqueue(MockResponse()
                    .setResponseCode(responseCode)
                    .setHeader("Content-Type", "application/json")
                    .setBody(Buffer().write(Json.stringify(UmAccount.serializer(),
                            UmAccount(0L)).toByteArray())))
        }else{
            mockWebServer.enqueue(MockResponse()
                    .setResponseCode(responseCode))
        }
    }

    private fun createPerson(withUsername: Boolean = false, isAdmin: Boolean = false,
                             matchPassword: Boolean = false): PersonWithAccount {

        Person().apply {
            admin = isAdmin
            username = "First"
            lastName = "User"
            personUid = 42
            dbRule.repo.personDao.insert(this)
        }


        val password = "password"
        val confirmPassword = if(matchPassword) password else "password1"

        return PersonWithAccount().apply {
            fatherName = "Doe"
            firstNames = "Jane"
            lastName = "Doe"
            if(withUsername){
                username = "dummyUserName"
            }
            personUid = mPersonUid
            newPassword = password
            currentPassword = password
            confirmedPassword = confirmPassword
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

    @AdbScreenRecord("given person account edit launched and active person is admin  current password should be hidden")
    @Test
    fun givenPersonAccountEditLaunched_whenActivePersonIsAdmin_thenShouldHideCurrentPassword(){
        val person = createPerson(true, isAdmin = true)
        launchFragment(person, false)
        onView(withId(R.id.current_password_textinputlayout)).check(matches(not(isDisplayed())))
    }

    @AdbScreenRecord("given person account edit launched and active person is not admin  current password should be hidden")
    @Test
    fun givenPersonAccountEditLaunched_whenActivePersonIsNotAdmin_thenShouldShowCurrentPassword(){
        val person = createPerson(true, isAdmin = false)
        launchFragment(person, false)
        onView(withId(R.id.current_password_textinputlayout)).check(matches(isDisplayed()))
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
        launchFragment(person, true, fillUsername = false)
        onView(withId(R.id.username_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))

    }

    @AdbScreenRecord("given person account edit launched when username is null and person not admin when save clicked should show error")
    @Test
    fun givenPersonAccountEditLaunched_whenUserIsNotAdminAndSaveClicked_shouldShowErrors() {
        val person = createPerson(true)
        launchFragment(person, true, fillUsername = false, fillConfirmPassword = false)

        onView(withId(R.id.current_password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))

        onView(withId(R.id.new_password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))

        onView(withId(R.id.confirm_password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))
    }

    @AdbScreenRecord("given person account edit launched and person is admin when new password not filled on save clicked should show error")
    @Test
    fun givenPersonAccountEditLaunchedAndPersonIsAdmin_whenNewPasswordIsNotFilledAndSaveClicked_thenShouldShowError(){
        val person = createPerson(true, isAdmin = true)
        launchFragment(person, true, fillUsername = false,
                fillCurrentPassword = false, fillConfirmPassword = false)

        onView(withId(R.id.current_password_textinputlayout)).check(matches(not(isDisplayed())))

        onView(withId(R.id.new_password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))

    }

    @AdbScreenRecord("given person account edit launched in password change mode when all fields are filled on save clicked should change password")
    @Test
    fun givenPersonAccountInPasswordChangeMode_whenAllFieldsAreFilledAndSaveClicked_thenShouldChangePassword(){
        enqueueResponse()
        val person = createPerson(true, isAdmin = false, matchPassword = true)
        launchFragment(person, true, fillUsername = false, fillCurrentPassword = true,
                fillNewPassword = true)

        onView(withId(R.id.current_password_textinputlayout)).check(matches(
                not(hasInputLayoutError(context.getString(R.string.field_required_prompt)))))

        onView(withId(R.id.new_password_textinputlayout)).check(matches(
                not(hasInputLayoutError(context.getString(R.string.field_required_prompt)))))

        onView(withId(R.id.confirm_password_textinputlayout)).check(matches(
                not(hasInputLayoutError(context.getString(R.string.field_required_prompt)))))
    }

    @AdbScreenRecord("given person account edit launched in password change mode when on save clicked and password do not match should show errors")
    @Test
    fun givenPersonAccountInPasswordChangeMode_whenSaveClickedAndPasswordDoNotMatch_thenShouldShowErrors(){
        enqueueResponse(false, 403)
        val person = createPerson(true, isAdmin = false, matchPassword = true)
        launchFragment(person, true, fillUsername = false, fillCurrentPassword = true,
                fillNewPassword = true, fillConfirmPassword = true)

        //wait for a network call - fixed timeout
        sleep(1000)

        onView(withId(R.id.current_password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.incorrect_current_password))))

    }


    @AdbScreenRecord("given person account edit launched in account creation mode when all fields are filled on save clicked should create and account")
    @Test
    fun givenPersonAccountInAccountCreationMode_whenAllFieldsAreFilledAndSaveClicked_thenShouldCreateAnAccount(){
        enqueueResponse()
        val person = createPerson(false, isAdmin = false, matchPassword = true)
        val fragmentScenario = launchFragment(person, true, fillUsername = true, fillCurrentPassword = false,
                fillNewPassword = true)

        val mPerson = dbRule.db.personDao.findByUidLive(person.personUid).waitUntilWithFragmentScenario(fragmentScenario) {
            it?.username != null
        }
        assertEquals("Account was created successfully", person.username , mPerson?.username)
    }

    @AdbScreenRecord("given person account edit launched in account creation mode when all fields are filled on save clicked but username exists should show error")
    @Test
    fun givenPersonAccountInAccountCreationMode_whenAllFieldsAreFilledAndSaveClickedButUsernameExists_thenShouldShowError(){
        enqueueResponse(false, 409)
        val person = createPerson(false, isAdmin = false, matchPassword = true)
        val fragmentScenario = launchFragment(person, true, fillUsername = true, fillCurrentPassword = false,
                fillNewPassword = true)

        dbRule.db.personDao.findByUidLive(person.personUid).waitUntilWithFragmentScenario(fragmentScenario) {
            it?.username != null
        }
        onView(withId(R.id.username_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.person_exists))))

    }


    @AdbScreenRecord("given person account edit launched in account creation mode when password doesn't match on save clicked should show errors")
    @Test
    fun givenPersonAccountInAccountCreationMode_whenPasswordFieldDoNotMatchAndSaveClicked_thenShouldShowErrors(){
        enqueueResponse()
        val person = createPerson(false, isAdmin = false, matchPassword = false)
        launchFragment(person, true, fillUsername = true, fillCurrentPassword = true,
                fillNewPassword = true)

        onView(withId(R.id.new_password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.filed_password_no_match))))

        onView(withId(R.id.confirm_password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.filed_password_no_match))))
    }


    private fun launchFragment(person: PersonWithAccount, fillForm: Boolean = false, fillUsername: Boolean = false,
                               fillCurrentPassword: Boolean = false, fillNewPassword: Boolean = false,
                               fillConfirmPassword: Boolean = true): FragmentScenario<PersonAccountEditFragment>{

        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString(),
                UstadView.ARG_SERVER_URL to serverUrl).toBundle()

        val scenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = args) {
            PersonAccountEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        if(fillForm){
            if(fillUsername){
                onView(withId(R.id.account_username_text)).perform(click(),typeText("dummyUser"))
            }

            if(fillCurrentPassword){
                onView(withId(R.id.current_password_text)).perform(click(),typeText(person.currentPassword))
            }

            if(fillNewPassword){
                onView(withId(R.id.new_password_text)).perform(click(),typeText(person.newPassword))
            }

            if(fillConfirmPassword){
                onView(withId(R.id.confirm_password_text)).perform(click(),typeText(person.confirmedPassword))
            }
            scenario.clickOptionMenu(R.id.menu_done)
        }
        return scenario
    }

}
*/
