package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.DiscussionPostDetailView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.lib.db.entities.MessageRead
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class DiscussionPostDetailPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: DiscussionPostDetailView, di: DI,
    lifecycleOwner: DoorLifecycleOwner)

    : UstadBaseController<DiscussionPostDetailView>(
        context, arguments, view, di), MessagesPresenter {

    val accountManager: UstadAccountManager by instance()

    val db: UmAppDatabase by on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_DB)

    val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_REPO)

    var postUid: Long = 0

    var loggedInPersonUid: Long = 0

    val ps = presenterScope

    var clazzUid: Long = 0

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        postUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        loggedInPersonUid = accountManager.activeAccount.personUid

        //Get replies
        view.replies = repo.messageDao.findAllMessagesByChatUid(postUid,
            DiscussionPost.TABLE_ID, loggedInPersonUid)


        //Update the title
        presenterScope.launch{
            val postTitle = repo.discussionPostDao.getPostTitle(
                postUid )
            view.title = postTitle
            val post = repo.discussionPostDao.findWithDetailsByUid(postUid)
            view.entity = post
            clazzUid = post?.discussionPostClazzUid?:0L

        }

    }

    /**
     * If link is on active endpoint :
    Then just use systemImpl.go and use logic as per goToDeepLink :
     */
    override fun handleClickLink(link: String){
        val systemImpl: UstadMobileSystemImpl by instance()
        if(link.contains(UstadMobileSystemCommon.LINK_ENDPOINT_VIEWNAME_DIVIDER)) {

            val viewUri =
                link.substringAfter(UstadMobileSystemCommon.LINK_ENDPOINT_VIEWNAME_DIVIDER)

            systemImpl.goToViewLink(viewUri, context)
        }else{

            //Send link to android system
            systemImpl.openLinkInBrowser(link, context)
        }
    }


    fun addMessage(message: String){
        presenterScope.launch {
            val updateListNeeded = postUid == 0L
            val loggedInPersonUid = accountManager.activeAccount.personUid

            repo.withDoorTransactionAsync(UmAppDatabase::class) { txRepo ->

                txRepo.messageDao.insertAsync(
                    Message(
                        loggedInPersonUid,
                        DiscussionPost.TABLE_ID,
                        postUid,
                        message,
                        clazzUid
                    )
                )

            }

            if (updateListNeeded) {
                view.replies = repo.messageDao.findAllMessagesByChatUid(
                    postUid,
                    DiscussionPost.TABLE_ID,
                    loggedInPersonUid)
            }

        }
    }

    override fun updateMessageRead(messageRead: MessageRead){
        presenterScope.launch {
            repo.withDoorTransactionAsync(UmAppDatabase::class){ txRepo ->
                txRepo.messageReadDao.insertAsync(messageRead)
            }
        }
    }

    fun updateMessageReadList(messageReadList: List<MessageRead>){
        presenterScope.launch {
            repo.withDoorTransactionAsync(UmAppDatabase::class) { txRepo ->
                txRepo.messageReadDao.insertList(messageReadList)
            }

        }
    }


}