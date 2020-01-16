package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_FILTER_BUTTONS
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_RECYCLED_CONTENT
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

    private var showLocationPermission = false

    private val homeView: HomeView = view


    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        showDownloadAll = impl.getAppConfigString(
                AppConfig.KEY_SHOW_DOWNLOAD_ALL_BTN, null, context)!!.toBoolean()
        handleShowDownloadButton(showDownloadAll)

        showLocationPermission = impl.getAppConfigString(
                AppConfig.KEY_SHOW_LOCATION_PERMISSION_PROMPT, null, context)!!.toBoolean()
        handleShowLocationPermissionPrompt(showLocationPermission)

        UmAccountManager.activeAccountLiveData.observeWithPresenter(this, ::onChanged)
    }

    private fun onChanged(nAccount: UmAccount?) {
        GlobalScope.launch {
            val contentEntryListArgs = mutableMapOf(ARG_CONTENT_ENTRY_UID to MASTER_SERVER_ROOT_ENTRY_UID.toString(),
                    ARG_LIBRARIES_CONTENT to "",
                    ARG_FILTER_BUTTONS to "$ARG_LIBRARIES_CONTENT,$ARG_DOWNLOADED_CONTENT")

            val options = mutableListOf<Pair<Int, String>>()

            if(nAccount != null){
                account = nAccount
                val person = personDao.findByUid(nAccount.personUid)
                if(person != null){
                    if(person.admin){
                        //TODO: This is using a DummyView
                        contentEntryListArgs[ARG_FILTER_BUTTONS] +=  ",$ARG_RECYCLED_CONTENT"
                        //options.add(Pair(MessageID.reports, "DashboardView"))
                    }

                    options.add(0,Pair(MessageID.bottomnav_feed_title,
                            FeedListView.VIEW_NAME))
                    options.add(1,Pair(MessageID.bottomnav_classes_title,
                            ClazzListView.VIEW_NAME))
                    options.add(2,Pair(MessageID.bottomnav_people_title,
                            PeopleListView.VIEW_NAME))
                    options.add(3,Pair(MessageID.bottomnav_reports_title,
                            BaseReportView.VIEW_NAME))

                    homeView.runOnUiThread(Runnable {
                        homeView.setLoggedPerson(person)
                        homeView.showSettings(person.admin)
                    })


                }


            }

            homeView.runOnUiThread(Runnable {
                homeView.loadProfileIcon(if(account == null) "" else "")
//                options.add(0, Pair(MessageID.contents,
//                        ContentEntryListView.VIEW_NAME + "?" +
//                                UMFileUtil.mapToQueryString(contentEntryListArgs)))
                homeView.setOptions(options)
            })
        }
    }

    fun handleShowDownloadButton(show: Boolean){
        homeView.runOnUiThread(Runnable {
            homeView.showDownloadAllButton(show && showDownloadAll)
        })
    }

    fun handleShowLocationPermissionPrompt(show: Boolean){

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

    fun handleClickSettings(){
        val args = HashMap<String, String>()
        impl.go(SettingsView.VIEW_NAME, args, context)
    }

    override fun handleNavigation() {}


    companion object {
        @JsName("MASTER_SERVER_ROOT_ENTRY_UID")
        const val MASTER_SERVER_ROOT_ENTRY_UID = -4103245208651563007L
    }
}