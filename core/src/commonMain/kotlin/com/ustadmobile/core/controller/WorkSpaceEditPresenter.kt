package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.WorkSpaceEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Site

import kotlinx.coroutines.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import kotlinx.serialization.builtins.list


class WorkSpaceEditPresenter(context: Any,
        arguments: Map<String, String>, view: WorkSpaceEditView,
        lifecycleOwner: DoorLifecycleOwner,
        di: DI)
    : UstadEditPresenter<WorkSpaceEditView, Site>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    val workspaceTermsOneToManyJoinEditHelper = DefaultOneToManyJoinEditHelper<SiteTermsWithLanguage>(SiteTerms::sTermsUid,
            "state_WorkspaceTerms_list", SiteTerms.serializer().list,
            SiteTermsWithLanguage.serializer().list, this, SiteTermsWithLanguage::class) {
        sTermsUid = it
    }

    fun handleAddOrEditWorkspaceTerms(workspaceTerms: SiteTermsWithLanguage) {
        workspaceTermsOneToManyJoinEditHelper.onEditResult(workspaceTerms)
    }

    fun handleRemoveWorkspaceTerms(workspaceTerms: SiteTermsWithLanguage) {
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

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Site? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val workSpace = db.onRepoWithFallbackToDb(5000) {
            it.siteDao.getSiteAsync()
        } ?: Site()

        val workspaceTerms = db.onRepoWithFallbackToDb(5000) {
            it.siteTermsDao.findAllWithLanguageAsList()
        }

        workspaceTermsOneToManyJoinEditHelper.liveList.sendValue(workspaceTerms)

        return workSpace
    }

    override fun onLoadFromJson(bundle: Map<String, String>): Site? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: Site? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, Site.serializer(), entityJsonStr)
        }else {
            editEntity = Site()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: Site) {
        GlobalScope.launch(doorMainDispatcher()) {
            repo.siteDao.updateAsync(entity)

            workspaceTermsOneToManyJoinEditHelper.commitToDatabase(repo.siteTermsDao) {
                //no need to set the foreign key
            }

            view.finishWithResult(listOf(entity))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}