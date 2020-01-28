package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.SaleDeliveryDetailView.Companion.ARG_SALE_DELIVERY_SALE_UID
import com.ustadmobile.core.view.SaleDeliveryDetailView.Companion.ARG_SALE_DELIVERY_UID
import com.ustadmobile.core.view.SaleDetailView.Companion.ARG_SALE_GEN_NAME
import com.ustadmobile.core.view.SaleDetailView.Companion.ARG_SALE_UID
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_NAME
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_UID
import com.ustadmobile.core.view.SalePaymentDetailView.Companion.ARG_SALE_PAYMENT_DEFAULT_VALUE
import com.ustadmobile.core.view.SalePaymentDetailView.Companion.ARG_SALE_PAYMENT_UID
import com.ustadmobile.core.view.SelectProducersView.Companion.ARG_SELECT_PRODUCERS_SALE_ITEM_PREORDER
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.io.IOException

/**
 * Presenter for SaleDetail view
 */
class SaleDetailPresenter(context: Any,
                          arguments: Map<String, String>?,
                          view: SaleDetailView)
    : UstadBaseController<SaleDetailView>(context, arguments!!, view) {

    private lateinit var umProvider: DataSource.Factory<Int,SaleItemListDetail>
    private lateinit var pProvider: DataSource.Factory<Int,SalePayment>
    private lateinit var dProvider: DataSource.Factory<Int,SaleDelivery>
    internal var repository: UmAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context)
    private val saleItemDao: SaleItemDao
    private val saleDeliveryDao : SaleDeliveryDao
    private val saleDao: SaleDao
    private val saleVoiceNoteDao: SaleVoiceNoteDao
    private val salePaymentDao: SalePaymentDao
    private val inventoryTransactionDao : InventoryTransactionDao
    private val personDao: PersonDao
    private val currentSaleItem: SaleItem? = null
    private var currentSale: Sale? = null
    private var updatedSale: Sale? = null
    private var customer: Person ? = null
    private val locationDao: LocationDao
    private var currentSaleName = ""
    private var customerUid : Long = 0L

    private var positionToLocationUid: MutableMap<Int, Long>? = null

    var isShowSaveButton = false

    var voiceNoteFileName: String? = null
    private var totalPayment: Long = 0
    private var totalAfterDiscount: Long = 0

    private var loggedInPersonUid: Long = 0

    init {

        //Get provider Dao
        saleItemDao = repository.saleItemDao
        salePaymentDao = repository.salePaymentDao
        saleDao = repository.saleDao
        locationDao = repository.locationDao
        saleVoiceNoteDao = UmAccountManager.getRepositoryForActiveAccount(context).saleVoiceNoteDao
        personDao = repository.personDao
        saleDeliveryDao = repository.saleDeliveryDao
        inventoryTransactionDao = repository.inventoryTransactionDao

        positionToLocationUid = HashMap()

        loggedInPersonUid = UmAccountManager.getActivePersonUid(context)

    }


    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (arguments.containsKey(ARG_SALE_UID)) {
            if (arguments.containsKey(ARG_SALE_GEN_NAME)) {
                currentSaleName = arguments[ARG_SALE_GEN_NAME].toString()
            }

            initFromSale(arguments[ARG_SALE_UID]!!.toLong())
            isShowSaveButton = true
            view.runOnUiThread(Runnable{
                view.showCalculations(true)
                view.showDeliveries(true)
                view.showDelivered(true)
                view.showNotes(true)
                view.showPayments(true)
            })

        } else {
            view.runOnUiThread(Runnable{
                view.showPayments(false)
                view.showDelivered(false)
            })
            updatedSale = Sale()
            updatedSale!!.salePersonUid = loggedInPersonUid
            updatedSale!!.saleCreationDate = UMCalendarUtil.getDateInMilliPlusDays(0)
            updatedSale!!.salePreOrder = true //ie: Not delivered unless ticked.
            updatedSale!!.saleDone = false
            updatedSale!!.saleActive = false

            GlobalScope.launch {
                val result = saleDao.insertAsync(updatedSale!!)
                updatedSale!!.saleUid = result
                initFromSale(result)
            }
        }
    }

    fun initFromSale(saleUid: Long) {

        if(saleUid != null) {
            val thisP = this

            GlobalScope.launch {
                //Get the sale entity
                val resultLive = saleDao.findByUidLive(saleUid)
                view.runOnUiThread(Runnable {
                    resultLive.observeWithPresenter(thisP, thisP::updateSaleOnView)
                })

                //startObservingLocations()

                val saleVoiceNote = saleVoiceNoteDao.findBySaleUidAsync(saleUid)
                if(saleVoiceNote != null) {
                    val saleVoiceNotePath = saleVoiceNoteDao.getAttachmentPath(saleVoiceNote!!)
                    if(saleVoiceNotePath != null){
                        view.runOnUiThread(Runnable { view.updateSaleVoiceNoteOnView(saleVoiceNotePath) })
                    }
                }
            }

            getTotalSaleOrderAndDiscountAndUpdateView(saleUid)
            updateSaleItemProvider(saleUid)
            updatePaymentItemProvider(saleUid)
            updateDeliveriesProvider(saleUid)
            getPaymentTotalAndUpdateView()
        }
    }

    private fun updateDeliveriesProvider(saleUid: Long){
        dProvider = saleDeliveryDao.findAllDeliveriesBySaleUid(saleUid)
        view.setDeliveriesProvider(dProvider)
    }

    fun updateSaleOnView(sale:Sale?){
        if(sale != null){

            //set the og person value
            if (currentSale == null)
                currentSale = sale

            updatedSale = sale
            view.runOnUiThread(Runnable {
                view.updateSaleOnView(updatedSale!!)
            })

            startObservingCustomer()
            startObservingLocations()
        }
    }


    private fun updateSaleItemProvider(saleUid: Long) {
        //Get provider
        umProvider = saleItemDao.findAllSaleItemListDetailActiveBySaleProvider(saleUid)
        view.setListProvider(umProvider)
    }

    fun updatePaymentItemProvider(saleUid: Long) {
        //Get provider
        pProvider = salePaymentDao.findBySaleProvider(saleUid)
        view.setPaymentProvider(pProvider)
    }

    fun getTotalSaleOrderAndDiscountAndUpdateView(saleUid: Long) {
        var thisP = this
        if(saleUid != 0L) {
            GlobalScope.launch {
                val resultLive = saleItemDao.getSaleItemCountFromSaleLive(saleUid)
                view.runOnUiThread(Runnable {
                    resultLive.observeWithPresenter(thisP, thisP::updateSaleNumbers)
                })

                val resLive = saleItemDao.findTotalPaidBySaleLive(saleUid)
                view.runOnUiThread(Runnable {
                    resLive.observeWithPresenter(thisP, thisP::updateOrderTotal)
                })
            }
        }
    }

    private fun updateOrderTotal(res: Long?){
        if(res!=null) {
            view.runOnUiThread(Runnable {
                view.updateOrderTotal(res)
            })
        }
    }

    private fun updateSaleNumbers(result : Int?){
        if (result != null && result > 0) {
            view.runOnUiThread(Runnable {
                view.showSaveButton(true)
                view.showNotes(true)
                view.showDelivered(true)
                view.showCalculations(true)
                view.showPayments(true)
                view.showDelivered(true)
            })
        }
    }

    private fun updatePaymentTotal(res:Int?){
        if(res!=null){
            totalPayment = res.toLong()
            updateBalance()
        }
    }

    //Called every time payment list gets updated (via Recycler Adapter's custom observer)
    fun getTotalPaymentsAndUpdateTotalView(saleUid: Long) {
        var thisP = this
        //Get total payment count
        GlobalScope.launch {
            val res = salePaymentDao.findTotalPaidBySaleLive(saleUid)
            view.runOnUiThread(Runnable {
                res.observeWithPresenter(thisP, thisP::updatePaymentTotal)
            })

        }
    }

    fun updateBalanceDueFromTotal(totalAD: Float) {
        totalAfterDiscount = totalAD.toLong()
        updateBalance()

    }

    fun updateBalance() {
        view.updateBalanceDue(totalAfterDiscount - totalPayment)
    }

    fun getPaymentTotalAndUpdateView() {
        if (currentSaleItem != null) {
            GlobalScope.launch {
                val result =
                        salePaymentDao.findTotalPaidBySaleAsync(currentSaleItem.saleItemUid)
                if(result!=null)
                    view.updatePaymentTotal(result.toLong())
            }

        }
    }

    private fun startObservingLocations() {
        val locLive = locationDao.findAllActiveLocationsLive()
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            locLive.observeWithPresenter(thisP, thisP::handleLocationsChanged)
        }
    }


    private fun startObservingCustomer(){
        val thisP = this
        if(updatedSale!!.saleCustomerUid != 0L){
            GlobalScope.launch(Dispatchers.Main){
                val customerLive = personDao.findByUidLive(updatedSale!!.saleCustomerUid)
                customerLive.observeWithPresenter(thisP, thisP::handleCustomerChanged)
            }
        }
    }

    private fun handleCustomerChanged(changedCustomer: Person?){
        var firstNames = ""
        var lastName = ""


        if(changedCustomer != null){
            if(changedCustomer.firstNames != null){
                firstNames = changedCustomer.firstNames!!
            }
            if(changedCustomer.lastName != null){
                lastName = changedCustomer.lastName!!
            }
            if(changedCustomer!!.personLocationUid != 0L) {
                updatedSale!!.saleLocationUid = changedCustomer.personLocationUid
            }
        }

        view.updateCustomerNameOnView(firstNames + " " + lastName)
    }

    private fun handleLocationsChanged(changedLocations: List<Location>?) {
        var selectedPosition = 0

        var locationUid: Long = 0

        if (updatedSale == null) {
            updatedSale = Sale()
            updatedSale!!.salePersonUid = loggedInPersonUid
            updatedSale!!.saleCreationDate = UMCalendarUtil.getDateInMilliPlusDays(0)
        }
        if (updatedSale!!.saleLocationUid != 0L) {
            locationUid = updatedSale!!.saleLocationUid
        }



        val locationList = ArrayList<String>()
        var spinnerId = 0
        for (el in changedLocations!!) {
            positionToLocationUid?.set(spinnerId, el.locationUid)

            val title = el.title
            if (title != null) {
                locationList.add(title)
            }
            if (locationUid == el.locationUid) {
                selectedPosition = spinnerId
            }
            spinnerId++
        }

        var locationPreset = locationList.toTypedArray<String>()

        view.setLocationPresets(locationPreset, selectedPosition)

    }

    fun handleClickSave() {

        if (updatedSale != null) {
            if(updatedSale!!.saleCustomerUid == null || updatedSale!!.saleCustomerUid == 0L){
                val selectCustomerMessage = UstadMobileSystemImpl.instance.getString(
                        MessageID.please_select_customer, context)
                view.sendMessage(selectCustomerMessage)
            }else{
                updatedSale!!.saleActive = true
                if (updatedSale!!.saleLocationUid == 0L) {
                    if(positionToLocationUid != null && positionToLocationUid!!.size > 0) {

                        if (positionToLocationUid!!.get(0) != null) {
                            updatedSale!!.saleLocationUid = positionToLocationUid!!.get(0)!!
                        } else {
                            updatedSale!!.saleLocationUid = 0
                        }
                    }
                }

                //Persist voice note
                if (voiceNoteFileName != null && voiceNoteFileName!!.isNotEmpty()) {
                    GlobalScope.launch {
                        try {
                            var voiceNoteUid : Long = 0L

                            var existingVN = saleVoiceNoteDao.findByPersonUidAsync(updatedSale!!.saleUid)
                            if(existingVN == null){
                                existingVN = SaleVoiceNote()
                                existingVN.saleVoiceNoteSaleUid = updatedSale!!.saleUid
                                existingVN.saleVoiceNoteTimestamp = DateTime.nowUnixLong()
                                voiceNoteUid = saleVoiceNoteDao.insertAsync(existingVN)
                                existingVN.saleVoiceNoteUid = voiceNoteUid
                            }

                            if(existingVN!=null) {
                                saleVoiceNoteDao.setAttachment(existingVN, voiceNoteFileName!!)
                            }
                        } catch (e: IOException) {
                            println(e!!.message)
                        }
                    }
                }

                val inventoryTransactionDao = repository.inventoryTransactionDao
                //Activate all transactions
                GlobalScope.launch {
                    inventoryTransactionDao.activateAllTransactionsBySaleAndLe(
                            updatedSale!!.saleUid, updatedSale!!.salePersonUid)

                }

                //Generate title for this Sale
                var thisP = this
                GlobalScope.launch {
                    val resultLive = saleItemDao.getTitleForSaleUidLive(updatedSale!!.saleUid)
                    view.runOnUiThread(Runnable {
                        resultLive.observeWithPresenter(thisP, thisP::handleUpdateSaleName)
                    })
                }

            }
        }
    }
    private fun handleUpdateSaleName(result:String?){
        updatedSale!!.saleTitle = result
        GlobalScope.launch {
            saleDao.updateAsync(updatedSale!!)
        }
        view.finish()
    }

    fun handleClickSaleItemEdit(saleItemUid: Long) {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(ARG_SALE_ITEM_UID, saleItemUid.toString())
        args.put(ARG_SALE_ITEM_NAME, currentSaleName)
        impl.go(SaleItemDetailView.VIEW_NAME, args, context)
    }

    fun handleClickAddPayment() {
        val newSalePayment = SalePayment()
        newSalePayment.salePaymentActive = false
        newSalePayment.salePaymentPaidDate = getSystemTimeInMillis() //default start to today
        newSalePayment.salePaymentPaidAmount = 0
        newSalePayment.salePaymentCurrency = "Afs"
        newSalePayment.salePaymentSaleUid = updatedSale!!.saleUid
        newSalePayment.salePaymentDone = false
        GlobalScope.launch {
            val result = salePaymentDao.insertAsync(newSalePayment)
            newSalePayment.salePaymentUid = result
            val impl = UstadMobileSystemImpl.instance
            val args = HashMap<String, String>()
            args.put(ARG_SALE_PAYMENT_UID, newSalePayment.salePaymentUid.toString())
            args.put(ARG_SALE_PAYMENT_DEFAULT_VALUE,
                    (totalAfterDiscount - totalPayment).toString())
            impl.go(SalePaymentDetailView.VIEW_NAME, args, context)
        }
    }

    fun handleClickAddDelivery(){

        if(updatedSale!!.saleActive == false){
            val saveMessage = UstadMobileSystemImpl.instance.getString(MessageID.save_sale, context)
            view.sendMessage(saveMessage)
        }else {
            GlobalScope.launch {
                val impl = UstadMobileSystemImpl.instance
                val args = HashMap<String, String>()

                args.put(ARG_SALE_DELIVERY_SALE_UID, updatedSale!!.saleUid.toString())
                impl.go(SaleDeliveryDetailView.VIEW_NAME, args, context)
            }
        }

    }

    fun updateCustomerUid(cUid: Long){
        customerUid = cUid
        updatedSale!!.saleCustomerUid = customerUid
        GlobalScope.launch {
            saleDao.updateAsync(updatedSale!!)

            customer = personDao.findByUid(customerUid)
            var firstNames = ""
            var lastName = ""
            if(customer!= null && customer!!.firstNames != null){
                firstNames = customer!!.firstNames!!
            }
            if(customer!= null && customer!!.lastName != null){
                lastName = customer!!.lastName!!
            }
            view.runOnUiThread(Runnable {
                view.updateCustomerNameOnView(firstNames + " " + lastName)
            })
        }
    }

    fun handleClickCustomer(){
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(SelectCustomerView.ARG_SP_LE_UID,
                UmAccountManager.getActiveAccount(context)!!.personUid.toString())
        impl.go(SelectCustomerView.VIEW_NAME, args, context)
    }

    fun handleClickAddSaleItem(){
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        impl.go(SelectSaleTypeDialogView.VIEW_NAME, args, context)
    }

    fun handleClickAddSaleItemSold(){
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args[SelectSaleProductView.ARG_INVENTORY_MODE] = "true"
        args[SelectProducersView.ARG_SELECT_PRODUCERS_INVENTORY_SELECTION] = "true"
        args[SelectProducersView.ARG_SELECT_PRODUCERS_SALE_UID] = currentSale!!.saleUid.toString()
        impl.go(SelectSaleProductView.VIEW_NAME, args, context)
    }

    fun handleClickAddSaleItemPreOrder(){

        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args[SelectSaleProductView.ARG_INVENTORY_MODE] = "true"
        args[SelectProducersView.ARG_SELECT_PRODUCERS_INVENTORY_SELECTION] = "false"
        args[SelectProducersView.ARG_SELECT_PRODUCERS_SALE_UID] = currentSale!!.saleUid.toString()
        args[ARG_SELECT_PRODUCERS_SALE_ITEM_PREORDER] = "true"
        impl.go(SelectSaleProductView.VIEW_NAME, args, context)
    }

    fun handleDiscountChanged(discount: Long) {
        updatedSale!!.saleDiscount = discount
        view.updateOrderTotalAfterDiscount(discount)
    }

    fun handleOrderNotesChanged(notes: String) {
        updatedSale!!.saleNotes = notes
    }

    fun handleSetDelivered(delivered: Boolean) {
        updatedSale!!.saleDone = delivered
        updatedSale!!.salePreOrder = !delivered
    }

    fun handleLocationSelected(position: Int) {
        if (position >= 0 && !positionToLocationUid!!.isEmpty()
                && positionToLocationUid!!.containsKey(position)) {
            val locationUid = positionToLocationUid!!.get(position)
            updatedSale!!.saleLocationUid = locationUid!!
        }
    }

    fun handleDeleteVoiceNote() {
        this.voiceNoteFileName = ""
    }

    fun handleDeletePayment(salePaymentUid: Long) {
        GlobalScope.launch {
            salePaymentDao.inactivateEntityAsync(salePaymentUid)
        }
    }

    fun handleEditPayment(salePaymentUid: Long) {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(ARG_SALE_PAYMENT_UID, salePaymentUid.toString())
        args.put(ARG_SALE_PAYMENT_DEFAULT_VALUE,
                (totalAfterDiscount - totalPayment).toString())
        impl.go(SalePaymentDetailView.VIEW_NAME, args, context)
    }

    fun handleEditDelivery(saleDeliveryUid: Long){
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(ARG_SALE_DELIVERY_UID, saleDeliveryUid.toString())
        args.put(ARG_SALE_DELIVERY_SALE_UID, currentSale!!.saleUid.toString())
        impl.go(SaleDeliveryDetailView.VIEW_NAME, args, context)
    }

    fun handleDeleteDelivery(saleDeliveryUid: Long){

        if(saleDeliveryUid != 0L && currentSale!!.saleUid != 0L){
            //1. Get all transactions that have the given delivery uid and current sale uid.
            //2. Update to false.
            //3. Update Delivery to false too
            GlobalScope.launch {
                inventoryTransactionDao.deactivateAllTransactionsBySaleDeliveryAndSale(
                        saleDeliveryUid, currentSale!!.saleUid)
                val saleDelivery = saleDeliveryDao.findByUidAsync(saleDeliveryUid)
                saleDelivery!!.saleDeliveryActive = false
                saleDeliveryDao.updateAsync(saleDelivery)
            }


        }


    }
}
