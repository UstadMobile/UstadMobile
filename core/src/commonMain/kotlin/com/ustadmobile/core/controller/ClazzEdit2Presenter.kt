package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.schedule.*
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_ID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchool
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.util.getDefaultTimeZoneId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance


class ClazzEdit2Presenter(context: Any,
                          arguments: Map<String, String>, view: ClazzEdit2View,  di : DI,
                          lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ClazzEdit2View, ClazzWithHolidayCalendarAndSchool>(context, arguments, view,
         di, lifecycleOwner) {

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

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzWithHolidayCalendarAndSchool? {
        val clazzUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L
        val clazz = withTimeoutOrNull(2000) {
            db.clazzDao.takeIf {clazzUid != 0L }?.findByUidWithHolidayCalendarAsync(clazzUid)
        }  ?: ClazzWithHolidayCalendarAndSchool().also {
            it.clazzName = ""
            it.isClazzActive = true
            it.clazzTimeZone = getDefaultTimeZoneId()
        }

        val schedules = withTimeoutOrNull(2000) {
            db.scheduleDao.takeIf { clazzUid != 0L }?.findAllSchedulesByClazzUidAsync(clazzUid)
        } ?: listOf()

        scheduleOneToManyJoinEditHelper.liveList.sendValue(schedules)
        return clazz
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzWithHolidayCalendarAndSchool? {
        super.onLoadFromJson(bundle)
        val clazzJsonStr = bundle[ARG_ENTITY_JSON]
        var clazz: ClazzWithHolidayCalendarAndSchool? = null
        if(clazzJsonStr != null) {
            clazz = Json.parse(ClazzWithHolidayCalendarAndSchool.serializer(), clazzJsonStr)
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

    override fun handleClickSave(entity: ClazzWithHolidayCalendarAndSchool) {
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.clazzUid == 0L) {
                entity.clazzUid = repo.clazzDao.insertAsync(entity)
            }else {
                repo.clazzDao.updateAsync(entity)
            }

            scheduleOneToManyJoinEditHelper.commitToDatabase(repo.scheduleDao) {
                it.scheduleClazzUid = entity.clazzUid
            }


            val fromDateTime = DateTime.now().toOffsetByTimezone(entity.effectiveTimeZone).localMidnight

            val clazzLogCreatorManager: ClazzLogCreatorManager by di.instance()
            clazzLogCreatorManager.requestClazzLogCreation(entity.clazzUid,
                    accountManager.activeAccount.endpointUrl,
                    fromDateTime.utc.unixMillisLong, fromDateTime.localEndOfDay.utc.unixMillisLong)

            view.finishWithResult(listOf(entity))
            onFinish(ClazzDetailView.VIEW_NAME, entity.clazzUid, entity)
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