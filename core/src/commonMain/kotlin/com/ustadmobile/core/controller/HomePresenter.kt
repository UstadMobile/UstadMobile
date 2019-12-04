package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.core.view.LoginView
import com.ustadmobile.core.view.UserProfileView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.js.JsName

class HomePresenter(context: Any, arguments: Map<String, String?>,  view: HomeView,
                    val personDao: PersonDao, impl: UstadMobileSystemImpl)
    : LanguageOptionPresenter(context, arguments, view, impl) {

    private var account: UmAccount? = null

    private val homeView = view

    private var showDownloadAll = false

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        showDownloadAll = impl.getAppConfigString(
                AppConfig.KEY_SHOW_DOWNLOAD_ALL_BTN, null, context)!!.toBoolean()
        handleShowDownloadButton(showDownloadAll)

        account = UmAccountManager.getActiveAccount(context)

        GlobalScope.launch {
            var showReport = false; var person: Person? = null
            if(account != null){
                person = personDao.findByUid(account!!.personUid)
                if(person != null){
                   showReport = person.admin
                }
            }

            view.runOnUiThread(Runnable {
                homeView.showReportMenu(showReport)
                if(person != null){
                    homeView.setLoggedPerson(person)
                }
                homeView.loadProfileIcon(if(account == null) "" else "")
            })
        }
    }

    fun handleShowDownloadButton(show: Boolean){
        view.runOnUiThread(Runnable {
            homeView.showDownloadAllButton(show && showDownloadAll)
        })
    }


    fun handleDownloadAllClicked(){
        val args = HashMap<String, String>()
        args["contentEntryUid"] = MASTER_SERVER_ROOT_ENTRY_UID.toString()
        impl.go("DownloadDialog", args, context)
    }

    @JsName("handleClickPersonIcon")
    fun handleClickPersonIcon(){
        val args = HashMap<String, String?>()
        args.putAll(arguments)
        impl.go(if(account != null && account!!.personUid != 0L) UserProfileView.VIEW_NAME
        else LoginView.VIEW_NAME , args, context)
    }

    override fun handleNavigation() {
    }


    companion object {
        @JsName("MASTER_SERVER_ROOT_ENTRY_UID")
        const val MASTER_SERVER_ROOT_ENTRY_UID = -4103245208651563007L
    }
}