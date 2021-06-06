package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.controller.TimeZoneListPresenter.Companion.RESULT_TIMEZONE_KEY
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.schedule.*
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_SCHOOL_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getDefaultTimeZoneId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.kodein.di.DI
import org.kodein.di.instance


class ClazzEdit2Presenter(context: Any,
                          arguments: Map<String, String>, view: ClazzEdit2View,  di : DI,
                          lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ClazzEdit2View, ClazzWithHolidayCalendarAndSchool>(context, arguments, view,
         di, lifecycleOwner) {

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

    val scopedGrantOneToManyHelper = ScopedGrantOneToManyHelper(this,
        requireBackStackEntry().savedStateHandle, Clazz.TABLE_ID)

    fun requireBackStackEntry() = ustadNavController.currentBackStackEntry!!

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.clazzSchedules = scheduleOneToManyJoinEditHelper.liveList
        view.scopedGrants = scopedGrantOneToManyHelper.liveList
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

        observeSavedStateResult(SAVEDSTATE_KEY_FEATURES,
            ListSerializer(LongWrapper.serializer()), LongWrapper::class) {
            val wrapper = it.firstOrNull() ?: return@observeSavedStateResult

            entity?.clazzFeatures = wrapper.longValue
            view.entity = entity
            requireSavedStateHandle()[SAVEDSTATE_KEY_FEATURES] = null
        }
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzWithHolidayCalendarAndSchool? {
        val clazzUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazz = db.onRepoWithFallbackToDb(2000) {
            it.clazzDao.takeIf {clazzUid != 0L }?.findByUidWithHolidayCalendarAsync(clazzUid)
        } ?: ClazzWithHolidayCalendarAndSchool().also { newClazz ->
            newClazz.clazzName = ""
            newClazz.isClazzActive = true
            newClazz.clazzTimeZone = getDefaultTimeZoneId()
            newClazz.clazzSchoolUid = arguments[ARG_SCHOOL_UID]?.toLong() ?: 0L
            newClazz.school = db.schoolDao.takeIf { newClazz.clazzSchoolUid != 0L }?.findByUidAsync(newClazz.clazzSchoolUid)
        }

        val schedules = db.onRepoWithFallbackToDb(2000) {
            it.scheduleDao.takeIf { clazzUid != 0L }?.findAllSchedulesByClazzUidAsync(clazzUid)
        } ?: listOf()
        scheduleOneToManyJoinEditHelper.liveList.sendValue(schedules)

        val scopedGrants = db.onRepoWithFallbackToDb(2000) {
            it.scopedGrantDao.takeIf {  clazzUid != 0L }?.findByTableIdAndEntityUid(
                Clazz.TABLE_ID, clazzUid)
        } ?: listOf() //TODO: Create default ScopedGrants for owner, students, teachers.
        scopedGrantOneToManyHelper.liveList.sendValue(scopedGrants)

        return clazz
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzWithHolidayCalendarAndSchool? {
        super.onLoadFromJson(bundle)
        val clazzJsonStr = bundle[ARG_ENTITY_JSON]
        var clazz: ClazzWithHolidayCalendarAndSchool? = null
        if(clazzJsonStr != null) {
            clazz = safeParse(di, ClazzWithHolidayCalendarAndSchool.serializer(), clazzJsonStr)
        }else {
            clazz = ClazzWithHolidayCalendarAndSchool()
        }

        scheduleOneToManyJoinEditHelper.onLoadFromJsonSavedState(bundle)
        return clazz
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity ?: return
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                    entityVal)
    }

    fun handleClickTimezone() {
        navigateForResult(NavigateForResultOptions<String>(
            this,
            currentEntityValue = entity?.clazzTimeZone,
            destinationViewName = TimeZoneListView.VIEW_NAME,
            entityClass = String::class,
            serializationStrategy = String.serializer(),
            destinationResultKey = RESULT_TIMEZONE_KEY))
    }

    fun handleClickHolidayCalendar() {
        navigateForResult(
            NavigateForResultOptions(this,
            null, HolidayCalendarListView.VIEW_NAME, HolidayCalendar::class,
            HolidayCalendar.serializer(), SAVEDSTATE_KEY_HOLIDAYCALENDAR)
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
        GlobalScope.launch(doorMainDispatcher()) {

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

            if(entity.clazzUid == 0L) {
                repo.createNewClazzAndGroups(entity, systemImpl, context)
            }else {
                repo.clazzDao.updateAsync(entity)
            }

            scheduleOneToManyJoinEditHelper.commitToDatabase(repo.scheduleDao) {
                it.scheduleClazzUid = entity.clazzUid
            }

            scopedGrantOneToManyHelper.commitToDatabase(repo, entity.clazzUid)


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
                        arguments.plus(UstadView.ARG_FILTER_BY_CLAZZUID to entity.clazzUid.toString()),
                        context)
            }else{
                onFinish(ClazzDetailView.VIEW_NAME, entity.clazzUid, entity)
            }
        }
    }

    companion object {

        const val ARG_SAVEDSTATE_SCHEDULES = "schedules"

        const val SAVEDSTATE_KEY_SCHOOL = "School"

        const val SAVEDSTATE_KEY_HOLIDAYCALENDAR = "HolidayCalendar"

        const val SAVEDSTATE_KEY_FEATURES = "ClazzFeatures"

    }

}