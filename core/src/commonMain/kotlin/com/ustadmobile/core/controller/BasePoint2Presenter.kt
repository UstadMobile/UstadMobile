package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.db.dao.SalePaymentDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * Presenter for BasePoint2 view
 */
class BasePoint2Presenter(context: Any,
                          arguments: Map<String, String>?,
                          view: BasePoint2View,
                          val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<BasePoint2View>(context, arguments!!, view) {

    internal var repository: UmAppDatabase
    internal var loggedInPersonUid: Long? = null
    internal var saleDao: SaleDao
    internal lateinit var preOrderLive: DoorLiveData<Int>
    internal lateinit var paymentsDueLive: DoorLiveData<Int>
    internal var salePaymentDao: SalePaymentDao
    internal var personPictureDao: PersonPictureDao

    private var preOrderCount = 0
    private var paymentsDueCount = 0

    private lateinit var personLive: DoorLiveData<Person?>
    private lateinit var personPictureLive: DoorLiveData<PersonPicture?>

    init {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        saleDao = repository.saleDao
        salePaymentDao = repository.salePaymentDao
        personPictureDao = repository.personPictureDao
    }

    /**
     * Gets logged in person and observes it.
     */
    fun getLoggedInPerson() {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

        personLive = repository.personDao.findByUidLive(loggedInPersonUid!!)
        val thisP = this
        personLive.observe(thisP, thisP::handlePersonValueChanged)

//        GlobalScope.launch(Dispatchers.Main) {
//
//        }

        personPictureLive = repository.personPictureDao.findByPersonUidLive(loggedInPersonUid!!)
        personPictureLive.observe(thisP, thisP::handlePersonPictureChanged)
//        GlobalScope.launch(Dispatchers.Main) {
//
//        }



    }

    private fun handlePersonPictureChanged(personPicture: PersonPicture?) {
        if (personPicture != null) {
            //TODO: Fix this
//            view.updateImageOnView(personPictureDao.getAttachmentPath(
//                    personPicture.personPictureUid))
        }
    }

    /**
     * Called on logged in person changed.
     *
     * @param loggedInPerson    The person changed.
     */
    private fun handlePersonValueChanged(loggedInPerson: Person?) {
        if (loggedInPerson != null) {
            view.checkPermissions()
            if (loggedInPerson.admin) {
                view.showCatalog(true)
                view.showInventory(true)
                view.showSales(true)
                view.showCourses(true)

                //Find pic and update on view
                personPictureDao = repository.personPictureDao
                GlobalScope.launch {
                    val personPicture =
                            personPictureDao.findByPersonUidAsync(loggedInPerson.personUid)

                    if (personPicture != null) {
                        //TODO: Fix this
                        //view.updateImageOnView(personPictureDao.getAttachmentPath(
                        //        personPicture.personPictureUid))
                    }
                }
            } else {
                view.showCatalog(true)
                view.showInventory(true)
                view.showSales(true)
                view.showCourses(true)
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
     * Logs out
     */
    fun handleLogOut() {
        val blankAccount = UmAccount(0, null, null,null)
        UmAccountManager.setActiveAccount(blankAccount, context)
        UmAccountManager.updatePasswordHash(null, context, impl)
        val args = HashMap<String, String>()
        impl.go(LoginView.VIEW_NAME, args, context)
    }

    /**
     * About menu clicked.
     */
    fun handleClickAbout() {
        val args = HashMap<String, String>()
        impl.go(AboutView.VIEW_NAME, args, context)
    }


    fun handleClickSettingsIcon(){
        val args = HashMap<String, String>()
        impl.go(SettingsView.VIEW_NAME, args, context)
    }


    /**
     * Confirm that user wants to share the app which will get the app set up file and share it
     * upon getting it from System Impl.
     */
    fun handleConfirmShareApp() {

        //Get setup file
        impl.getAppSetupFile(context, false, object : UmCallback<Any> {

            override fun onSuccess(result: Any?) {
                //Share it on the view
                view.shareAppSetupFile(result!!.toString())
            }

            override fun onFailure(exception: Throwable?) {
                println(exception!!.message)
            }
        })
    }


    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        getLoggedInPerson()
    }

    fun updateDueCountOnView() {

        preOrderLive = saleDao.getPreOrderSaleCountLive()
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            preOrderLive!!.observe(thisP, thisP::handlePreOrderCountUpdate)
        }

        paymentsDueLive = salePaymentDao.getPaymentsDueCountLive()
        GlobalScope.launch(Dispatchers.Main) {
            paymentsDueLive!!.observe(thisP, thisP::handlePaymnetsDueCountUpdate)
        }

    }

    fun handlePreOrderCountUpdate(count: Int?) {
        preOrderCount = count!!
        //view.updateNotificationForSales(count);
        view.updateNotificationForSales(preOrderCount + paymentsDueCount)
    }

    fun handlePaymnetsDueCountUpdate(count: Int?) {
        paymentsDueCount = count!!
        view.updateNotificationForSales(preOrderCount + paymentsDueCount)
    }

    fun handleClickPersonIcon() {
        val args = HashMap<String, String>()
        impl.go(UserProfileView.VIEW_NAME, args, context)
    }

}
