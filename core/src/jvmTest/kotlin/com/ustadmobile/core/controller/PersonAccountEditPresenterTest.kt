package com.ustadmobile.core.controller

import com.google.gson.Gson
import com.ustadmobile.core.account.AccountRegisterOptions
import com.ustadmobile.core.account.Endpoint
import org.mockito.kotlin.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.view.PersonAccountEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.lib.db.entities.UmAccount
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton


class PersonAccountEditPresenterTest  {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: PersonAccountEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var di: DI

    private lateinit var mockWebServer: MockWebServer

    private lateinit var repo: UmAppDatabase

    private lateinit var serverUrl: String

    private val defaultTimeOut: Long = 5000

    private val mPersonUid: Long = 234567

    private val loggedInPersonUid:Long = 234568

    private lateinit var accountManager: UstadAccountManager

    private lateinit var impl: UstadMobileSystemImpl

    @Before
    fun setUp() {
        context = Any()
        mockLifecycleOwner = mock { }

        mockView = mock{}
        impl = mock()

        mockWebServer = MockWebServer()
        mockWebServer.start()

        serverUrl = mockWebServer.url("/").toString()

        accountManager = mock{
            on { activeEndpoint }.thenReturn(Endpoint(serverUrl))
            on{activeAccount}.thenReturn(UmAccount(loggedInPersonUid,"","",serverUrl))
        }

        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadAccountManager>(overrides = true) with singleton { accountManager }
            bind<UstadMobileSystemImpl>(overrides = true) with singleton { impl }
        }

        repo = di.directActiveRepoInstance()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun enQueuePasswordChangeResponse(success: Boolean = true){
        if(success){
            mockWebServer.enqueue(MockResponse()
                    .setResponseCode(200)
                    .setBody(Gson().toJson(UmAccount(mPersonUid, "", "auth", "")))
                    .setHeader("Content-Type", "application/json"))
        }else{
            mockWebServer.enqueue(MockResponse().setResponseCode(403))
        }
    }


    private fun createPerson(withUsername: Boolean = false, isAdmin: Boolean = false,
                             matchPassword: Boolean = false): PersonWithAccount {
        val password = "password"
        val confirmPassword = if(matchPassword) password else "password1"

        val person =  PersonWithAccount().apply {
            fatherName = "Doe"
            firstNames = "Jane"
            lastName = "Doe"
            if(withUsername){
                username = "dummyUserName"
            }
            personUid = mPersonUid
            newPassword = password
            confirmedPassword = confirmPassword
            runBlocking { repo.insertPersonAndGroup(this@apply) }
        }


        //Create an person object for the logged in person
        val loggedInPerson = PersonWithAccount().apply {
            admin = isAdmin
            username = "First"
            lastName = "User"
            personUid = loggedInPersonUid
            runBlocking { repo.insertPersonAndGroup(this@apply)}
        }

        if(isAdmin) {
            runBlocking {
                repo.grantScopedPermission(loggedInPerson.personGroupUid, Role.ALL_PERMISSIONS,
                    ScopedGrant.ALL_TABLES, ScopedGrant.ALL_ENTITIES)
            }
        }

        return person
    }

    @Test
    fun givenPersonAccountEditLaunched_whenActivePersonIsNotAdmin_thenShouldShowCurrentPassword(){
        val person = createPerson(true)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        val presenter = PersonAccountEditPresenter(context, args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(defaultTimeOut).atLeastOnce()).currentPasswordVisible = eq(true)
    }


    @Test
    fun givenPersonAccountEditLaunched_whenActivePersonIsAdmin_thenShouldHideCurrentPassword(){
        val person = createPerson(true, isAdmin = true)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        val presenter = PersonAccountEditPresenter(context, args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(defaultTimeOut).atLeastOnce()).currentPasswordVisible = eq(false)
    }

    @Test
    fun givenPresenterCreated_whenUsernameIsNullAndSaveClicked_shouldShowErrors(){
        val person = createPerson(false)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        val presenter = PersonAccountEditPresenter(context, args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(defaultTimeOut).atLeastOnce()).entity = any()

        presenter.handleClickSave(person)

        val expectedMessage = impl.getString(MessageID.field_required_prompt, context)

        verify(mockView, timeout(defaultTimeOut).atLeastOnce()).usernameError = eq(expectedMessage)
    }

    @Test
    fun givenPresenterCreated_whenUserIsNotAdminAndSaveClicked_shouldShowErrors() {
        val person = createPerson()
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        val presenter = PersonAccountEditPresenter(context,args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(defaultTimeOut).atLeastOnce()).entity = any()
        presenter.handleClickSave(person)

        val expectedMessage = impl.getString(MessageID.field_required_prompt, context)
        argumentCaptor<String>{
            verify(mockView, timeout(defaultTimeOut).atLeastOnce()).currentPasswordError = capture()
            assertEquals("Current password field error was shown", expectedMessage, firstValue)
        }
    }

    @Test
    fun givenPresenterCreated_whenNewPasswordIsNotFilledAndSaveClicked_thenShouldShowError(){
        val person = createPerson(isAdmin = true)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        val presenter = PersonAccountEditPresenter(context, args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(defaultTimeOut).atLeastOnce()).entity = any()
        presenter.handleClickSave(person)
        val expectedMessage = impl.getString(MessageID.field_required_prompt, context)
        verify(mockView, timeout(defaultTimeOut).atLeastOnce()).newPasswordError = eq(expectedMessage)
    }

    @Test
    fun givenPresenterCreatedInPasswordResetMode_whenAllFieldsAreFilledAndSaveClicked_thenShouldChangePassword(){
        enQueuePasswordChangeResponse()

        val person = createPerson(isAdmin = true, withUsername = true, matchPassword = true)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString(),
                UstadView.ARG_SERVER_URL to serverUrl)
        val presenter = PersonAccountEditPresenter(context, args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(defaultTimeOut).atLeastOnce()).entity = any()

        person.newPassword = "new"
        person.confirmedPassword = "new"
        presenter.handleClickSave(person)

        verifyBlocking(accountManager, timeout(defaultTimeOut)) {
            changePassword(eq(person.username!!), anyOrNull(), eq(person.newPassword!!),
                    eq(serverUrl))
        }

        verify(mockView, timeout(defaultTimeOut)).finishWithResult(any())
    }


    @Test
    fun givenPresenterCreatedInAccountCreationMode_whenAllFieldsAreFilledAndSaveClicked_thenShouldCreateAnAccount(){
        enQueuePasswordChangeResponse()

        val person = createPerson(isAdmin = false, withUsername = false, matchPassword = true)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString(),
                UstadView.ARG_SERVER_URL to serverUrl)
        val presenter = PersonAccountEditPresenter(context, args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(defaultTimeOut).atLeastOnce()).entity = any()

        presenter.handleClickSave(person.apply { username = "username" })
        verifyBlocking(accountManager, timeout(defaultTimeOut).atLeastOnce()){
            register(any(), any(), any<AccountRegisterOptions>())
        }
    }


    @Test
    fun givenPresenterCreatedInAccountCreationMode_whenPasswordDoNotMatchAndSaveClicked_thenShouldErrors(){
        enQueuePasswordChangeResponse()

        val person = createPerson(isAdmin = true, withUsername = false, matchPassword = false)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString(),
                UstadView.ARG_SERVER_URL to serverUrl)
        val presenter = PersonAccountEditPresenter(context, args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(defaultTimeOut).atLeastOnce()).entity = any()

        presenter.handleClickSave(person.apply { username = "username" })
        val expectedMessage = impl.getString(MessageID.filed_password_no_match, context)
        verify(mockView, timeout(defaultTimeOut).atLeastOnce()).noPasswordMatchError = eq(expectedMessage)
    }

}