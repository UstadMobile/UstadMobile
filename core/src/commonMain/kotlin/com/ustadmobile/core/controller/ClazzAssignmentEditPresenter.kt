package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toLocalMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzWithSchool
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class ClazzAssignmentEditPresenter(context: Any,
                                   arguments: Map<String, String>, view: ClazzAssignmentEditView,
                                   lifecycleOwner: DoorLifecycleOwner,
                                   di: DI)
    : UstadEditPresenter<ClazzAssignmentEditView, ClazzAssignment>(context, arguments, view, di, lifecycleOwner) {

    enum class SubmissionTypeOptions(val optionVal: Int, val messageId: Int){
        INDIVIDUAL(ClazzAssignment.SUBMISSION_TYPE_INDIVIDUAL, MessageID.individual),
        GROUP(ClazzAssignment.SUBMISSION_TYPE_GROUP, MessageID.group)
    }
    class SubmissionTypeOptionsMessageIdOption(day: SubmissionTypeOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    enum class TextLimitTypeOptions(val optionVal: Int, val messageId: Int){
        WORDS(ClazzAssignment.TEXT_WORD_LIMIT, MessageID.words),
        CHARS(ClazzAssignment.TEXT_CHAR_LIMIT, MessageID.characters)
    }
    class TextLimitTypeOptionsMessageIdOption(day: TextLimitTypeOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    enum class CompletionCriteriaOptions(val optionVal: Int, val messageId: Int){
        SUBMITTED(ClazzAssignment.COMPLETION_CRITERIA_SUBMIT, MessageID.submitted_cap),
        GRADED(ClazzAssignment.COMPLETION_CRITERIA_GRADED, MessageID.graded)
    }
    class CompletionCriteriaOptionsMessageIdOption(day: CompletionCriteriaOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    enum class EditAfterSubmissionOptions(val optionVal: Int, val messageId: Int) {
        ALLOWED_DEADLINE(ClazzAssignment.EDIT_AFTER_SUBMISSION_TYPE_ALLOWED_DEADLINE,
                MessageID.allowed_till_deadline),
        ALLOWED_GRACE(ClazzAssignment.EDIT_AFTER_SUBMISSION_TYPE_ALLOWED_GRACE,
                MessageID.allowed_till_grace),
        NOT_ALLOWED(ClazzAssignment.EDIT_AFTER_SUBMISSION_TYPE_NOT_ALLOWED,
                MessageID.not_allowed)
    }

    class  EditAfterSubmissionOptionsMessageIdOption(day: EditAfterSubmissionOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    enum class FileTypeOptions(val optionVal: Int, val messageId: Int) {
        ANY(ClazzAssignment.FILE_TYPE_ANY,
                MessageID.file_type_any),
        DOCUMENT(ClazzAssignment.FILE_TYPE_DOC,
                MessageID.file_document),
        IMAGE(ClazzAssignment.FILE_TYPE_IMAGE,
                MessageID.file_image),
        VIDEO(ClazzAssignment.FILE_TYPE_VIDEO,
                MessageID.video),
        AUDIO(ClazzAssignment.FILE_TYPE_AUDIO,
                MessageID.audio)
    }

    class  FileTypeOptionsMessageIdOption(day: FileTypeOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    enum class MarkingTypeOptions(val optionVal: Int, val messageId: Int){
        TEACHER(ClazzAssignment.MARKED_BY_COURSE_LEADER, MessageID.course_leader),
        PEERS(ClazzAssignment.MARKED_BY_PEERS, MessageID.peers)
    }
    class MarkingTypeOptionsMessageIdOption(day: MarkingTypeOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.markingTypeOptions = MarkingTypeOptions.values().map { MarkingTypeOptionsMessageIdOption(it, context) }
        view.submissionTypeOptions = SubmissionTypeOptions.values().map { SubmissionTypeOptionsMessageIdOption(it, context) }
        view.completionCriteriaOptions = CompletionCriteriaOptions.values().map { CompletionCriteriaOptionsMessageIdOption(it, context) }
        view.editAfterSubmissionOptions = EditAfterSubmissionOptions.values().map { EditAfterSubmissionOptionsMessageIdOption(it, context) }
        view.fileTypeOptions = FileTypeOptions.values().map { FileTypeOptionsMessageIdOption(it, context) }
        view.textLimitTypeOptions = TextLimitTypeOptions.values().map { TextLimitTypeOptionsMessageIdOption(it, context) }
    }


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzAssignment? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazzAssignment = db.onRepoWithFallbackToDb(2000) {
            it.clazzAssignmentDao.findByUidAsync(entityUid)
        } ?: ClazzAssignment().apply {
            caClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: throw IllegalArgumentException("clazzUid was not given")
            caUid = db.doorPrimaryKeyManager.nextIdAsync(ClazzAssignment.TABLE_ID)
        }

        val clazzWithSchool = db.onRepoWithFallbackToDb(2000) {
            it.clazzDao.getClazzWithSchool(clazzAssignment.caClazzUid)
        } ?: ClazzWithSchool()

        val timeZone = clazzWithSchool.effectiveTimeZone()
        view.timeZone = timeZone

        loadEntityIntoDateTime(clazzAssignment)

        return clazzAssignment
    }


    override fun onLoadFromJson(bundle: Map<String, String>): ClazzAssignment {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        val editEntity = if (entityJsonStr != null) {
             safeParse(di, ClazzAssignment.serializer(), entityJsonStr)
        }else{
            ClazzAssignment().apply {
                caClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0L
                caUid = db.doorPrimaryKeyManager.nextId(ClazzAssignment.TABLE_ID)
            }
        }

        presenterScope.launch {
            val caClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: editEntity.caClazzUid
            val clazzWithSchool = db.onRepoWithFallbackToDb(2000) {
                it.clazzDao.getClazzWithSchool(caClazzUid)
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

    fun loadEntityIntoDateTime(entity: ClazzAssignment){
        val timeZone = view.timeZone ?: "UTC"

        if(entity.caStartDate != 0L){
            val startDateTimeMidnight = DateTime(entity.caStartDate)
                    .toLocalMidnight(timeZone).unixMillisLong
            view.startDate = startDateTimeMidnight
            view.startTime = entity.caStartDate - startDateTimeMidnight
        }else{
            view.startDate = 0
        }


        if(entity.caDeadlineDate != Long.MAX_VALUE){
            val deadlineDateTimeMidnight = DateTime(entity.caDeadlineDate)
                    .toLocalMidnight(timeZone).unixMillisLong
            view.deadlineDate = deadlineDateTimeMidnight
            view.deadlineTime = entity.caDeadlineDate - deadlineDateTimeMidnight
        }else{
            view.deadlineDate = Long.MAX_VALUE
        }

        if(entity.caGracePeriodDate != Long.MAX_VALUE){
            val gracePeriodDateTimeMidnight = DateTime(entity.caGracePeriodDate)
                    .toLocalMidnight(timeZone).unixMillisLong
            view.gracePeriodDate = gracePeriodDateTimeMidnight
            view.gracePeriodTime = entity.caGracePeriodDate - gracePeriodDateTimeMidnight
        }else{
            view.gracePeriodDate = Long.MAX_VALUE
        }
    }

    fun saveDateTimeIntoEntity(entity: ClazzAssignment){
        val timeZone = view.timeZone ?: "UTC"

        entity.caStartDate = DateTime(view.startDate).toOffsetByTimezone(timeZone)
                .localMidnight.utc.unixMillisLong + view.startTime

        if(view.deadlineDate != Long.MAX_VALUE){
            entity.caDeadlineDate = DateTime(view.deadlineDate).toOffsetByTimezone(timeZone)
                    .localMidnight.utc.unixMillisLong + view.deadlineTime
        }

        if(view.gracePeriodDate != Long.MAX_VALUE){
            entity.caGracePeriodDate = DateTime(view.gracePeriodDate).toOffsetByTimezone(timeZone)
                    .localMidnight.utc.unixMillisLong + view.gracePeriodTime
        }
    }

    override fun handleClickSave(entity: ClazzAssignment) {
        presenterScope.launch {

            if (entity.caTitle.isNullOrEmpty()) {
                view.caTitleError = systemImpl.getString(MessageID.field_required_prompt, context)
                return@launch
            }

            saveDateTimeIntoEntity(entity)

            if (entity.caDeadlineDate <= entity.caStartDate) {
                view.caDeadlineError = systemImpl.getString(MessageID.end_is_before_start_error, context)
                return@launch
            }


            if (entity.caGracePeriodDate < entity.caDeadlineDate) {
                view.caGracePeriodError = systemImpl.getString(MessageID.after_deadline_date_error, context)
                return@launch
            }


            if(entity.caMaxPoints == 0){
                view.caMaxPointsError = systemImpl.getString(MessageID.field_required_prompt, context)
                return@launch
            }
            

            view.loading = true
            view.fieldsEnabled = false

            // if grace period is not set, set the date to equal the deadline
            if(entity.caGracePeriodDate == Long.MAX_VALUE || view.gracePeriodTime == 0L){
                entity.caGracePeriodDate = entity.caDeadlineDate
            }


            finishWithResult(safeStringify(di,
                            ListSerializer(ClazzAssignment.serializer()),
                            listOf(entity)))

            view.loading = false
            view.fieldsEnabled = true

        }
    }




    companion object {

        const val ARG_SAVEDSTATE_CONTENT = "contents"

    }

}