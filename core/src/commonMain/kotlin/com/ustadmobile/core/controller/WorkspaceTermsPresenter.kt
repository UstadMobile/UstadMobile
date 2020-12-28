package com.ustadmobile.core.controller

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.WorkspaceTermsView
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.onDbThenRepoWithTimeout
import com.ustadmobile.lib.db.entities.WorkspaceTerms
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

/**
 * Show the user the terms for using a given workspace (e.g. privacy policy, terms and conditions, etc)
 */
class WorkspaceTermsPresenter(context: Any, arguments: Map<String, String>, view: WorkspaceTermsView,
                              di: DI)

    : UstadBaseController<WorkspaceTermsView>(context, arguments, view, di){

    private val accountManager: UstadAccountManager by di.instance()

    private val systemImpl: UstadMobileSystemImpl by di.instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        val serverUrl = arguments[UstadView.ARG_SERVER_URL] ?: accountManager.activeAccount.endpointUrl
        val endpoint = Endpoint(serverUrl)

        val repo: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_REPO)
        val displayedLocale = systemImpl.getDisplayedLocale(context)

        view.loading = true

        GlobalScope.launch(doorMainDispatcher()) {
            repo.onDbThenRepoWithTimeout(10000) {db, lastVal: WorkspaceTerms? ->
                val terms = db.workspaceTermsDao.findWorkspaceTerms(displayedLocale)
                if(terms?.termsHtml != null && terms.termsHtml != lastVal?.termsHtml){
                    view.termsHtml = terms.termsHtml
                }

                terms
            }

            view.loading = false
        }
    }

    fun handleClickAccept(){
        systemImpl.go(PersonEditView.VIEW_NAME_REGISTER, arguments, context)
    }

}