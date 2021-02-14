package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzEnrolmentEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzEnrolment

import kotlinx.coroutines.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import com.ustadmobile.core.util.safeParse


class ClazzEnrolmentEditPresenter(context: Any,
                                   arguments: Map<String, String>, view: ClazzEnrolmentEditView,
                                   lifecycleOwner: DoorLifecycleOwner,
                                   di: DI)
    : UstadEditPresenter<ClazzEnrolmentEditView, ClazzEnrolment>(context, arguments, view, di, lifecycleOwner) {

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

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzEnrolment? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        //TODO: Load the list for any one to many join helper here
        /* e.g.
         val clazzEnrolment = withTimeoutOrNull {
             db.clazzEnrolment.findByUid(entityUid)
         } ?: ClazzEnrolment()
         return clazzEnrolment
         */
        return TODO("Implement load from Database or return null if using PERSISTENCE_MODE.JSON")
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzEnrolment? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ClazzEnrolment? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, ClazzEnrolment.serializer(), entityJsonStr)
        }else {
            editEntity = ClazzEnrolment()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }


    override fun handleClickSave(entity: ClazzEnrolment) {
        //TODO: Any validation that is needed before accepting / saving this entity
        //TODO: Only save to the database when the persistence mode is PERSISTENCE_MODE.DB
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.clazzEnrolmentUid == 0L) {
                entity.clazzEnrolmentUid = repo.clazzEnrolmentDao.insertAsync(entity)
            }else {
                repo.clazzEnrolmentDao.updateAsync(entity)
            }

            //TODO: Call commitToDatabase on any onetomany join helpers
            view.finishWithResult(listOf(entity))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}