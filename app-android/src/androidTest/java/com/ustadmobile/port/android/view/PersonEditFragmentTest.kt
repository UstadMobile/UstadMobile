package com.ustadmobile.port.android.view

import android.app.Application
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.port.android.screen.PersonEditScreen
import com.ustadmobile.port.android.screen.PersonEditScreen.scrollToBottom
import com.ustadmobile.test.port.android.UmViewActions.hasInputLayoutError
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matchers.not
import org.junit.*
import java.lang.Thread.sleep


@AdbScreenRecord("PersonEdit screen Test")
class PersonEditFragmentTest : TestCase() {

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private lateinit var mockWebServer: MockWebServer

    private lateinit var serverUrl: String

    val impl = UstadMobileSystemImpl.instance

    @Before
    fun setUp() {
        impl.messageIdMap = MessageIDMap.ID_MAP
        mockWebServer = MockWebServer()
        mockWebServer.start()
        serverUrl = mockWebServer.url("/").toString()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }


    @AdbScreenRecord("given person edit opened in normal mode classes should be shown")
    @Test
    fun givenPersonEditOpened_whenInNoRegistrationMode_thenClassesShouldBeShown() {

        launchFragment(false, fillForm = false)
        PersonEditScreen {
            scrollToBottom()
            ClazzListRecyclerView {
                isDisplayed()
            }
            clazzListHeaderTextView {
                isDisplayed()
            }
        }
    }

    @AdbScreenRecord("given person edit opened in normal mode and admin, " +
            "roles and permissions should be shown")
    //@Test
    fun givenPersonEditOpenedAsAdmin_whenInNoRegistrationMode_thenRolesShouldBeShown() {

        init {
            val newRole = Role().apply {
                roleName = "Role A"
                roleUid = dbRule.db.roleDao.insert(this)
            }
            val schoolA = School().apply {
                schoolName = "School A"
                schoolActive = true
                schoolUid = dbRule.db.schoolDao.insert(this)
            }
            val entityRoles = listOf(EntityRoleWithNameAndRole().apply {
                entityRoleRole = newRole
                entityRoleScopeName = "Role A @ School A"
                erGroupUid = 0
                erEntityUid = schoolA.schoolUid
                erTableId = School.TABLE_ID
                erActive = true
            })

            launchFragment(false, fillForm = true, leftOutUsername = true,
                    leftOutPassword = true, entityRoles = entityRoles)
        }.run {

            PersonEditScreen {
                scrollToBottom()
                rolesList {
                    isDisplayed()
                }
                roleHeaderTextView {
                    isDisplayed()
                }

                val allPeople = dbRule.db.personDao.getAllPerson()
                Assert.assertTrue(allPeople.isNotEmpty())
            }

        }

    }

    @AdbScreenRecord("given person edit for existing opened in normal mode and admin, " +
            "roles and permissions should be shown")
//@Test
    fun givenPersonEditOpenedDoeExistingAsAdmin_whenInNoRegistrationMode_thenRolesShouldBeShown() {


        val newRole = Role().apply {
            roleName = "Role A"
            roleUid = dbRule.db.roleDao.insert(this)
        }
        val schoolA = School().apply {
            schoolName = "School A"
            schoolActive = true
            schoolUid = dbRule.db.schoolDao.insert(this)
        }
        val entityRoles = listOf(EntityRoleWithNameAndRole().apply {
            entityRoleRole = newRole
            entityRoleScopeName = "School A"
            erGroupUid = 0
            erEntityUid = schoolA.schoolUid
            erTableId = School.TABLE_ID
            erActive = true
        })

        val person = Person().apply {
            firstNames = "Person"
            lastName = "One"
            active = true
            admin = false
            personUid = dbRule.db.insertPersonOnlyAndGroup(this).personUid
        }
        val scenario = launchFragment(false, fillForm = false, leftOutUsername = true,
                leftOutPassword = true, entityRoles = entityRoles, personUid = person.personUid)

        PersonEditScreen {

            scrollToBottom()
            rolesList {
                isDisplayed()
            }
            roleHeaderTextView {
                isDisplayed()
            }

            val allPeople = dbRule.db.personDao.getAllPerson()

            Assert.assertTrue(allPeople.isNotEmpty())
            GlobalScope.launch {
                val savedRoles = dbRule.db.entityRoleDao.filterByPersonWithExtraAsList(
                        person.personGroupUid)
                Assert.assertTrue(savedRoles.isNotEmpty())
            }
        }
    }

    @AdbScreenRecord("given person edit opened in normal mode username and password " +
            "should be hidden")
    @Test
    fun givenPersonEditOpened_whenInNoRegistrationMode_thenUsernameAndPasswordShouldBeHidden() {
        launchFragment(false, fillForm = false)

        onView(withId(R.id.username_textinputlayout)).check(matches(not(isDisplayed())))
        onView(withId(R.id.password_textinputlayout)).check(matches(not(isDisplayed())))
        onView(withId(R.id.confirm_password_textinputlayout)).check(matches(not(isDisplayed())))
    }

    @AdbScreenRecord("given person edit opened in registration mode classes should be hidden")
    @Test
    fun givenPersonEditOpened_whenInRegistrationMode_thenClassesShouldBeHidden() {
        launchFragment(true, fillForm = false)


        scrollToBottom()

        onView(withId(R.id.clazzlist_recyclerview)).check(matches(not(isDisplayed())))
        onView(withId(R.id.clazzlist_header_textview)).check(matches(not(isDisplayed())))
    }

    @AdbScreenRecord("given person edit opened in registration mode when username and " +
            "password are not filled and save is clicked should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenUserNameAndPasswordAreNotFilled_thenShouldShowErrors() {
        launchFragment(registrationMode = true, leftOutPassword = true, leftOutUsername = true)

        scrollToBottom()

        onView(withId(R.id.username_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))

        onView(withId(R.id.password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))
    }

    @AdbScreenRecord("given person edit opened in registration mode when dateOfBirth " +
            "is not filled and save is clicked should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenDateOfBirthAreNotFilled_thenShouldShowErrors() {
        launchFragment(registrationMode = true, leftOutDateOfBirth = true)
        onView(withId(R.id.birthday_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.field_required_prompt))))
    }

    @AdbScreenRecord("given person edit opened in registration mode when dateOfBirth " +
            "is less than 13 years of age and save is clicked should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenDateOfBirthIsLessThan13YearsOfAge_thenShouldShowErrors() {
        launchFragment(registrationMode = true,
                selectedDateOfBirth = DateTime(2010, 10, 24).unixMillisLong)
        onView(withId(R.id.birthday_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.underRegistrationAgeError))))
    }


    @AdbScreenRecord("given person edit opened in registration mode when password " +
            "doesn't match and save is clicked should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenPasswordDoNotMatch_thenShouldShowErrors() {
        launchFragment(registrationMode = true, misMatchPassword = true)

        scrollToBottom()

        onView(withId(R.id.password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.filed_password_no_match))))

        onView(withId(R.id.confirm_password_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.filed_password_no_match))))
    }

    @AdbScreenRecord("given person edit opened in registration mode when try to register " +
            "existing person should show errors")
    @Test
    fun givenPersonEditOpenedInRegistrationMode_whenTryToRegisterExistingPerson_thenShouldShowErrors() {
        mockWebServer.enqueue(MockResponse().setResponseCode(409))
        launchFragment(registrationMode = true, misMatchPassword = false, leftOutUsername = false)

        sleep(5000)

        scrollToBottom()
        onView(withId(R.id.username_textinputlayout)).check(matches(
                hasInputLayoutError(context.getString(R.string.person_exists))))
    }


    private fun launchFragment(registrationMode: Boolean = false, misMatchPassword: Boolean = false,
                               leftOutPassword: Boolean = false, leftOutUsername: Boolean = false,
                               fillForm: Boolean = true,
                               entityRoles: List<EntityRoleWithNameAndRole> = listOf(),
                               entityRolesOnForm: List<EntityRoleWithNameAndRole>? = null,
                               personUid: Long = 0, leftOutDateOfBirth: Boolean = false,
                               selectedDateOfBirth: Long = DateTime(1990, 10, 18).unixMillisLong)
            : FragmentScenario<PersonEditFragment> {

        val password = "password"
        val confirmedPassword = if (misMatchPassword) "password1" else password

        var args = mapOf(PersonEditView.ARG_REGISTRATION_MODE to registrationMode.toString(),
                ARG_SERVER_URL to serverUrl)
        if (personUid != 0L) {
            args = mapOf(PersonEditView.ARG_REGISTRATION_MODE to registrationMode.toString(),
                    ARG_SERVER_URL to serverUrl,
                    UstadView.ARG_ENTITY_UID to personUid.toString())
        }

        val scenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = args.toBundle()) {
            PersonEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        //Soft keyboard tend to hide views, when try to type will throw exception so
        // instead of type we'll replace text
        if (fillForm) {

            val personOnForm = scenario.letOnFragment { it.entity }

            val person = Person().apply {
                firstNames = "Jane"
                lastName = "Doe"
                phoneNum = "00000000000"
                gender = Person.GENDER_MALE
                dateOfBirth = selectedDateOfBirth
                emailAddr = "email@dummy.com"
                personAddress = "dummy address, 101 dummy"
                if (!leftOutUsername) {
                    username = "jane.doe"
                }
            }

            person.firstNames.takeIf { it != personOnForm?.firstNames }?.also {
                onView(withId(R.id.firstnames_text)).perform(replaceText(it))
            }

            person.lastName.takeIf { it != personOnForm?.lastName }?.also {
                onView(withId(R.id.lastname_text)).perform(replaceText(it))
            }

            person.gender.takeIf { it != personOnForm?.gender }?.also {
                setMessageIdOption(R.id.gender_value, impl.getString(MessageID.male, context))
            }

            person.phoneNum.takeIf { it != personOnForm?.phoneNum }?.also {
                //scroll
                onView(withId(R.id.phonenumber_text)).perform(replaceText(it))
            }

            person.emailAddr.takeIf { it != personOnForm?.emailAddr }?.also {
                onView(withId(R.id.email_text)).perform(replaceText(it))
            }

            person.personAddress.takeIf { it != personOnForm?.personAddress }?.also {
                onView(withId(R.id.address_text)).perform(replaceText(it))
            }

            if (!leftOutDateOfBirth) {
                person.dateOfBirth.takeIf { it != personOnForm?.dateOfBirth }?.also {
                    setDateField(R.id.birthday_text, it)
                }
            }

            if (!leftOutUsername) {
                //scroll
                scrollToBottom()
                person.username.takeIf { it != personOnForm?.username }?.also {
                    onView(withId(R.id.username_text)).perform(replaceText(it))
                }
            }

            if (!leftOutPassword) {
                //scroll
                scrollToBottom()
                onView(withId(R.id.password_text)).perform(replaceText(password))
                onView(withId(R.id.confirm_password_text)).perform(replaceText(confirmedPassword))
            }

            scenario.clickOptionMenu(R.id.menu_done)
        }

        //Add Roles and assignments

        if (entityRoles.isNotEmpty()) {
            entityRoles.filter { entityRolesOnForm == null || it !in entityRolesOnForm }.forEach { entityRole ->
                scenario.onFragment {
                    it.findNavController().currentBackStackEntry?.savedStateHandle
                            ?.set("EntityRoleWithNameAndRole", defaultGson().toJson(listOf(entityRole)))
                }
                Espresso.onIdle()
            }
        }

        return scenario
    }

}