package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_WORKSPACE
import com.ustadmobile.core.view.WorkspaceEnterLinkView
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.lib.util.UMUtil
import io.ktor.client.request.get
import kotlinx.coroutines.*
import org.kodein.di.DI
import org.kodein.di.instance

class WorkspaceEnterLinkPresenter(context: Any, arguments: Map<String, String>, view: WorkspaceEnterLinkView,
                                  di: DI) :
        UstadBaseController<WorkspaceEnterLinkView>(context, arguments, view, di) {

    private var workSpace: WorkSpace? = null

    private var checkTextLinkJob: Deferred<Unit>? = null

    private val impl: UstadMobileSystemImpl by instance()

    fun handleClickNext(){
        impl.go(Login2View.VIEW_NAME, mapOf(ARG_SERVER_URL to view.workspaceLink,
                ARG_WORKSPACE to workSpace?.toString()), context)
    }

    fun handleCheckLinkText(href: String){

        if(checkTextLinkJob != null){
            checkTextLinkJob?.cancel()
            checkTextLinkJob = null
        }

        checkTextLinkJob = GlobalScope.async(doorMainDispatcher()) {
            try {
                val formattedHref = if(href.startsWith("http")) href else "https://$href"
                workSpace = defaultHttpClient().get<WorkSpace>(
                        UMFileUtil.joinPaths(formattedHref, "workspace","verify"))
                view.progressVisible = false
                view.validLink = workSpace != null
            }catch (e: Exception) {
                view.progressVisible = false
                view.validLink = false
            }
             return@async
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        checkTextLinkJob = null
        workSpace = null
    }

}
