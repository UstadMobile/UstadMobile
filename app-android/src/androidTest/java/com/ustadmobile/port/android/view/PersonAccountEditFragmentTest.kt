package com.ustadmobile.port.android.view

import android.app.Application
import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.screen.PersonAccountEditScreen
import com.ustadmobile.test.port.android.UmViewActions.hasInputLayoutError
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.waitUntilWithFragmentScenario
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIAware


@AdbScreenRecord("PersonAccountEdit screen Test")
class PersonAccountEditFragmentTest : TestCase(){

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

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

    private fun launchFragment(person: PersonWithAccount, fillForm: Boolean = false, fillUsername: Boolean = false,
                       fillCurrentPassword: Boolean = false, fillNewPassword: Boolean = false,
                       fillConfirmPassword: Boolean = true,
                       serverUrl: String, systemImplNavRule: SystemImplTestNavHostRule): FragmentScenario<PersonAccountEditFragment> {

        val args = mapOf(
            UstadView.ARG_ENTITY_UID to person.personUid.toString(),
            UstadView.ARG_SERVER_URL to serverUrl).toBundle()

        val scenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
            fragmentArgs = args) {
            PersonAccountEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        PersonAccountEditScreen.takeIf { fillForm }?.apply {

            if(fillUsername){
                usernameTextInput {
                    edit {
                        click()
                        typeText("dummyUser")
                    }
                }
            }

            if(fillCurrentPassword){
                currentPasswordTextInput {
                    edit {
                        click()
                        typeText(person.currentPassword!!)
                    }
                }
            }

            if(fillNewPassword){
                newPasswordTextInput {
                    edit {
                        click()
                        typeText(person.newPassword!!)
                    }
                }
            }

            if(fillConfirmPassword){
                confirmNewPassTextInput {
                    edit {
                        click()
                        typeText(person.confirmedPassword!!)
                    }
                }
            }
            scenario.clickOptionMenu(R.id.menu_done)
        }
        return scenario
    }


    private fun enqueueResponse(success:Boolean = true, responseCode: Int = 200){
        if(success){
            mockWebServer.enqueue(MockResponse()
                    .setResponseCode(responseCode)
                    .setHeader("Content-Type", "application/json")
                    .setBody(Buffer().write(Json.encodeToString(UmAccount.serializer(),
                            UmAccount(0L)).toByteArray())))
        }else{
            mockWebServer.enqueue(MockResponse()
                    .setResponseCode(responseCode))
        }
    }

    private fun createPerson(withUsername: Boolean = false, isAdmin: Boolean = false,
                             matchPassword: Boolean = false,
                             activeUserIsEditingOwnAccount: Boolean = false): PersonWithAccount {

        if(!activeUserIsEditingOwnAccount) {
            val activeUserPerson = dbRule.insertPersonAndStartSession(Person().apply {
                admin = isAdmin
                username = "First"
                lastName = "User"
                personUid = UmAppDatabaseAndroidClientRule.DEFAULT_ACTIVE_USER_PERSONUID
            })

            if(!isAdmin) {
                //scoped grant must be made for account management permission for the person being displayed
                runBlocking {
                    dbRule.repo.grantScopedPermission(activeUserPerson, Role.PERMISSION_RESET_PASSWORD,
                        Person.TABLE_ID, mPersonUid)
                }
            }
        }


        val password = "password"
        val confirmPassword = if(matchPassword) password else "password1"

        val personToEditAccount = PersonWithAccount().apply {
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
        }

        if(activeUserIsEditingOwnAccount) {
            dbRule.insertPersonAndStartSession(personToEditAccount)
        }else {
            runBlocking { dbRule.repo.insertPersonAndGroup(personToEditAccount) } as Person
        }

        return personToEditAccount
    }


    @AdbScreenRecord("given person account edit launched when username is null should show username field")
    @Test
    fun givenPersonAccountEditLaunched_whenUsernameIsNull_themUsernameFieldShouldBeVisible(){

        init{

        }.run{
            val person = createPerson(false)
            PersonAccountEditScreen{

                launchFragment(person, systemImplNavRule = systemImplNavRule, serverUrl = serverUrl)
                usernameTextInput{
                    isDisplayed()
                }
            }
        }

    }

    @AdbScreenRecord("given person account edit launched and active person is admin  current password should be hidden")
    @Test
    fun givenPersonAccountEditLaunched_whenActivePersonIsAdmin_thenShouldHideCurrentPassword(){
        init{

        }.run{
            val person = createPerson(true, isAdmin = true)
            PersonAccountEditScreen{

                launchFragment(person,false, systemImplNavRule = systemImplNavRule, serverUrl = serverUrl)
                currentPasswordTextInput{
                    isNotDisplayed()
                }
            }
        }
    }

    @AdbScreenRecord("given person account edit launched and active person is not admin  current password should be displayed")
    @Test
    fun givenPersonAccountEditLaunched_whenActivePersonIsNotAdmin_thenShouldShowCurrentPassword(){

        init{

        }.run{
            val person = createPerson(true, isAdmin = false,
                activeUserIsEditingOwnAccount = true)
            PersonAccountEditScreen{

                launchFragment(person,false, systemImplNavRule = systemImplNavRule, serverUrl = serverUrl)
                currentPasswordTextInput{
                    isDisplayed()
                }
            }
        }
    }


    @AdbScreenRecord("given person account edit launched when username is null should hide username field")
    @Test
    fun givenPersonAccountEditLaunched_whenUsernameIsNotNull_themUsernameFieldShouldBeHidden(){

        init{

        }.run{
            val person = createPerson(true)
            PersonAccountEditScreen{

                launchFragment(person, systemImplNavRule = systemImplNavRule, serverUrl = serverUrl)
                usernameTextInput{
                    isNotDisplayed()
                }
            }
        }
    }

    @AdbScreenRecord("given person account edit launched when username is null and not filled on save clicked should show error")
    @Test
    fun givenPersonAccountEditLaunched_whenUsernameIsNullAndSaveClicked_shouldShowErrors(){

        init{

        }.run{
            val person = createPerson(false)
            PersonAccountEditScreen{

                launchFragment(person,true, fillUsername = false,
                        systemImplNavRule = systemImplNavRule, serverUrl = serverUrl)
                usernameTextInput{
                   hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
            }
        }




    }

    @AdbScreenRecord("given person account edit launched when username is null and person not admin when save clicked should show error")
    @Test
    fun givenPersonAccountEditLaunched_whenUserIsNotAdminAndSaveClicked_shouldShowErrors() {

        init{

        }.run{
            val person = createPerson(true)
            PersonAccountEditScreen{

                launchFragment(person,true, fillUsername = false,
                        fillConfirmPassword = false,
                        systemImplNavRule = systemImplNavRule, serverUrl = serverUrl)
                currentPasswordTextInput{
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
                newPasswordTextInput{
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
                confirmNewPassTextInput{
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
            }
        }

    }

    @AdbScreenRecord("given person account edit launched and person is admin when new password not filled on save clicked should show error")
    @Test
    fun givenPersonAccountEditLaunchedAndPersonIsAdmin_whenNewPasswordIsNotFilledAndSaveClicked_thenShouldShowError(){

        init{

        }.run{
            val person = createPerson(true, isAdmin = true)
            PersonAccountEditScreen{

                launchFragment(person,true, fillUsername = false,
                        fillCurrentPassword = false ,fillConfirmPassword = false,
                        systemImplNavRule = systemImplNavRule, serverUrl = serverUrl)
                currentPasswordTextInput{
                    isNotDisplayed()
                }
                newPasswordTextInput{
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
            }
        }

    }

    @AdbScreenRecord("given person account edit launched in password change mode when all fields are filled on save clicked should change password")
    @Test
    fun givenPersonAccountInPasswordChangeMode_whenAllFieldsAreFilledAndSaveClicked_thenShouldChangePassword(){

        init{
            enqueueResponse()
        }.run{
            val person = createPerson(true, isAdmin = false, matchPassword = true,
                activeUserIsEditingOwnAccount = true)
            PersonAccountEditScreen{

                launchFragment(person, true, fillUsername = false, fillCurrentPassword = true,
                        fillNewPassword = true, systemImplNavRule = systemImplNavRule, serverUrl = serverUrl)

                currentPasswordTextInput{
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
                newPasswordTextInput{
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
                confirmNewPassTextInput{
                    hasInputLayoutError(context.getString(R.string.field_required_prompt))
                }
            }
        }
    }

    @AdbScreenRecord("given person account edit launched in password change mode when on save clicked and password do not match should show errors")
    @Test
    fun givenPersonAccountInPasswordChangeMode_whenSaveClickedAndPasswordDoNotMatch_thenShouldShowErrors(){

        init{
            enqueueResponse(false, 403)

        }.run{
            val person = createPerson(true, isAdmin = false, matchPassword = true,
                activeUserIsEditingOwnAccount = true)
            PersonAccountEditScreen{

                launchFragment(person, true, fillUsername = false, fillCurrentPassword = true,
                        fillNewPassword = true, fillConfirmPassword = true,
                        systemImplNavRule = systemImplNavRule, serverUrl = serverUrl)

                currentPasswordTextInput{
                    hasInputLayoutError(context.getString(R.string.incorrect_current_password))
                }
            }
        }


    }


    @AdbScreenRecord("given person account edit launched in account creation mode when all fields are filled on save clicked should create and account")
    @Test
    fun givenPersonAccountInAccountCreationMode_whenAllFieldsAreFilledAndSaveClicked_thenShouldCreateAnAccount(){

        init{
            enqueueResponse()
        }.run {
            val person = createPerson(false, isAdmin = false, matchPassword = true)
            PersonAccountEditScreen{

                val fragmentScenario = launchFragment(person, true, fillUsername = true,
                        fillCurrentPassword = false, fillNewPassword = true,
                        systemImplNavRule = systemImplNavRule, serverUrl = serverUrl)
                val personInDb = dbRule.db.personDao.findByUidLive(person.personUid).waitUntilWithFragmentScenario(fragmentScenario) {
                    it?.username != null
                }
                assertEquals("Account was created successfully", "dummyUser" , personInDb?.username)

            }


        }

    }

    @AdbScreenRecord("given person account edit launched in account creation mode when all fields are filled on save clicked but username exists should show error")
    @Test
    fun givenPersonAccountInAccountCreationMode_whenAllFieldsAreFilledAndSaveClickedButUsernameExists_thenShouldShowError(){

        init{
            enqueueResponse(false, 409)
        }.run {
            val person = createPerson(false, isAdmin = false, matchPassword = true)
            PersonAccountEditScreen{

                val fragmentScenario = launchFragment(person, true, fillUsername = true,
                        fillCurrentPassword = false, fillNewPassword = true,
                        systemImplNavRule = systemImplNavRule, serverUrl = serverUrl)

                dbRule.db.personDao.findByUidLive(person.personUid).waitUntilWithFragmentScenario(fragmentScenario) {
                    it?.username != null
                }

                usernameTextInput{
                    hasInputLayoutError(context.getString(R.string.person_exists))
                }

            }

        }

    }


    @AdbScreenRecord("given person account edit launched in account creation mode when password doesn't match on save clicked should show errors")
    @Test
    fun givenPersonAccountInAccountCreationMode_whenPasswordFieldDoNotMatchAndSaveClicked_thenShouldShowErrors(){

        init{
            enqueueResponse()
        }.run {
            val person = createPerson(false, isAdmin = false, matchPassword = false)
            PersonAccountEditScreen{

                launchFragment(person, true, fillUsername = true, fillCurrentPassword = false,
                        fillNewPassword = true,
                        systemImplNavRule = systemImplNavRule, serverUrl = serverUrl)


                newPasswordTextInput{
                    hasInputLayoutError(context.getString(R.string.filed_password_no_match))
                }
                confirmNewPassTextInput{
                    hasInputLayoutError(context.getString(R.string.filed_password_no_match))
                }

            }

        }

    }

}
