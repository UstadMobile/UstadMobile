package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.view.ChatDetailView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_INTENT_MESSAGE
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class ChatDetailPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: ChatDetailView, di: DI,
    lifecycleOwner: DoorLifecycleOwner)

    : UstadBaseController<ChatDetailView>(
        context, arguments, view, di) {

    val accountManager: UstadAccountManager by instance()

    val db: UmAppDatabase by on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_DB)

    val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_REPO)

    var chatUid: Long = 0

    var otherPersonUid: Long = 0

    var loggedInPersonUid: Long = 0

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        chatUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        otherPersonUid = arguments[ARG_PERSON_UID]?.toLong() ?: 0L
        view.messageList = repo.messageDao.findAllMessagesByChatUid(chatUid, Chat.TABLE_ID)

        loggedInPersonUid = accountManager.activeAccount.personUid

        presenterScope.launch{
            //TODO: Get the title as query
            //Get chat title etc
            val chatWithLatestMessageAndCount = repo.chatDao.getChatWithLatestMessageAndCount(
                chatUid,
                loggedInPersonUid )
            view.title = chatWithLatestMessageAndCount?.chatName?:"Chat"
        }


    }

    /**
     * If link is on active endpoint :
    Then just use systemImpl.go and use logic as per goToDeepLink :
     */
    fun handleClickLink(link: String){
        val systemImpl: UstadMobileSystemImpl by instance()
        if(link.contains(UstadMobileSystemCommon.LINK_ENDPOINT_VIEWNAME_DIVIDER)) {
            val endpointUrl =
                link.substringBefore(UstadMobileSystemCommon.LINK_ENDPOINT_VIEWNAME_DIVIDER)
                    .requirePostfix("/")
            val viewUri =
                link.substringAfter(UstadMobileSystemCommon.LINK_ENDPOINT_VIEWNAME_DIVIDER)

            //TODO: call systemImpl go with args
            systemImpl.go(viewUri, mapOf(), context)
        }else{

            //Send link to android system
            systemImpl.openLinkInBrowser(link, context)
        }
    }


    fun addMessage(message: String){
        presenterScope.launch {
            val updateListNeeded = chatUid == 0L
            val isGroup = arguments[ARG_CHAT_IS_GROUP] != null
            val loggedInPersonUid = accountManager.activeAccount.personUid

            repo.withDoorTransactionAsync(UmAppDatabase::class) { txRepo ->
                if (chatUid == 0L) {
                    chatUid = txRepo.chatDao.insertAsync(Chat("", isGroup))
                    txRepo.chatMemberDao.insertAsync(
                        ChatMember(chatUid, loggedInPersonUid)
                    )
                    if(!isGroup && otherPersonUid != 0L){
                        txRepo.chatMemberDao.insertAsync(
                            ChatMember(chatUid, otherPersonUid)
                        )
                    }

                }
                txRepo.messageDao.insertAsync(
                    Message(
                        loggedInPersonUid,
                        Chat.TABLE_ID,
                        chatUid,
                        message,
                        systemTimeInMillis()
                    )
                )

            }

            if (updateListNeeded) {
                view.messageList = repo.messageDao.findAllMessagesByChatUid(chatUid, Chat.TABLE_ID)
            }

        }
    }

    companion object{
        val ARG_CHAT_IS_GROUP = "isChatGroup"
    }
}