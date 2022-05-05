package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.screen.ChatListScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Chat list screen tests")
class ChatListFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()
    
    var loggedInTestUser: Person? = null
    var chatTestUserPersonOne: Chat? = null

    //TODO: Use loop
    private fun addSomeChats(){


        loggedInTestUser = Person("testuser", "Test", "User").apply {
            personUid = dbRule.repo.personDao.insert(this)
        }
        val personOne = Person("personone", "Person", "One").apply {
            personUid = dbRule.repo.personDao.insert(this)
        }
        val personTwo = Person("persontwo", "Person", "Two").apply {
            personUid = dbRule.repo.personDao.insert(this)
        }
        val personThree = Person("personthree", "Person", "Three").apply {
            personUid = dbRule.repo.personDao.insert(this)
        }
        val personFour = Person("personfour", "Person", "Four").apply {
            personUid = dbRule.repo.personDao.insert(this)
        }

        val chatPersonOnePersonTwo = Chat("", false, systemTimeInMillis()).apply {
            chatUid = dbRule.repo.chatDao.insert(this)
        }
        val chatPersonTwoPersonThree = Chat("", false, systemTimeInMillis()).apply {
            chatUid = dbRule.repo.chatDao.insert(this)
        }
        chatTestUserPersonOne = Chat("", false, systemTimeInMillis()).apply {
            chatUid = dbRule.repo.chatDao.insert(this)
        }
        val chatTestUserPersonTwo = Chat("", false, systemTimeInMillis()).apply {
            chatUid = dbRule.repo.chatDao.insert(this)
        }
        val chatGroupPersonOnePersonTwoPersonFour = Chat("", true, systemTimeInMillis()).apply {
            chatUid = dbRule.repo.chatDao.insert(this)
        }
        val chatGroupTestUserPersonOnePersonThree = Chat("", true, systemTimeInMillis()).apply {
            chatUid = dbRule.repo.chatDao.insert(this)
        }


        //chatPersonOnePersonTwo
        dbRule.repo.chatMemberDao.insert(ChatMember(chatPersonOnePersonTwo.chatUid, personOne.personUid))
        dbRule.repo.chatMemberDao.insert(ChatMember(chatPersonOnePersonTwo.chatUid, personTwo.personUid))

        //chatPersonTwoPersonThree
        dbRule.repo.chatMemberDao.insert(ChatMember(chatPersonTwoPersonThree.chatUid, personTwo.personUid))
        dbRule.repo.chatMemberDao.insert(ChatMember(chatPersonTwoPersonThree.chatUid, personThree.personUid))

        //chatTestUserPersonOne
        dbRule.repo.chatMemberDao.insert(ChatMember(chatTestUserPersonOne!!.chatUid, loggedInTestUser!!.personUid))
        dbRule.repo.chatMemberDao.insert(ChatMember(chatTestUserPersonOne!!.chatUid, personOne!!.personUid))

        //chatTestUserPersonTwo
        dbRule.repo.chatMemberDao.insert(ChatMember(chatTestUserPersonTwo.chatUid, loggedInTestUser!!.personUid))
        dbRule.repo.chatMemberDao.insert(ChatMember(chatTestUserPersonTwo.chatUid, personTwo.personUid))

        //chatGroupPersonOnePersonTwoPersonFour
        dbRule.repo.chatMemberDao.insert(ChatMember(chatGroupPersonOnePersonTwoPersonFour.chatUid, personOne.personUid))
        dbRule.repo.chatMemberDao.insert(ChatMember(chatGroupPersonOnePersonTwoPersonFour.chatUid, personTwo.personUid))
        dbRule.repo.chatMemberDao.insert(ChatMember(chatGroupPersonOnePersonTwoPersonFour.chatUid, personFour.personUid))

        //chatGroupTestUserPersonOnePersonThree
        dbRule.repo.chatMemberDao.insert(ChatMember(chatGroupTestUserPersonOnePersonThree.chatUid, loggedInTestUser!!.personUid))
        dbRule.repo.chatMemberDao.insert(ChatMember(chatGroupTestUserPersonOnePersonThree.chatUid, personOne.personUid))
        dbRule.repo.chatMemberDao.insert(ChatMember(chatGroupTestUserPersonOnePersonThree.chatUid, personThree.personUid))


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
        dbRule.repo.messageDao.insertList(messageList)

        //Message Read ?
    }

    //Disabled 16/Apr/22 - BROKEN. Needs fixed on this branch.
//    @AdbScreenRecord("List screen should show chat in database and allow clicking on item")
//    @Test
    fun givenClazzPresent_whenClickOnClazz_thenShouldNavigateToClazzDetail() {


        init {

            addSomeChats()


            val fragmentScenario = launchFragmentInContainer(
                    bundleOf(), themeResId = R.style.UmTheme_App){
                ChatListFragment().also {
                    it.installNavController(systemImplNavRule.navController,
                            initialDestId = R.id.chat_list_home_dest)
                } }

            fragmentScenario.onFragment {
                Navigation.setViewNavController(it.requireView(), systemImplNavRule.navController)
            }

        }.run {

            ChatListScreen{

                recycler{

                    childWith<ChatListScreen.MainItem> {
                        withTag(chatTestUserPersonOne!!.chatUid)
                    } perform {
                        isDisplayed()
                        click()
                    }

                }

            }

            flakySafely {
                Assert.assertEquals("After clicking on item, it navigates to detail view",
                        R.id.chat_detail_dest, systemImplNavRule.navController.currentDestination?.id)
            }


        }



    }

}