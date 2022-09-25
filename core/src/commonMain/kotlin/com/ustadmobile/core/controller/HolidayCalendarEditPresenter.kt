package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.deactivateByUids
import com.ustadmobile.core.util.OneToManyJoinEditHelperMp
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.HolidayCalendarEditView
import com.ustadmobile.core.view.HolidayEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.lib.db.entities.HolidayCalendar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class HolidayCalendarEditPresenter(context: Any,
                          arguments: Map<String, String>, view: HolidayCalendarEditView,
                          lifecycleOwner: LifecycleOwner,
                          di: DI)
    : UstadEditPresenter<HolidayCalendarEditView, HolidayCalendar>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private val holidayOneToManyJoinEditHelper
            = OneToManyJoinEditHelperMp(Holiday::holUid,
        ARG_SAVED_STATE_HOLIDAY,
        ListSerializer(Holiday.serializer()),
        ListSerializer(Holiday.serializer()),
        this,
        requireSavedStateHandle(),
        Holiday::class) {holUid = it}

    val holidayToManyJoinListener = holidayOneToManyJoinEditHelper.createNavigateForResultListener(
        HolidayEditView.VIEW_NAME, Holiday.serializer())


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.holidayList = holidayOneToManyJoinEditHelper.liveList
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): HolidayCalendar? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val holidayCalendar = withTimeoutOrNull(2000) {
            db.holidayCalendarDao.findByUidAsync(entityUid)
        } ?: HolidayCalendar()

        val holidayList = withTimeoutOrNull(2000) {
            db.holidayDao.findByHolidayCalendaUidAsync(entityUid)
        } ?: listOf()
        holidayOneToManyJoinEditHelper.liveList.postValue(holidayList)

        return holidayCalendar
    }

    override fun onLoadFromJson(bundle: Map<String, String>): HolidayCalendar? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: HolidayCalendar? = null
        editEntity = if(entityJsonStr != null) {
            safeParse(di, HolidayCalendar.serializer(), entityJsonStr)
        }else {
            HolidayCalendar()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json, HolidayCalendar.serializer(), entity)
    }

    override fun handleClickSave(entity: HolidayCalendar) {
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.umCalendarUid == 0L) {
                entity.umCalendarUid = repo.holidayCalendarDao.insertAsync(entity)
            }else {
                repo.holidayCalendarDao.updateAsync(entity)
            }

            repo.withDoorTransactionAsync { txRepo ->
                holidayOneToManyJoinEditHelper.commitToDatabase(txRepo.holidayDao,
                    { repo.holidayDao.deactivateByUids(it, systemTimeInMillis()) }
                ) {
                    it.holHolidayCalendarUid = entity.umCalendarUid
                }
            }

            finishWithResult(safeStringify(di, ListSerializer(HolidayCalendar.serializer()),
                listOf(entity))
            )
        }
    }


    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers
        const val ARG_SAVED_STATE_HOLIDAY = "Holiday"
    }

}