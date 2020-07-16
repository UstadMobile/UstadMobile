package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.view.PersonAccountEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import junit.framework.Assert.assertEquals
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
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

    private val defaultTimeOut: Long = 2000

    private val mPersonUid: Long = 234567

    private lateinit var accountManager: UstadAccountManager


    @Before
    fun setUp() {
        context = Any()
        mockLifecycleOwner = mock { }

        mockView = mock{}


        mockWebServer = MockWebServer()
        mockWebServer.start()

        serverUrl = mockWebServer.url("/").toString()

        accountManager = mock{
            on{activeAccount}.thenReturn(UmAccount(mPersonUid,"","",serverUrl))
        }

        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadAccountManager>(overrides = true) with singleton { accountManager }
        }

        repo = di.directActiveRepoInstance()
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
            verify(mockView, timeout(defaultTimeOut).atLeastOnce()).currentPasswordRequiredErrorVisible = capture()
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
            verify(mockView, timeout(defaultTimeOut).atLeastOnce()).newPasswordRequiredErrorVisible = capture()
            assertEquals("Password field error was shown", true, firstValue)
        }
    }

    @Test
    fun givenPresenterCreated_whenAllFieldsAreFilledAndSaveClicked_thenShouldChangePassword(){

        mockWebServer.enqueue(MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(Buffer().write(Json.stringify(UmAccount.serializer(),
                        UmAccount(0L)).toByteArray())))

        mockView = mock{
            on{currentPassword}.thenReturn("oldPassword")
            on{newPassword}.thenReturn("password")
        }
        val person = createPerson(isAdmin = true, withUsername = true)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        val presenter = PersonAccountEditPresenter(context, args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        presenter.handleClickSave(person)
        argumentCaptor<String>{
            verifyBlocking(accountManager, timeout(defaultTimeOut).atLeastOnce()){
                changePassword(capture(), any(), any(), any())
                assertEquals("Password change request was sent", person.username, firstValue)
            }
        }
    }

}