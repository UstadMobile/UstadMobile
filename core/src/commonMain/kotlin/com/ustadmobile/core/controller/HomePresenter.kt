package com.ustadmobile.core.controller

import com.ustadmobile.core.controller.ContentEntryListPresenter.Companion.ARG_ACTIVE_INDEX
import com.ustadmobile.core.controller.ContentEntryListPresenter.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryListView
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_FILTER_BUTTONS
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.core.view.LoginView
import com.ustadmobile.core.view.UserProfileView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.js.JsName

class HomePresenter(context: Any, arguments: Map<String, String?>,  view: HomeView,
                    val personDao: PersonDao, impl: UstadMobileSystemImpl)
    : LanguageOptionPresenter(context, arguments, view, impl){

    private var account: UmAccount? = null

    private var showDownloadAll = false

    private val homeView: HomeView = view


    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        showDownloadAll = impl.getAppConfigString(
                AppConfig.KEY_SHOW_DOWNLOAD_ALL_BTN, null, context)!!.toBoolean()
        handleShowDownloadButton(showDownloadAll)
        DoorMutableLiveData<UmAccount?>(null).observe(this, ::onChanged)

    }

    private fun onChanged(t: UmAccount?) {
        GlobalScope.launch {
            var filterOptions = "${impl.getString(MessageID.libraries, context)},${impl.getString(MessageID.downloaded, context)}"
            var options = listOf(Pair(MessageID.contents,
                    "${ContentEntryListView.VIEW_NAME}?$ARG_FILTER_BUTTONS=$filterOptions&$ARG_LIBRARIES_CONTENT=''&$ARG_ACTIVE_INDEX=0"))
            if(t != null){
                account = t
                val person = personDao.findByUid(t.personUid)
                if(person != null){

                    if(person.admin){
                        //TODO: Make sure you change report view name when integrating other nav items
                        filterOptions = "$filterOptions, ${impl.getString(MessageID.libraries, context)}"
                        options = options.plus(Pair(MessageID.reports, "DashboardView?$ARG_FILTER_BUTTONS=$filterOptions"))
                    }
                    view.runOnUiThread(Runnable {
                        homeView.setLoggedPerson(person)
                    })
                }
            }
            view.runOnUiThread(Runnable {
                homeView.loadProfileIcon(if(account == null) "" else "")
                homeView.setOptions(options)
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

    fun handleClickShareApp() {
        homeView.showShareAppDialog()
    }

    override fun handleNavigation() {}


    companion object {
        @JsName("MASTER_SERVER_ROOT_ENTRY_UID")
        const val MASTER_SERVER_ROOT_ENTRY_UID = -4103245208651563007L
    }
}