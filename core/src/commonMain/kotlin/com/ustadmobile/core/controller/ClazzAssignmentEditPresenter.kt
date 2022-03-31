package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class ClazzAssignmentEditPresenter(context: Any,
                                   arguments: Map<String, String>, view: ClazzAssignmentEditView,
                                   lifecycleOwner: DoorLifecycleOwner,
                                   di: DI)
    : UstadEditPresenter<ClazzAssignmentEditView, ClazzAssignment>(context, arguments, view, di, lifecycleOwner) {

    enum class LateSubmissionOptions(val optionVal: Int, val messageId: Int) {
        REJECT(ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_REJECT,
                MessageID.reject),
        ACCEPT(ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_ACCEPT,
                MessageID.accept),
        MARK_PENALTY(ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_PENALTY,
                MessageID.mark_penalty)
    }

    class LateSubmissionOptionsMessageIdOption(day: LateSubmissionOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    private val contentOneToManyJoinEditHelper
            = OneToManyJoinEditHelperMp(ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer::contentEntryUid,
            ARG_SAVEDSTATE_CONTENT,
            ListSerializer(ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer.serializer()),
            ListSerializer(ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer.serializer()),
            this,
            requireSavedStateHandle(),
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer::class) {contentEntryUid = it}


    val contentOneToManyJoinListener = contentOneToManyJoinEditHelper.createNavigateForResultListener(
            ContentEntryList2View.VIEW_NAME,
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer.serializer(),
            mutableMapOf(ContentEntryList2View.ARG_CLAZZ_ASSIGNMENT_FILTER to entity?.caUid.toString(),
                    ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_OPTION to ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_PARENT,
                    UstadView.ARG_PARENT_ENTRY_UID to  UstadView.MASTER_SERVER_ROOT_ENTRY_UID.toString(),
                    ContentEntryList2View.ARG_SELECT_FOLDER_VISIBLE to false.toString()))


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.clazzAssignmentContent = contentOneToManyJoinEditHelper.liveList
        view.lateSubmissionOptions = LateSubmissionOptions.values().map { LateSubmissionOptionsMessageIdOption(it, context) }
    }


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzAssignment? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazzAssignment = db.onRepoWithFallbackToDb(2000) {
            it.clazzAssignmentDao.findByUidAsync(entityUid)
        } ?: ClazzAssignment().apply {
            caClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: throw IllegalArgumentException("clazzUid was not given")
        }

        val clazzWithSchool = db.onRepoWithFallbackToDb(2000) {
            it.clazzDao.getClazzWithSchool(clazzAssignment.caClazzUid)
        } ?: ClazzWithSchool()

        val timeZone = clazzWithSchool.effectiveTimeZone()
        view.timeZone = timeZone

        val loggedInPersonUid = accountManager.activeAccount.personUid

        val contentList = db.onRepoWithFallbackToDb(2000) {
            it.clazzAssignmentContentJoinDao.findAllContentByClazzAssignmentUidAsync(
                    clazzAssignment.caUid, loggedInPersonUid)
        }

        contentOneToManyJoinEditHelper.liveList.sendValue(contentList)

        return clazzAssignment
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzAssignmentWithTimezone? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ClazzAssignmentWithTimezone? = null
        if (entityJsonStr != null) {
            editEntity = safeParse(di, ClazzAssignmentWithTimezone.serializer(), entityJsonStr)
        }

        view.timeZone = editEntity?.effectiveTimeZone

        contentOneToManyJoinEditHelper.onLoadFromJsonSavedState(bundle)

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: ClazzAssignment) {
        presenterScope.launch {

            if (entity.caTitle.isNullOrEmpty()) {
                view.caTitleError = systemImpl.getString(MessageID.field_required_prompt, context)
                return@launch
            }

            if (entity.caStartDate == 0L) {
                view.caStartDateError = systemImpl.getString(MessageID.field_required_prompt, context)
                return@launch
            }

            if (entity.caDeadlineDate <= entity.caStartDate) {
                view.caDeadlineError = systemImpl.getString(MessageID.end_is_before_start_error, context)
                return@launch
            }

            if (entity.caLateSubmissionType == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_ACCEPT ||
                    entity.caLateSubmissionType == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_PENALTY) {

                if (entity.caDeadlineDate == Long.MAX_VALUE) {
                    view.caDeadlineError = systemImpl.getString(MessageID.field_required_prompt, context)
                    return@launch
                }

                if (entity.caGracePeriodDate <= entity.caDeadlineDate) {
                    view.caGracePeriodError = systemImpl.getString(MessageID.after_deadline_date_error, context)
                    return@launch
                }
            }

            view.loading = true
            view.fieldsEnabled = false

            if(entity.caLateSubmissionType == 0 ||
                    entity.caLateSubmissionType == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_REJECT) {
                entity.caGracePeriodDate = entity.caDeadlineDate
            }

            if(entity.caLateSubmissionType != ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_PENALTY){
                entity.caLateSubmissionPenalty = 0
            }


            if (entity.caUid == 0L) {
                entity.caUid = repo.clazzAssignmentDao.insertAsync(entity)
            } else {
                repo.clazzAssignmentDao.updateAsync(entity)
            }

            repo.clazzAssignmentRollUpDao.invalidateCacheByAssignment(entity.caUid)

            val contentToInsert = contentOneToManyJoinEditHelper.entitiesToInsert
            val contentToDelete = contentOneToManyJoinEditHelper.primaryKeysToDeactivate

            repo.clazzAssignmentContentJoinDao.insertListAsync(contentToInsert.map {
                ClazzAssignmentContentJoin().apply {
                    cacjContentUid = it.contentEntryUid
                    cacjAssignmentUid = entity.caUid
                }
            })

            repo.clazzAssignmentContentJoinDao.deactivateByUids(contentToDelete, entity.caUid)

            repo.clazzAssignmentRollUpDao.deleteCachedInactiveContent()

            onFinish(ClazzAssignmentDetailView.VIEW_NAME, entity.caUid, entity, ClazzAssignment.serializer())

            view.loading = false
            view.fieldsEnabled = true

        }
    }




    companion object {

        const val ARG_SAVEDSTATE_CONTENT = "contents"

    }

}