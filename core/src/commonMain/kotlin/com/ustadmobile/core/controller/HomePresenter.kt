package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.ContentEntryListView
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_FILTER_BUTTONS
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_RECYCLED_CONTENT
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.core.view.LoginView
import com.ustadmobile.core.view.UserProfileView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.js.JsName

class HomePresenter(context: Any, arguments: Map<String, String?>,  view: HomeView,
                    val personDao: PersonDao, val impl: UstadMobileSystemImpl)
    : LanguageOptionPresenter(context, arguments, view, impl){

    private var account: UmAccount? = null

    private var showDownloadAll = false

    private val homeView: HomeView = view


    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        showDownloadAll = impl.getAppConfigString(
                AppConfig.KEY_SHOW_DOWNLOAD_ALL_BTN, null, context)!!.toBoolean()
        handleShowDownloadButton(showDownloadAll)
        UmAccountManager.activeAccountLiveData.observeWithPresenter(this, ::onChanged)
    }

    private fun onChanged(t: UmAccount?) {
        GlobalScope.launch {
            val contentEntryListArgs = mutableMapOf(ARG_CONTENT_ENTRY_UID to MASTER_SERVER_ROOT_ENTRY_UID.toString(),
                    ARG_LIBRARIES_CONTENT to "",
                    ARG_FILTER_BUTTONS to "$ARG_LIBRARIES_CONTENT,$ARG_DOWNLOADED_CONTENT")

            val options = mutableListOf<Pair<Int, String>>()

            if(t != null){
                account = t
                val person = personDao.findByUid(t.personUid)
                if(person != null){
                    if(person.admin){
                        //TODO: This is using a DummyView
                        contentEntryListArgs[ARG_FILTER_BUTTONS] +=  ",$ARG_RECYCLED_CONTENT"
                        options.add(Pair(MessageID.reports, "DashboardView"))
                    }

                    homeView.runOnUiThread(Runnable {
                        homeView.setLoggedPerson(person)
                    })
                }
            }

            homeView.runOnUiThread(Runnable {
                homeView.loadProfileIcon(if(account == null) "" else "")
                options.add(0, Pair(MessageID.contents,
                        ContentEntryListView.VIEW_NAME + "?" +
                                UMFileUtil.mapToQueryString(contentEntryListArgs)))
                homeView.setOptions(options)
            })
        }
    }

    fun handleShowDownloadButton(show: Boolean){
        homeView.runOnUiThread(Runnable {
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