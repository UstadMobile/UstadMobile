package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.NavigationItem
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.LoginView.Companion.ARG_STARTSYNCING
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class BasePointPresenter
/**
 * Gets arguments and initialises
 * @param context       Context
 * @param arguments     Arguments
 * @param view          View
 */
(context: Any, arguments: Map<String, String>?, view: BasePointView,
 val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<BasePointView>(context, arguments!!, view) {

    //Database repository
    internal lateinit var repository: UmAppDatabase

    private var loggedInPersonUid: Long = 0L
    private var personDao: PersonDao

    private var account: UmAccount? = null

    private var showDownloadAll = false

    private var personPictureDao: PersonPictureDao? = null

    /**
     * Gets sync started flag
     * @return  true if syncStarted set to true, else false
     */
    var isSyncStarted = false
        private set

    init {

        if (arguments != null && arguments.containsKey(ARG_STARTSYNCING)) {
            if (arguments.get(ARG_STARTSYNCING) == "true") {
                isSyncStarted = true
            }
        }
        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        personDao = repository.personDao
        personPictureDao = repository.personPictureDao
    }

    /**
     * Gets logged in person and observes it.
     */
    fun getLoggedInPerson() {
        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid
        val personLive = personDao.findByUidLive(loggedInPersonUid)
        personLive.observe(this, this::handlePersonValueChanged)
    }

    /**
     * Called on logged in person changed.
     *
     * @param loggedInPerson    The person changed.
     */
    private fun handlePersonValueChanged(loggedInPerson: Person?) {
        if (loggedInPerson != null) {
            view.updatePermissionCheck()
            if (loggedInPerson.admin) {
                view.showBulkUploadForAdmin(true)
                view.showSettings(true)

            } else {
                view.showBulkUploadForAdmin(false)
                view.showSettings(false)
            }

            GlobalScope.launch {
                val personPicture = personPictureDao!!.findByPersonUidAsync(loggedInPerson!!.personUid)
                if (personPicture != null) {
                    view.runOnUiThread(Runnable {
                        val imagePath = personPictureDao!!.getAttachmentPath(personPicture)
                        if(!imagePath.isEmpty()){
                            view.loadProfileImage(imagePath)
                        }else{
                            view.loadProfileIcon("")
                        }
                    })
                }
            }

        }
    }

    /**
     * Shows the share app dialog screen
     */
    fun handleClickShareIcon() {
        view.showShareAppDialog()
    }

    /**
     * Goes to bulk upload screen.
     */
    fun handleClickBulkUpload() {
        val args = HashMap<String, String>()
        impl.go(BulkUploadMasterView.VIEW_NAME, args, context)
    }

    /**
     * Logs out of the application.
     */
    fun handleLogOut() {
        UmAccountManager.setActiveAccount(null!!, context)
        val args = HashMap<String, String>()
        impl.go(LoginView.VIEW_NAME, args, context)
    }

    /**
     * Goes to settings screen view.
     */
    fun handleClickSettingsIcon() {
        val args = HashMap<String, String>()
        impl.go(SettingsView.VIEW_NAME, args, context)
    }

    /**
     * About menu clicked. Goes to about screen
     */
    fun handleClickAbout() {
        val args = HashMap<String, String>()
        impl.go(AboutView.VIEW_NAME, args, context)
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

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        account = UmAccountManager.getActiveAccount(context)

        var isAdmin: Boolean = false
        GlobalScope.launch {
            if (loggedInPersonUid != 0L) {
                val loggedInPerson = personDao.findByUid(loggedInPersonUid)
                if (loggedInPerson != null) {
                    isAdmin = loggedInPerson.admin
                } else {
                    isAdmin = false
                }
            }else{
                isAdmin = false
            }
        }

        view.runOnUiThread(Runnable {
            view.loadProfileIcon(if(account == null) "" else "")
        })

        val navItemsString = impl.getAppConfigString(
                AppConfig.NAVIGATION_ITEMS, null, context)

        if(navItemsString!=null){
            val items:MutableList<NavigationItem> = mutableListOf<NavigationItem>()
            val split = navItemsString.split(":")
            for(s:String in split){
                val navItem = NavigationItem(s, HashMap<String, String>(), "Title")
                items.add(navItem)
            }
            view.setupNavigation(items)

        }
    }

    /**
     * Confirm that user wants to share the app which will get the app set up file and share it
     * upon getting it from System Impl.
     */
    fun handleConfirmShareApp() {

        //Get setup file

        impl.getAppSetupFile(context, false, object : UmCallback<String> {
            override fun onSuccess(result: String?) {
                //Share it on the view
                view.shareAppSetupFile(result!!.toString())
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }

    companion object {
        const val MASTER_SERVER_ROOT_ENTRY_UID = -4103245208651563007L
    }

}
