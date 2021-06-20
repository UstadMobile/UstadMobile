package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ScopedGrantOneToManyHelper
import com.ustadmobile.core.util.ext.createNewSchoolAndGroups
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.SchoolDetailView
import com.ustadmobile.core.view.SchoolEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_STUDENT_GROUP
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_TEACHER_GROUP
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI


class SchoolEditPresenter(context: Any,
                          arguments: Map<String, String>, view: SchoolEditView, di: DI,
                          lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<SchoolEditView, SchoolWithHolidayCalendar>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    val scopedGrantOneToManyHelper = ScopedGrantOneToManyHelper(repo, this,
        requireBackStackEntry().savedStateHandle, School.TABLE_ID)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.scopedGrants = scopedGrantOneToManyHelper.liveList

    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): SchoolWithHolidayCalendar? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val school = withTimeoutOrNull(2000) {
            db.schoolDao.findByUidWithHolidayCalendarAsync(entityUid)
        } ?: SchoolWithHolidayCalendar()

        if(entityUid != 0L) {
            val scopedGrants = db.onRepoWithFallbackToDb(2000) {
                it.scopedGrantDao.findByTableIdAndEntityUid(School.TABLE_ID, entityUid)
            }

            scopedGrantOneToManyHelper.liveList.setVal(scopedGrants)
        }else if(db is DoorDatabaseRepository){
            //Add default roles
            scopedGrantOneToManyHelper.onEditResult(ScopedGrantAndName().apply {
                name = "Teachers"
                scopedGrant = ScopedGrant().apply {
                    sgFlags = FLAG_TEACHER_GROUP.or(ScopedGrant.FLAG_NO_DELETE)
                    sgPermissions = Role.ROLE_SCHOOL_STAFF_PERMISSIONS_DEFAULT
                }
            })

            scopedGrantOneToManyHelper.onEditResult(ScopedGrantAndName().apply {
                name = "Students"
                scopedGrant = ScopedGrant().apply {
                    sgFlags = FLAG_STUDENT_GROUP.or(ScopedGrant.FLAG_NO_DELETE)
                    sgPermissions = Role.ROLE_SCHOOL_STUDENT_PERMISSION_DEFAULT
                }
            })
        }


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

            scopedGrantOneToManyHelper.commitToDatabase(repo, entity.schoolUid,
                flagToGroupMap = mapOf(
                    FLAG_TEACHER_GROUP to entity.schoolTeachersPersonGroupUid,
                    FLAG_STUDENT_GROUP to entity.schoolStudentsPersonGroupUid)
            )

            onFinish(SchoolDetailView.VIEW_NAME, entity.schoolUid, entity)
        }
    }

    companion object {
    }

}