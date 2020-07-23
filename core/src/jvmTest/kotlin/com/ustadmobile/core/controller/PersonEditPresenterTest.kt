package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
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


class PersonEditPresenterTest  {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: PersonEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private val timeoutInMill: Long = 5000

    private lateinit var accountManager: UstadAccountManager

    private lateinit var di: DI

    private lateinit var mockWebServer: MockWebServer

    private lateinit var mockDao:PersonDao

    private lateinit var repo: UmAppDatabase

    private lateinit var serverUrl: String


    @Before
    fun setUp() {
        context = Any()
        mockLifecycleOwner = mock { }

        mockView = mock{}


        mockWebServer = MockWebServer()
        mockWebServer.start()

        serverUrl = mockWebServer.url("/").toString()

        accountManager = mock{
            on{activeAccount}.thenReturn(UmAccount(0L,"","",serverUrl))
        }

        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadAccountManager>(overrides = true) with singleton { accountManager }
        }

        repo = di.directActiveRepoInstance()
        mockDao = spy(repo.personDao)
        whenever(repo.personDao).thenReturn(mockDao)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }


    private fun createPerson(matchPassword: Boolean = true, leftOutPassword: Boolean = false): PersonWithAccount {
        val password = "password"
        val confirmPassword = if(matchPassword) password else "password1"
        return PersonWithAccount().apply {
            fatherName = "Doe"
            firstNames = "Jane"
            lastName = "Doe"
            if(!leftOutPassword){
                newPassword = password
                confirmedPassword = confirmPassword
            }
        }
    }

    @Test
    fun givenPresenterCreatedInRegistrationMode_whenUsernameAndPasswordNotFilledClickSave_shouldShowErrors() {
        val args = mapOf(UstadView.ARG_WORKSPACE to Json.stringify(WorkSpace.serializer(), WorkSpace().apply {
            registrationAllowed = true
        }))

        val person = createPerson(leftOutPassword = true)
        val presenter = PersonEditPresenter(context, args,mockView, di,mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickSave(person)

        argumentCaptor<Boolean>().apply {
            verify(mockView, timeout(timeoutInMill)).passwordRequiredErrorVisible = capture()
            verify(mockView, timeout(timeoutInMill)).usernameRequiredErrorVisible = capture()
            assertEquals("Required password field errors were shown", true, firstValue)
            assertEquals("Required username field errors were shown", true, secondValue)
        }

    }

    @Test
    fun givenPresenterCreatedInRegistrationMode_whenPasswordAndConfirmPasswordDoesNotMatchClickSave_shouldShowErrors() {
        val args = mapOf(UstadView.ARG_WORKSPACE to Json.stringify(WorkSpace.serializer(), WorkSpace().apply {
            registrationAllowed = true
        }))

        val person = createPerson(false).apply {
            username = "dummyUsername"
        }

        val presenter = PersonEditPresenter(context, args,mockView, di,mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickSave(person)

        argumentCaptor<Boolean>().apply {
            verify(mockView, timeout(timeoutInMill)).noMatchPasswordErrorVisible = capture()
            assertEquals("Password doesn't match field errors were shown", true, firstValue)
        }

    }


    @Test
    fun givenPresenterCreatedInRegistrationMode_whenFormFilledAndClickSave_shouldRegisterAPerson() {

        mockWebServer.enqueue(MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(Buffer().write(Json.stringify(UmAccount.serializer(),
                        UmAccount(0L)).toByteArray())))

        val args = mapOf(UstadView.ARG_WORKSPACE to Json.stringify(WorkSpace.serializer(), WorkSpace().apply {
            registrationAllowed = true
        }), ARG_SERVER_URL to serverUrl)

        val person = createPerson().apply {
            username = "dummyUsername"
        }
        val presenter = PersonEditPresenter(context, args,mockView, di,mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickSave(person)

        argumentCaptor<PersonWithAccount>().apply {
            verifyBlocking(accountManager, timeout(timeoutInMill)){
                register(capture(), eq(serverUrl), eq(true))
                assertEquals("Person registration was done", person.personUid, firstValue.personUid)
            }
        }

    }

    @Test
    fun givenPresenterCreatedInNonRegistrationMode_whenFormFilledAndClickSave_shouldSaveAPersonInDb() {
        val args = mapOf(UstadView.ARG_WORKSPACE to Json.stringify(WorkSpace.serializer(), WorkSpace().apply {
            registrationAllowed = false
        }))

        val person = createPerson().apply {
            username = "dummyUsername"
        }
        val presenter = PersonEditPresenter(context, args,mockView, di,mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickSave(person)

        argumentCaptor<Person>().apply {
            verifyBlocking(mockDao, timeout(timeoutInMill)){
                insertAsync(capture())
                assertEquals("Person saved in the db", person, firstValue)
            }
        }
    }

}