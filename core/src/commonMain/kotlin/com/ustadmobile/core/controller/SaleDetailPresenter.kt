package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.SaleDetailView.Companion.ARG_SALE_GEN_NAME
import com.ustadmobile.core.view.SaleDetailView.Companion.ARG_SALE_UID
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_NAME
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_UID
import com.ustadmobile.core.view.SalePaymentDetailView.Companion.ARG_SALE_PAYMENT_DEFAULT_VALUE
import com.ustadmobile.core.view.SalePaymentDetailView.Companion.ARG_SALE_PAYMENT_UID
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
    internal var repository: UmAppDatabase
    private val saleItemDao: SaleItemDao
    private val saleDao: SaleDao
    private val saleVoiceNoteDao: SaleVoiceNoteDao
    private val salePaymentDao: SalePaymentDao
    private val currentSaleItem: SaleItem? = null
    private var currentSale: Sale? = null
    private var updatedSale: Sale? = null
    private val locationDao: LocationDao
    private var currentSaleName = ""

    private var locationLiveData: DoorLiveData<List<Location>>? = null

    private var positionToLocationUid: MutableMap<Int, Long>? = null

    var isShowSaveButton = false

    var voiceNoteFileName: String? = null
    private var totalPayment: Long = 0
    private var totalAfterDiscount: Long = 0

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        saleItemDao = repository.saleItemDao
        salePaymentDao = repository.salePaymentDao
        saleDao = repository.saleDao
        locationDao = repository.locationDao
        saleVoiceNoteDao = UmAppDatabase.getInstance(context).saleVoiceNoteDao

        positionToLocationUid = HashMap()

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
                view.showSignature(true)
                view.showDelivered(true)
                view.showNotes(true)
                view.showPayments(true)
            })

        } else {
            view.runOnUiThread(Runnable{ view.showPayments(false) })
            updatedSale = Sale()
            updatedSale!!.salePreOrder = true //ie: Not delivered unless ticked.
            updatedSale!!.saleDone = false
            updatedSale!!.saleActive = false
            view.showSignature(false)

            GlobalScope.launch {
                val result = saleDao.insertAsync(updatedSale!!)
                initFromSale(result)
            }
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
        if(saleUid != null) {
            GlobalScope.launch {
                val result = saleItemDao.getSaleItemCountFromSale(saleUid)
                if (result > 0) {
                    view.runOnUiThread(Runnable {
                        view.showSaveButton(true)
                        view.showNotes(true)
                        view.showDelivered(true)
                        view.showCalculations(true)
                        view.showPayments(true)
                    })
                }

                val res = saleItemDao.findTotalPaidBySaleAsync(saleUid)
                if(res!=null) {
                    view.updateOrderTotal(res as Long)
                }
            }
        }

    }

    //Called every time payment list gets updated (via Recycler Adapter's custom observer)
    fun getTotalPaymentsAndUpdateTotalView(saleUid: Long) {
        //Get total payment count
        GlobalScope.launch {
            val res = salePaymentDao.findTotalPaidBySaleAsync(saleUid)
            if(res!=null){
                totalPayment = res
                updateBalance()
            }
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
                    view.updatePaymentTotal(result)
            }

        }
    }

    fun initFromSale(saleUid: Long) {

        if(saleUid != null) {
            //Observe this sale entity
            val saleLiveData = saleDao.findByUidLive(saleUid)

            val thisP = this
            GlobalScope.launch(Dispatchers.Main) {
                saleLiveData.observe(thisP, thisP::handleSaleChanged)
            }

            GlobalScope.launch {
                //Get the sale entity
                val result = saleDao.findByUidAsync(saleUid)
                updatedSale = result
                view.updateSaleOnView(updatedSale!!)
                startObservingLocations()
            }

            //Any voice notes
            //TODO: Implement this on KMP
            //        saleVoiceNoteDao.findBySaleUidAsync(saleUid, object : UmCallback<SaleVoiceNote> {
            //            override fun onSuccess(result: SaleVoiceNote?) {
            //                if (result != null) {
            //                    val voiceNotePath = saleVoiceNoteDao.getAttachmentPath(result.saleVoiceNoteUid)
            //                    if (voiceNotePath != null && !voiceNotePath!!.isEmpty()) {
            //                        view.updateSaleVoiceNoteOnView(voiceNotePath!!)
            //                    }
            //                }
            //            }
            //            override fun onFailure(exception: Throwable?) {
            //
            //            }
            //        })

            getTotalSaleOrderAndDiscountAndUpdateView(saleUid)
            updateSaleItemProvider(saleUid)
            updatePaymentItemProvider(saleUid)

            getPaymentTotalAndUpdateView()
        }
    }


    private fun startObservingLocations() {
        val locLive = locationDao.findAllActiveLocationsLive()
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            locLive.observe(thisP, thisP::handleLocationsChanged)
        }
    }

    private fun handleSaleChanged(sale: Sale?) {
        //set the og person value
        if (currentSale == null)
            currentSale = sale

        if (updatedSale == null || updatedSale != sale) {
            if (sale != null) {
                updatedSale = sale
                view.updateSaleOnView(updatedSale!!)
            }
        }

    }

    fun refreshSaleOnView() {
        view.updateSaleOnView(updatedSale!!)
    }

    fun handleLocationsChanged(changedLocations: List<Location>?) {
        var selectedPosition = 0

        var locationUid: Long = 0

        if (updatedSale == null) {
            updatedSale = Sale()
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
            updatedSale!!.saleActive = true
            if (updatedSale!!.saleLocationUid == 0L) {
                updatedSale!!.saleLocationUid = positionToLocationUid!!.get(0)!!
            }

            //Persist voice note
            if (voiceNoteFileName != null && voiceNoteFileName!!.isNotEmpty()) {
                val voiceNote = SaleVoiceNote()
                voiceNote.saleVoiceNoteSaleUid = updatedSale!!.saleUid
                GlobalScope.launch {
                    try {
                        val result = saleVoiceNoteDao.insertAsync(voiceNote)

                        //TODO: File stuff in KMP
//                        val `is` = FileInputStream(voiceNoteFileName!!)
//                        saleVoiceNoteDao.setAttachment(result, `is`)

                    } catch (e: IOException) {
                        println(e!!.message)
                    }
                }
            }

            GlobalScope.launch {
                val result = saleItemDao.getTitleForSaleUidAsync(updatedSale!!.saleUid)
                updatedSale!!.saleTitle = result
                saleDao.updateAsync(updatedSale!!)
                view.finish()
            }
        }
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
            newSalePayment.salePaymentUid = result!!
            val impl = UstadMobileSystemImpl.instance
            val args = HashMap<String, String>()
            args.put(ARG_SALE_PAYMENT_UID, newSalePayment.salePaymentUid.toString())
            args.put(ARG_SALE_PAYMENT_DEFAULT_VALUE,
                    (totalAfterDiscount - totalPayment).toString())
            impl.go(SalePaymentDetailView.VIEW_NAME, args, context)
        }
    }

    fun handleClickAddSaleItem() {

        val saleItem = SaleItem()
        saleItem.saleItemSaleUid = updatedSale!!.saleUid
        saleItem.saleItemDueDate = UMCalendarUtil.getDateInMilliPlusDays(2)
        GlobalScope.launch {
            try {
                val saleItemUid = saleItemDao.insertAsync(saleItem)
                saleItem.saleItemUid = saleItemUid

                val impl = UstadMobileSystemImpl.instance
                val args = HashMap<String, String>()
                args.put(ARG_SALE_ITEM_UID, saleItemUid.toString())
                impl.go(SelectProducerView.VIEW_NAME, args, context)
            }catch(e:Exception){
                println(e.message)
            }
        }
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
        //args.put(ARG_SALE_PAYMENT_DEFAULT_VALUE, String.valueOf(totalAfterDiscount - totalPayment));
        impl.go(SalePaymentDetailView.VIEW_NAME, args, context)
    }

    fun handleClickAddSignature() {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(ARG_SALE_UID, currentSale!!.saleUid.toString())
        impl.go(SaleDetailSignatureView.VIEW_NAME, args, context)
    }
}
