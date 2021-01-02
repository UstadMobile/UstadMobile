package com.ustadmobile.core.controller

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.WorkspaceTermsDetailView
import com.ustadmobile.core.view.WorkspaceTermsDetailView.Companion.ARG_SHOW_ACCEPT_BUTTON
import com.ustadmobile.core.view.WorkspaceTermsDetailView.Companion.ARG_USE_DISPLAY_LOCALE
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.db.entities.SiteTerms
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


class WorkspaceTermsDetailPresenter(context: Any,
        arguments: Map<String, String>, view: WorkspaceTermsDetailView,
        lifecycleOwner: DoorLifecycleOwner,
        di: DI)
    : UstadDetailPresenter<WorkspaceTermsDetailView, SiteTerms>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.acceptButtonVisible = arguments[ARG_SHOW_ACCEPT_BUTTON]?.toBoolean() == true
    }


    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        if((account?.personUid ?: 0L) == 0L || view.acceptButtonVisible) {
            return false
        }else {
            return repo.onRepoWithFallbackToDb(5000) {
                it.personDao.personIsAdmin(account?.personUid ?: 0)
            }
        }
    }


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): SiteTerms? {
        //The active account has not been set or changed yet - the db could be from another endpoint
        //Therefor we will lookup the db/repo ourselves instead of using the val provided by the argument
        //itself.

        val serverUrl = arguments[UstadView.ARG_SERVER_URL] ?: accountManager.activeAccount.endpointUrl
        val endpoint = Endpoint(serverUrl)

        val dbToUse : UmAppDatabase = if(db is DoorDatabaseRepository) {
            di.on(endpoint).direct.instance(tag = DoorTag.TAG_REPO)
        }else {
            di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
        }

        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val displayedLocale = systemImpl.getDisplayedLocale(context)

        return dbToUse.onRepoWithFallbackToDb(5000) {
            if(arguments[ARG_USE_DISPLAY_LOCALE]?.toBoolean() == true) {
                db.siteTermsDao.findSiteTerms(displayedLocale)
            }else {
                db.siteTermsDao.findByUidAsync(entityUid)
            }
        }
    }

    fun handleClickAccept(){
        systemImpl.go(PersonEditView.VIEW_NAME_REGISTER, arguments, context)
    }


    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}