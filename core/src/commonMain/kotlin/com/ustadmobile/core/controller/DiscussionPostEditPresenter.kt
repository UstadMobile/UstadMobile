package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.DiscussionPostDetailView
import com.ustadmobile.core.view.DiscussionPostEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.DiscussionPost
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI


class DiscussionPostEditPresenter(context: Any,
                                  arguments: Map<String, String>, view: DiscussionPostEditView, di: DI,
                                  lifecycleOwner: LifecycleOwner)
    : UstadEditPresenter<DiscussionPostEditView, DiscussionPost>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private var topicUid: Long = 0L
    private var clazzUid: Long = 0L

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): DiscussionPost? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        topicUid = arguments[ARG_DISCUSSION_TOPIC_UID]?.toLong()?:0L
        clazzUid = arguments[ARG_CLAZZUID]?.toLong()?:0L

        val discussionPost = withTimeoutOrNull(2000) {
            db.discussionPostDao.findByUid(entityUid)
        } ?: DiscussionPost().apply{
            discussionPostClazzUid = clazzUid
            discussionPostDiscussionTopicUid = topicUid
            discussionPostStartedPersonUid = accountManager.activeAccount.personUid
            discussionPostStartDate = systemTimeInMillis()
        }


        return discussionPost
    }

    override fun onLoadFromJson(bundle: Map<String, String>): DiscussionPost? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        topicUid = bundle[ARG_DISCUSSION_TOPIC_UID]?.toLong()?:0L
        val editEntity = if(entityJsonStr != null) {
            safeParse(di, DiscussionPost.serializer(), entityJsonStr)
        }else {
            DiscussionPost()
        }
        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json, DiscussionPost.serializer(), entityVal)
    }

    override fun handleClickSave(entity: DiscussionPost) {

        presenterScope.launch {
            if(entity.discussionPostUid == 0L) {
                entity.discussionPostUid = repo.discussionPostDao.insertAsync(entity)
            }else {
                repo.discussionPostDao.updateAsync(entity)
            }

            onFinish(DiscussionPostDetailView.VIEW_NAME, entity.discussionPostUid, entity,
                DiscussionPost.serializer())
        }
    }


    companion object{
        const val ARG_DISCUSSION_TOPIC_UID = "ArgDiscussionPostUid"
    }

}