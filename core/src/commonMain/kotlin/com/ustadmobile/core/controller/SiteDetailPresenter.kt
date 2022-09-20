package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.view.SiteDetailView
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.SiteEditView
import com.ustadmobile.core.view.SiteTermsDetailView
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI


class SiteDetailPresenter(context: Any,
                          arguments: Map<String, String>, view: SiteDetailView,
                          lifecycleOwner: LifecycleOwner,
                          di: DI)
    : UstadDetailPresenter<SiteDetailView, Site>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return true
//        return repo.personDao.takeIf { account != null }
//                ?.personIsAdmin(account?.personUid ?: 0) ?: false
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Site {
        val workspace = withTimeoutOrNull(5000) {
             db.siteDao.getSiteAsync()
        } ?: Site()

        if(db !is DoorDatabaseRepository) {
            view.siteTermsList = repo.siteTermsDao.findAllTermsAsFactory()
        }

        return workspace
    }

    override fun handleClickEdit() {
        val siteUid = entity?.siteUid ?: return
        navigateForResult(NavigateForResultOptions(this,
            entity, SiteEditView.VIEW_NAME, Site::class, Site.serializer(),
            arguments = mutableMapOf(ARG_ENTITY_UID to siteUid.toString())))
    }

    fun handleClickTerms(terms: SiteTermsWithLanguage?){
        systemImpl.go(SiteTermsDetailView.VIEW_NAME,
                mapOf(ARG_ENTITY_UID to terms?.sTermsUid?.toString()), context)
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}