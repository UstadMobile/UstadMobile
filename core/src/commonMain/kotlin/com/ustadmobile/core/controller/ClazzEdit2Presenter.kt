package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendar
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list

class ClazzEdit2Presenter(context: Any,
                          arguments: Map<String, String>, view: ClazzEdit2View,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadEditPresenter<ClazzEdit2View, ClazzWithHolidayCalendar>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    private val scheduleOneToManyJoinEditHelper
            = DefaultOneToManyJoinEditHelper<Schedule>(Schedule::scheduleUid,
            ARG_SAVEDSTATE_SCHEDULES, Schedule.serializer().list,
            Schedule.serializer().list, this) {scheduleUid = it}

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.clazzSchedules = scheduleOneToManyJoinEditHelper.liveList
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzWithHolidayCalendar? {
        val clazzUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L
        val clazz = withTimeoutOrNull(2000) {
            db.clazzDao.takeIf {clazzUid != 0L }?.findByUidWithHolidayCalendarAsync(clazzUid) ?:
                ClazzWithHolidayCalendar().also {
                    it.clazzName = ""
                    it.isClazzActive = true
                }
        }  ?: return null

        val schedules = withTimeoutOrNull(2000) {
            db.scheduleDao.findAllSchedulesByClazzUidAsync(clazzUid)
        } ?: listOf()

        scheduleOneToManyJoinEditHelper.liveList.sendValue(schedules)
        return clazz
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzWithHolidayCalendar? {
        super.onLoadFromJson(bundle)
        val clazzJsonStr = bundle[ARG_ENTITY_JSON]
        var clazz: ClazzWithHolidayCalendar? = null
        if(clazzJsonStr != null) {
            clazz = Json.parse(ClazzWithHolidayCalendar.serializer(), clazzJsonStr)
        }else {
            clazz = ClazzWithHolidayCalendar()
        }

        scheduleOneToManyJoinEditHelper.onLoadFromJsonSavedState(bundle)

        return clazz
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                    entityVal)
    }

    override fun handleClickSave(entity: ClazzWithHolidayCalendar) {
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.clazzUid == 0L) {
                entity.clazzUid = repo.clazzDao.insertAsync(entity)
            }else {
                repo.clazzDao.updateAsync(entity)
            }

            scheduleOneToManyJoinEditHelper.commitToDatabase(repo.scheduleDao) {
                it.scheduleClazzUid = entity.clazzUid
            }

            view.finishWithResult(entity)
        }
    }

    fun handleAddOrEditSchedule(schedule: Schedule) {
        scheduleOneToManyJoinEditHelper.onEditResult(schedule)
    }

    fun handleRemoveSchedule(schedule: Schedule) {
        scheduleOneToManyJoinEditHelper.onDeactivateEntity(schedule)
    }

    companion object {

        const val ARG_SAVEDSTATE_SCHEDULES = "schedules"

    }

}