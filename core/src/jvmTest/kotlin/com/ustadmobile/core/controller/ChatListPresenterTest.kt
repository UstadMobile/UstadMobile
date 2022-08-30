
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.core.db.dao.ChatDao
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.test.ext.startLocalTestSessionBlocking
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class ChatListPresenterTest {


    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ChatListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoChatDaoSpy: ChatDao

    private lateinit var di: DI

    var loggedInTestUser: Person? = null

    var chatTestUserPersonOne: Chat? = null

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()
        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoChatDaoSpy = spy(repo.chatDao)
        whenever(repo.chatDao).thenReturn(repoChatDaoSpy)
    }



    private fun addSomeChats(){
        val repo: UmAppDatabase by di.activeRepoInstance()
        val accountManager: UstadAccountManager by di.instance<UstadAccountManager>()


        loggedInTestUser = Person("testuser", "Test", "User").apply {
            personUid = repo.personDao.insert(this)
        }
        val personOne = Person("personone", "Person", "One").apply {
            personUid = repo.personDao.insert(this)
        }
        val personTwo = Person("persontwo", "Person", "Two").apply {
            personUid = repo.personDao.insert(this)
        }
        val personThree = Person("personthree", "Person", "Three").apply {
            personUid = repo.personDao.insert(this)
        }
        val personFour = Person("personfour", "Person", "Four").apply {
            personUid = repo.personDao.insert(this)
        }

        val chatPersonOnePersonTwo = Chat("", false, systemTimeInMillis()).apply {
            chatUid = repo.chatDao.insert(this)
        }
        val chatPersonTwoPersonThree = Chat("", false, systemTimeInMillis()).apply {
            chatUid = repo.chatDao.insert(this)
        }
        chatTestUserPersonOne = Chat("", false, systemTimeInMillis()).apply {
            chatUid = repo.chatDao.insert(this)
        }
        val chatTestUserPersonTwo = Chat("", false, systemTimeInMillis()).apply {
            chatUid = repo.chatDao.insert(this)
        }
        val chatGroupPersonOnePersonTwoPersonFour = Chat("", true, systemTimeInMillis()).apply {
            chatUid = repo.chatDao.insert(this)
        }
        val chatGroupTestUserPersonOnePersonThree = Chat("", true, systemTimeInMillis()).apply {
            chatUid = repo.chatDao.insert(this)
        }


        //chatPersonOnePersonTwo
        repo.chatMemberDao.insert(ChatMember(chatPersonOnePersonTwo.chatUid, personOne.personUid))
        repo.chatMemberDao.insert(ChatMember(chatPersonOnePersonTwo.chatUid, personTwo.personUid))

        //chatPersonTwoPersonThree
        repo.chatMemberDao.insert(ChatMember(chatPersonTwoPersonThree.chatUid, personTwo.personUid))
        repo.chatMemberDao.insert(ChatMember(chatPersonTwoPersonThree.chatUid, personThree.personUid))

        //chatTestUserPersonOne
        repo.chatMemberDao.insert(ChatMember(chatTestUserPersonOne!!.chatUid, loggedInTestUser!!.personUid))
        repo.chatMemberDao.insert(ChatMember(chatTestUserPersonOne!!.chatUid, personOne.personUid))

        //chatTestUserPersonTwo
        repo.chatMemberDao.insert(ChatMember(chatTestUserPersonTwo.chatUid, loggedInTestUser!!.personUid))
        repo.chatMemberDao.insert(ChatMember(chatTestUserPersonTwo.chatUid, personTwo.personUid))

        //chatGroupPersonOnePersonTwoPersonFour
        repo.chatMemberDao.insert(ChatMember(chatGroupPersonOnePersonTwoPersonFour.chatUid, personOne.personUid))
        repo.chatMemberDao.insert(ChatMember(chatGroupPersonOnePersonTwoPersonFour.chatUid, personTwo.personUid))
        repo.chatMemberDao.insert(ChatMember(chatGroupPersonOnePersonTwoPersonFour.chatUid, personFour.personUid))

        //chatGroupTestUserPersonOnePersonThree
        repo.chatMemberDao.insert(ChatMember(chatGroupTestUserPersonOnePersonThree.chatUid, loggedInTestUser!!.personUid))
        repo.chatMemberDao.insert(ChatMember(chatGroupTestUserPersonOnePersonThree.chatUid, personOne.personUid))
        repo.chatMemberDao.insert(ChatMember(chatGroupTestUserPersonOnePersonThree.chatUid, personThree.personUid))


        //Messages ?
        val messageList = listOf(
            //chatPersonOnePersonTwo
            Message(personOne.personUid, Chat.TABLE_ID, chatPersonOnePersonTwo.chatUid,
                    "Hello Two, I am Person One."),
            Message(personOne.personUid, Chat.TABLE_ID, chatPersonOnePersonTwo.chatUid,
                "Hello Two, are you there?"),
            Message(personTwo.personUid, Chat.TABLE_ID, chatPersonOnePersonTwo.chatUid,
                "Hi Person One, yes I am here. Nice to meet you."),

            //chatPersonTwoPersonThree


            //chatTestUserPersonOne

            //chatTestUserPersonTwo

            //chatGroupPersonOnePersonTwoPersonFour

            //chatGroupTestUserPersonOnePersonThree

        )
        repo.messageDao.insertList(messageList)

        //Message Read ?


    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()
        val accountManager: UstadAccountManager by di.instance<UstadAccountManager>()

        addSomeChats()


        val presenterArgs = mapOf<String,String>()
        val presenter = ChatListPresenter(context, presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoChatDaoSpy, timeout(5000)).findAllChatsForUser(
                eq("%"), eq(accountManager.activeAccount.personUid))
        verify(mockView, timeout(5000)).list = any()

    }


    //Disabled until issue 720 is fixed as per:
    // https://taiga.ustadmobile.com/project/mike-core-development/issue/720
    //@Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()
        val systemImpl: UstadMobileSystemImpl by di.instance()

        addSomeChats()

        val presenterArgs = mapOf<String,String>()

        val accountManager = di.direct.instance<UstadAccountManager>()
        val endpointUrl = accountManager.activeEndpoint.url
        accountManager.startLocalTestSessionBlocking(loggedInTestUser!!, endpointUrl)

        val presenter = ChatListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()


        presenter.handleClickEntry(chatTestUserPersonOne!!)

        verify(systemImpl, timeout(5000)).go(eq(ChatDetailView.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to chatTestUserPersonOne?.chatUid.toString())), any())
    }


}