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
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.get
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance

class WorkspaceEnterLinkPresenter(context: Any, arguments: Map<String, String>, view: WorkspaceEnterLinkView,
                                  di: DI) :
        UstadBaseController<WorkspaceEnterLinkView>(context, arguments, view, di) {

    private var workSpace: WorkSpace? = null

    private var checkTextLinkJob: Deferred<Unit>? = null

    private val impl: UstadMobileSystemImpl by instance()

    private val httpClient: HttpClient by instance()

    fun handleClickNext(){
        val mWorkSpace = workSpace
        if(mWorkSpace != null){
            impl.go(Login2View.VIEW_NAME, mapOf(ARG_SERVER_URL to view.workspaceLink,
                    ARG_WORKSPACE to Json.stringify(WorkSpace.serializer(),mWorkSpace)), context)
        }
    }

    fun handleCheckLinkText(href: String){

        if(checkTextLinkJob != null){
            checkTextLinkJob?.cancel()
            checkTextLinkJob = null
        }

        checkTextLinkJob = GlobalScope.async(doorMainDispatcher()) {
            try {
                var formattedHref = if(href.startsWith("http")) href else "https://$href"
                formattedHref = UMFileUtil.joinPaths(formattedHref, "Workspace","verify")
                workSpace = httpClient.get<WorkSpace>(formattedHref) {
                    timeout {
                        requestTimeoutMillis = LINK_REQUEST_TIMEOUT
                    }
                }
                view.validLink = workSpace != null
            }catch (e: Exception) {
                view.validLink = false
            }

            view.progressVisible = false
            checkTextLinkJob = null

            return@async
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        checkTextLinkJob = null
        workSpace = null
    }

    companion object {

        const val LINK_REQUEST_TIMEOUT: Long = 10000

    }

}
