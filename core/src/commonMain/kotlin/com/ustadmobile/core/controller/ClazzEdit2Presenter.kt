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
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.ext.toTermMap
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_SCHOOL_UID
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.onDbThenRepoWithTimeout
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_NO_DELETE
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_PARENT_GROUP
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_STUDENT_GROUP
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_TEACHER_GROUP
import com.ustadmobile.lib.util.getDefaultTimeZoneId
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance


class ClazzEdit2Presenter(context: Any,
                          arguments: Map<String, String>, view: ClazzEdit2View,  di : DI,
                          lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ClazzEdit2View, ClazzWithHolidayCalendarAndSchoolAndTerminology>(context, arguments, view,
         di, lifecycleOwner), TreeOneToManyJoinEditListener<CourseBlockWithEntity>, ItemTouchHelperListener {

    private val json: Json by di.instance()

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

    val scopedGrantOneToManyHelper = ScopedGrantOneToManyHelper(repo, this,
        requireBackStackEntry().savedStateHandle, Clazz.TABLE_ID)


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
        view.scopedGrants = scopedGrantOneToManyHelper.liveList
        view.courseBlocks = courseBlockOneToManyJoinEditHelper.liveList
    }

    override fun onLoadDataComplete() {
        super.onLoadDataComplete()

        requireSavedStateHandle().getLiveData<String?>(RESULT_TIMEZONE_KEY).observe(lifecycleOwner) {
            entity?.clazzTimeZone = it
            view.entity = entity
        }

        observeSavedStateResult(SAVEDSTATE_KEY_SCHOOL, ListSerializer(School.serializer()),
            School::class) {
            val school = it.firstOrNull() ?: return@observeSavedStateResult
            entity?.school = school
            entity?.clazzSchoolUid = school.schoolUid
            view.entity = entity
            UmPlatformUtil.run{
                requireSavedStateHandle()[SAVEDSTATE_KEY_SCHOOL] = null
            }
        }

        observeSavedStateResult(SAVEDSTATE_KEY_HOLIDAYCALENDAR,
            ListSerializer(HolidayCalendar.serializer()), HolidayCalendar::class) {
            val calendar = it.firstOrNull() ?: return@observeSavedStateResult
            entity?.holidayCalendar = calendar
            entity?.clazzHolidayUMCalendarUid = calendar.umCalendarUid
            view.entity = entity
            UmPlatformUtil.run {
                requireSavedStateHandle()[SAVEDSTATE_KEY_HOLIDAYCALENDAR] = null
            }
        }

        observeSavedStateResult(
            SAVEDSTATE_KEY_TERMINOLOGY,
            ListSerializer(CourseTerminology.serializer()), CourseTerminology::class) {
            val terminology = it.firstOrNull() ?: return@observeSavedStateResult
            entity?.clazzTerminologyUid = terminology.ctUid
            entity?.terminology = terminology
            view.entity = entity
            UmPlatformUtil.run {
                requireSavedStateHandle()[SAVEDSTATE_KEY_TERMINOLOGY] = null
            }
        }

        observeSavedStateResult(
            RESULT_TIMEZONE_KEY,
            ListSerializer(String.serializer()), String::class) {
            val timeZone = it.firstOrNull() ?: return@observeSavedStateResult
            entity?.clazzTimeZone = timeZone
            view.entity = entity
            UmPlatformUtil.run {
                requireSavedStateHandle()[RESULT_TIMEZONE_KEY] = null
            }
        }

        observeSavedStateResult(SAVEDSTATE_KEY_FEATURES,
            ListSerializer(LongWrapper.serializer()), LongWrapper::class) {
            val wrapper = it.firstOrNull() ?: return@observeSavedStateResult
            entity?.clazzFeatures = wrapper.longValue
            view.entity = entity
            UmPlatformUtil.run {
                requireSavedStateHandle()[SAVEDSTATE_KEY_FEATURES] = null
            }
        }

        observeSavedStateResult(SAVEDSTATE_KEY_ASSIGNMENT,
            ListSerializer(ClazzAssignmentWithCourseBlock.serializer()), ClazzAssignmentWithCourseBlock::class){
            val newAssignment = it.firstOrNull() ?: return@observeSavedStateResult

            val foundBlock: CourseBlockWithEntity = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.find {
                assignment -> assignment.assignment?.caUid == newAssignment.caUid
            } ?: CourseBlockWithEntity().apply {
                cbClazzUid = newAssignment.caClazzUid
                cbEntityUid = newAssignment.caUid
                cbTitle = newAssignment.caTitle
                cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                cbDescription = newAssignment.caDescription
                cbIndex = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.size ?: 0
                cbUid = newAssignment.block?.cbUid ?: db.doorPrimaryKeyManager.nextId(CourseBlock.TABLE_ID)
                cbHideUntilDate = newAssignment.block?.cbHideUntilDate ?: 0
                cbDeadlineDate = newAssignment.block?.cbDeadlineDate ?: Long.MAX_VALUE
                cbGracePeriodDate = newAssignment.block?.cbHideUntilDate ?: newAssignment.block?.cbDeadlineDate ?: Long.MAX_VALUE
                cbLateSubmissionPenalty = newAssignment.block?.cbLateSubmissionPenalty ?: 0
                cbMaxPoints = newAssignment.block?.cbMaxPoints ?: 10
                assignment = newAssignment
            }

            foundBlock.assignment = newAssignment
            foundBlock.cbTitle = newAssignment.caTitle
            foundBlock.cbDescription = newAssignment.caDescription
            foundBlock.cbHideUntilDate = newAssignment.block?.cbHideUntilDate ?: 0
            foundBlock.cbDeadlineDate = newAssignment.block?.cbDeadlineDate ?: Long.MAX_VALUE
            foundBlock.cbGracePeriodDate = newAssignment.block?.cbHideUntilDate ?: newAssignment.block?.cbDeadlineDate ?: Long.MAX_VALUE
            foundBlock.cbLateSubmissionPenalty = newAssignment.block?.cbLateSubmissionPenalty ?: 0
            foundBlock.cbMaxPoints = newAssignment.block?.cbMaxPoints ?: 10

            courseBlockOneToManyJoinEditHelper.onEditResult(foundBlock)

            UmPlatformUtil.run {
                requireSavedStateHandle()[SAVEDSTATE_KEY_ASSIGNMENT] = null
            }
        }

        observeSavedStateResult(
            SAVEDSTATE_KEY_CONTENT,
            ListSerializer(ContentEntry.serializer()), ContentEntry::class) {
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
                    cbUid = db.doorPrimaryKeyManager.nextId(CourseBlock.TABLE_ID)
                    entry = newContent
                }

            foundBlock.entry = newContent
            foundBlock.cbTitle = newContent.title
            foundBlock.cbDescription = newContent.description

            courseBlockOneToManyJoinEditHelper.onEditResult(foundBlock)

            UmPlatformUtil.run {
                requireSavedStateHandle()[SAVEDSTATE_KEY_CONTENT] = null
            }

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

            UmPlatformUtil.run {
                requireSavedStateHandle()[ARG_SAVEDSTATE_MODULE] = null
            }
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

            UmPlatformUtil.run {
                requireSavedStateHandle()[ARG_SAVEDSTATE_TEXT] = null
            }
        }
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzWithHolidayCalendarAndSchoolAndTerminology? {
        val clazzUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazz = db.onRepoWithFallbackToDb(2000) {
            it.clazzDao.takeIf {clazzUid != 0L }?.findByUidWithHolidayCalendarAsync(clazzUid)
        } ?: ClazzWithHolidayCalendarAndSchoolAndTerminology().also { newClazz ->
            newClazz.clazzUid = db.doorPrimaryKeyManager.nextId(Clazz.TABLE_ID)
            newClazz.clazzName = ""
            newClazz.isClazzActive = true
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

        val courseBlocks: List<CourseBlockWithEntity> = db.onRepoWithFallbackToDb(2000){
            it.courseBlockDao.takeIf { clazzUid != 0L }?.findAllCourseBlockByClazzUidAsync(clazzUid)
        } ?: listOf()
        courseBlockOneToManyJoinEditHelper.liveList.sendValue(courseBlocks)

        val termMap = db.courseTerminologyDao.findByUidAsync(clazz.clazzTerminologyUid)
            .toTermMap(json, systemImpl, context)

        if(clazzUid != 0L) {
            val scopedGrants = db.onRepoWithFallbackToDb(2000) {
                it.scopedGrantDao.findByTableIdAndEntityUid(Clazz.TABLE_ID, clazzUid)
            }
            scopedGrantOneToManyHelper.liveList.setVal(scopedGrants)
        }else if(db is DoorDatabaseRepository){
            /*
            This should be enabled once a field has been added on Clazz for the adminGroupUid
            scopedGrantOneToManyHelper.onEditResult(ScopedGrantAndName().apply {
                name = "Admins"
                scopedGrant = ScopedGrant().apply {
                    sgFlags = ScopedGrant.FLAG_ADMIN_GROUP.or(FLAG_NO_DELETE)
                    sgPermissions = Role.ALL_PERMISSIONS
                }
            })
            */

            scopedGrantOneToManyHelper.onEditResult(ScopedGrantAndName().apply {
                name = termMap[TerminologyKeys.TEACHERS_KEY]
                scopedGrant = ScopedGrant().apply {
                    sgFlags = ScopedGrant.FLAG_TEACHER_GROUP.or(FLAG_NO_DELETE)
                    sgPermissions = Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT
                }
            })

            scopedGrantOneToManyHelper.onEditResult(ScopedGrantAndName().apply {
                name = termMap[TerminologyKeys.STUDENTS_KEY]
                scopedGrant = ScopedGrant().apply {
                    sgFlags = ScopedGrant.FLAG_STUDENT_GROUP.or(FLAG_NO_DELETE)
                    sgPermissions = Role.ROLE_CLAZZ_STUDENT_PERMISSIONS_DEFAULT
                }
            })

            scopedGrantOneToManyHelper.onEditResult(ScopedGrantAndName().apply {
                name = "Parents"
                scopedGrant = ScopedGrant().apply {
                    sgFlags = (ScopedGrant.FLAG_PARENT_GROUP or FLAG_NO_DELETE)
                    sgPermissions = Role.ROLE_CLAZZ_PARENT_PERMISSION_DEFAULT
                }
            })
        }

        return clazz
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzWithHolidayCalendarAndSchoolAndTerminology? {
        super.onLoadFromJson(bundle)
        val clazzJsonStr = bundle[ARG_ENTITY_JSON]
        var clazz: ClazzWithHolidayCalendarAndSchoolAndTerminology? = null
        clazz = if(clazzJsonStr != null) {
            safeParse(di, ClazzWithHolidayCalendarAndSchoolAndTerminology.serializer(), clazzJsonStr)
        }else {
            ClazzWithHolidayCalendarAndSchoolAndTerminology()
        }

        scheduleOneToManyJoinEditHelper.onLoadFromJsonSavedState(bundle)
        courseBlockOneToManyJoinEditHelper.onLoadFromJsonSavedState(bundle)
        return clazz
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity ?: return
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                    entityVal)
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
        presenterScope.launch {

            if (entity.clazzStartTime == 0L) {
                view.clazzStartDateError = systemImpl.getString(MessageID.field_required_prompt, context)
                return@launch
            }

            if (entity.clazzEndTime <= entity.clazzStartTime) {
                view.clazzEndDateError = systemImpl.getString(MessageID.end_is_before_start_error, context)
                return@launch
            }

            view.loading = true
            view.fieldsEnabled = false

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
                    txDb.createNewClazzAndGroups(entity, systemImpl, context)
                }else {
                    txDb.clazzDao.updateAsync(entity)
                }

                scheduleOneToManyJoinEditHelper.commitToDatabase(txDb.scheduleDao) {
                    it.scheduleClazzUid = entity.clazzUid
                }

                scopedGrantOneToManyHelper.commitToDatabase(txDb, entity.clazzUid, flagToGroupMap = mapOf(
                    FLAG_TEACHER_GROUP to entity.clazzTeachersPersonGroupUid,
                    FLAG_STUDENT_GROUP to entity.clazzStudentsPersonGroupUid,
                    FLAG_PARENT_GROUP to entity.clazzParentsPersonGroupUid,
                ))


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

                txDb.courseBlockDao.replaceListAsync(courseBlockList)
                txDb.courseBlockDao.deactivateByUids(
                    courseBlockOneToManyJoinEditHelper.primaryKeysToDeactivate,
                    systemTimeInMillis())

            }

            UmPlatformUtil.runAsync {
                val coursePictureVal = view.coursePicture
                if(coursePictureVal != null) {
                    coursePictureVal.coursePictureClazzUid = entity.clazzUid

                    if(coursePictureVal.coursePictureUid == 0L) {
                        repo.coursePictureDao.insertAsync(coursePictureVal)
                    }else {
                        repo.coursePictureDao.updateAsync(coursePictureVal)
                    }
                }
            }


            val fromDateTime = DateTime.now().toOffsetByTimezone(entity.effectiveTimeZone).localMidnight
            val clazzLogCreatorManager: ClazzLogCreatorManager by di.instance()
            clazzLogCreatorManager.requestClazzLogCreation(entity.clazzUid,
                    accountManager.activeAccount.endpointUrl,
                    fromDateTime.utc.unixMillisLong, fromDateTime.localEndOfDay.utc.unixMillisLong)
            view.loading = false

            //Handle the following scenario: PersonEdit (user selects to add an enrolment), ClazzList
            // ClazzEdit, EnrolmentEdit
            if(arguments.containsKey(UstadView.ARG_GO_TO_COMPLETE)) {
                systemImpl.go(arguments[UstadView.ARG_GO_TO_COMPLETE].toString(),
                        arguments.plus(UstadView.ARG_CLAZZUID to entity.clazzUid.toString()),
                        context)
            }else{
                onFinish(ClazzDetailView.VIEW_NAME, entity.clazzUid, entity, ClazzWithHolidayCalendarAndSchoolAndTerminology.serializer())
            }
        }
    }

    fun handleClickAddAssignment() {
        val args = mutableMapOf<String, String>()
        args[UstadView.ARG_CLAZZUID] = entity?.clazzUid.toString()

        navigateForResult(NavigateForResultOptions(
                this,
                currentEntityValue = null,
                destinationViewName = ClazzAssignmentEditView.VIEW_NAME,
                entityClass = ClazzAssignmentWithCourseBlock::class,
                serializationStrategy = ClazzAssignmentWithCourseBlock.serializer(),
                destinationResultKey = SAVEDSTATE_KEY_ASSIGNMENT,
                arguments = args))
    }

    fun handleClickAddContent(){
        val args = mutableMapOf(
            ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_OPTION to
                ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_PARENT,
            UstadView.ARG_PARENT_ENTRY_UID to UstadView.MASTER_SERVER_ROOT_ENTRY_UID.toString(),
            ContentEntryList2View.ARG_SELECT_FOLDER_VISIBLE to false.toString()
        )

        navigateForResult(NavigateForResultOptions(
            this, null,
            ContentEntryList2View.VIEW_NAME,
            ContentEntry::class,
            ContentEntry.serializer(),
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

    }

    override fun onClickNew() {
    }

    override fun onClickEdit(joinedEntity: CourseBlockWithEntity) {

        val navigateForResultOptions = when(joinedEntity.cbType){
            CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
                val args = mutableMapOf<String, String>()
                args[UstadView.ARG_CLAZZUID] = (joinedEntity.assignment?.caClazzUid ?: entity?.clazzUid ?: 0L).toString()
                args[UstadView.ARG_ENTITY_UID] = (joinedEntity.assignment?.caUid ?: 0L).toString()

                NavigateForResultOptions(
                        this,
                        currentEntityValue = null,
                        destinationViewName = ClazzAssignmentEditView.VIEW_NAME,
                        entityClass = ClazzAssignmentWithCourseBlock::class,
                        serializationStrategy = ClazzAssignmentWithCourseBlock.serializer(),
                        destinationResultKey = SAVEDSTATE_KEY_ASSIGNMENT,
                        arguments = args)
            }
            CourseBlock.BLOCK_CONTENT_TYPE -> {
                NavigateForResultOptions(
                    this, null,
                    ContentEntryEdit2View.VIEW_NAME,
                    ContentEntry::class,
                    ContentEntry.serializer(),
                    SAVEDSTATE_KEY_CONTENT,
                    arguments = mutableMapOf(
                        UstadView.ARG_ENTITY_UID to joinedEntity.entry?.contentEntryUid.toString(),
                        UstadView.ARG_LEAF to true.toString()))
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
                    ?.cbHidden = !it.cbHidden
            }
        }
        courseBlockOneToManyJoinEditHelper.liveList.sendValue(newList)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val currentList = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.toMutableList() ?: mutableListOf()

        val fromLocation = currentList[fromPosition]

        // check if its module
        if(fromLocation.cbType == CourseBlock.BLOCK_MODULE_TYPE &&
                fromLocation.cbModuleParentBlockUid != currentList[toPosition].cbModuleParentBlockUid){
            courseBlockOneToManyJoinEditHelper.liveList.sendValue(currentList.toList())
            return false
        }

        currentList.removeAt(fromPosition)
        currentList.add(toPosition, fromLocation)

        val movedBlock = currentList[toPosition]
        if(movedBlock.cbType == CourseBlock.BLOCK_MODULE_TYPE){
            //if module moves, move all children to new index
            val childBlocks = currentList.filter { it.cbModuleParentBlockUid == movedBlock.cbUid }
            currentList.removeAll(childBlocks)
            val index = currentList.indexOf(movedBlock) + 1
            currentList.addAll(index, childBlocks)

        }else if(movedBlock.cbIndentLevel != 0){
            //if child moves out of module, update child to have parentBlock = 0 or find new parent
            movedBlock.cbModuleParentBlockUid = 0
            for(n in toPosition downTo 0){
                if(currentList[n].cbType == CourseBlock.BLOCK_MODULE_TYPE){
                    movedBlock.cbModuleParentBlockUid = currentList[n].cbUid
                    break
                }
            }
            currentList[toPosition] = movedBlock
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

}