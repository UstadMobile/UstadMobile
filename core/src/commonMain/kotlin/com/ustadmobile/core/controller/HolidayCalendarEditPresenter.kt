package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.HolidayCalendarEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.HolidayCalendar

import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.list
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.lib.db.entities.Holiday


class HolidayCalendarEditPresenter(context: Any,
                          arguments: Map<String, String>, view: HolidayCalendarEditView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadEditPresenter<HolidayCalendarEditView, HolidayCalendar>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    val holidayOneToManyJoinEditHelper = DefaultOneToManyJoinEditHelper<Holiday>(Holiday::holUid,
            "state_Holiday_list", Holiday.serializer().list,
            Holiday.serializer().list, this) { holUid = it }

    fun handleAddOrEditHoliday(holiday: Holiday) {
        holidayOneToManyJoinEditHelper.onEditResult(holiday)
    }

    fun handleRemoveHoliday(holiday: Holiday) {
        holidayOneToManyJoinEditHelper.onDeactivateEntity(holiday)
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.holidayList = holidayOneToManyJoinEditHelper.liveList
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): HolidayCalendar? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val holidayCalendar = withTimeoutOrNull(2000) {
            db.holidayCalendarDao.findByUid(entityUid)
        } ?: HolidayCalendar()

        val holidayList = withTimeoutOrNull(2000) {
            db.holidayDao.findByHolidayCalendaUid(entityUid)
        } ?: listOf()
        holidayOneToManyJoinEditHelper.liveList.sendValue(holidayList)

        return holidayCalendar
    }

    override fun onLoadFromJson(bundle: Map<String, String>): HolidayCalendar? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: HolidayCalendar? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(HolidayCalendar.serializer(), entityJsonStr)
        }else {
            editEntity = HolidayCalendar()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: HolidayCalendar) {
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.umCalendarUid == 0L) {
                entity.umCalendarUid = repo.holidayCalendarDao.insertAsync(entity)
            }else {
                repo.holidayCalendarDao.updateAsync(entity)
            }

            holidayOneToManyJoinEditHelper.commitToDatabase(repo.holidayDao) {
                it.holHolidayCalendarUid = entity.umCalendarUid
            }

            view.finishWithResult(entity)
        }
    }


    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}