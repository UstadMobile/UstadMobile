package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.createNewSchoolAndGroups
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.SchoolDetailView
import com.ustadmobile.core.view.SchoolEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class SchoolEditPresenter(context: Any,
                          arguments: Map<String, String>, view: SchoolEditView, di: DI,
                          lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<SchoolEditView, SchoolWithHolidayCalendar>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private val clazzOneToManyJoinEditHelper = DefaultOneToManyJoinEditHelper<Clazz>(
            Clazz::clazzUid,"state_Clazz_list",
            ListSerializer(Clazz.serializer()),
            ListSerializer(Clazz.serializer()), this, Clazz::class) { clazzUid = it }

    fun handleRemoveSchedule(clazz: Clazz) {
        clazzOneToManyJoinEditHelper.onDeactivateEntity(clazz)
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): SchoolWithHolidayCalendar? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val school = withTimeoutOrNull(2000) {
            db.schoolDao.findByUidWithHolidayCalendarAsync(entityUid)
        } ?: SchoolWithHolidayCalendar()


        return school
    }

    override fun onLoadFromJson(bundle: Map<String, String>): SchoolWithHolidayCalendar? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: SchoolWithHolidayCalendar? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, SchoolWithHolidayCalendar.serializer(), entityJsonStr)
        }else {
            editEntity = SchoolWithHolidayCalendar()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: SchoolWithHolidayCalendar) {

        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.schoolUid == 0L) {
                entity.schoolActive = true
                entity.schoolUid = repo.createNewSchoolAndGroups(entity, systemImpl, context)
            }else {
                repo.schoolDao.updateAsync(entity)
            }

            onFinish(SchoolDetailView.VIEW_NAME, entity.schoolUid, entity)
        }
    }

    companion object {
    }

}