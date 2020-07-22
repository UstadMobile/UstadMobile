package com.ustadmobile.core.controller

import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.view.PersonAccountEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import junit.framework.Assert.assertEquals
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import java.lang.Thread.sleep


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
            on{activeAccount}.thenReturn(UmAccount(mPersonUid,"","",serverUrl))
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
            repo.personDao.insert(this)
        }
    }

    @Test
    fun givenPresenterCreated_whenUsernameIsNullAndSaveClicked_shouldShowErrors(){
        val person = createPerson(false)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        val presenter = PersonAccountEditPresenter(context, args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        presenter.handleClickSave(person)
        argumentCaptor<Boolean>{
            verify(mockView, timeout(defaultTimeOut).atLeastOnce()).usernameRequiredErrorVisible = capture()
            assertEquals("Username field error was shown", true, firstValue)
        }
    }

    @Test
    fun givenPresenterCreated_whenUserIsNotAdminAndSaveClicked_shouldShowErrors() {
        val person = createPerson()
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        val presenter = PersonAccountEditPresenter(context,args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        presenter.handleClickSave(person)
        argumentCaptor<Boolean>{
            verify(mockView, timeout(defaultTimeOut).atLeastOnce()).firstPasswordFieldRequiredErrorVisible = capture()
            assertEquals("Current password field error was shown", true, firstValue)
        }
    }

    @Test
    fun givenPresenterCreated_whenNewPasswordIsNotFilledAndSaveClicked_thenShouldShowError(){
        val person = createPerson(isAdmin = true)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        val presenter = PersonAccountEditPresenter(context, args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        presenter.handleClickSave(person)
        argumentCaptor<Boolean>{
            verify(mockView, timeout(defaultTimeOut).atLeastOnce()).secondPasswordFieldRequiredErrorVisible = capture()
            assertEquals("Password field error was shown", true, firstValue)
        }
    }

    @Test
    fun givenPresenterCreatedInPasswordResetMode_whenAllFieldsAreFilledAndSaveClicked_thenShouldChangePassword(){
        enQueuePasswordChangeResponse()

        mockView = mock{
            on{firstPassword}.thenReturn("oldPassword")
            on{secondPassword}.thenReturn("password")
        }
        val person = createPerson(isAdmin = true, withUsername = true)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString(),
                UstadView.ARG_SERVER_URL to serverUrl)
        val presenter = PersonAccountEditPresenter(context, args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        presenter.handleClickSave(person)
        argumentCaptor<String>{
            verifyBlocking(accountManager, timeout(defaultTimeOut).atLeastOnce()){
                changePassword(capture(), any(), any(), any())
                assertEquals("Password change task was started", person.username, firstValue)
            }
        }
        verify(mockView, timeout(defaultTimeOut)).finishWithResult(any())
    }


    @Test
    fun givenPresenterCreatedInAccountCreationMode_whenAllFieldsAreFilledAndSaveClicked_thenShouldChangePassword(){
        enQueuePasswordChangeResponse()

        mockView = mock{
            on{firstPassword}.thenReturn("password")
            on{secondPassword}.thenReturn("password")
        }
        val person = createPerson(isAdmin = true, withUsername = false)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString(),
                UstadView.ARG_SERVER_URL to serverUrl)
        val presenter = PersonAccountEditPresenter(context, args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        //wait for onLoadEntityFromDb to be called
        sleep(1000)
        presenter.handleClickSave(person.apply { username = "username" })
        verifyBlocking(accountManager, timeout(defaultTimeOut).atLeastOnce()){
            register(any(), any(), any(), any())
        }
    }


    @Test
    fun givenPresenterCreatedInAccountCreationMode_whenPasswordDoNotMatchAndSaveClicked_thenShouldErrors(){
        enQueuePasswordChangeResponse()

        mockView = mock{
            on{firstPassword}.thenReturn("oldPassword")
            on{secondPassword}.thenReturn("password")
        }
        val person = createPerson(isAdmin = true, withUsername = false)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString(),
                UstadView.ARG_SERVER_URL to serverUrl)
        val presenter = PersonAccountEditPresenter(context, args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //wait for onLoadEntityFromDb to be called
        sleep(1000)
        presenter.handleClickSave(person.apply { username = "username" })
        verify(mockView, timeout(defaultTimeOut).atLeastOnce()).showPasswordDoNotMatchError()
    }

}