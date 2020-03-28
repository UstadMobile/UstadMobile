package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.HolidayCalendarEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.HolidayCalendar

import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON


class HolidayCalendarEditPresenter(context: Any,
                          arguments: Map<String, String>, view: HolidayCalendarEditView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadEditPresenter<HolidayCalendarEditView, HolidayCalendar>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = TODO("PERSISTENCE_MODE.DB OR PERSISTENCE_MODE.JSON")

    //TODO: Add any required one to many join helpers here. e.g.
    /*
    private val fooOneToManyJoinEditHelper
            = DefaultOneToManyJoinEditHelper<Foo>(Foo::scheduleUid,
            ARG_SAVEDSTATE_FOOS, Foo.serializer().list,
            Foo.serializer().list) {fooUid = it}
     */

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //TODO: setup any joined fields etc. here
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): HolidayCalendar? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        return TODO("Implement load from Database or return null if using PERSISTENCE_MODE.JSON")
    }

    override fun onLoadFromJson(bundle: Map<String, String>): HolidayCalendar? {
        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: HolidayCalendar? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(HolidayCalendar.serializer(), entityJsonStr)
        }else {
            editEntity = HolidayCalendar()
        }

        //TODO: Call onLoadFromJsonSavedState on any One to Many Join Helpers here
        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)

        //TODO: call onSaveState for any One to Many Join Helpers here
    }

    override fun handleClickSave(entity: HolidayCalendar) {
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.umCalendarUid == 0L) {
                entity.umCalendarUid = repo.holidayCalendarDao.insertAsync(entity)
            }else {
                repo.holidayCalendarDao.updateAsync(entity)
            }

            //TODO: call commitToDatabase on any One to Many Join Helpers here e.g.
            /*
            scheduleOneToManyJoinEditHelper.commitToDatabase(repo.scheduleDao) {
                it.scheduleClazzUid = entity.clazzUid
            }
            */

            view.finishWithResult(entity)
        }
    }


    //TODO: Add handleAddOrEdit and handleRemove functions that handle when one-many joins are changed
    //e.g.
    /*
    fun handleAddOrEditFoo(foo: Foo) {
        fooOneToManyJoinEditHelper.onEditResult(foo)
    }

    fun handleRemoveFoo(foo: foo) {
        fooOneToManyJoinEditHelper.onDeactivateEntity(schedule)
    }
     */

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}