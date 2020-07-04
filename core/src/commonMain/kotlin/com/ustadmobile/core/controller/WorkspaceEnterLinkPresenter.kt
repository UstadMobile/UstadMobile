package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_WORKSPACE
import com.ustadmobile.core.view.WorkspaceEnterLinkView
import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.lib.util.UMUtil
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class WorkspaceEnterLinkPresenter(context: Any, arguments: Map<String, String>, view: WorkspaceEnterLinkView,
                                  val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<WorkspaceEnterLinkView>(context, arguments, view) {

    private var workSpace: WorkSpace? = null

    fun handleClickNext(){
        impl.go(Login2View.VIEW_NAME, mapOf(ARG_SERVER_URL to view.workspaceLink,
                ARG_WORKSPACE to workSpace?.toString()), context)
    }

    fun handleCheckLinkText(href: String){
        //if(!UMUtil.isValidUrl(href)) return
        GlobalScope.launch {
            try {
                workSpace = defaultHttpClient().get<WorkSpace>(href)
                view.runOnUiThread(Runnable {
                    view.progressVisible = false
                    view.validLink = workSpace != null
                })
            }catch (e: Exception) {
                view.runOnUiThread(Runnable {
                    view.progressVisible = false
                    view.validLink = false
                })
            }
        }
    }

}
