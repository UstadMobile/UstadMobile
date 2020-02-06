package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_RECYCLED_CONTENT
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.js.JsName

class HomePresenter(context: Any, arguments: Map<String, String?>,  view: HomeView,
                    val personDao: PersonDao, val impl: UstadMobileSystemImpl,
                    val repository : UmAppDatabase =
                            UmAccountManager.getRepositoryForActiveAccount(this))
    : LanguageOptionPresenter(context, arguments, view, impl){

    private var account: UmAccount? = null

    private var showDownloadAll = false

    private val homeView: HomeView = view

    var showLocationPermission = true

    private var personPictureDao: PersonPictureDao = repository.personPictureDao

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        showDownloadAll = impl.getAppConfigString(
                AppConfig.KEY_SHOW_DOWNLOAD_ALL_BTN, null, context)!!.toBoolean()
        handleShowDownloadButton(showDownloadAll)

        showLocationPermission = impl.getAppConfigString(
                AppConfig.KEY_SHOW_LOCATION_PERMISSION_PROMPT, null, context)!!.toBoolean()

        UmAccountManager.activeAccountLiveData.observeWithPresenter(this, ::onChanged)
    }

    private fun onChanged(t: UmAccount?) {
        GlobalScope.launch {
            val contentEntryListTabsArgs = mutableMapOf(
                    "0" to "${MessageID.libraries};$ARG_CONTENT_ENTRY_UID=$MASTER_SERVER_ROOT_ENTRY_UID" +
                            "&$ARG_LIBRARIES_CONTENT",
                    "1" to "${MessageID.downloaded};$ARG_DOWNLOADED_CONTENT")

            val options = mutableListOf<Pair<Int, String>>()

            var profilePicturePath = ""
            if(t != null) {
                account = t
                val person = personDao.findByUid(t.personUid)
                if(person != null){
                    if(person.admin){
                        contentEntryListTabsArgs["2"] =  "${MessageID.recycled};$ARG_RECYCLED_CONTENT"
                        options.add(Pair(MessageID.reports, ReportDashboardView.VIEW_NAME))
                    }

                    //For ClassBook removing reports for the ClassBook's own report
                    options.clear()
                    options.add(0, Pair(MessageID.catalog,
                            SelectSaleProductView.VIEW_NAME))
                    options.add(1, Pair(MessageID.inventory,
                            InventoryListView.VIEW_NAME))
                    options.add(2, Pair(MessageID.sales,
                            SaleListView.VIEW_NAME))


                    options.add(3, Pair(MessageID.content,
                            "${ContentEntryListView.VIEW_NAME}?$ARG_CONTENT_ENTRY_UID=$MASTER_SERVER_ROOT_ENTRY_UID" +
                                    "&$ARG_LIBRARIES_CONTENT&${ContentEntryListView.EDIT_BUTTONS_NEWFOLDER}=2" +
                                    "&${ContentEntryListView.ARG_EDIT_BUTTONS_CONTROL_FLAG}=2"))


                    options.add(4, Pair(MessageID.reports,
                            DashboardEntryListView.VIEW_NAME))

                    homeView.runOnUiThread(Runnable {
                        homeView.setLoggedPerson(person)
                    })

                    val personPicture = personPictureDao.findByPersonUidAsync(person.personUid)
                    if (personPicture != null) {
                        val imagePath = personPictureDao.getAttachmentPath(personPicture)
                        if (imagePath != null && imagePath.isNotEmpty()) {
                            profilePicturePath=imagePath
                        }
                    }
                }
            }

            homeView.runOnUiThread(Runnable {

                homeView.loadProfileIcon(profilePicturePath)
//                options.add(0, Pair(MessageID.contents,
//                        HOME_CONTENTENTRYLIST_TABS_VIEWNAME + "?" +
//                                UMFileUtil.mapToQueryString(contentEntryListTabsArgs)))
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

    /**
     * When settings gear clicked in the menu options - Goes to the settings activity.
     */
    fun handleClickSettings(){
        val args = HashMap<String, String>()
        impl.go(SettingsView.VIEW_NAME, args, context)
    }

    override fun handleNavigation() {}

    companion object {
        @JsName("MASTER_SERVER_ROOT_ENTRY_UID")
        const val MASTER_SERVER_ROOT_ENTRY_UID = -4103245208651542427L

        /**
         * This view name will generate a tabbed set of ContentEntryList views. See
         * HomeContentEntryTabsFragment for information about arguments
         */
        const val HOME_CONTENTENTRYLIST_TABS_VIEWNAME = "ContentEntryListTabs"

    }
}