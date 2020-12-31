package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.WorkSpaceEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.WorkSpace

import kotlinx.coroutines.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.WorkspaceTerms
import com.ustadmobile.lib.db.entities.WorkspaceTermsWithLanguage
import kotlinx.serialization.builtins.list


class WorkSpaceEditPresenter(context: Any,
        arguments: Map<String, String>, view: WorkSpaceEditView,
        lifecycleOwner: DoorLifecycleOwner,
        di: DI)
    : UstadEditPresenter<WorkSpaceEditView, WorkSpace>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    val workspaceTermsOneToManyJoinEditHelper = DefaultOneToManyJoinEditHelper<WorkspaceTermsWithLanguage>(WorkspaceTerms::wtUid,
            "state_WorkspaceTerms_list", WorkspaceTerms.serializer().list,
            WorkspaceTermsWithLanguage.serializer().list, this, WorkspaceTermsWithLanguage::class) {
        wtUid = it
    }

    fun handleAddOrEditWorkspaceTerms(workspaceTerms: WorkspaceTermsWithLanguage) {
        workspaceTermsOneToManyJoinEditHelper.onEditResult(workspaceTerms)
    }

    fun handleRemoveWorkspaceTerms(workspaceTerms: WorkspaceTermsWithLanguage) {
        workspaceTermsOneToManyJoinEditHelper.onDeactivateEntity(workspaceTerms)
    }


    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.workspaceTermsList = workspaceTermsOneToManyJoinEditHelper.liveList
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): WorkSpace? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val workSpace = db.onRepoWithFallbackToDb(5000) {
            it.workSpaceDao.getWorkspaceAsync()
        } ?: WorkSpace()

        val workspaceTerms = db.onRepoWithFallbackToDb(5000) {
            it.workspaceTermsDao.findAllWithLanguageAsList()
        }

        workspaceTermsOneToManyJoinEditHelper.liveList.sendValue(workspaceTerms)

        return workSpace
    }

    override fun onLoadFromJson(bundle: Map<String, String>): WorkSpace? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: WorkSpace? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, WorkSpace.serializer(), entityJsonStr)
        }else {
            editEntity = WorkSpace()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: WorkSpace) {
        GlobalScope.launch(doorMainDispatcher()) {
            repo.workSpaceDao.updateAsync(entity)

            workspaceTermsOneToManyJoinEditHelper.commitToDatabase(repo.workspaceTermsDao) {
                //no need to set the foreign key
            }

            view.finishWithResult(listOf(entity))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}