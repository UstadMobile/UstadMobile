package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.core.view.WorkspaceEnterLinkView
import io.ktor.client.request.get
import io.ktor.client.statement.HttpStatement
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class WorkspaceEnterLinkPresenter(context: Any, arguments: Map<String, String>, view: WorkspaceEnterLinkView,
                                  val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<WorkspaceEnterLinkView>(context, arguments, view) {

    private lateinit var validWorkspaceHost: String

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        validWorkspaceHost = impl.getAppConfigString(AppConfig.KEY_VALID_WORKSPACE,
                "ustadmobile.com", context) ?: ""
    }

    fun goToNext(){
        impl.go(Login2View.VIEW_NAME, mapOf(ARG_SERVER_URL to view.workspaceLink), context)
    }

    fun checkLinkValidity(){
        view.progressVisible = true
        val link = view.workspaceLink
        if(link != null){
            GlobalScope.launch {
                try {
                    val response = defaultHttpClient().get<HttpStatusCode>(link)
                    view.runOnUiThread(Runnable {
                        view.progressVisible = false
                        view.validLink = link.contains(validWorkspaceHost)
                                && response == HttpStatusCode.OK
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

}
