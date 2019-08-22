package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.core.view.LoginView
import com.ustadmobile.core.view.UserProfileView
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.Runnable

class HomePresenter(context: Any, arguments: Map<String, String?>, view: HomeView)
    : UstadBaseController<HomeView>(context, arguments, view) {

    val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

    private var account: UmAccount? = null

    private var showDownloadAll = false

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        showDownloadAll = impl.getAppConfigString(
                AppConfig.KEY_SHOW_DOWNLOAD_ALL_BTN, null, context)!!.toBoolean()
        handleShowDownloadButton(showDownloadAll)

        account = UmAccountManager.getActiveAccount(context)

        view.runOnUiThread(Runnable {
            view.loadProfileIcon(if(account == null) "" else "")
        })
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

    fun handleClickPersonIcon(){
        val args = HashMap<String, String?>()
        args.putAll(arguments)
        impl.go(if(account != null && account!!.personUid != 0L) UserProfileView.VIEW_NAME
        else LoginView.VIEW_NAME , args, context)
    }


    companion object {
        const val MASTER_SERVER_ROOT_ENTRY_UID = -4103245208651563007L
    }
}