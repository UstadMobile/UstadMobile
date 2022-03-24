package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.soywiz.klock.DateTime
import com.soywiz.klock.years
import com.ustadmobile.core.account.AccountRegisterOptions
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.EntityRoleDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.RegisterMinorWaitForParentView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.UmAccount
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.*
import org.kodein.di.*


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

    private lateinit var impl: UstadMobileSystemImpl

    @Before
    fun setUp() {
        context = Any()
        mockLifecycleOwner = mock { }

        mockView = mock{}
        impl = mock {
            on { getString(any(), any()) }.thenAnswer {
                it.arguments[0].toString()
            }
        }


        mockWebServer = MockWebServer()
        mockWebServer.start()

        serverUrl = mockWebServer.url("/").toString()

        accountManager = mock{
            on { activeEndpoint }.thenReturn(Endpoint(serverUrl))
            on{activeAccount}.thenReturn(UmAccount(0L,"","",serverUrl))
        }

        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadAccountManager>(overrides = true) with singleton { accountManager }
            bind<UstadMobileSystemImpl>(overrides = true) with singleton { impl }
        }

        repo = di.directActiveRepoInstance()
        mockDao = spy(repo.personDao)
        whenever(repo.personDao).thenReturn(mockDao)

    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }


    private fun createPerson(matchPassword: Boolean = true, leftOutPassword: Boolean = false,
                             leftOutDateOfBirth: Boolean = false,
                             selectedDateOfBirth: Long = DateTime(1990, 10, 24).unixMillisLong): PersonWithAccount {
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
            if(!leftOutDateOfBirth){
                dateOfBirth = selectedDateOfBirth
            }
        }
    }


    private fun createPersonAndInsert(matchPassword: Boolean = true, leftOutPassword: Boolean = false): PersonWithAccount {
        val password = "password"
        val confirmPassword = if(matchPassword) password else "password1"
        var personWithAccount =  PersonWithAccount().apply {
            fatherName = "Doe"
            firstNames = "Jane"
            lastName = "Doe"
            if(!leftOutPassword){
                newPassword = password
                confirmedPassword = confirmPassword
            }
        }

        GlobalScope.launch {
            personWithAccount = repo.insertPersonAndGroup(personWithAccount)
        }
        return personWithAccount
    }

    @Test
    fun givenPresenterCreatedInRegistrationMode_whenUsernameAndPasswordNotFilledClickSave_shouldShowErrors() {
        val args = mapOf(PersonEditView.ARG_REGISTRATION_MODE to PersonEditView.REGISTER_MODE_ENABLED.toString())

        val person = createPerson(leftOutPassword = true)
        val presenter = PersonEditPresenter(context, args,mockView, di,mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickSave(person)
        val expectedMessage = impl.getString(MessageID.field_required_prompt, context)

        verify(mockView, timeout(timeoutInMill)).passwordError = eq(expectedMessage)
        verify(mockView, timeout(timeoutInMill)).usernameError = eq(expectedMessage)

    }

    @Test
    fun givenPresenterCreatedInRegistrationMode_whenDateOfBirthNotFilledClickSave_shouldShowErrors() {
        val args = mapOf(PersonEditView.ARG_REGISTRATION_MODE to PersonEditView.REGISTER_MODE_ENABLED.toString())

        val person = createPerson(leftOutDateOfBirth = true)
        val presenter = PersonEditPresenter(context, args,mockView, di,mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickSave(person)
        val expectedMessage = impl.getString(MessageID.field_required_prompt, context)

        verify(mockView, timeout(timeoutInMill)).dateOfBirthError = eq(expectedMessage)

    }

    @Test
    fun givenPresenterCreatedInRegistrationMode_whenPasswordAndConfirmPasswordDoesNotMatchClickSave_shouldShowErrors() {
        val args = mapOf(PersonEditView.ARG_REGISTRATION_MODE to PersonEditView.REGISTER_MODE_ENABLED.toString())

        val person = createPerson(false).apply {
            username = "dummyUsername"
        }

        val presenter = PersonEditPresenter(context, args,mockView, di,mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickSave(person)
        val expectedMessage = impl.getString(MessageID.filed_password_no_match, context)
        verify(mockView, timeout(timeoutInMill)).noMatchPasswordError = eq(expectedMessage)

    }


    @Test
    fun givenPresenterCreatedInRegistrationMode_whenFormFilledAndClickSave_shouldRegisterAPerson() {

        mockWebServer.enqueue(MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(Buffer().write(Json.encodeToString(UmAccount.serializer(),
                        UmAccount(0L)).toByteArray())))

        val args = mapOf(
                PersonEditView.ARG_REGISTRATION_MODE to PersonEditView.REGISTER_MODE_ENABLED.toString(),
                ARG_SERVER_URL to serverUrl)

        val person = createPerson().apply {
            username = "dummyUsername"
        }
        val presenter = PersonEditPresenter(context, args,mockView, di,mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickSave(person)

        verifyBlocking(accountManager, timeout(timeoutInMill)) {
            register(argWhere { it.personUid == person.personUid }, eq(serverUrl),
                argWhere { it.makeAccountActive } )
        }
    }

    @Test
    fun givenPresenterCreatedInNonRegistrationMode_whenFormFilledAndClickSave_shouldSaveAPersonInDb() {
        val args = mapOf(UstadView.ARG_REGISTRATION_ALLOWED to false.toString())

        val person = createPerson().apply {
            username = "dummyUsername"
        }
        val presenter = PersonEditPresenter(context, args,mockView, di,mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickSave(person)

        verifyBlocking(mockDao, timeout(timeoutInMill)) {
            insertAsync(argWhere { it.username ==  "dummyUsername"})
        }
    }

    @Test
    fun givenPresenterCreatedInRegisterMinorMode_whenFormFilledAndClickSave_thenShouldGoToWaitForParentScreen() {
        val minorDateOfBirth = (DateTime.now() - 10.years).unixMillisLong
        val args = mapOf(PersonEditView.ARG_REGISTRATION_MODE to
                (PersonEditView.REGISTER_MODE_ENABLED or PersonEditView.REGISTER_MODE_MINOR).toString(),
                ARG_SERVER_URL to serverUrl,
                PersonEditView.ARG_DATE_OF_BIRTH to minorDateOfBirth.toString())

        val presenter = PersonEditPresenter(context, args, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        nullableArgumentCaptor<PersonParentJoin>() {
            verify(mockView, timeout(5000)).approvalPersonParentJoin = capture()
            firstValue?.ppjEmail = "parent@somewhere.com"
        }


        val personToRegister : PersonWithAccount = mockView.captureLastEntityValue(5000)!!
        personToRegister.apply {
            firstNames = "Jane"
            lastName = "Doe"
            username = "janedoe"
            newPassword = "secret"
            confirmedPassword = "secret"
        }

        presenter.handleClickSave(personToRegister)

        verifyBlocking(accountManager, timeout(timeoutInMill)) {
            register(argWhere { it.personUid == personToRegister.personUid },
                eq(serverUrl), argWhere<AccountRegisterOptions> { it.parentJoin?.ppjEmail == "parent@somewhere.com" })
        }

        argumentCaptor<Map<String, String>> {
            verify(impl, timeout(5000)).go(eq(RegisterMinorWaitForParentView.VIEW_NAME),
                    capture(), any(), any())
            Assert.assertEquals("Arg for username was provided", personToRegister.username,
                    firstValue[RegisterMinorWaitForParentView.ARG_USERNAME])
            Assert.assertEquals("Arg for parent contact was provided", "parent@somewhere.com",
                firstValue[RegisterMinorWaitForParentView.ARG_PARENT_CONTACT])
        }
    }

    @Test
    fun givenPresenterCreatedInRegisterMinorMode_whenNoParentEmailGiven_thenShouldShowFieldRequiredError() {
        val minorDateOfBirth = (DateTime.now() - 10.years).unixMillisLong
        val args = mapOf(PersonEditView.ARG_REGISTRATION_MODE to
                (PersonEditView.REGISTER_MODE_ENABLED or PersonEditView.REGISTER_MODE_MINOR).toString(),
                PersonEditView.ARG_DATE_OF_BIRTH to minorDateOfBirth.toString())

        val presenter = PersonEditPresenter(context, args, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        val personToRegister = mockView.captureLastEntityValue(5000)!!
        personToRegister.apply {
            //TODO
            firstNames = "Jane"
            lastName = "Doe"
            username = "janedoe"
            newPassword = "secret"
            confirmedPassword = "secret"
        }

        val systemImpl : UstadMobileSystemImpl = di.direct.instance()


        presenter.handleClickSave(personToRegister)

        val fieldRequiredErr =systemImpl.getString(MessageID.field_required_prompt, context)

        verify(mockView, timeout(5000)).parentContactError = fieldRequiredErr
        mockView.stub {
            on { parentContactError }.thenReturn(fieldRequiredErr)
        }

        verify(mockView, timeout(5000)).parentContactError = impl.getString(
                MessageID.field_required_prompt, context)
    }




}