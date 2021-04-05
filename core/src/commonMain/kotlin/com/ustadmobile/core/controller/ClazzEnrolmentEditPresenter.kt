package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.notification.NotificationCheckersManager
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.createPersonGroupAndMemberWithEnrolment
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzEnrolmentEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher

import kotlinx.coroutines.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_ENROLMENT_ROLE
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_SAVE_TO_DB
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


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

    enum class OutcomeOptions(val optionVal: Int, val messageId: Int) {
        INPROGRESS(ClazzEnrolment.OUTCOME_IN_PROGRESS, MessageID.in_progress),
        GRADUATED(ClazzEnrolment.OUTCOME_GRADUATED, MessageID.graduated),
        FAILED(ClazzEnrolment.OUTCOME_FAILED, MessageID.failed),
        DROPPED_OUT(ClazzEnrolment.OUTCOME_DROPPED_OUT, MessageID.dropped_out),
    }

    class OutcomeMessageIdOption(day: OutcomeOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    var selectedPerson: Long = 0L
    var selectedClazz: Long = 0L
    var selectedRole: Int = 0
    var hasAddStudentPermission = false
    var hasAddTeacherPermission = false

    val loggedInPersonUid = accountManager.activeAccount.personUid

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.statusList = OutcomeOptions.values().map { OutcomeMessageIdOption(it, context) }
        selectedPerson = arguments[ARG_PERSON_UID]?.toLong() ?: 0L
        selectedClazz = arguments[ARG_FILTER_BY_CLAZZUID]?.toLong() ?: 0L
        selectedRole = arguments[ARG_FILTER_BY_ENROLMENT_ROLE]?.toInt() ?: 0
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzEnrolmentWithLeavingReason? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazzWithSchoolVal = repo.clazzDao.getClazzWithSchool(selectedClazz)

        val clazzEnrolment = db.onRepoWithFallbackToDb(2000) {
            it.takeIf { entityUid != 0L }?.clazzEnrolmentDao?.findEnrolmentWithLeavingReason(entityUid)
        } ?: ClazzEnrolmentWithLeavingReason().apply {
            val clazzTimeZone = clazzWithSchoolVal?.effectiveTimeZone ?: "UTC"
            val joinTime = DateTime.now().toOffsetByTimezone(clazzTimeZone).localMidnight.utc.unixMillisLong
            clazzEnrolmentDateJoined = joinTime
            clazzEnrolmentPersonUid = selectedPerson
            clazzEnrolmentClazzUid = selectedClazz
            clazzEnrolmentRole = selectedRole
        }

        handleRoleOptionsList()

        return clazzEnrolment
    }

    private suspend fun handleRoleOptionsList(){
        GlobalScope.launch(doorMainDispatcher()){
            hasAddStudentPermission = repo.clazzDao.personHasPermissionWithClazz(loggedInPersonUid,
                    selectedClazz,  Role.PERMISSION_CLAZZ_ADD_STUDENT)
            hasAddTeacherPermission = repo.clazzDao.personHasPermissionWithClazz(loggedInPersonUid,
                    selectedClazz,  Role.PERMISSION_CLAZZ_ADD_TEACHER)

            val roleList = mutableListOf<RoleMessageIdOption>()
            if(hasAddStudentPermission){
                roleList.add(RoleMessageIdOption(RoleOptions.STUDENT, context))
            }
            if(hasAddTeacherPermission){
                roleList.add(RoleMessageIdOption(RoleOptions.TEACHER, context))
            }
            view.roleList = roleList.toList()
        }
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzEnrolmentWithLeavingReason? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        val editEntity: ClazzEnrolment?
        if (entityJsonStr != null) {
            editEntity = safeParse(di, ClazzEnrolmentWithLeavingReason.serializer(), entityJsonStr)
        } else {
            editEntity = ClazzEnrolmentWithLeavingReason().apply {
                clazzEnrolmentPersonUid = selectedPerson
                clazzEnrolmentClazzUid = selectedClazz
                clazzEnrolmentRole = selectedRole
            }
        }

        GlobalScope.launch {
            handleRoleOptionsList()
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

            // must be filled
            if(entity.clazzEnrolmentRole == 0){
                view.roleSelectionError = systemImpl.getString(MessageID.field_required_prompt, context)
                return@launch
            }

            // must be filled
            if (entity.clazzEnrolmentDateJoined == 0L) {
                view.startDateErrorWithDate = Pair(systemImpl.getString(MessageID.field_required_prompt, context),0)
                return@launch
            }

            if (entity.clazzEnrolmentDateLeft <= entity.clazzEnrolmentDateJoined) {
                view.endDateError = systemImpl.getString(MessageID.end_is_before_start_error, context)
                return@launch
            }

            val clazzData = repo.clazzDao.findByUidAsync(selectedClazz)

            // if date joined entered is before clazz start date
            if ((clazzData?.clazzStartTime ?: 0) > entity.clazzEnrolmentDateJoined) {
                view.startDateErrorWithDate = Pair(systemImpl.getString(
                        MessageID.error_start_date_before_clazz_date, context),0)
                return@launch
            }
            val maxDate = repo.clazzEnrolmentDao.findMaxEndDateForEnrolment(selectedClazz, selectedPerson, entity.clazzEnrolmentUid)
            // if date joined is before previous enrolment end date
            if (maxDate != 0L && maxDate != Long.MAX_VALUE && entity.clazzEnrolmentDateJoined < maxDate) {
                view.startDateErrorWithDate = Pair(systemImpl.getString(
                        MessageID.error_start_date_before_previous_enrolment_date, context),maxDate)
                return@launch
            }

            view.startDateErrorWithDate = null
            view.endDateError = null

            val saveToDb = arguments[ARG_SAVE_TO_DB]?.toBoolean() ?: false
            if(saveToDb){
                repo.createPersonGroupAndMemberWithEnrolment(entity)
                val notificationCheckersManager : NotificationCheckersManager = di.on(accountManager.activeAccount)
                    .direct.instance()
                notificationCheckersManager.invalidateClazzScheduleRelatedCheckers(entity.clazzEnrolmentClazzUid)
            }

            view.finishWithResult(listOf(entity))
        }
    }


}