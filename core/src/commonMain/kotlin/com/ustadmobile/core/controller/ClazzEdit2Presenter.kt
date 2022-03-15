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
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_SCHOOL_UID
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.onDbThenRepoWithTimeout
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_NO_DELETE
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_PARENT_GROUP
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_STUDENT_GROUP
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_TEACHER_GROUP
import com.ustadmobile.lib.util.getDefaultTimeZoneId
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.kodein.di.DI
import org.kodein.di.instance


class ClazzEdit2Presenter(context: Any,
                          arguments: Map<String, String>, view: ClazzEdit2View,  di : DI,
                          lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ClazzEdit2View, ClazzWithHolidayCalendarAndSchool>(context, arguments, view,
         di, lifecycleOwner), TreeOneToManyJoinEditListener<CourseBlockWithEntity>, ItemTouchHelperListener {

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
            = DefaultOneToManyJoinEditHelper(CourseBlockWithEntity::cbUid,
            ARG_SAVEDSTATE_BLOCK,
            ListSerializer(CourseBlockWithEntity.serializer()),
            ListSerializer(CourseBlockWithEntity.serializer()),
            this,
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
            ListSerializer(ClazzAssignment.serializer()), ClazzAssignment::class){
            val newAssignment = it.firstOrNull() ?: return@observeSavedStateResult

            val foundBlock: CourseBlockWithEntity = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.find {
                assignment -> assignment.assignment?.caUid == newAssignment.caUid
            } ?: CourseBlockWithEntity().apply {
                cbClazzUid = newAssignment.caClazzUid
                cbTableId = ClazzAssignment.TABLE_ID
                cbTableUid = newAssignment.caUid
                cbTitle = newAssignment.caTitle
                cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                cbDescription = newAssignment.caDescription
                cbIndex = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.size ?: 0
                assignment = newAssignment
            }

            foundBlock.assignment = newAssignment
            foundBlock.cbTitle = newAssignment.caTitle
            foundBlock.cbDescription = newAssignment.caDescription

            courseBlockOneToManyJoinEditHelper.onEditResult(foundBlock)

            UmPlatformUtil.run {
                requireSavedStateHandle()[SAVEDSTATE_KEY_ASSIGNMENT] = null
            }
        }
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzWithHolidayCalendarAndSchool? {
        val clazzUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazz = db.onRepoWithFallbackToDb(2000) {
            it.clazzDao.takeIf {clazzUid != 0L }?.findByUidWithHolidayCalendarAsync(clazzUid)
        } ?: ClazzWithHolidayCalendarAndSchool().also { newClazz ->
            newClazz.clazzUid = db.doorPrimaryKeyManager.nextId(Clazz.TABLE_ID)
            newClazz.clazzName = ""
            newClazz.isClazzActive = true
            newClazz.clazzTimeZone = getDefaultTimeZoneId()
            newClazz.clazzSchoolUid = arguments[ARG_SCHOOL_UID]?.toLong() ?: 0L
            newClazz.school = db.schoolDao.takeIf { newClazz.clazzSchoolUid != 0L }?.findByUidAsync(newClazz.clazzSchoolUid)
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
                name = "Teachers"
                scopedGrant = ScopedGrant().apply {
                    sgFlags = ScopedGrant.FLAG_TEACHER_GROUP.or(FLAG_NO_DELETE)
                    sgPermissions = Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT
                }
            })

            scopedGrantOneToManyHelper.onEditResult(ScopedGrantAndName().apply {
                name = "Students"
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

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzWithHolidayCalendarAndSchool? {
        super.onLoadFromJson(bundle)
        val clazzJsonStr = bundle[ARG_ENTITY_JSON]
        var clazz: ClazzWithHolidayCalendarAndSchool? = null
        clazz = if(clazzJsonStr != null) {
            safeParse(di, ClazzWithHolidayCalendarAndSchool.serializer(), clazzJsonStr)
        }else {
            ClazzWithHolidayCalendarAndSchool()
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

    fun handleClickFeatures() {
        navigateForResult(NavigateForResultOptions(this,
            LongWrapper(entity?.clazzFeatures ?: 0),
            BitmaskEditView.VIEW_NAME,
            LongWrapper::class,
            LongWrapper.serializer(),
            SAVEDSTATE_KEY_FEATURES))
    }

    override fun handleClickSave(entity: ClazzWithHolidayCalendarAndSchool) {
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

            repo.withDoorTransactionAsync(UmAppDatabase::class) { txDb ->

                if(arguments[UstadView.ARG_ENTITY_UID]?.toLongOrNull() == 0L) {
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
                        courseBlockOneToManyJoinEditHelper.primaryKeysToDeactivate)

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

                courseBlockOneToManyJoinEditHelper.commitToDatabase(txDb.courseBlockDao){
                    it.cbClazzUid = entity.clazzUid
                }
            }

            val coursePictureVal = view.coursePicture
            if(coursePictureVal != null) {
                coursePictureVal.coursePictureClazzUid = entity.clazzUid

                if(coursePictureVal.coursePictureUid == 0L) {
                    repo.coursePictureDao.insertAsync(coursePictureVal)
                }else {
                    repo.coursePictureDao.updateAsync(coursePictureVal)
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
                onFinish(ClazzDetailView.VIEW_NAME, entity.clazzUid, entity, ClazzWithHolidayCalendarAndSchool.serializer())
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
                entityClass = ClazzAssignment::class,
                serializationStrategy = ClazzAssignment.serializer(),
                destinationResultKey = SAVEDSTATE_KEY_ASSIGNMENT,
                arguments = args))
    }

    fun handleClickAddModule() {

    }

    companion object {

        const val ARG_SAVEDSTATE_SCHEDULES = "schedules"

        const val ARG_SAVEDSTATE_BLOCK = "courseBlocks"

        const val SAVEDSTATE_KEY_SCHOOL = "School"

        const val SAVEDSTATE_KEY_ASSIGNMENT = "Assignment"

        const val SAVEDSTATE_KEY_HOLIDAYCALENDAR = "ClazzHolidayCalendar"

        const val SAVEDSTATE_KEY_FEATURES = "ClazzFeatures"

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
                        currentEntityValue = joinedEntity.assignment,
                        destinationViewName = ClazzAssignmentEditView.VIEW_NAME,
                        entityClass = ClazzAssignment::class,
                        serializationStrategy = ClazzAssignment.serializer(),
                        destinationResultKey = SAVEDSTATE_KEY_ASSIGNMENT,
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
        joinedEntity.cbIndentLevel++
        courseBlockOneToManyJoinEditHelper.onEditResult(joinedEntity)
    }

    override fun onClickUnIndent(joinedEntity: CourseBlockWithEntity) {
        joinedEntity.cbIndentLevel--
        courseBlockOneToManyJoinEditHelper.onEditResult(joinedEntity)
    }

    override fun onClickHide(joinedEntity: CourseBlockWithEntity) {
        joinedEntity.cbHidden = !joinedEntity.cbHidden
        courseBlockOneToManyJoinEditHelper.onEditResult(joinedEntity)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val currentList = courseBlockOneToManyJoinEditHelper.liveList.getValue()?.toMutableList() ?: mutableListOf()
        // Collections.swap (android only)
        currentList[fromPosition] = currentList.set(toPosition, currentList[fromPosition])
        courseBlockOneToManyJoinEditHelper.liveList.sendValue(currentList.toList())
        return true
    }

    override fun onItemDismiss(position: Int) {

    }

}