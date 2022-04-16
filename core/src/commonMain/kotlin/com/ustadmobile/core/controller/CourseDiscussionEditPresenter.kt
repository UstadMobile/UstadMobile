package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toLocalMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.CourseDiscussionEditView
import com.ustadmobile.core.view.DiscussionTopicEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class CourseDiscussionEditPresenter(context: Any,
                                    arguments: Map<String, String>,
                                    view: CourseDiscussionEditView,
                                    lifecycleOwner: DoorLifecycleOwner,
                                    di: DI)
    : UstadEditPresenter<CourseDiscussionEditView, CourseBlockWithEntity>(  context,
                                                                            arguments,
                                                                            view,
                                                                            di,
                                                                            lifecycleOwner) {

    private var clazzUid: Long = 0L

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

    }

    override fun onLoadDataComplete() {
        super.onLoadDataComplete()

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
                cbEntityUid =  arguments[ARG_ENTITY_UID]?.toLong()
                    ?: db.doorPrimaryKeyManager.nextId(CourseDiscussion.TABLE_ID)
                cbType = CourseBlock.BLOCK_DISCUSSION_TYPE
                courseDiscussion = CourseDiscussion().apply {
                    courseDiscussionUid = cbEntityUid
                    courseDiscussionClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0L
                }
            }
        }

        presenterScope.launch {


            clazzUid = editEntity.courseDiscussion?.courseDiscussionClazzUid ?: arguments[ARG_CLAZZUID]?.toLong() ?: 0
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
        }
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
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

    fun saveDateTimeIntoEntity(entity: CourseBlockWithEntity){
        val timeZone = view.timeZone ?: "UTC"

        entity.cbHideUntilDate = DateTime(view.startDate).toOffsetByTimezone(timeZone)
                .localMidnight.utc.unixMillisLong + view.startTime


    }

    fun handleClickTopic(discussionTopic: DiscussionTopic){
        //TODO: Go to Topic edit

    }

    fun handleClickAddTopic(){
        navigateForResult(
            NavigateForResultOptions(this,
                null,
                DiscussionTopicEditView.VIEW_NAME,
                DiscussionTopic::class,
                DiscussionTopic.serializer(),
                SAVEDSTATE_KEY_DISCUSSION_TOPIC,
                arguments = mutableMapOf(
                    ARG_CLAZZUID to clazzUid.toString()
                )
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


            finishWithResult(safeStringify(di,
                            ListSerializer(CourseBlockWithEntity.serializer()),
                            listOf(entity)))

            view.loading = false
            view.fieldsEnabled = true

        }
    }




    companion object {

        const val SAVEDSTATE_KEY_DISCUSSION_TOPIC = "DiscussionTopic"

    }

}