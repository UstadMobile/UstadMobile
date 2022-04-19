package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.view.CourseDiscussionDetailView
import com.ustadmobile.core.view.DiscussionTopicDetailView
import com.ustadmobile.core.view.DiscussionTopicEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionTopic
import com.ustadmobile.lib.db.entities.DiscussionTopicListDetail
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI


class CourseDiscussionDetailPresenter(
        context: Any, arguments: Map<String, String>,
        view: CourseDiscussionDetailView,
        di: DI,
        lifecycleOwner: DoorLifecycleOwner
    ) : UstadDetailPresenter<CourseDiscussionDetailView, CourseDiscussion>(
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
        return false
    }

    override fun onLoadLiveData(repo: UmAppDatabase): DoorLiveData<CourseDiscussion?>? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        view.topics = repo.discussionTopicDao.getListOfTopicsByDiscussion(entityUid)

        return repo.courseDiscussionDao.getCourseDiscussionByUid(entityUid)
    }

    override fun handleClickEdit() {
        // Not supported
    }


    fun onClickAddTopic(){
        val args = mutableMapOf<String, String>()


//        navigateForResult(
//            NavigateForResultOptions(this,
//                null,
//                DiscussionTopicEditView.VIEW_NAME,
//                DiscussionTopic::class,
//                DiscussionTopic.serializer(),
//                CourseDiscussionEditPresenter.SAVEDSTATE_KEY_DISCUSSION_TOPIC,
//                arguments = mutableMapOf(
//                )
//            )
//        )

        ustadNavController.navigate(
            DiscussionTopicEditView.VIEW_NAME, args)
    }



    fun onClickTopic(discussionTopic: DiscussionTopicListDetail){

        val args = mutableMapOf<String, String>()
        args[ARG_ENTITY_UID] = discussionTopic.discussionTopicUid.toString() ?: ""
        args[DiscussionTopicEditPresenter.ARG_TOPIC_PERSIST] = "true"

        ustadNavController.navigate(
            DiscussionTopicDetailView.VIEW_NAME, args)
    }

    fun onClickDeleteTopic(discussionTopic: DiscussionTopicListDetail){
        //Update topic
        repo.discussionTopicDao.update(discussionTopic.apply {
            discussionTopicArchive = true
            discussionTopicVisible = false
        })
    }



}