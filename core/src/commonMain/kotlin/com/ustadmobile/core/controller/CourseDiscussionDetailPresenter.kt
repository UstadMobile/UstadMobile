package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.view.CourseDiscussionDetailView
import com.ustadmobile.core.view.DiscussionPostDetailView
import com.ustadmobile.core.view.DiscussionPostEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.DI


class CourseDiscussionDetailPresenter(
        context: Any, arguments: Map<String, String>,
        view: CourseDiscussionDetailView,
        di: DI,
        lifecycleOwner: LifecycleOwner
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

    override fun onLoadLiveData(repo: UmAppDatabase): LiveData<CourseDiscussion?>? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        //view.topics = repo.discussionTopicDao.getListOfTopicsByDiscussion(entityUid)
        view.posts = repo.discussionPostDao.getPostsByDiscussionUid(entityUid)

        return repo.courseDiscussionDao.getCourseDiscussionByUid(entityUid)
    }

    override fun handleClickEdit() {
        // Not supported
    }


    fun onClickAddPost() {
        val discussionUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        navigateForResult(
            NavigateForResultOptions(
                this, null,
                DiscussionPostEditView.VIEW_NAME,
                DiscussionPost::class,
                DiscussionPost.serializer(),
                DiscussionTopicDetailPresenter.RESULT_NEW_POST,
                arguments = mutableMapOf(
                    ARG_DISCUSSION_UID to
                            discussionUid.toString(),
                    ARG_CLAZZUID to view.entity?.courseDiscussionClazzUid.toString()
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

    fun onClickTopic(discussionTopic: DiscussionTopicListDetail){

//        val args = mutableMapOf<String, String>()
//        args[ARG_ENTITY_UID] = discussionTopic.discussionTopicUid.toString() ?: ""
//        ustadNavController?.navigate(
//            DiscussionTopicDetailView.VIEW_NAME, args)
    }

    companion object{
        const val ARG_DISCUSSION_UID = "ArgDiscussionUid"
    }




}