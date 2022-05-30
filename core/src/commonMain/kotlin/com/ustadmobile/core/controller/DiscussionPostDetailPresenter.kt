package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmLifecycleListener
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.DiscussionPostDetailView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class DiscussionPostDetailPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: DiscussionPostDetailView,
    di: DI,
    lifecycleOwner: DoorLifecycleOwner

)

    : UstadDetailPresenter<DiscussionPostDetailView, DiscussionPostWithDetails>(
        context, arguments, view, di, lifecycleOwner), MessagesPresenter {




    var postUid: Long = 0

    var loggedInPersonUid: Long = 0

    val ps = presenterScope

    var clazzUid: Long = 0



    override fun onLoadLiveData(repo: UmAppDatabase): DoorLiveData<DiscussionPostWithDetails?>? {
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
            Napier.d("POTATO POST1 " + postUid)
            val post = repo.discussionPostDao.findWithDetailsByUid(postUid)
            clazzUid = post?.discussionPostClazzUid?:0L

        }
        Napier.d("POTATO POST2 " + postUid)
        return repo.discussionPostDao.findWithDetailsByUidLive(postUid)

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

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return false
    }

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.LIVEDATA


}