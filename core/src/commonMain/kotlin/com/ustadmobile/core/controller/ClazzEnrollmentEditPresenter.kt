package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzEnrollmentEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzEnrollment

import kotlinx.coroutines.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.lib.db.entities.ReportSeries


class ClazzEnrollmentEditPresenter(context: Any,
                                   arguments: Map<String, String>, view: ClazzEnrollmentEditView,
                                   lifecycleOwner: DoorLifecycleOwner,
                                   di: DI)
    : UstadEditPresenter<ClazzEnrollmentEditView, ClazzEnrollment>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    enum class RoleOptions(val optionVal: Int, val messageId: Int) {

    }

    class RoleMessageIdOption(day: RoleOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    enum class StatusOptions(val optionVal: Int, val messageId: Int) {

    }

    class StatusMessageIdOption(day: RoleOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //TODO: Set any additional fields (e.g. joinlist) on the view
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzEnrollment? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        //TODO: Load the list for any one to many join helper here
        /* e.g.
         val clazzEnrollment = withTimeoutOrNull {
             db.clazzEnrollment.findByUid(entityUid)
         } ?: ClazzEnrollment()
         return clazzEnrollment
         */
        return TODO("Implement load from Database or return null if using PERSISTENCE_MODE.JSON")
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzEnrollment? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ClazzEnrollment? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, ClazzEnrollment.serializer(), entityJsonStr)
        }else {
            editEntity = ClazzEnrollment()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }


    override fun handleClickSave(entity: ClazzEnrollment) {
        //TODO: Any validation that is needed before accepting / saving this entity
        //TODO: Only save to the database when the persistence mode is PERSISTENCE_MODE.DB
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.clazzEnrollmentUid == 0L) {
                entity.clazzEnrollmentUid = repo.clazzEnrollmentDao.insertAsync(entity)
            }else {
                repo.clazzEnrollmentDao.updateAsync(entity)
            }

            //TODO: Call commitToDatabase on any onetomany join helpers
            view.finishWithResult(listOf(entity))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}