package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.SiteEditView
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
import kotlinx.serialization.builtins.ListSerializer


class SiteEditPresenter(context: Any,
                        arguments: Map<String, String>, view: SiteEditView,
                        lifecycleOwner: DoorLifecycleOwner,
                        di: DI)
    : UstadEditPresenter<SiteEditView, Site>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    val siteTermsOneToManyJoinEditHelper = DefaultOneToManyJoinEditHelper<SiteTermsWithLanguage>(SiteTerms::sTermsUid,
            "state_SiteTerms_list", ListSerializer(SiteTerms.serializer()),
            ListSerializer(SiteTermsWithLanguage.serializer()), this, SiteTermsWithLanguage::class) {
        sTermsUid = it
    }

    fun handleAddOrEditSiteTerms(siteTerms: SiteTermsWithLanguage) {
        siteTermsOneToManyJoinEditHelper.onEditResult(siteTerms)
    }

    fun handleRemoveSiteTerms(siteTerms: SiteTermsWithLanguage) {
        siteTermsOneToManyJoinEditHelper.onDeactivateEntity(siteTerms)
    }


    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.siteTermsList = siteTermsOneToManyJoinEditHelper.liveList
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Site? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val site = db.onRepoWithFallbackToDb(5000) {
            it.siteDao.getSiteAsync()
        } ?: Site()

        val siteTerms = db.onRepoWithFallbackToDb(5000) {
            it.siteTermsDao.findAllWithLanguageAsList()
        }

        siteTermsOneToManyJoinEditHelper.liveList.sendValue(siteTerms)

        return site
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

            siteTermsOneToManyJoinEditHelper.commitToDatabase(repo.siteTermsDao) {
                //no need to set the foreign key
            }

            view.finishWithResult(listOf(entity))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}