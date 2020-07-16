package com.ustadmobile.port.android.view

import android.app.Application
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
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
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("PersonEdit screen Test")
class PersonEditFragmentTest {

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


    @AdbScreenRecord("given person edit opened in normal mode classes should be shown")
    @Test
    fun givenPersonEditOpened_whenInNoRegistrationMode_thenClassesShouldBeShown(){
        launchFragment(false, fillForm = false)
        onView(withId(R.id.clazzlist_recyclerview)).check(matches(isDisplayed()))
        onView(withId(R.id.clazzlist_header_textview)).check(matches(isDisplayed()))
    }

    @AdbScreenRecord("given person edit opened in registration mode classes should be hidden")
    @Test
    fun givenPersonEditOpened_whenInRegistrationMode_thenClassesShouldBeHidden(){
        launchFragment(true, fillForm = false)
        onView(withId(R.id.clazzlist_recyclerview)).check(matches(not(isDisplayed())))
        onView(withId(R.id.clazzlist_header_textview)).check(matches(not(isDisplayed())))
    }

    @AdbScreenRecord("given person edit opened in registration mode when username and password are not filled and save is clicked should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenUserNameAndPasswordAreNotFilled_thenShouldShowErrors(){
        launchFragment(allowedRegistration = true,leftOutPassword = true, leftOutUsername = true)

        onView(withId(R.id.username_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))

        onView(withId(R.id.password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))
    }

    @AdbScreenRecord("given person edit opened in registration mode when password doesn't match and save is clicked should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenPasswordDoNotMatch_thenShouldShowErrors(){
        launchFragment(misMatchPassword = true)
        onView(withId(R.id.password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.filed_password_no_match))))

        onView(withId(R.id.confirm_password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.filed_password_no_match))))
    }



    private fun launchFragment(allowedRegistration: Boolean = false,misMatchPassword: Boolean = false,
                               leftOutPassword: Boolean = false, leftOutUsername: Boolean = false, fillForm: Boolean = true){

        val workspace = WorkSpace().apply {
            registrationAllowed = allowedRegistration
        }
        val password = "password"
        val confirmedPassword = if(misMatchPassword) "password1" else password

        val args = mapOf(UstadView.ARG_WORKSPACE to Json.stringify(WorkSpace.serializer(), workspace))
        val bundle = args.plus(mapOf(UstadView.ARG_SERVER_URL to serverUrl)).toBundle()

        val scenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundle) {
            PersonEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        if(fillForm){

            val personOnForm = Person()
            val person = Person().apply {
                firstNames = "Jane"
                lastName = "Doe"
                phoneNum = "00000000000"
                dateOfBirth = (DateTime.now().toOffsetByTimezone("Asia/Dubai").localMidnight - 7.days)
                        .utc.unixMillisLong
                emailAddr = "email@dummy.com"
                personAddress = "dummy address, 101 dummy"
            }

            person.firstNames.takeIf { it != personOnForm.firstNames }?.also {
                onView(withId(R.id.firstnames_text)).perform(clearText(), typeText(it))
            }

            person.lastName.takeIf { it != personOnForm.lastName }?.also {
                onView(withId(R.id.lastname_text)).perform(clearText(), typeText(it))
            }

            person.phoneNum.takeIf { it != personOnForm.phoneNum }?.also {
                onView(withId(R.id.phonenumber_text)).perform(clearText(), typeText(it))
            }

            person.dateOfBirth.takeIf { it != personOnForm.dateOfBirth }?.also {
                setDateField(R.id.birthday_text,it)
            }

            person.emailAddr.takeIf { it != personOnForm.emailAddr }?.also {
                onView(withId(R.id.email_text)).perform(clearText(), typeText(it))
            }

            if(!leftOutPassword){
                onView(withId(R.id.password_text)).perform(clearText(), typeText(password))
                onView(withId(R.id.confirm_password_text)).perform(clearText(), typeText(confirmedPassword))
            }

            if(!leftOutUsername){
                onView(withId(R.id.username_text)).perform(clearText(), typeText(confirmedPassword))
            }

            scenario.clickOptionMenu(R.id.menu_done)
        }
    }

}