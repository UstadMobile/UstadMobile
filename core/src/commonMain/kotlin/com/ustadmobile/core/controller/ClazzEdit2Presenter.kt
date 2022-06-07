package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.controller.TimeZoneListPresenter.Companion.RESULT_TIMEZONE_KEY
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.localEndOfDay
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_SCHOOL_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.onDbThenRepoWithTimeout
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getDefaultTimeZoneId
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance

fun CourseBlockWithEntityDb.asCourseBlockWithEntity(topicList: List<DiscussionTopic>):
        CourseBlockWithEntity {
    val relevantTopics: List<DiscussionTopic> = topicList.filter {
        it.discussionTopicCourseDiscussionUid == this.courseDiscussion?.courseDiscussionUid
    }.sortedBy { it.discussionTopicIndex }

    val courseBlockWithEntity = CourseBlockWithEntity()
    courseBlockWithEntity.createFromDb(this)
    courseBlockWithEntity.topics = relevantTopics
    courseBlockWithEntity.topicUidsToRemove = listOf()


    return courseBlockWithEntity

}

class ClazzEdit2Presenter(
    context: Any,
    arguments: Map<String, String>,
    view: ClazzEdit2View,
    di : DI,
    lifecycleOwner: DoorLifecycleOwner
) : UstadEditPresenter<ClazzEdit2View, ClazzWithHolidayCalendarAndSchoolAndTerminology>(
    context, arguments, view, di, lifecycleOwner
), TreeOneToManyJoinEditListener<CourseBlockWithEntity>, ItemTouchHelperListener {

    private val json: Json by di.instance()

    enum class EnrolmentPolicyOptions(val optionVal: Int, val messageId: Int) {
        OPEN(Clazz.CLAZZ_ENROLMENT_POLICY_OPEN,
            MessageID.open_enrolment),
        INVITE(Clazz.CLAZZ_ENROLMENT_POLICY_WITH_LINK,
            MessageID.managed_enrolment),
    }

    class  EnrolmentPolicyOptionsMessageIdOption(day: EnrolmentPolicyOptions, context: Any, di: DI)
        : MessageIdOption(day.messageId, context, day.optionVal, di)


    private val scheduleOneToManyJoinEditHelper
            = OneToManyJoinEditHelperMp(Schedule::scheduleUid,
            ARG_SAVEDSTATE_SCHEDULES,
            ListSerializer(Schedule.serializer()),
            ListSerializer(Schedule.serializer()),
            this,
            requireSavedStateHandle(),
            Schedule::class) {scheduleUid = it}

    val scheduleOneToManyJoinListener = scheduleOneToManyJoinEditHelper.createNavigateForResultListener(
        ScheduleEditView.VIEW_NAME, Schedule.serializer())

    lateinit var topics: List<DiscussionTopic>

    private val courseBlockOneToManyJoinEditHelper
            = OneToManyJoinEditHelperMp(CourseBlockWithEntity::cbUid,
            ARG_SAVEDSTATE_BLOCK,
            ListSerializer(CourseBlockWithEntity.serializer()),
            ListSerializer(CourseBlockWithEntity.serializer()),
            this,
            requireSavedStateHandle(),
            CourseBlockWithEntity::class) {cbUid = it}

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.clazzSchedules = scheduleOneToManyJoinEditHelper.liveList
        view.courseBlocks = courseBlockOneToManyJoinEditHelper.liveList
        view.enrolmentPolicyOptions = EnrolmentPolicyOptions.values().map {
            EnrolmentPolicyOptionsMessageIdOption(it, context, di)
        }
    }

    override fun onLoadDataComplete() {
        super.onLoadDataComplete()

        requireSavedStateHandle().getLiveData<String?>(RESULT_TIMEZONE_KEY).observe(lifecycleOwner) {
            val timezone = it ?: return@observe
            entity?.clazzTimeZone = timezone
            view.entity = entity
            requireSavedStateHandle()[RESULT_TIMEZONE_KEY] = null
        }

        observeSavedStateResult(SAVEDSTATE_KEY_SCHOOL, ListSerializer(School.serializer()),
            School::class) {
            val school = it.firstOrNull() ?: return@observeSavedStateResult
            entity?.school = school
            entity?.clazzSchoolUid = school.schoolUid
            view.entity = entity
            requireSavedStateHandle()[SAVEDSTATE_KEY_SCHOOL] = null
        }

        observeSavedStateResult(SAVEDSTATE_KEY_HOLIDAYCALENDAR,
            ListSerializer(HolidayCalendar.serializer()), HolidayCalendar::class) {
            val calendar = it.firstOrNull() ?: return@observeSavedStateResult
            entity?.holidayCalendar = calendar
            entity?.clazzHolidayUMCalendarUid = calendar.umCalendarUid
            view.entity = entity
            requireSavedStateHandle()[SAVEDSTATE_KEY_HOLIDAYCALENDAR] = null
        }

        observeSavedStateResult(
            SAVEDSTATE_KEY_TERMINOLOGY,
            ListSerializer(CourseTerminology.serializer()), CourseTerminology::class) {
            val terminology = it.firstOrNull() ?: return@observeSavedStateResult
            entity?.clazzTerminologyUid = terminology.ctUid
            entity?.terminology = terminology
            view.entity = entity
            requireSavedStateHandle()[SAVEDSTATE_KEY_TERMINOLOGY] = null
        }

        observeSavedStateResult(
            RESULT_TIMEZONE_KEY,
            ListSerializer(String.serializer()), String::class) {
            val timeZone = it.firstOrNull() ?: return@observeSavedStateResult
            entity?.clazzTimeZone = timeZone
            view.entity = entity
            requireSavedStateHandle()[RESULT_TIMEZONE_KEY] = null
        }

        observeSavedStateResult(SAVEDSTATE_KEY_FEATURES,
            ListSerializer(LongWrapper.serializer()), LongWrapper::class) {
            val wrapper = it.firstOrNull() ?: return@observeSavedStateResult
            entity?.clazzFeatures = wrapper.longValue
            view.entity = entity
            requireSavedStateHandle()[SAVEDSTATE_KEY_FEATURES] = null
        }

        observeSavedStateResult(SAVEDSTATE_KEY_ASSIGNMENT,
            ListSerializer(CourseBlockWithEntity.serializer()), CourseBlockWithEntity::class){
            val newAssignment = it.firstOrNull() ?: return@observeSavedStateResult

            val foundBlock: CourseBlockWithEntity = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.find {
                assignment -> assignment.assignment?.caUid == newAssignment.assignment?.caUid
            } ?: CourseBlockWithEntity().apply {
                cbClazzUid = newAssignment.cbClazzUid
                cbEntityUid = newAssignment.assignment?.caUid ?: 0
                cbTitle = newAssignment.assignment?.caTitle
                cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                cbDescription = newAssignment.assignment?.caDescription
                cbIndex = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.size ?: 0
                cbUid = newAssignment.cbUid
                cbHideUntilDate = newAssignment.cbHideUntilDate
                cbDeadlineDate = newAssignment.cbDeadlineDate
                cbGracePeriodDate = newAssignment.cbGracePeriodDate
                cbLateSubmissionPenalty = newAssignment.cbLateSubmissionPenalty
                cbCompletionCriteria = newAssignment.cbCompletionCriteria
                cbMaxPoints = newAssignment.cbMaxPoints

                assignment = newAssignment.assignment
            }

            foundBlock.assignment = newAssignment.assignment
            foundBlock.cbTitle = newAssignment.assignment?.caTitle
            foundBlock.cbDescription = newAssignment.assignment?.caDescription
            foundBlock.cbHideUntilDate = newAssignment.cbHideUntilDate
            foundBlock.cbDeadlineDate = newAssignment.cbDeadlineDate
            foundBlock.cbGracePeriodDate = newAssignment.cbGracePeriodDate
            foundBlock.cbCompletionCriteria = newAssignment.cbCompletionCriteria
            foundBlock.cbLateSubmissionPenalty = newAssignment.cbLateSubmissionPenalty
            foundBlock.cbMaxPoints = newAssignment.cbMaxPoints

            courseBlockOneToManyJoinEditHelper.onEditResult(foundBlock)

            requireSavedStateHandle()[SAVEDSTATE_KEY_ASSIGNMENT] = null

        }

        observeSavedStateResult(
            SAVEDSTATE_KEY_CONTENT,
            ListSerializer(ContentEntryWithBlockAndLanguage.serializer()), ContentEntryWithBlockAndLanguage::class) {
            val newContent = it.firstOrNull() ?: return@observeSavedStateResult

            val foundBlock: CourseBlockWithEntity =
                courseBlockOneToManyJoinEditHelper.liveList.getValue()?.find { assignment ->
                    assignment.entry?.contentEntryUid == newContent.contentEntryUid
                } ?: CourseBlockWithEntity().apply {
                    cbClazzUid = entity?.clazzUid ?: 0L
                    cbEntityUid = newContent.contentEntryUid
                    cbTitle = newContent.title
                    cbType = CourseBlock.BLOCK_CONTENT_TYPE
                    cbDescription = newContent.description
                    cbIndex = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.size ?: 0
                    cbUid = newContent.block?.cbUid ?: db.doorPrimaryKeyManager.nextId(CourseBlock.TABLE_ID)

                    cbHideUntilDate = newContent.block?.cbHideUntilDate ?: 0
                    cbDeadlineDate = newContent.block?.cbDeadlineDate ?: Long.MAX_VALUE
                    cbGracePeriodDate = newContent.block?.cbGracePeriodDate ?: Long.MAX_VALUE
                    cbLateSubmissionPenalty = newContent.block?.cbLateSubmissionPenalty ?: 0
                    cbCompletionCriteria = newContent.block?.cbCompletionCriteria ?: 0
                    cbMaxPoints = newContent.block?.cbMaxPoints ?: 10
                    cbMinPoints = newContent.block?.cbMinPoints ?: 0

                    entry = newContent
                    language = newContent.language
                }

            foundBlock.entry = newContent
            foundBlock.language = newContent.language

            foundBlock.cbTitle = newContent.title
            foundBlock.cbDescription = newContent.description

            foundBlock.cbHideUntilDate = newContent.block?.cbHideUntilDate ?: 0
            foundBlock.cbDeadlineDate = newContent.block?.cbDeadlineDate ?: Long.MAX_VALUE
            foundBlock.cbGracePeriodDate = newContent.block?.cbGracePeriodDate ?: Long.MAX_VALUE
            foundBlock.cbLateSubmissionPenalty = newContent.block?.cbLateSubmissionPenalty ?: 0
            foundBlock.cbCompletionCriteria = newContent.block?.cbCompletionCriteria ?: 0
            foundBlock.cbMaxPoints = newContent.block?.cbMaxPoints ?: 10
            foundBlock.cbMinPoints = newContent.block?.cbMinPoints ?: 0

            courseBlockOneToManyJoinEditHelper.onEditResult(foundBlock)

            requireSavedStateHandle()[SAVEDSTATE_KEY_CONTENT] = null
        }

        observeSavedStateResult(ARG_SAVEDSTATE_MODULE,
                ListSerializer(CourseBlock.serializer()), CourseBlock::class){
            val moduleBlock = it.firstOrNull() ?: return@observeSavedStateResult

            val foundBlock: CourseBlockWithEntity = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.find {
                    block -> block.cbUid == moduleBlock.cbUid
            } ?: CourseBlockWithEntity().apply {
                cbClazzUid = moduleBlock.cbClazzUid
                cbEntityUid = moduleBlock.cbUid
                cbTitle = moduleBlock.cbTitle
                cbType = CourseBlock.BLOCK_MODULE_TYPE
                cbDescription = moduleBlock.cbDescription
                cbHideUntilDate = moduleBlock.cbHideUntilDate
                cbIndex = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.size ?: 0
                cbUid = moduleBlock.cbUid
            }

            foundBlock.cbTitle = moduleBlock.cbTitle
            foundBlock.cbDescription = moduleBlock.cbDescription
            foundBlock.cbHideUntilDate = moduleBlock.cbHideUntilDate

            courseBlockOneToManyJoinEditHelper.onEditResult(foundBlock)

            requireSavedStateHandle()[ARG_SAVEDSTATE_MODULE] = null
        }
        observeSavedStateResult(ARG_SAVEDSTATE_TEXT,
            ListSerializer(CourseBlock.serializer()), CourseBlock::class){
            val textBlock = it.firstOrNull() ?: return@observeSavedStateResult

            val foundBlock: CourseBlockWithEntity = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.find {
                    block -> block.cbUid == textBlock.cbUid
            } ?: CourseBlockWithEntity().apply {
                cbClazzUid = textBlock.cbClazzUid
                cbEntityUid = textBlock.cbUid
                cbTitle = textBlock.cbTitle
                cbType = CourseBlock.BLOCK_TEXT_TYPE
                cbDescription = textBlock.cbDescription
                cbHideUntilDate = textBlock.cbHideUntilDate
                cbIndex = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.size ?: 0
                cbUid = textBlock.cbUid
            }

            foundBlock.cbTitle = textBlock.cbTitle
            foundBlock.cbDescription = textBlock.cbDescription
            foundBlock.cbHideUntilDate = textBlock.cbHideUntilDate

            courseBlockOneToManyJoinEditHelper.onEditResult(foundBlock)

            requireSavedStateHandle()[ARG_SAVEDSTATE_TEXT] = null
        }


        observeSavedStateResult(
            SAVEDSTATE_KEY_DISCUSSION,
            ListSerializer(CourseBlockWithEntity.serializer()), CourseBlockWithEntity::class){
            val newDiscussion = it.firstOrNull() ?: return@observeSavedStateResult

            val foundBlock: CourseBlockWithEntity = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.find {
                    discussion -> discussion.courseDiscussion?.courseDiscussionUid == newDiscussion.courseDiscussion?.courseDiscussionUid
            } ?: CourseBlockWithEntity().apply {
                cbClazzUid = newDiscussion.cbClazzUid
                cbEntityUid = newDiscussion.courseDiscussion?.courseDiscussionUid ?: 0
                cbTitle = newDiscussion.courseDiscussion?.courseDiscussionTitle
                cbType = CourseBlock.BLOCK_DISCUSSION_TYPE
                cbDescription = newDiscussion.courseDiscussion?.courseDiscussionDesc
                cbIndex = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.size ?: 0
                cbUid = newDiscussion.cbUid
                cbHideUntilDate = newDiscussion.cbHideUntilDate
                cbDeadlineDate = newDiscussion.cbDeadlineDate
                cbGracePeriodDate = newDiscussion.cbGracePeriodDate
                cbLateSubmissionPenalty = newDiscussion.cbLateSubmissionPenalty
                cbCompletionCriteria = newDiscussion.cbCompletionCriteria
                cbMaxPoints = newDiscussion.cbMaxPoints

                courseDiscussion = newDiscussion.courseDiscussion

                topics = newDiscussion.topics
                topicUidsToRemove = newDiscussion.topicUidsToRemove
            }

            foundBlock.courseDiscussion = newDiscussion.courseDiscussion
            foundBlock.cbTitle = newDiscussion.courseDiscussion?.courseDiscussionTitle
            foundBlock.cbDescription = newDiscussion.courseDiscussion?.courseDiscussionDesc
            foundBlock.cbHideUntilDate = newDiscussion.cbHideUntilDate
            foundBlock.cbDeadlineDate = newDiscussion.cbDeadlineDate
            foundBlock.cbGracePeriodDate = newDiscussion.cbGracePeriodDate
            foundBlock.cbCompletionCriteria = newDiscussion.cbCompletionCriteria
            foundBlock.cbLateSubmissionPenalty = newDiscussion.cbLateSubmissionPenalty
            foundBlock.cbMaxPoints = newDiscussion.cbMaxPoints

            foundBlock.topics = newDiscussion.topics
            foundBlock.topicUidsToRemove = newDiscussion.topicUidsToRemove

            courseBlockOneToManyJoinEditHelper.onEditResult(foundBlock)

            requireSavedStateHandle()[SAVEDSTATE_KEY_DISCUSSION] = null
        }
    }



    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzWithHolidayCalendarAndSchoolAndTerminology {
        val clazzUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazz = db.onRepoWithFallbackToDb(2000) {
            it.clazzDao.takeIf {clazzUid != 0L }?.findByUidWithHolidayCalendarAsync(clazzUid)
        } ?: ClazzWithHolidayCalendarAndSchoolAndTerminology().also { newClazz ->
            newClazz.clazzUid = db.doorPrimaryKeyManager.nextId(Clazz.TABLE_ID)
            newClazz.clazzName = ""
            newClazz.isClazzActive = true
            newClazz.clazzStartTime = systemTimeInMillis()
            newClazz.clazzTimeZone = getDefaultTimeZoneId()
            newClazz.clazzSchoolUid = arguments[ARG_SCHOOL_UID]?.toLong() ?: 0L
            newClazz.school = db.schoolDao.takeIf { newClazz.clazzSchoolUid != 0L }?.findByUidAsync(newClazz.clazzSchoolUid)
            newClazz.terminology = db.courseTerminologyDao.takeIf { newClazz.clazzTerminologyUid != 0L }?.findByUidAsync(newClazz.clazzTerminologyUid)
        }

        view.coursePicture = db.onDbThenRepoWithTimeout(2000) { dbToUse, _ ->
            dbToUse.takeIf { clazzUid != 0L }?.coursePictureDao?.findByClazzUidAsync(clazzUid)
        } ?: CoursePicture()

        val schedules = db.onRepoWithFallbackToDb(2000) {
            it.scheduleDao.takeIf { clazzUid != 0L }?.findAllSchedulesByClazzUidAsync(clazzUid)
        } ?: listOf()
        scheduleOneToManyJoinEditHelper.liveList.sendValue(schedules)

        val courseBlocksDb: List<CourseBlockWithEntityDb> = db.onRepoWithFallbackToDb(2000){
            it.courseBlockDao.takeIf { clazzUid != 0L }?.findAllCourseBlockByClazzUidAsync(clazzUid)
        } ?: listOf()

        //Get topics
        topics = db.onRepoWithFallbackToDb(2000){
            it.discussionTopicDao.getTopicsByClazz(clazzUid)
        }

        val courseBlocks: List<CourseBlockWithEntity> = courseBlocksDb.map {
            it.asCourseBlockWithEntity(topics)
        }

        courseBlockOneToManyJoinEditHelper.liveList.sendValue(courseBlocks)

        return clazz
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzWithHolidayCalendarAndSchoolAndTerminology {
        super.onLoadFromJson(bundle)
        val clazzJsonStr = bundle[ARG_ENTITY_JSON]
        val clazz = if(clazzJsonStr != null) {
            safeParse(di, ClazzWithHolidayCalendarAndSchoolAndTerminology.serializer(), clazzJsonStr)
        }else {
            ClazzWithHolidayCalendarAndSchoolAndTerminology()
        }

        scheduleOneToManyJoinEditHelper.onLoadFromJsonSavedState(bundle)
        courseBlockOneToManyJoinEditHelper.onLoadFromJsonSavedState(bundle)
        view.coursePicture = bundle[SAVEDSTATE_KEY_COURSEPICTURE]?.let {
            json.decodeFromString(CoursePicture.serializer(), it)
        }
        return clazz
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = view.entity ?: return
        savedState.putEntityAsJson(SAVEDSTATE_KEY_COURSEPICTURE, null, view.coursePicture)
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null, entityVal)
    }

    fun handleClickTimezone() {
        navigateForResult(NavigateForResultOptions(
            this,
            entity?.clazzTimeZone,
            TimeZoneListView.VIEW_NAME,
            String::class,
            String.serializer(),
            RESULT_TIMEZONE_KEY))
    }

    fun handleHolidayCalendarClicked() {
        navigateForResult(
            NavigateForResultOptions(this,
            null,
                HolidayCalendarListView.VIEW_NAME,
                HolidayCalendar::class,
            HolidayCalendar.serializer(),
                SAVEDSTATE_KEY_HOLIDAYCALENDAR)
        )
    }

    fun handleClickSchool() {
        val args = mutableMapOf(
            UstadView.ARG_FILTER_BY_PERMISSION to Role.PERMISSION_PERSON_DELEGATE.toString())
        navigateForResult(
            NavigateForResultOptions(this,
            null, SchoolListView.VIEW_NAME, School::class,
            School.serializer(), SAVEDSTATE_KEY_SCHOOL,
                arguments = args)
        )
    }

    fun handleTerminologyClicked(){
        navigateForResult(NavigateForResultOptions(this,
            null,
            CourseTerminologyListView.VIEW_NAME,
            CourseTerminology::class,
            CourseTerminology.serializer(),
            SAVEDSTATE_KEY_TERMINOLOGY))
    }

    fun handleClickFeatures() {
        navigateForResult(NavigateForResultOptions(this,
            LongWrapper(entity?.clazzFeatures ?: 0),
            BitmaskEditView.VIEW_NAME,
            LongWrapper::class,
            LongWrapper.serializer(),
            SAVEDSTATE_KEY_FEATURES))
    }

    override fun handleClickSave(entity: ClazzWithHolidayCalendarAndSchoolAndTerminology) {
        if(!view.fieldsEnabled)
            return

        view.fieldsEnabled = false

        presenterScope.launch {

            if (entity.clazzStartTime == 0L) {
                view.clazzStartDateError = systemImpl.getString(MessageID.field_required_prompt, context)
                view.fieldsEnabled = true
                return@launch
            }

            if (entity.clazzEndTime <= entity.clazzStartTime) {
                view.clazzEndDateError = systemImpl.getString(MessageID.end_is_before_start_error, context)
                view.fieldsEnabled = true
                return@launch
            }

            view.loading = true

            entity.clazzStartTime = DateTime(entity.clazzStartTime)
                    .toOffsetByTimezone(entity.effectiveTimeZone).localMidnight.utc.unixMillisLong
            if(entity.clazzEndTime != Long.MAX_VALUE){
                entity.clazzEndTime = DateTime(entity.clazzEndTime)
                        .toOffsetByTimezone(entity.effectiveTimeZone).localEndOfDay.utc.unixMillisLong
            }

            val courseBlockList = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.toList() ?: listOf()
            var currentParentBlock: CourseBlock? = null
            courseBlockList.forEachIndexed { index, item ->
                item.cbIndex = index
                if(item.cbType == CourseBlock.BLOCK_MODULE_TYPE){
                    currentParentBlock = item
                }else if(item.cbIndentLevel != 0){
                    item.cbModuleParentBlockUid = currentParentBlock?.cbUid ?: 0L
                }
            }
            courseBlockOneToManyJoinEditHelper.liveList.sendValue(courseBlockList)


            repo.withDoorTransactionAsync(UmAppDatabase::class) { txDb ->

                if((arguments[UstadView.ARG_ENTITY_UID]?.toLongOrNull() ?: 0L) == 0L) {
                    val termMap = db.courseTerminologyDao.findByUidAsync(entity.clazzTerminologyUid)
                        .toTermMap(json, systemImpl, context)
                    txDb.createNewClazzAndGroups(entity, systemImpl, termMap, context)
                }else {
                    txDb.clazzDao.updateAsync(entity)
                }

                scheduleOneToManyJoinEditHelper.commitToDatabase(txDb.scheduleDao) {
                    it.scheduleClazzUid = entity.clazzUid
                }

                val assignmentList = courseBlockOneToManyJoinEditHelper.entitiesToInsert.mapNotNull { it.assignment }
                txDb.clazzAssignmentDao.insertListAsync(assignmentList)
                txDb.clazzAssignmentDao.updateListAsync(
                        courseBlockOneToManyJoinEditHelper.entitiesToUpdate
                                .mapNotNull { it.assignment })
                txDb.clazzAssignmentDao.deactivateByUids(
                        courseBlockOneToManyJoinEditHelper.primaryKeysToDeactivate, systemTimeInMillis())

                assignmentList.forEach { assignment ->
                    val clazzAssignmentObjectId = UMFileUtil.joinPaths(accountManager.activeAccount.endpointUrl,
                            "/clazzAssignment/${assignment.caUid}")
                    val xobject = XObjectEntity().apply {
                        this.objectId = clazzAssignmentObjectId
                        this.objectType = "Activity"
                    }
                    xobject.xObjectUid = txDb.xObjectDao.insertAsync(xobject)
                    assignment.caXObjectUid = xobject.xObjectUid
                    txDb.clazzAssignmentDao.updateAsync(assignment)
                }

                txDb.courseDiscussionDao.replaceListAsync(
                    courseBlockOneToManyJoinEditHelper.entitiesToInsert.mapNotNull {
                        it.courseDiscussion
                    } +
                    courseBlockOneToManyJoinEditHelper.entitiesToUpdate.mapNotNull {
                        it.courseDiscussion
                    })
                txDb.courseDiscussionDao.deactivateByUids(
                    courseBlockOneToManyJoinEditHelper.primaryKeysToDeactivate, systemTimeInMillis())


                //Save Discussion Topic

                val tti : List<DiscussionTopic> = courseBlockList.mapNotNull{
                    it.topics
                }.flatten()

                txDb.discussionTopicDao.replaceListAsync(tti)

                val topicUidsToDelete: List<Long> = courseBlockList.mapNotNull{it.topicUidsToRemove }
                    .flatten()

                txDb.discussionTopicDao.deactivateByUids(topicUidsToDelete, systemTimeInMillis())

                txDb.courseBlockDao.replaceListAsync(courseBlockList)
                txDb.courseBlockDao.deactivateByUids(
                    courseBlockOneToManyJoinEditHelper.primaryKeysToDeactivate,
                    systemTimeInMillis())

                val coursePictureVal = view.coursePicture
                if(coursePictureVal != null) {
                    coursePictureVal.coursePictureClazzUid = entity.clazzUid

                    if(coursePictureVal.coursePictureUid == 0L) {
                        txDb.coursePictureDao.insertAsync(coursePictureVal)
                    }else {
                        txDb.coursePictureDao.updateAsync(coursePictureVal)
                    }
                }
            }

            val fromDateTime = DateTime.now().toOffsetByTimezone(entity.effectiveTimeZone).localMidnight
            val clazzLogCreatorManager: ClazzLogCreatorManager by di.instance()
            clazzLogCreatorManager.requestClazzLogCreation(entity.clazzUid,
                    accountManager.activeAccount.endpointUrl,
                    fromDateTime.utc.unixMillisLong, fromDateTime.localEndOfDay.utc.unixMillisLong)
            view.loading = false

            onFinish(ClazzDetailView.VIEW_NAME, entity.clazzUid, entity,
                ClazzWithHolidayCalendarAndSchoolAndTerminology.serializer())
        }
    }

    fun handleClickAddAssignment() {
        val args = mutableMapOf<String, String>()
        args[UstadView.ARG_CLAZZUID] = entity?.clazzUid.toString()
        if(entity != null){
            args[ClazzAssignmentEditView.TERMINOLOGY_ID] = entity?.clazzTerminologyUid.toString()
        }

        navigateForResult(NavigateForResultOptions(
                this,
                currentEntityValue = null,
                destinationViewName = ClazzAssignmentEditView.VIEW_NAME,
                entityClass = CourseBlockWithEntity::class,
                serializationStrategy = CourseBlockWithEntity.serializer(),
                destinationResultKey = SAVEDSTATE_KEY_ASSIGNMENT,
                arguments = args))
    }

    fun handleClickAddContent(){
        val args = mutableMapOf(
            ContentEntryList2View.ARG_SELECT_FOLDER_VISIBLE to false.toString(),
            ContentEntryList2View.ARG_USE_CHIPS to true.toString(),
            UstadView.ARG_CLAZZUID to entity?.clazzUid.toString(),
            ContentEntryEdit2View.BLOCK_REQUIRED to true.toString(),
        )

        navigateForResult(NavigateForResultOptions(
            this, null,
            ContentEntryList2View.VIEW_NAME,
            ContentEntryWithBlockAndLanguage::class,
            ContentEntryWithBlockAndLanguage.serializer(),
            SAVEDSTATE_KEY_CONTENT,
            arguments = args)
        )
    }

    fun handleClickAddModule() {
        val args = mutableMapOf<String, String>()
        args[UstadView.ARG_CLAZZUID] = entity?.clazzUid.toString()

        navigateForResult(NavigateForResultOptions(
            this,
            currentEntityValue = null,
            destinationViewName = ModuleCourseBlockEditView.VIEW_NAME,
            entityClass = CourseBlock::class,
            serializationStrategy = CourseBlock.serializer(),
            destinationResultKey = ARG_SAVEDSTATE_MODULE,
            arguments = args))
    }

    fun handleClickAddDiscussion(){
        val args = mutableMapOf<String, String>()
        args[UstadView.ARG_CLAZZUID] = entity?.clazzUid.toString()

        navigateForResult(NavigateForResultOptions(
            this,
            currentEntityValue = null,
            destinationViewName = CourseDiscussionEditView.VIEW_NAME,
            entityClass = CourseDiscussion::class,
            serializationStrategy = CourseDiscussion.serializer(),
            destinationResultKey = SAVEDSTATE_KEY_DISCUSSION,
            arguments = args))

    }

    fun handleClickAddText(){
        val args = mutableMapOf<String, String>()
        args[UstadView.ARG_CLAZZUID] = entity?.clazzUid.toString()

        navigateForResult(NavigateForResultOptions(
            this,
            currentEntityValue = null,
            destinationViewName = TextCourseBlockEditView.VIEW_NAME,
            entityClass = CourseBlock::class,
            serializationStrategy = CourseBlock.serializer(),
            destinationResultKey = ARG_SAVEDSTATE_TEXT,
            arguments = args))
    }

    override fun onClickNew() {
    }

    override fun onClickEdit(joinedEntity: CourseBlockWithEntity) {

        val navigateForResultOptions = when(joinedEntity.cbType){
            CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
                val args = mutableMapOf<String, String>()
                args[UstadView.ARG_CLAZZUID] = (joinedEntity.assignment?.caClazzUid ?: entity?.clazzUid ?: 0L).toString()
                args[UstadView.ARG_ENTITY_UID] = (joinedEntity.assignment?.caUid ?: 0L).toString()
                if(entity != null){
                    args[ClazzAssignmentEditView.TERMINOLOGY_ID] = entity?.clazzTerminologyUid.toString()
                }

                NavigateForResultOptions(
                        this,
                        currentEntityValue = joinedEntity,
                        destinationViewName = ClazzAssignmentEditView.VIEW_NAME,
                        entityClass = CourseBlockWithEntity::class,
                        serializationStrategy = CourseBlockWithEntity.serializer(),
                        destinationResultKey = SAVEDSTATE_KEY_ASSIGNMENT,
                        arguments = args)
            }
            CourseBlock.BLOCK_CONTENT_TYPE -> {

                val entry = joinedEntity.entry ?: return

                val entity = ContentEntryWithBlockAndLanguage().apply {
                    contentEntryUid = entry.contentEntryUid
                    title = entry.title
                    description = entry.description
                    author = entry.author
                    publisher = entry.publisher
                    licenseType = entry.licenseType
                    licenseName = entry.licenseName
                    licenseUrl = entry.licenseUrl
                    sourceUrl = entry.sourceUrl
                    lastModified = entry.lastModified
                    primaryLanguageUid = entry.primaryLanguageUid
                    languageVariantUid = entry.languageVariantUid
                    contentFlags = entry.contentFlags
                    leaf = entry.leaf
                    publik = entry.publik
                    ceInactive = entry.ceInactive
                    contentTypeFlag = entry.contentTypeFlag
                    contentOwner = entry.contentOwner
                    contentEntryLocalChangeSeqNum = entry.contentEntryLocalChangeSeqNum
                    contentEntryMasterChangeSeqNum = entry.contentEntryMasterChangeSeqNum
                    contentEntryLastChangedBy = entry.contentEntryLastChangedBy
                    contentEntryLct = entry.contentEntryLct

                    block = joinedEntity
                    language = joinedEntity.language
                }


                NavigateForResultOptions(
                    this, entity,
                    ContentEntryEdit2View.VIEW_NAME,
                    ContentEntryWithBlockAndLanguage::class,
                    ContentEntryWithBlockAndLanguage.serializer(),
                    SAVEDSTATE_KEY_CONTENT,
                    arguments = mutableMapOf(
                        UstadView.ARG_ENTITY_UID to joinedEntity.entry?.contentEntryUid.toString(),
                        UstadView.ARG_LEAF to true.toString(),
                        UstadView.ARG_CLAZZUID to joinedEntity.cbClazzUid.toString(),
                        ContentEntryEdit2View.BLOCK_REQUIRED to true.toString()))
            }
            CourseBlock.BLOCK_MODULE_TYPE -> {
                val args = mutableMapOf<String, String>()
                args[UstadView.ARG_CLAZZUID] = joinedEntity.cbClazzUid.toString()
                args[UstadView.ARG_ENTITY_UID] = joinedEntity.cbUid.toString()

                NavigateForResultOptions(
                    this,
                    currentEntityValue = joinedEntity,
                    destinationViewName = ModuleCourseBlockEditView.VIEW_NAME,
                    entityClass = CourseBlock::class,
                    serializationStrategy = CourseBlock.serializer(),
                    destinationResultKey = ARG_SAVEDSTATE_MODULE,
                    arguments = args)
            }
            CourseBlock.BLOCK_TEXT_TYPE -> {
                val args = mutableMapOf<String, String>()
                args[UstadView.ARG_CLAZZUID] = joinedEntity.cbClazzUid.toString()
                args[UstadView.ARG_ENTITY_UID] = joinedEntity.cbUid.toString()

                NavigateForResultOptions(
                    this,
                    currentEntityValue = joinedEntity,
                    destinationViewName = TextCourseBlockEditView.VIEW_NAME,
                    entityClass = CourseBlock::class,
                    serializationStrategy = CourseBlock.serializer(),
                    destinationResultKey = ARG_SAVEDSTATE_TEXT,
                    arguments = args)
            }
            CourseBlock.BLOCK_DISCUSSION_TYPE -> {
                val args = mutableMapOf<String, String>()
                args[UstadView.ARG_CLAZZUID] =
                    (joinedEntity.courseDiscussion?.courseDiscussionClazzUid ?: entity?.clazzUid ?: 0L)
                        .toString()
                args[UstadView.ARG_ENTITY_UID] =
                    (joinedEntity.courseDiscussion?.courseDiscussionUid?: 0L).toString()

                NavigateForResultOptions(
                    this,
                    currentEntityValue = joinedEntity,
                    destinationViewName = CourseDiscussionEditView.VIEW_NAME,
                    entityClass = CourseBlockWithEntity::class,
                    serializationStrategy = CourseBlockWithEntity.serializer(),
                    destinationResultKey = SAVEDSTATE_KEY_DISCUSSION,
                    arguments = args)

            }

            else -> return
        }


        navigateForResult(navigateForResultOptions)
    }

    override fun onClickDelete(joinedEntity: CourseBlockWithEntity) {
        courseBlockOneToManyJoinEditHelper.onDeactivateEntity(joinedEntity)
    }

    override fun onClickIndent(joinedEntity: CourseBlockWithEntity) {
        if(joinedEntity.cbModuleParentBlockUid == 0L){
            val currentList = courseBlockOneToManyJoinEditHelper.liveList.getValue() ?: listOf()
            val index = currentList.indexOf(joinedEntity)
            for(n in index downTo 0){
                if(currentList[n].cbType == CourseBlock.BLOCK_MODULE_TYPE){
                    joinedEntity.cbModuleParentBlockUid = currentList[n].cbUid
                    break
                }
            }
        }
        joinedEntity.cbIndentLevel++
        courseBlockOneToManyJoinEditHelper.onEditResult(joinedEntity)
    }

    override fun onClickUnIndent(joinedEntity: CourseBlockWithEntity) {
        val newList = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.toMutableList() ?: mutableListOf()
        val foundBlock = newList.find { it.cbUid == joinedEntity.cbUid } ?: return
        foundBlock.cbIndentLevel--
        if(foundBlock.cbIndentLevel == 0){
            foundBlock.cbModuleParentBlockUid = 0L
        }
        newList[foundBlock.cbIndex] = foundBlock
        courseBlockOneToManyJoinEditHelper.liveList.sendValue(newList)
        view.courseBlocks = courseBlockOneToManyJoinEditHelper.liveList
    }

    /*
     * Will hide or unhide the courseBlock, if block is a module, its children will also change
     */
    override fun onClickHide(joinedEntity: CourseBlockWithEntity) {
        val newList = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.toMutableList() ?: return
        val foundBlock = newList.find { it.cbUid == joinedEntity.cbUid } ?: return
        foundBlock.cbHidden = !foundBlock.cbHidden
        newList[foundBlock.cbIndex] = foundBlock

        if(foundBlock.cbType == CourseBlock.BLOCK_MODULE_TYPE) {
            newList.forEach{
                it.takeIf { it.cbModuleParentBlockUid == foundBlock.cbUid }
                    ?.cbHidden = foundBlock.cbHidden
            }
        }
        courseBlockOneToManyJoinEditHelper.liveList.sendValue(newList)
    }


    /*

    If moving a module:
         If destinationBlock is:
        A block with parentModule = 0, then:
            If module being moved is going down, then it's position in the new list is (destinationBlock + 1 + destinationBlock.numChildren)
              If module being moved is going up, then it's destination in in the new list is destubationBlock

         A block is child and end of a module:
            Then its destination in the new list is destinationBlock.index + 1 to move below it
        else not end of module - reject it

     If moving a non-module block:
      If destinationBlock.previous is part of a module, or is itself a module,
            then the block being moved is assigned to that module
                and given the same indentation (minimum indent level = 1)
            else set module to zero and indentation to zero.

     */
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val currentList = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.toMutableList() ?: mutableListOf()

        val movingBlock = currentList[fromPosition]

        val destinationBlock = currentList[toPosition]

        val nextBlock = currentList.getOrNull(toPosition + 1)
        val isChildBlock = destinationBlock.cbModuleParentBlockUid != 0L
        val blockMovingDown = fromPosition < toPosition
        val lastBlockInModule = destinationBlock.cbModuleParentBlockUid != nextBlock?.cbModuleParentBlockUid

        // reject if moving a module, destination is a child block and not last in the block
        if(movingBlock.cbType == CourseBlock.BLOCK_MODULE_TYPE
            && isChildBlock && !lastBlockInModule) {
            courseBlockOneToManyJoinEditHelper.liveList.sendValue(currentList.toList())
            return false
        }

        // remove the block from the list
        currentList.removeAt(fromPosition)

        if(movingBlock.cbType == CourseBlock.BLOCK_MODULE_TYPE){

            val destinationBlockChildren = currentList.filter { it.cbModuleParentBlockUid == destinationBlock.cbUid }
            // if destination is parent block
            if(destinationBlock.cbModuleParentBlockUid == 0L){
                if(blockMovingDown) {
                    // if moving downwards, and destination has children, move below the children of destination
                    currentList.addSafelyToPosition(toPosition + destinationBlockChildren.size, movingBlock)
                }else{
                    currentList.add(toPosition, movingBlock)
                }
            }else {
                // else, destination is child and is last child of module, move the block below it
                currentList.addSafelyToPosition(toPosition + 1, movingBlock)
            }

            // remove all the child and move it below the destination
            val childBlocks = currentList.filter { it.cbModuleParentBlockUid == movingBlock.cbUid }
            currentList.removeAll(childBlocks)
            val index = currentList.indexOf(movingBlock) + 1
            currentList.addAll(index, childBlocks)


        }else {
            //if child moves out of module, update child to have parentBlock = 0 or find new parent
            currentList.add(toPosition, movingBlock)
            val previousBlock = currentList.getOrNull(toPosition - 1)
            when {
                previousBlock == null -> {
                    movingBlock.cbModuleParentBlockUid = 0
                    movingBlock.cbIndentLevel = 0
                }
                previousBlock.cbType == CourseBlock.BLOCK_MODULE_TYPE -> {
                    movingBlock.cbModuleParentBlockUid = previousBlock.cbUid
                    movingBlock.cbIndentLevel = 1
                }
                else -> {
                    movingBlock.cbModuleParentBlockUid = previousBlock.cbModuleParentBlockUid
                    movingBlock.cbIndentLevel = previousBlock.cbIndentLevel
                }
            }
            currentList[toPosition] = movingBlock
        }
        
        // finally update the list with new index values
        currentList.forEachIndexed{ index , item ->
            item.cbIndex = index
        }


        courseBlockOneToManyJoinEditHelper.liveList.sendValue(currentList.toList())
        return true
    }

    override fun onItemDismiss(position: Int) {

    }

    companion object {

        const val ARG_SAVEDSTATE_SCHEDULES = "schedules"

        const val ARG_SAVEDSTATE_BLOCK = "courseBlocks"

        const val ARG_SAVEDSTATE_MODULE = "courseModule"

        const val ARG_SAVEDSTATE_TEXT = "courseText"

        const val SAVEDSTATE_KEY_SCHOOL = "School"

        const val SAVEDSTATE_KEY_ASSIGNMENT = "Assignment"

        const val SAVEDSTATE_KEY_CONTENT = "courseContent"

        const val SAVEDSTATE_KEY_HOLIDAYCALENDAR = "ClazzHolidayCalendar"

        const val SAVEDSTATE_KEY_FEATURES = "ClazzFeatures"

        const val SAVEDSTATE_KEY_TERMINOLOGY ="ClazzTerminology"

        const val SAVEDSTATE_KEY_DISCUSSION = "CourseDiscussion"

        const val SAVEDSTATE_KEY_COURSEPICTURE = "CoursePicture"

    }

}