package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toLocalMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.OneToManyJoinEditHelperMp
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.CourseDiscussionEditView
import com.ustadmobile.core.view.DiscussionTopicEditView
import com.ustadmobile.core.view.ItemTouchHelperListener
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class CourseDiscussionEditPresenter(context: Any,
                                    arguments: Map<String, String>,
                                    view: CourseDiscussionEditView,
                                    lifecycleOwner: LifecycleOwner,
                                    di: DI)
    : UstadEditPresenter<CourseDiscussionEditView, CourseBlockWithEntity>(  context,
                                                                            arguments,
                                                                            view,
                                                                            di,
                                                                            lifecycleOwner),
    ItemTouchHelperListener {

    private var clazzUid: Long = 0L


    private val postsOneToManyJoinEditHelper
            = OneToManyJoinEditHelperMp(DiscussionPost::discussionPostUid,
        ARG_SAVEDSTATE_DISCUSSION_TOPIC,
        ListSerializer(DiscussionPost.serializer()),
        ListSerializer(DiscussionPost.serializer()),
        this,
        requireSavedStateHandle(),
        DiscussionPost::class) { discussionPostUid = it }

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.postList = postsOneToManyJoinEditHelper.liveList

    }

    override fun onLoadDataComplete() {
        super.onLoadDataComplete()


        observeSavedStateResult(
            SAVEDSTATE_KEY_DISCUSSION_POST,
            ListSerializer(DiscussionPost.serializer()), DiscussionPost::class){
            val newPost = it.firstOrNull() ?: return@observeSavedStateResult

            val foundPost: DiscussionPost = postsOneToManyJoinEditHelper.liveList.getValue()?.find {
                    topic -> topic.discussionPostUid == newPost.discussionPostUid
            } ?: DiscussionPost().apply{
                //Creating a new one from newPost
                discussionPostUid = newPost.discussionPostUid
                discussionPostStartDate = systemTimeInMillis()
                //TODO: rename this to discussionPostDiscussionUid
                discussionPostDiscussionTopicUid = entity?.cbEntityUid?:0L
                discussionPostTitle = newPost.discussionPostTitle
                discussionPostMessage  = newPost.discussionPostMessage
                discussionPostStartedPersonUid = accountManager.activeAccount.personUid
                discussionPostClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0L
            }

            //Any updated title desc
            foundPost.discussionPostTitle = newPost.discussionPostTitle
            foundPost.discussionPostMessage  = newPost.discussionPostMessage
            foundPost.discussionPostClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0L

            postsOneToManyJoinEditHelper.onEditResult(foundPost)

            requireSavedStateHandle()[SAVEDSTATE_KEY_DISCUSSION_POST] = null

        }

    }

    override fun onLoadFromJson(bundle: Map<String, String>): CourseBlockWithEntity {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]

        val editEntity = if (entityJsonStr != null) {
             safeParse(di, CourseBlockWithEntity.serializer(), entityJsonStr)
        }else{
            CourseBlockWithEntity().apply {
                cbUid = db.doorPrimaryKeyManager.nextId(CourseBlock.TABLE_ID)
                cbClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0L
                cbEntityUid =  db.doorPrimaryKeyManager.nextId(CourseDiscussion.TABLE_ID)
                cbType = CourseBlock.BLOCK_DISCUSSION_TYPE
                courseDiscussion = CourseDiscussion().apply {
                    courseDiscussionUid = cbEntityUid
                    courseDiscussionClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0L
                }
            }
        }


        postsOneToManyJoinEditHelper.onLoadFromJsonSavedState(bundle)

        postsOneToManyJoinEditHelper.liveList.postValue(editEntity.posts ?: listOf())
        presenterScope.launch {


            clazzUid = editEntity.courseDiscussion?.courseDiscussionClazzUid
                ?: arguments[ARG_CLAZZUID]?.toLong() ?: 0
            val clazzWithSchool = db.onRepoWithFallbackToDb(2000) {
                it.clazzDao.getClazzWithSchool(clazzUid)
            } ?: ClazzWithSchool()

            val timeZone = clazzWithSchool.effectiveTimeZone()
            view.timeZone = timeZone
            loadEntityIntoDateTime(editEntity)
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        if (entityVal != null) {
            saveDateTimeIntoEntity(entityVal)
            entityVal.posts = postsOneToManyJoinEditHelper.liveList.getValue()
        }
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json, CourseBlockWithEntity.serializer(),
                entityVal)
    }

    fun loadEntityIntoDateTime(entity: CourseBlockWithEntity){
        val timeZone = view.timeZone ?: "UTC"


        if(entity.cbHideUntilDate != 0L){
            val startDateTimeMidnight = DateTime(entity.cbHideUntilDate)
                    .toLocalMidnight(timeZone).unixMillisLong
            view.startDate = startDateTimeMidnight
            view.startTime = entity.cbHideUntilDate - startDateTimeMidnight
        }else{
            view.startDate = 0
        }

    }

    private fun saveDateTimeIntoEntity(entity: CourseBlockWithEntity){
        val timeZone = view.timeZone ?: "UTC"

        entity.cbHideUntilDate = DateTime(view.startDate).toOffsetByTimezone(timeZone)
                .localMidnight.utc.unixMillisLong + view.startTime


    }


    fun handleClickTopic(discussionTopic: DiscussionTopic){
        navigateForResult(
            NavigateForResultOptions(this,
                discussionTopic,
                DiscussionTopicEditView.VIEW_NAME,
                DiscussionTopic::class,
                DiscussionTopic.serializer(),
                SAVEDSTATE_KEY_DISCUSSION_TOPIC,
                arguments = mutableMapOf(
                    ARG_CLAZZUID to clazzUid.toString()
                )
            ))
    }

    fun handleClickAddTopic(){

        navigateForResult(
            NavigateForResultOptions(this,
                null,
                DiscussionTopicEditView.VIEW_NAME,
                DiscussionTopic::class,
                DiscussionTopic.serializer(),
                SAVEDSTATE_KEY_DISCUSSION_TOPIC,
                arguments = arguments.toMutableMap()
        ))
    }


    override fun handleClickSave(entity: CourseBlockWithEntity) {
        presenterScope.launch {

            saveDateTimeIntoEntity(entity)

            var foundError = false
            if (entity.courseDiscussion?.courseDiscussionTitle.isNullOrEmpty()) {
                view.blockTitleError =
                    systemImpl.getString(MessageID.field_required_prompt, context)
                foundError = true
            }else{
                view.blockTitleError = null
            }

            if(foundError){
                return@launch
            }
            

            view.loading = true
            view.fieldsEnabled = false


            entity.posts = postsOneToManyJoinEditHelper.entitiesToInsert +
                    postsOneToManyJoinEditHelper.entitiesToUpdate

            entity.postUidsToRemove = postsOneToManyJoinEditHelper.primaryKeysToDeactivate

            finishWithResult(safeStringify(di,
                            ListSerializer(CourseBlockWithEntity.serializer()),
                            listOf(entity)))

            view.loading = false
            view.fieldsEnabled = true

        }
    }

    //TODO: Remove this
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {

        val currentList = postsOneToManyJoinEditHelper.liveList.getValue()?.toMutableList() ?: mutableListOf()

        val movingBlock = currentList[fromPosition]

        currentList.remove(movingBlock)
        currentList.add(toPosition, movingBlock)

        // finally update the list with new index values
        currentList.forEachIndexed{ index , item ->
            //item.discussionTopicIndex = index
        }

        postsOneToManyJoinEditHelper.liveList.postValue(currentList.toList())

        return true
    }

    override fun onItemDismiss(position: Int) {
        //Does nothing
    }



    companion object {

        const val SAVEDSTATE_KEY_DISCUSSION_TOPIC = "DiscussionTopic"
        const val SAVEDSTATE_KEY_DISCUSSION_POST = "DiscussionPost"

        const val ARG_SAVEDSTATE_DISCUSSION_TOPIC = "ArgSavedStateDiscussionTopic"
        const val ARG_SAVEDSTATE_DISCUSSION_POST = "ArgSavedStateDiscussionPost"

    }

}