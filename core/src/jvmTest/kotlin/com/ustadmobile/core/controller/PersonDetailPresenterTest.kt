
package com.ustadmobile.core.controller

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import org.mockito.kotlin.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

class PersonDetailPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: PersonDetailView

    private lateinit var context: Any

    private lateinit var accountManager: UstadAccountManager

    private val defaultTimeout:Long = 5000

    private lateinit var impl: UstadMobileSystemImpl

    private val  activeAccountLive = MutableLiveData<UmAccount>()

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var di: DI

    private lateinit var mockWebServer: MockWebServer

    private lateinit var repo: UmAppDatabase

    private lateinit var serverUrl: String

    @Before
    fun setup() {
        context = Any()
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)

        mockView = mock{}
        impl = mock()

        mockWebServer = MockWebServer()
        mockWebServer.start()

        serverUrl = mockWebServer.url("/").toString()

        di = DI {
            import(ustadTestRule.diModule)
        }

        repo = di.directActiveRepoInstance()


    }

    private fun createPerson(isAdmin: Boolean = true, withUsername: Boolean = true, sameUser: Boolean = false): Person{
        val mPersonUid:Long = 121212
        val loggedPersonUid: Long = 42

        val activeAccountUid = if(sameUser) mPersonUid else loggedPersonUid
        accountManager = mock{
            on{activeAccount}.thenReturn(UmAccount(activeAccountUid,"","",serverUrl))
            on{activeAccountLive}.thenReturn(activeAccountLive)
            on { activeSession }.thenReturn(UserSessionWithPersonAndEndpoint(
                UserSession().apply {
                    usPersonUid = activeAccountUid
                    usStatus = UserSession.STATUS_ACTIVE
                },
                Person().apply {
                    personUid = activeAccountUid
                    username = "tester"
                    firstNames = "test"
                    lastName = "user"
                },
                Endpoint(serverUrl)
            ))

            on { activeEndpoint }.thenReturn(Endpoint(serverUrl))
        }

        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadAccountManager>(overrides = true) with singleton { accountManager }
            bind<UstadMobileSystemImpl>(overrides = true) with singleton { impl }
        }

        repo = di.directActiveRepoInstance()

        val person = Person().apply {
            fatherName = "Doe"
            firstNames = "Jane"
            lastName = "Doe"
            username = if(withUsername) "jane.Doe" else null
            personUid = mPersonUid

        }

        repo.insertPersonOnlyAndGroup(person)

        if(!sameUser){
            runBlocking {
                val loggedInPerson = repo.insertPersonAndGroup(Person().apply {
                    admin = isAdmin
                    username = "Admin"
                    lastName = "User"
                    personUid = activeAccountUid
                })

                if(isAdmin) {
                    repo.grantScopedPermission(loggedInPerson, Role.ALL_PERMISSIONS,
                        ScopedGrant.ALL_TABLES, ScopedGrant.ALL_ENTITIES)
                }
            }
        }
        return person
    }

    @Test
    fun givenPersonDetails_whenPersonUsernameIsNullAndCanManageAccount_thenCreateAccountShouldBeHidden(){
        val person = createPerson(withUsername = false, isAdmin = false)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        val presenter = PersonDetailPresenter(context, args,mockView,di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(defaultTimeout).atLeastOnce()).changePasswordVisible = eq(false)
        verify(mockView, timeout(defaultTimeout).atLeastOnce()).showCreateAccountVisible = eq(false)
    }

    @Test
    fun givenPersonDetailsAndAdminLogged_whenPersonUsernameIsNullAndCanManageAccount_thenCreateAccountShouldBeShown(){
        val person = createPerson(withUsername = false)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        val presenter = PersonDetailPresenter(context, args,mockView,di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(defaultTimeout).atLeastOnce()).changePasswordVisible = eq(false)
        verify(mockView, timeout(defaultTimeout).atLeastOnce()).showCreateAccountVisible = eq(true)
    }

    @Test
    fun givenPersonDetailsAndAdminLogged_whenPersonUsernameIsNotNullAndCanManageAccount_thenChangePasswordShouldBeShown(){
        val person = createPerson(isAdmin = true)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        val presenter = PersonDetailPresenter(context, args,mockView,di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(defaultTimeout).atLeastOnce()).changePasswordVisible = eq(true)
        verify(mockView, timeout(defaultTimeout).atLeastOnce()).showCreateAccountVisible = eq(false)
    }

    @Test
    fun givenPersonDetails_whenOpenedActivePersonDetailPersonAndCanManageAccount_thenChangePasswordShouldBeShown(){
        val person = createPerson(isAdmin = false, sameUser = true)
        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        val presenter = PersonDetailPresenter(context, args,mockView,di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(defaultTimeout).atLeastOnce()).changePasswordVisible = eq(true)
        verify(mockView, timeout(defaultTimeout).atLeastOnce()).showCreateAccountVisible = eq(false)
    }

    @Test
    fun givenActiveUserIsParent_whenOpenChildProfile_thenShouldShowManageParentalConsent() {
        val person = createPerson(isAdmin = false, sameUser = true)

        val child = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                firstNames = "Bob"
                lastName = "Young"
                dateOfBirth = systemTimeInMillis() - (10 * 365 * 24 * 60 * 60 * 1000L)
                username = "young"
            })
        }

        runBlocking {
            repo.personParentJoinDao.insertAsync(PersonParentJoin().apply {
                ppjMinorPersonUid = child.personUid
                ppjParentPersonUid = person.personUid
                ppjRelationship = PersonParentJoin.RELATIONSHIP_MOTHER
            })
        }

        val args = mapOf(UstadView.ARG_ENTITY_UID to child.personUid.toString())
        val presenter = PersonDetailPresenter(context, args ,mockView,di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(5000).atLeastOnce()).entity = argWhere {
            it.parentJoin != null
        }
    }

    @Test
    fun givenActiveUserIsNotParent_whenOpenChildProfile_thenShouldShowManageParentalConsent() {
        createPerson(isAdmin = false, sameUser = true)

        val child = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                firstNames = "Bob"
                lastName = "Young"
                dateOfBirth = systemTimeInMillis() - (10 * 365 * 24 * 60 * 60 * 1000L)
                username = "young"
            })
        }


        val args = mapOf(UstadView.ARG_ENTITY_UID to child.personUid.toString())
        val presenter = PersonDetailPresenter(context, args ,mockView,di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(5000).atLeastOnce()).entity = argWhere {
            it.parentJoin == null
        }
    }
}