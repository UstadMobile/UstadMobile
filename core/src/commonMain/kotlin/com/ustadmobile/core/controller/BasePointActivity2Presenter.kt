package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.Login2View.Companion.ARG_STARTSYNCING
import com.ustadmobile.lib.db.entities.Person

class BasePointActivity2Presenter
/**
 * Gets arguments and initialises
 * @param context       Context
 * @param arguments     Arguments
 * @param view          View
 */
(context: Any, arguments: Map<String, String>?, view: BasePointView2,
        val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<BasePointView2>(context, arguments!!, view) {

    //Database repository
    internal lateinit var repository: UmAppDatabase

    /**
     * Gets sync started flag
     * @return  true if syncStarted set to true, else false
     */
    var isSyncStarted = false
        private set

    init {

        if (arguments != null && arguments!!.containsKey(ARG_STARTSYNCING)) {
            if (arguments!!.get(ARG_STARTSYNCING) == "true") {
                isSyncStarted = true
            }
        }
    }

    /**
     * Gets logged in person and observes it.
     */
    fun getLoggedInPerson() {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        val loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid
        val personLive = repository.personDao.findByUidLive(loggedInPersonUid)
        personLive.observe(this@BasePointActivity2Presenter,
                UmObserver<Person> { this@BasePointActivity2Presenter.handlePersonValueChanged(it) })
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
        impl.go(Login2View.VIEW_NAME, args, context)
    }

    /**
     * Goes to settings screen view.
     */
    fun handleClickSettingsIcon() {
        val args = HashMap<String, String>()
        impl.go(SettingsView.VIEW_NAME, args, context)
    }

    /**
     * Goes to Search activity. This method will not do anything. The Search will figure out
     * where it has been clicked.
     */
    fun handleClickSearchIcon() {

        //Update: This method will not do anything the Search will figure out where it it
        // has been clicked.
    }

    /**
     * About menu clicked. Goes to about screen
     */
    fun handleClickAbout() {
        val args = HashMap<String, String>()
        impl.go(AboutView.VIEW_NAME, args, context)
    }

    /**
     * Confirm that user wants to share the app which will get the app set up file and share it
     * upon getting it from System Impl.
     */
    fun handleConfirmShareApp() {

        //Get setup file
        impl.getAppSetupFile(context, false, object : UmCallback {

            override fun onSuccess(result: Any?) {
                //Share it on the view
                view.shareAppSetupFile(result!!.toString())
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }

}
