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

    enum class GenderOptions(val optionVal: Int, val messageId: Int){
        MIXED(School.SCHOOL_GENDER_MIXED,
                MessageID.mixed),
        FEMALE(School.SCHOOL_GENDER_FEMALE,
                MessageID.female),
        MALE(School.SCHOOL_GENDER_MALE,
                MessageID.male)
    }

    class GenderTypeMessageIdOption(day: GenderOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private val clazzOneToManyJoinEditHelper = DefaultOneToManyJoinEditHelper<Clazz>(
            Clazz::clazzUid,"state_Clazz_list",
            ListSerializer(Clazz.serializer()),
            ListSerializer(Clazz.serializer()), this, Clazz::class) { clazzUid = it }

    fun handleAddOrEditClazz(clazz: Clazz) {
        clazzOneToManyJoinEditHelper.onEditResult(clazz)
    }

    fun handleRemoveSchedule(clazz: Clazz) {
        clazzOneToManyJoinEditHelper.onDeactivateEntity(clazz)
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.genderOptions = GenderOptions.values().map { GenderTypeMessageIdOption(it, context) }
        view.schoolClazzes = clazzOneToManyJoinEditHelper.liveList

    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): SchoolWithHolidayCalendar? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val school = withTimeoutOrNull(2000) {
            db.schoolDao.findByUidWithHolidayCalendarAsync(entityUid)
        } ?: SchoolWithHolidayCalendar()

        val clazzes = withTimeoutOrNull(2000){
            db.takeIf { entityUid != 0L }?.clazzDao?.findAllClazzesBySchool(entityUid)
        }?: listOf()

        clazzOneToManyJoinEditHelper.liveList.sendValue(clazzes)

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

            val allClazzes = clazzOneToManyJoinEditHelper.liveList.getValue() ?: listOf()
            val clazzesToAssign = allClazzes.filter { it.clazzSchoolUid != entity.schoolUid}
            repo.clazzDao.assignClassesToSchool(clazzesToAssign.map { it.clazzUid }, entity.schoolUid)
            repo.clazzDao.assignClassesToSchool(
                    clazzOneToManyJoinEditHelper.primaryKeysToDeactivate, 0L)

            onFinish(SchoolDetailView.VIEW_NAME, entity.schoolUid, entity)
        }
    }

    companion object {
    }

}