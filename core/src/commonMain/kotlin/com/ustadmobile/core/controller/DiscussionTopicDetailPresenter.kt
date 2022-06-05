package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.view.DiscussionPostDetailView
import com.ustadmobile.core.view.DiscussionPostEditView
import com.ustadmobile.core.view.DiscussionTopicDetailView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.DiscussionTopic
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI


class DiscussionTopicDetailPresenter(
        context: Any, arguments: Map<String, String>,
        view: DiscussionTopicDetailView,
        di: DI,
        lifecycleOwner: DoorLifecycleOwner
    ) : UstadDetailPresenter<DiscussionTopicDetailView, DiscussionTopic>(
                context,
                arguments,
                view,
                di,
                lifecycleOwner
    )
{

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.LIVEDATA

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return true
    }

    override fun onLoadLiveData(repo: UmAppDatabase): DoorLiveData<DiscussionTopic?>? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        view.posts = repo.discussionPostDao.getPostsByDiscussionTopic(entityUid)

        return repo.discussionTopicDao.getDiscussionTopicByUid(entityUid)
    }

    override fun handleClickEdit() {
        // Not supported
    }


    fun onClickAddPost() {
        val topicUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        navigateForResult(
            NavigateForResultOptions(
                this, null,
                DiscussionPostEditView.VIEW_NAME,
                DiscussionPost::class,
                DiscussionPost.serializer(),
                RESULT_NEW_POST,
                arguments = mutableMapOf(
                    DiscussionPostEditPresenter.ARG_DISCUSSION_TOPIC_UID to
                            topicUid.toString(),
                    ARG_CLAZZUID to view.entity?.discussionTopicClazzUid.toString()
                )
            )
        )
    }


    fun onClickPost(discussionPost: DiscussionPostWithDetails){

        val args = mutableMapOf<String, String>()
        args[ARG_ENTITY_UID] = discussionPost.discussionPostUid.toString()

        ustadNavController?.navigate(
            DiscussionPostDetailView.VIEW_NAME, args)
    }

    companion object{
        const val RESULT_NEW_POST = "ResultNewPost"
    }


}