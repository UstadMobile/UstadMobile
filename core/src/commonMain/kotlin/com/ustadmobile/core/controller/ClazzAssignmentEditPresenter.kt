package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toLocalMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher

import kotlinx.coroutines.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.lib.db.entities.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind


class ClazzAssignmentEditPresenter(context: Any,
                                   arguments: Map<String, String>, view: ClazzAssignmentEditView,
                                   lifecycleOwner: DoorLifecycleOwner,
                                   di: DI)
    : UstadEditPresenter<ClazzAssignmentEditView, ClazzAssignment>(context, arguments, view, di, lifecycleOwner) {

    enum class LateSubmissionOptions(val optionVal: Int, val messageId: Int) {
        REJECT(ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_REJECT,
                MessageID.reject),
        ACCEPT(ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_ACCEPT,
                MessageID.accept),
        MARK_PENALTY(ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_PENALTY,
                MessageID.mark_penalty)
    }

    class LateSubmissionOptionsMessageIdOption(day: LateSubmissionOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private val contentJoinEditHelper = DefaultOneToManyJoinEditHelper(
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer::contentEntryUid,
            "state_ContentEntryWithMetrics_list",
            ListSerializer(ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer.serializer()),
            ListSerializer(ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer.serializer()),
            this, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer::class) { contentEntryUid = it }

    fun handleAddOrEditContent(entityClass: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        contentJoinEditHelper.onEditResult(entityClass)
    }

    fun handleRemoveContent(entityClass: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        contentJoinEditHelper.onDeactivateEntity(entityClass)
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.clazzAssignmentContent = contentJoinEditHelper.liveList
        view.lateSubmissionOptions = LateSubmissionOptions.values().map { LateSubmissionOptionsMessageIdOption(it, context) }
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzAssignment? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazzAssignment = withTimeoutOrNull(2000) {
            db.clazzAssignmentDao.findByUidAsync(entityUid)
        } ?: ClazzAssignment()

        val clazzWithSchool = withTimeoutOrNull(2000) {
            db.clazzDao.getClazzWithSchool(clazzAssignment.caClazzUid)
        } ?: ClazzWithSchool()

        setTimeZoneDate(clazzWithSchool, clazzAssignment)

        val loggedInPersonUid = accountManager.activeAccount.personUid

        val contentList = withTimeoutOrNull(2000) {
            db.clazzAssignmentContentJoinDao.findAllContentByClazzAssignmentUidAsync(
                    clazzAssignment.caUid, loggedInPersonUid
            )
        } ?: listOf()

        contentJoinEditHelper.liveList.sendValue(contentList)

        return clazzAssignment
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzAssignment? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ClazzAssignment? = null
        if (entityJsonStr != null) {
            editEntity = safeParse(di, ClazzAssignment.serializer(), entityJsonStr)
        } else {
            editEntity = ClazzAssignment()
        }

        GlobalScope.launch {
            val clazzWithSchool = withTimeoutOrNull(2000) {
                db.clazzDao.getClazzWithSchool(editEntity.caClazzUid)
            } ?: ClazzWithSchool()

            setTimeZoneDate(clazzWithSchool, editEntity)

        }

        contentJoinEditHelper.onLoadFromJsonSavedState(bundle)


        return editEntity
    }

    private fun setTimeZoneDate(clazzWithSchool: ClazzWithSchool, entity: ClazzAssignment) {

        val timeZone = clazzWithSchool.effectiveTimeZone()
        view.timeZone = timeZone

        if(entity.caStartDate != 0L){
            val localStartDateMidnight = DateTime(entity.caStartDate).toLocalMidnight(timeZone).unixMillisLong
            view.startDate = localStartDateMidnight
            view.startTime = entity.caStartDate - localStartDateMidnight
        }else{
            view.startDate = 0
            view.startTime = 0
        }

        if(entity.caDeadlineDate != Long.MAX_VALUE){
            val localDeadlineDateMidnight = DateTime(entity.caDeadlineDate).toLocalMidnight(timeZone).unixMillisLong
            view.deadlineDate = localDeadlineDateMidnight
            view.deadlineTime = entity.caDeadlineDate - localDeadlineDateMidnight
        }else{
            view.deadlineDate = Long.MAX_VALUE
            view.deadlineTime = 0
        }

        if(entity.caGracePeriodDate == 0L || entity.caGracePeriodDate == Long.MAX_VALUE){
            view.gracePeriodDate = Long.MAX_VALUE
            view.gracePeriodTime = 0
        }else{
            val localGracePeriodDateMidnight = DateTime(entity.caGracePeriodDate).toLocalMidnight(timeZone).unixMillisLong
            view.gracePeriodDate = localGracePeriodDateMidnight
            view.gracePeriodTime = entity.caGracePeriodDate - localGracePeriodDateMidnight
        }

    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: ClazzAssignment) {
        GlobalScope.launch(doorMainDispatcher()) {

            if (entity.caTitle.isNullOrEmpty()) {
                view.caTitleError = systemImpl.getString(MessageID.field_required_prompt, context)
                return@launch
            }

            val timeZone = view.timeZone ?: "UTC"

            if(view.startDate != 0L){
                entity.caStartDate = DateTime(view.startDate).toOffsetByTimezone(timeZone)
                        .localMidnight.utc.unixMillisLong + view.startTime
            }else{
                entity.caStartDate = 0L
            }

            if(view.deadlineDate != Long.MAX_VALUE){
                entity.caDeadlineDate = DateTime(view.deadlineDate).toOffsetByTimezone(timeZone)
                        .localMidnight.utc.unixMillisLong + view.deadlineTime
            }else{
                entity.caDeadlineDate = Long.MAX_VALUE
            }

            if(view.gracePeriodDate != Long.MAX_VALUE){
                entity.caGracePeriodDate = DateTime(view.gracePeriodDate).toOffsetByTimezone(timeZone)
                        .localMidnight.utc.unixMillisLong + view.gracePeriodTime
            }

            if (entity.caStartDate == 0L || entity.caStartDate == 0L) {
                view.caStartDateError = systemImpl.getString(MessageID.field_required_prompt, context)
                return@launch
            }

            if (entity.caDeadlineDate <= entity.caStartDate) {
                view.caDeadlineError = systemImpl.getString(MessageID.end_is_before_start_error, context)
                return@launch
            }

            if (entity.caLateSubmissionType == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_ACCEPT ||
                    entity.caLateSubmissionType == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_PENALTY) {

                if (entity.caDeadlineDate == Long.MAX_VALUE) {
                    view.caDeadlineError = systemImpl.getString(MessageID.field_required_prompt, context)
                    return@launch
                }

                if (entity.caGracePeriodDate <= entity.caDeadlineDate) {
                    view.caGracePeriodError = systemImpl.getString(MessageID.after_deadline_date_error, context)
                    return@launch
                }
            }

            if(entity.caLateSubmissionType == 0 ||
                    entity.caLateSubmissionType == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_REJECT) {
                entity.caGracePeriodDate = entity.caDeadlineDate
            }

            if(entity.caLateSubmissionType != ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_PENALTY){
                entity.caLateSubmissionPenalty = 0
            }


            if (entity.caUid == 0L) {
                entity.caUid = repo.clazzAssignmentDao.insertAsync(entity)
            } else {
                repo.clazzAssignmentDao.updateAsync(entity)
            }

            repo.cacheClazzAssignmentDao.invalidateCacheByAssignment(entity.caUid)

            val contentToInsert = contentJoinEditHelper.entitiesToInsert
            val contentToDelete = contentJoinEditHelper.primaryKeysToDeactivate

            repo.clazzAssignmentContentJoinDao.insertListAsync(contentToInsert.map {
                ClazzAssignmentContentJoin().apply {
                    cacjContentUid = it.contentEntryUid
                    cacjAssignmentUid = entity.caUid
                }
            })

            repo.clazzAssignmentContentJoinDao.deactivateByUids(contentToDelete, entity.caUid)

            repo.cacheClazzAssignmentDao.deleteCachedInactiveContent()

            onFinish(ClazzAssignmentDetailView.VIEW_NAME, entity.caUid, entity)

        }
    }

}