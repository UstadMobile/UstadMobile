package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toLocalMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.CourseGroupSetListView
import com.ustadmobile.core.view.PeerReviewerAllocationEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.getFirstValue
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance
import kotlin.math.abs


class ClazzAssignmentEditPresenter(context: Any,
                                   arguments: Map<String, String>, view: ClazzAssignmentEditView,
                                   lifecycleOwner: DoorLifecycleOwner,
                                   di: DI)
    : UstadEditPresenter<ClazzAssignmentEditView, CourseBlockWithEntity>(context, arguments, view, di, lifecycleOwner) {


    private val json: Json by instance()

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

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
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
        observeSavedStateResult(ARG_SAVED_STATE_PEER_ALLOCATION,
            ListSerializer(PeerReviewerAllocationList.serializer()), PeerReviewerAllocationList::class){
            val allocations = it.firstOrNull() ?: return@observeSavedStateResult
            entity?.assignmentPeerAllocations = allocations.allocations
            view.entity = entity

            requireSavedStateHandle()[ARG_SAVED_STATE_PEER_ALLOCATION] = null
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

            repo.courseAssignmentSubmissionDao
                .checkNoSubmissionsMade(editEntity.assignment?.caUid ?: 0)
                .observeWithLifecycleOwner(lifecycleOwner){
                    view.groupSetEnabled = it == true
                }

            repo.courseAssignmentMarkDao
                .checkNoSubmissionsMarked(editEntity.assignment?.caUid ?: 0)
                .observeWithLifecycleOwner(lifecycleOwner){
                    view.markingTypeEnabled = it == true
                }

            val clazzUid = editEntity.assignment?.caClazzUid ?: arguments[ARG_CLAZZUID]?.toLong() ?: 0
            val clazzWithSchool = db.onRepoWithFallbackToDb(2000) {
                it.clazzDao.getClazzWithSchool(clazzUid)
            } ?: ClazzWithSchool()

            val terminologyUid = arguments[ClazzAssignmentEditView.TERMINOLOGY_ID]?.toLongOrNull() ?: clazzWithSchool.clazzTerminologyUid
            val terminology = db.courseTerminologyDao.findByUidAsync(terminologyUid)
            val termMap = terminology.toTermMap(json, systemImpl, context)

            view.markingTypeOptions = listOf(
                IdOption(termMap.getValue(TerminologyKeys.TEACHER_KEY), ClazzAssignment.MARKED_BY_COURSE_LEADER),
                MessageIdOption(MessageID.peers, context, ClazzAssignment.MARKED_BY_PEERS, di))

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

        if(view.startDate != 0L){
            entity.cbHideUntilDate = DateTime(view.startDate).toOffsetByTimezone(timeZone)
                .localMidnight.utc.unixMillisLong + view.startTime
        }

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
        val clazzUid = arguments[ARG_CLAZZUID]
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

    fun handleAssignReviewersClicked(){
        val assignment = entity?.assignment ?: return
        val reviewerCount = entity?.assignment?.caPeerReviewerCount ?: 0
        presenterScope.launch(doorMainDispatcher()) {
            val totalSubmitterSize = repo.clazzAssignmentDao.getSubmitterCountFromAssignment(
                assignment.caGroupUid, assignment.caClazzUid, "")
            if((reviewerCount) <= 0 || reviewerCount >= totalSubmitterSize){
                // show error on view
                view.reviewerCountError = " "
                return@launch
            }

            navigateForResult(
                NavigateForResultOptions(this@ClazzAssignmentEditPresenter,
                    PeerReviewerAllocationList(entity?.assignmentPeerAllocations),
                    PeerReviewerAllocationEditView.VIEW_NAME,
                    PeerReviewerAllocationList::class,
                    PeerReviewerAllocationList.serializer(),
                    ARG_SAVED_STATE_PEER_ALLOCATION,
                    arguments = mutableMapOf(
                        ARG_CLAZZUID to entity?.cbClazzUid.toString(),
                        PeerReviewerAllocationEditView.ARG_REVIEWERS_COUNT to
                                entity?.assignment?.caPeerReviewerCount.toString(),
                        UstadView.ARG_CLAZZ_ASSIGNMENT_UID to entity?.assignment?.caUid.toString(),
                        PeerReviewerAllocationEditView.ARG_ASSIGNMENT_GROUP
                                to entity?.assignment?.caGroupUid.toString())
                ))
        }

    }


    override fun handleClickSave(entity: CourseBlockWithEntity) {
        if(!view.fieldsEnabled)
            //Do nothing - prevent anger clicks
            return

        view.loading = true
        view.fieldsEnabled = false
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

            if(entity.assignment?.caMarkingType == ClazzAssignment.MARKED_BY_PEERS){
                val reviewerCount = entity.assignment?.caPeerReviewerCount ?: 0
                val totalSubmitterSize = repo.clazzAssignmentDao.getSubmitterCountFromAssignment(
                    entity.assignment?.caGroupUid ?: 0, entity.cbClazzUid, "")
                if((reviewerCount) <= 0 || reviewerCount >= totalSubmitterSize){
                    // show error on view
                    view.reviewerCountError = " "
                    foundError = true
                }
            }

            val dbGroupUid = repo.clazzAssignmentDao.getGroupUidFromAssignment(entity.assignment?.caUid?: 0L)

            // groups have changed
            if(dbGroupUid != -1L && dbGroupUid != entity.assignment?.caGroupUid){

                val noSubmissionMade = repo.courseAssignmentSubmissionDao
                    .checkNoSubmissionsMade(entity.assignment?.caUid ?: 0L).getFirstValue()

                if(!noSubmissionMade){
                    foundError = true
                    view.showSnackBar(systemImpl.getString(MessageID.error , context))
                }
            }

            val markingTypeDb = repo.clazzAssignmentDao.getMarkingTypeFromAssignment(entity.assignment?.caUid ?: 0L)

            if(markingTypeDb != -1 && markingTypeDb != entity.assignment?.caMarkingType){

                val noSubmissionMarked = repo.courseAssignmentMarkDao
                    .checkNoSubmissionsMarked(entity.assignment?.caUid ?: 0L).getFirstValue()

                if(!noSubmissionMarked){
                    foundError = true
                    view.showSnackBar(systemImpl.getString(MessageID.error , context))
                }
            }


            if(foundError){
                view.loading = false
                view.fieldsEnabled = true
                return@launch
            }

            if(entity.assignment?.caMarkingType == ClazzAssignment.MARKED_BY_PEERS){

                // get list of submitters
                val submitters = repo.clazzAssignmentDao.getSubmitterListForAssignmentList(
                    entity.assignment?.caGroupUid ?: 0, entity.cbClazzUid,
                    systemImpl.getString(MessageID.group_number, context).replace("%1\$s",""))


                val reviewerCount = entity.assignment?.caPeerReviewerCount ?: 0
                val allocatedGroup = entity.assignmentPeerAllocations?.groupBy { it.praToMarkerSubmitterUid }
                val allocatedCount = allocatedGroup?.values?.firstOrNull()?.size ?: 0
                val remainingCount = reviewerCount - allocatedCount

                val peerAllocations = mutableListOf<PeerReviewerAllocation>()
                when{
                    (remainingCount > 0) -> {
                        // add any existing allocations
                        entity.assignmentPeerAllocations?.let { peerAllocations.addAll(it) }

                        // create toBucket for remaining to be allocated
                        val toBucket = submitters.assignRandomly(remainingCount, entity.assignmentPeerAllocations)

                        // for each submitter add more reviewers based on toBucket
                        submitters.forEach { submitter ->
                            val toList = toBucket[submitter.submitterUid] ?: listOf()
                            toList.forEach {
                                peerAllocations.add(PeerReviewerAllocation().apply {
                                        praAssignmentUid = entity.assignment?.caUid ?: 0L
                                        praMarkerSubmitterUid = it
                                        praToMarkerSubmitterUid = submitter.submitterUid
                                })
                            }
                        }
                        entity.assignmentPeerAllocations = peerAllocations
                    }
                    remainingCount < 0 -> {
                        // using the grouped allocations, remove from list and add to allocationsToRemove
                        val allocationsToRemove = mutableListOf<PeerReviewerAllocation>()
                        val removeCount = abs(remainingCount)
                        allocatedGroup?.forEach { map ->
                            val allocation = map.value.toMutableList()
                            repeat(removeCount){
                                allocationsToRemove.add(allocation.removeLast())
                            }
                        }
                        entity.assignmentPeerAllocationsToRemove = allocationsToRemove.map { it.praUid }.filter { it != 0L }
                        // remove from entity list
                        val list = entity.assignmentPeerAllocations?.toMutableList()
                        list?.removeAll(allocationsToRemove)
                        entity.assignmentPeerAllocations = list
                    }
                }
            }else{
                entity.assignmentPeerAllocationsToRemove = entity.assignmentPeerAllocations?.map { it.praUid }?.filter { it != 0L }
                entity.assignmentPeerAllocations = null
            }

            // if grace period is not set, set the date to equal the deadline
            if(entity.cbGracePeriodDate == Long.MAX_VALUE){
                entity.cbGracePeriodDate = entity.cbDeadlineDate
            }

            finishWithResult(safeStringify(di,
                            ListSerializer(CourseBlockWithEntity.serializer()),
                            listOf(entity)))

            view.loading = false

        }
    }




    companion object {

        const val ARG_SAVEDSTATE_CONTENT = "contents"

        const val SAVEDSTATE_KEY_SUBMISSION_TYPE = "submissionType"

        const val ARG_SAVED_STATE_PEER_ALLOCATION = "peerReviewerAllocation"

    }

}