package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.deactivateByUids
import com.ustadmobile.core.util.OneToManyJoinEditHelperMp
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.SiteEditView
import com.ustadmobile.core.view.SiteTermsEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class SiteEditPresenter(context: Any,
                        arguments: Map<String, String>, view: SiteEditView,
                        lifecycleOwner: LifecycleOwner,
                        di: DI)
    : UstadEditPresenter<SiteEditView, Site>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private val siteTermsOneToManyJoinEditHelper = OneToManyJoinEditHelperMp(
        SiteTermsWithLanguage::sTermsUid,
        ARG_SAVEDSTATE_TERMS,
        ListSerializer(SiteTerms.serializer()),
        ListSerializer(SiteTermsWithLanguage.serializer()),
        this, requireSavedStateHandle(),
        SiteTermsWithLanguage::class) { sTermsUid = it }

    val siteTermsOneToManyJoinListener = siteTermsOneToManyJoinEditHelper.createNavigateForResultListener(
        SiteTermsEditView.VIEW_NAME, SiteTermsWithLanguage.serializer())


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

        siteTermsOneToManyJoinEditHelper.liveList.postValue(siteTerms)

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
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json, Site.serializer(), entity)
    }

    override fun handleClickSave(entity: Site) {
        GlobalScope.launch(doorMainDispatcher()) {
            repo.siteDao.updateAsync(entity)

            repo.withDoorTransactionAsync { txRepo ->
                siteTermsOneToManyJoinEditHelper.commitToDatabase(txRepo.siteTermsDao,
                    { txRepo.siteTermsDao.deactivateByUids(it, systemTimeInMillis()) }
                ) {
                    //no need to set the foreign key
                }
            }

            finishWithResult(safeStringify(di, ListSerializer(Site.serializer()), listOf(entity)))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

        const val ARG_SAVEDSTATE_TERMS = "terms"

    }

}