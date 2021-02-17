package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
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
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.Report


class ClazzEnrolmentEditPresenter(context: Any,
                                   arguments: Map<String, String>, view: ClazzEnrolmentEditView,
                                   lifecycleOwner: DoorLifecycleOwner,
                                   di: DI)
    : UstadEditPresenter<ClazzEnrolmentEditView, ClazzEnrolmentWithLeavingReason>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    enum class RoleOptions(val optionVal: Int, val messageId: Int) {
        STUDENT(ClazzEnrolment.ROLE_STUDENT, MessageID.student),
        TEACHER(ClazzEnrolment.ROLE_TEACHER, MessageID.teacher)
    }

    class RoleMessageIdOption(day: RoleOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    enum class StatusOptions(val optionVal: Int, val messageId: Int) {
        ENROLED(ClazzEnrolment.STATUS_ENROLED, MessageID.enroled),
        GRADUATED(ClazzEnrolment.STATUS_GRADUATED, MessageID.graduated),
        FAILED(ClazzEnrolment.STATUS_FAILED, MessageID.failed),
        DROPPED_OUT(ClazzEnrolment.STATUS_DROPPED_OUT, MessageID.dropped_out),
        MOVED(ClazzEnrolment.STATUS_MOVED, MessageID.moved)
    }

    class StatusMessageIdOption(day: StatusOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    var selectedPerson: Long = 0L
    var selectedClazz: Long = 0L

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.roleList =  RoleOptions.values().map { RoleMessageIdOption(it, context) }
        view.statusList = StatusOptions.values().map { StatusMessageIdOption(it, context) }
        selectedPerson = arguments[ARG_PERSON_UID]?.toLong() ?: 0L
        selectedClazz = arguments[ARG_FILTER_BY_CLAZZUID]?.toLong() ?: 0L
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzEnrolmentWithLeavingReason? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazzEnrolment = db.onRepoWithFallbackToDb(2000) {
            it.takeIf { entityUid != 0L }?.clazzEnrolmentDao?.findEnrolmentWithLeavingReason(entityUid)
        } ?: ClazzEnrolmentWithLeavingReason().apply {
            // TODO set dateJoined to today
            clazzEnrolmentPersonUid = selectedPerson
            clazzEnrolmentClazzUid = selectedClazz
        }

        return clazzEnrolment
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzEnrolmentWithLeavingReason? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        val editEntity: ClazzEnrolment?
        if(entityJsonStr != null) {
            editEntity = safeParse(di, ClazzEnrolmentWithLeavingReason.serializer(), entityJsonStr)
        }else {
            editEntity = ClazzEnrolmentWithLeavingReason().apply {
                // TODO set dateJoined to today
                clazzEnrolmentPersonUid = selectedPerson
                clazzEnrolmentClazzUid = selectedClazz
            }
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }


    override fun handleClickSave(entity: ClazzEnrolmentWithLeavingReason) {
        GlobalScope.launch(doorMainDispatcher()) {
            // TODO get clazz data, check if enrolment join date is after start date of class
            val clazzData = repo.clazzDao.findByUidAsync(selectedClazz)
            if((clazzData?.clazzStartTime?: 0) > entity.clazzEnrolmentDateJoined){
                view.startDateError = systemImpl.getString(
                        MessageID.error_join_date_before_start_date, context)
            }
            // TODO get last enrolment, check if join date is after previous end date of class


            if(entity.clazzEnrolmentDateJoined == 0L){
                view.startDateError = systemImpl.getString(MessageID.field_required_prompt, context)
            }else{
                view.startDateError = null
            }

            // TODO check end is not before start
            if(entity.clazzEnrolmentDateLeft <= entity.clazzEnrolmentDateJoined){
                view.endDateError = systemImpl.getString(MessageID.end_is_before_start_error, context)
            }

            // TODO check if arg save to db is true before updating db
            if(entity.clazzEnrolmentUid == 0L){
                entity.clazzEnrolmentUid = repo.clazzEnrolmentDao.insertAsync(entity)
            }else {
                repo.clazzEnrolmentDao.updateAsync(entity)
            }
            view.finishWithResult(listOf(entity))
        }
    }


}