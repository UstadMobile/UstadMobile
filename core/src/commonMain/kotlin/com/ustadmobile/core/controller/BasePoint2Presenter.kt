package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.db.dao.SalePaymentDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AboutView
import com.ustadmobile.core.view.BasePoint2View
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.UserProfileView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * Presenter for BasePoint2 view
 */
class BasePoint2Presenter(context: Any,
                          arguments: Map<String, String>,
                          view: BasePoint2View)
    : UstadBaseController<BasePoint2View>(context, arguments, view) {

    internal var repository: UmAppDatabase
    internal var loggedInPersonUid: Long? = null
    internal var saleDao: SaleDao
    internal var preOrderLive: DoorLiveData<Int>? = null
    internal var paymentsDueLive: DoorLiveData<Int>? = null
    internal var salePaymentDao: SalePaymentDao
    internal var personPictureDao: PersonPictureDao

    private var preOrderCount = 0
    private var paymentsDueCount = 0

    private var personLive: DoorLiveData<Person>? = null
    private var personPictureLive: DoorLiveData<PersonPicture>? = null

    init {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        saleDao = repository.getSaleDao()
        salePaymentDao = repository.getSalePaymentDao()
        personPictureDao = repository.personPictureDao
    }

    /**
     * Gets logged in person and observes it.
     */
    fun getLoggedInPerson() {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

        personLive = repository.personDao.findByUidLive(loggedInPersonUid!!)
        personLive!!.observe(this, this::handlePersonValueChanged)

        personPictureLive = repository.personPictureDao.findByPersonUidLive(loggedInPersonUid!!)
        personPictureLive!!.observe(this, this::handlePersonPictureChanged)


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
        UmAccountManager.setActiveAccount(null!!, context)
        val impl = UstadMobileSystemImpl.instance
        UmAccountManager.updatePasswordHash(null, context, impl)
        val args = HashMap<String, String>()
        impl.go(Login2View.VIEW_NAME, args, context)
    }

    /**
     * About menu clicked.
     */
    fun handleClickAbout() {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        impl.go(AboutView.VIEW_NAME, args, context)
    }

    /**
     * Confirm that user wants to share the app which will get the app set up file and share it
     * upon getting it from System Impl.
     */
    fun handleConfirmShareApp() {
        val impl = UstadMobileSystemImpl.instance

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
        preOrderLive!!.observe(this, this::handlePreOrderCountUpdate)

        paymentsDueLive = salePaymentDao.getPaymentsDueCountLive()
        paymentsDueLive!!.observe(this, this::handlePaymnetsDueCountUpdate)

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
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        impl.go(UserProfileView.VIEW_NAME, args, context)
    }

}
