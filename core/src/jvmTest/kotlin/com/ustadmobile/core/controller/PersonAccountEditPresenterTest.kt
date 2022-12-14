package com.ustadmobile.core.controller

import com.ustadmobile.core.account.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.view.PersonAccountEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.lib.db.entities.UmAccount
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.kodein.di.*
import org.mockito.kotlin.*


class PersonAccountEditPresenterTest  {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: PersonAccountEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var di: DI

    private lateinit var repo: UmAppDatabase

    private lateinit var serverUrl: String

    private val defaultTimeOut: Long = 5000

    private val mPersonUid: Long = 234567

    private val loggedInPersonUid:Long = 234568

    private lateinit var accountManager: UstadAccountManager

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var mockAuthManager: AuthManager

    private lateinit var mockNavController: UstadNavController

    @Before
    fun setUp() {
        context = Any()
        mockLifecycleOwner = mock { }

        mockView = mock{}
        impl = mock()


        serverUrl = "https://dummysite.ustadmobile.app/"

        accountManager = mock{
            on { activeEndpoint }.thenReturn(Endpoint(serverUrl))
            on{activeAccount}.thenReturn(UmAccount(loggedInPersonUid,"","",serverUrl))
        }

        mockAuthManager = mock {

        }

        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadAccountManager>(overrides = true) with singleton { accountManager }
            bind<UstadMobileSystemImpl>(overrides = true) with singleton { impl }
            bind<AuthManager>() with singleton { mockAuthManager }
        }

        repo = di.directActiveRepoInstance()
        mockNavController = di.direct.instance()
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

        val person = createPerson(isAdmin = true, withUsername = true, matchPassword = true)

        mockAuthManager.stub {
            onBlocking { authenticate(eq(person.username!!), eq("existingpassword"), any()) }
                .thenReturn(AuthResult(person, true))
        }

        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString(),
            UstadView.ARG_RESULT_DEST_VIEWNAME   to "",
            UstadView.ARG_RESULT_DEST_KEY to "",
                UstadView.ARG_SERVER_URL to serverUrl)
        val presenter = PersonAccountEditPresenter(context, args,mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(defaultTimeOut).atLeastOnce()).entity = any()

        person.newPassword = "new"
        person.confirmedPassword = "new"
        presenter.handleClickSave(person.also {
            it.currentPassword = "existingpassword"
        })

        verifyBlocking(mockAuthManager, timeout(defaultTimeOut)) {
            setAuth(eq(person.personUid), eq(person.newPassword!!))
        }

        verify(mockNavController, timeout(defaultTimeOut).times(1)).popBackStack(any(), any())
    }


    @Test
    fun givenPresenterCreatedInAccountCreationMode_whenAllFieldsAreFilledAndSaveClicked_thenShouldCreateAnAccount(){
        val person = createPerson(isAdmin = false, withUsername = false, matchPassword = true)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString(),
                UstadView.ARG_SERVER_URL to serverUrl)
        val presenter = PersonAccountEditPresenter(context, args, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(defaultTimeOut).atLeastOnce()).entity = any()

        presenter.handleClickSave(person.apply { username = "username" })
        verifyBlocking(mockAuthManager, timeout(defaultTimeOut)) {
            setAuth(eq(person.personUid), eq(person.newPassword!!))
        }

        val personInDb = runBlocking { repo.personDao.findByUid(person.personUid) }
        Assert.assertEquals("Person now has username set", person.username,
            personInDb?.username)
    }


    @Test
    fun givenPresenterCreatedInAccountCreationMode_whenPasswordDoNotMatchAndSaveClicked_thenShouldErrors(){
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