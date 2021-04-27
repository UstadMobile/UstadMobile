package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher

import kotlinx.coroutines.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.lib.db.entities.*
import kotlinx.serialization.builtins.ListSerializer


class ClazzAssignmentEditPresenter(context: Any,
        arguments: Map<String, String>, view: ClazzAssignmentEditView,
        lifecycleOwner: DoorLifecycleOwner,
        di: DI)
    : UstadEditPresenter<ClazzAssignmentEditView, ClazzAssignment>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private val contentJoinEditHelper = DefaultOneToManyJoinEditHelper(
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer::contentEntryUid,
            "state_clazzAssignment_entry_list",
            ListSerializer(ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer.serializer()),
            ListSerializer(ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer.serializer()),
            this, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer::class) { contentEntryUid = it }

    fun handleAddOrEditContent(entityClass: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        contentJoinEditHelper.onEditResult(entityClass)
    }

    fun handleRemoveContent(entityClass: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        contentJoinEditHelper.onDeactivateEntity(entityClass)
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.clazzAssignmentContent = contentJoinEditHelper.liveList
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzAssignment? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazzAssignment = withTimeoutOrNull(2000){
            db.clazzAssignmentDao.findByUidAsync(entityUid)
        }?: ClazzAssignment()

        val clazzWithSchool = withTimeoutOrNull(2000){
            db.clazzDao.getClazzWithSchool(clazzAssignment.caClazzUid)
        }?: ClazzWithSchool()

        view.timeZone = clazzWithSchool.effectiveTimeZone()

        val loggedInPersonUid = accountManager.activeAccount.personUid

        val contentList = withTimeoutOrNull(2000) {
            db.clazzWorkContentJoinDao.findAllContentByClazzWorkUidAsync(
                    clazzAssignment.caUid, loggedInPersonUid
            )
        }?: listOf()

        contentJoinEditHelper.liveList.sendValue(contentList)

        return clazzAssignment
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzAssignment? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ClazzAssignment? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, ClazzAssignment.serializer(), entityJsonStr)
        }else {
            editEntity = ClazzAssignment()
        }

        GlobalScope.launch {
            val clazzWithSchool = withTimeoutOrNull(2000) {
                db.clazzDao.getClazzWithSchool(editEntity.caClazzUid)
            } ?: ClazzWithSchool()

            view.timeZone = clazzWithSchool.effectiveTimeZone()
        }


        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: ClazzAssignment) {
        //TODO: Any validation that is needed before accepting / saving this entity
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.caUid == 0L) {
                entity.caUid = repo.clazzAssignmentDao.insertAsync(entity)
            }else {
                repo.clazzAssignmentDao.updateAsync(entity)
            }

            val contentToInsert = contentJoinEditHelper.entitiesToInsert
            val contentToDelete = contentJoinEditHelper.primaryKeysToDeactivate

            repo.clazzAssignmentContentJoinDao.insertListAsync(contentToInsert.map {
                ClazzAssignmentContentJoin().apply {
                    cacjContentUid = it.contentEntryUid
                    cacjAssignmentUid = entity.caUid
                }
            })

            repo.clazzWorkContentJoinDao.deactivateByUids(contentToDelete)

            onFinish(ClazzAssignmentDetailView.VIEW_NAME, entity.caUid, entity)

        }
    }

}