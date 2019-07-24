package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.HomeView
import kotlinx.coroutines.Runnable

class HomePresenter(context: Any, arguments: Map<String, String?>, view: HomeView)
    : UstadBaseController<HomeView>(context, arguments, view) {

    val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

    private var showDownloadAll = false

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        showDownloadAll = impl.getAppConfigString(
                AppConfig.KEY_SHOW_DOWNLOAD_ALL_BTN, null, this)!!.toBoolean()
        handleShowDownloadButton(showDownloadAll)
    }

    fun handleShowDownloadButton(show: Boolean){
        view.runOnUiThread(Runnable {
            view.showDownloadAllButton(show && showDownloadAll)
        })
    }


    fun handleDownloadAllClicked(){
        val args = HashMap<String, String>()
        args["contentEntryUid"] = MASTER_SERVER_ROOT_ENTRY_UID.toString()
        impl.go("DownloadDialog", args, context)
    }


    companion object {
        const val MASTER_SERVER_ROOT_ENTRY_UID = -4103245208651563007L
    }
}