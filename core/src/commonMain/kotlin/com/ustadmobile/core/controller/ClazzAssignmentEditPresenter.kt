package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toLocalMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.UmPlatformUtil
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.fallbackIndividualSet
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.CourseGroupSetListView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class ClazzAssignmentEditPresenter(context: Any,
                                   arguments: Map<String, String>, view: ClazzAssignmentEditView,
                                   lifecycleOwner: DoorLifecycleOwner,
                                   di: DI)
    : UstadEditPresenter<ClazzAssignmentEditView, CourseBlockWithEntity>(context, arguments, view, di, lifecycleOwner) {

    enum class TextLimitTypeOptions(val optionVal: Int, val messageId: Int){
        WORDS(ClazzAssignment.TEXT_WORD_LIMIT, MessageID.words),
        CHARS(ClazzAssignment.TEXT_CHAR_LIMIT, MessageID.characters)
    }
    class TextLimitTypeOptionsMessageIdOption(day: TextLimitTypeOptions, context: Any, di: DI)
        : MessageIdOption(day.messageId, context, day.optionVal, di = di)

    enum class CompletionCriteriaOptions(val optionVal: Int, val messageId: Int){
        SUBMITTED(ClazzAssignment.COMPLETION_CRITERIA_SUBMIT, MessageID.submitted_cap),
        GRADED(ClazzAssignment.COMPLETION_CRITERIA_GRADED, MessageID.graded)
    }
    class CompletionCriteriaOptionsMessageIdOption(day: CompletionCriteriaOptions, context: Any, di: DI)
        : MessageIdOption(day.messageId, context, day.optionVal, di = di)


    enum class SubmissionPolicyOptions(val optionVal: Int, val messageId: Int) {
        SUBMIT_ALL_AT_ONCE(ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE,
                MessageID.submit_all_at_once_submission_policy),
        MULTIPLE_SUBMISSIONS(ClazzAssignment.SUBMISSION_POLICY_MULTIPLE_ALLOWED,
                MessageID.multiple_submission_allowed_submission_policy),
    }

    class  SubmissionPolicyOptionsMessageIdOption(day: SubmissionPolicyOptions, context: Any, di: DI)
        : MessageIdOption(day.messageId, context, day.optionVal, di = di)

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

    class  FileTypeOptionsMessageIdOption(day: FileTypeOptions, context: Any, di: DI)
        : MessageIdOption(day.messageId, context, day.optionVal, di = di)


    enum class MarkingTypeOptions(val optionVal: Int, val messageId: Int){
        TEACHER(ClazzAssignment.MARKED_BY_COURSE_LEADER, MessageID.course_leader),
        PEERS(ClazzAssignment.MARKED_BY_PEERS, MessageID.peers)
    }
    class MarkingTypeOptionsMessageIdOption(day: MarkingTypeOptions, context: Any, di: DI)
        : MessageIdOption(day.messageId, context, day.optionVal, di = di)


    private var clazzUid: Long = 0L

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.markingTypeOptions = MarkingTypeOptions.values().map { MarkingTypeOptionsMessageIdOption(it, context, di) }
        view.completionCriteriaOptions = CompletionCriteriaOptions.values().map { CompletionCriteriaOptionsMessageIdOption(it, context, di) }
        view.submissionPolicyOptions = SubmissionPolicyOptions.values().map { SubmissionPolicyOptionsMessageIdOption(it, context, di) }
        view.fileTypeOptions = FileTypeOptions.values().map { FileTypeOptionsMessageIdOption(it, context, di) }
        view.textLimitTypeOptions = TextLimitTypeOptions.values().map { TextLimitTypeOptionsMessageIdOption(it, context, di) }
    }

    override fun onLoadDataComplete() {
        super.onLoadDataComplete()
        observeSavedStateResult(
            SAVEDSTATE_KEY_SUBMISSION_TYPE,
            ListSerializer(CourseGroupSet.serializer()), CourseGroupSet::class) {
            val group = it.firstOrNull() ?: return@observeSavedStateResult
            entity?.assignment?.caGroupUid = group.cgsUid
            view.groupSet = group
            view.entity = entity
            requireSavedStateHandle()[SAVEDSTATE_KEY_SUBMISSION_TYPE] = null
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
                cbEntityUid =  arguments[ARG_ENTITY_UID]?.toLong() ?: db.doorPrimaryKeyManager.nextId(ClazzAssignment.TABLE_ID)
                cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                assignment = ClazzAssignment().apply {
                    caUid = cbEntityUid
                    caClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0L
                }
            }
        }

        presenterScope.launch {

            val group = db.courseGroupSetDao.findByUidAsync(editEntity.assignment?.caGroupUid ?: 0)
                .fallbackIndividualSet(systemImpl, context)
            view.groupSet = group

            clazzUid = editEntity.assignment?.caClazzUid ?: arguments[ARG_CLAZZUID]?.toLong() ?: 0
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


        if(entity.cbDeadlineDate != Long.MAX_VALUE){
            val deadlineDateTimeMidnight = DateTime(entity.cbDeadlineDate)
                    .toLocalMidnight(timeZone).unixMillisLong
            view.deadlineDate = deadlineDateTimeMidnight
            view.deadlineTime = entity.cbDeadlineDate - deadlineDateTimeMidnight
        }else{
            view.deadlineDate = Long.MAX_VALUE
        }

        if(entity.cbGracePeriodDate != Long.MAX_VALUE){
            val gracePeriodDateTimeMidnight = DateTime(entity.cbGracePeriodDate)
                    .toLocalMidnight(timeZone).unixMillisLong
            view.gracePeriodDate = gracePeriodDateTimeMidnight
            view.gracePeriodTime = entity.cbGracePeriodDate - gracePeriodDateTimeMidnight
        }else{
            view.gracePeriodDate = Long.MAX_VALUE
        }
    }

    fun saveDateTimeIntoEntity(entity: CourseBlockWithEntity){
        val timeZone = view.timeZone ?: "UTC"

        entity.cbHideUntilDate = DateTime(view.startDate).toOffsetByTimezone(timeZone)
                .localMidnight.utc.unixMillisLong + view.startTime

        if(view.deadlineDate != Long.MAX_VALUE){
            entity.cbDeadlineDate = DateTime(view.deadlineDate).toOffsetByTimezone(timeZone)
                    .localMidnight.utc.unixMillisLong + view.deadlineTime
        }

        if(view.gracePeriodDate != Long.MAX_VALUE){
            entity.cbGracePeriodDate = DateTime(view.gracePeriodDate).toOffsetByTimezone(timeZone)
                    .localMidnight.utc.unixMillisLong + view.gracePeriodTime
        }
    }

    fun handleSubmissionTypeClicked(){
        navigateForResult(
            NavigateForResultOptions(this,
                null,
                CourseGroupSetListView.VIEW_NAME,
                CourseGroupSet::class,
                CourseGroupSet.serializer(),
                SAVEDSTATE_KEY_SUBMISSION_TYPE,
                arguments = mutableMapOf(
                    ARG_CLAZZUID to clazzUid.toString(),
                    CourseGroupSetListView.ARG_SHOW_INDIVIDUAL to true.toString()))
        )
    }


    override fun handleClickSave(entity: CourseBlockWithEntity) {
        presenterScope.launch {

            saveDateTimeIntoEntity(entity)

            var foundError = false
            if (entity.assignment?.caTitle.isNullOrEmpty()) {
                view.caTitleError = systemImpl.getString(MessageID.field_required_prompt, context)
                foundError = true
            }else{
                view.caTitleError = null
            }

            if(entity.cbMaxPoints == 0){
                view.caMaxPointsError = systemImpl.getString(MessageID.field_required_prompt, context)
                foundError = true
            }else{
                view.caMaxPointsError = null
            }

            if (entity.cbDeadlineDate <= entity.cbHideUntilDate) {
                view.caDeadlineError = systemImpl.getString(MessageID.end_is_before_start_error, context)
                foundError = true
            }else{
                view.caDeadlineError = null
            }

            if (entity.cbGracePeriodDate < entity.cbDeadlineDate) {
                view.caGracePeriodError = systemImpl.getString(MessageID.after_deadline_date_error, context)
                foundError = true
            }else{
                view.caGracePeriodError = null
            }

            if(entity.assignment?.caRequireTextSubmission == false && entity.assignment?.caRequireFileSubmission == false){
                foundError = true
                view.showSnackBar(systemImpl.getString(MessageID.text_file_submission_error, context))
            }


            if(foundError){
                return@launch
            }
            

            view.loading = true
            view.fieldsEnabled = false

            // if grace period is not set, set the date to equal the deadline
            if(entity.cbGracePeriodDate == Long.MAX_VALUE){
                entity.cbGracePeriodDate = entity.cbDeadlineDate
            }

            finishWithResult(safeStringify(di,
                            ListSerializer(CourseBlockWithEntity.serializer()),
                            listOf(entity)))

            view.loading = false
            view.fieldsEnabled = true

        }
    }




    companion object {

        const val ARG_SAVEDSTATE_CONTENT = "contents"

        const val SAVEDSTATE_KEY_SUBMISSION_TYPE = "submissionType"

    }

}