package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SaleItemDao
import com.ustadmobile.core.db.dao.SaleItemReminderDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AddReminderDialogView
import com.ustadmobile.core.view.SaleItemDetailView
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_DUE_DATE
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_NAME
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_PRODUCT_UID
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_UID
import com.ustadmobile.core.view.SelectProducerView.Companion.ARG_PRODUCER_UID
import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.lib.db.entities.SaleItemReminder
import com.ustadmobile.lib.db.entities.SaleProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * Presenter for SaleItemDetail view
 */
class SaleItemDetailPresenter : UstadBaseController<SaleItemDetailView> {

    internal var repository: UmAppDatabase
    private var saleItemDao: SaleItemDao
    private var reminderDao: SaleItemReminderDao

    private var currentSaleItem: SaleItem? = null
    private var updatedSaleItem: SaleItem? = null
    private var productUid: Long = 0
    private var producerUid: Long = 0
    private var saleItemUid: Long = 0
    private var saleItemDueDate: Long = 0
    private var saleTitle: String? = null
    private var saleProductName: String? = null

    private var refreshSaleItem = true
    val thisP = this

    constructor(context: Any,
                arguments: Map<String, String?>,
                view: SaleItemDetailView)
            : super(context, arguments, view) {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        saleItemDao = repository.saleItemDao
        reminderDao = repository.saleItemReminderDao
    }

    constructor(context: Any,
                arguments: Map<String, String?>,
                view: SaleItemDetailView,
                refresh: Boolean)
            : super(context, arguments, view) {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        saleItemDao = repository.saleItemDao
        reminderDao = repository.saleItemReminderDao
        refreshSaleItem = refresh
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (arguments.containsKey(ARG_SALE_ITEM_PRODUCT_UID)) {
            productUid = (arguments[ARG_SALE_ITEM_PRODUCT_UID]!!.toLong())
        }

        if (arguments.containsKey(ARG_PRODUCER_UID)) {
            producerUid = (arguments[ARG_PRODUCER_UID]!!.toLong())
        }

        if (arguments.containsKey(ARG_SALE_ITEM_UID)) {
            saleItemUid = (arguments[ARG_SALE_ITEM_UID]!!.toLong())
        }

        if (arguments.containsKey(ARG_SALE_ITEM_NAME)) {
            saleTitle = arguments[ARG_SALE_ITEM_NAME].toString()
        }

        if (arguments.containsKey(ARG_SALE_ITEM_DUE_DATE)) {
            saleItemDueDate = (arguments[ARG_SALE_ITEM_DUE_DATE]!!.toLong())
        }

        if (arguments.containsKey(ARG_SALE_ITEM_UID)) {
            initFromSaleItem(arguments[ARG_SALE_ITEM_UID]!!.toLong())
        } else {

            //Create the new SaleItem
            updatedSaleItem = SaleItem(productUid)
            GlobalScope.launch {
                try {
                    val result = saleItemDao.insertAsync(updatedSaleItem!!)
                    initFromSaleItem(result)
                }catch(e:Exception){
                    println(e.message)
                }
            }
        }
    }

    private fun initFromSaleItem(saleItemUid: Long) {

        //Get the sale item entity
        GlobalScope.launch {

            //Observe it.
            val saleItemLiveData = saleItemDao.findByUidLive(saleItemUid)

            GlobalScope.launch(Dispatchers.Main) {
                saleItemLiveData.observe(thisP, thisP::handleSaleItemChanged)
            }

            if (refreshSaleItem) {
                //Notification observer
                val provider =
                        reminderDao!!.findBySaleItemUid(saleItemUid)

                view.runOnUiThread(Runnable { view.setReminderProvider(provider) })
            }
        }
    }


    private fun handleSaleItemChanged(changedSaleItem: SaleItem?) {
        if(changedSaleItem!=null) {

            saleItemDueDate = changedSaleItem.saleItemDueDate

            if (currentSaleItem == null) {
                currentSaleItem = changedSaleItem
            }

            var saleProductUid: Long = 0L
            if (updatedSaleItem == null || updatedSaleItem != changedSaleItem) {
                saleProductUid = changedSaleItem.saleItemProductUid
                updatedSaleItem = changedSaleItem
            }else if(updatedSaleItem!!.saleItemProductUid != 0L){
                saleProductUid = updatedSaleItem!!.saleItemProductUid
            }

            GlobalScope.launch {
                val saleProductLive = repository.saleProductDao.findByUidLive(saleProductUid)
                GlobalScope.launch(Dispatchers.Main) {
                    saleProductLive.observe(thisP, thisP::handleSaleProductLive)
                }

            }
        }
    }

    private fun handleSaleProductLive(saleProduct : SaleProduct?){
        if(saleProduct!=null) {
            var productName: String? = ""
            productName = saleProduct!!.saleProductName
            view.updateSaleItemOnView(updatedSaleItem!!, productName!!)
        }
    }

    fun handleChangeQuantity(quantity: Int) {
        updatedSaleItem!!.saleItemQuantity = quantity

    }

    fun handleChangePPP(ppp: Long) {
        updatedSaleItem!!.saleItemPricePerPiece = ppp.toFloat()
    }

    fun updateTotal(q: Int, p: Long) {
        view.runOnUiThread(Runnable{ view.updateTotal(p * q) })
    }

    fun setSold(sold: Boolean) {
        updatedSaleItem!!.saleItemPreorder = !sold
        updatedSaleItem!!.saleItemSold = sold
    }

    fun setPreOrder(po: Boolean) {
        if (!updatedSaleItem!!.saleItemPreorder && po) {
            //Add 1 day reminder
            handleAddReminder(1)
        }
        updatedSaleItem!!.saleItemSold = !po
        updatedSaleItem!!.saleItemPreorder = po
        //If recently clicked. Set Reminder. Set reminder on Save as well.

    }

    fun handleChangeOrderDueDate(date: Long) {
        updatedSaleItem!!.saleItemDueDate = date
    }

    fun handleClickAddReminder() {

        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()

        if (arguments.containsKey(ARG_SALE_ITEM_UID)) {
            args.put(ARG_SALE_ITEM_UID, arguments[ARG_SALE_ITEM_UID].toString())
        }
        if (arguments.containsKey(ARG_SALE_ITEM_NAME)) {
            args.put(ARG_SALE_ITEM_NAME, arguments[ARG_SALE_ITEM_NAME].toString())
        } else {
            if(saleProductName == null){
                saleProductName = ""
            }
            args.put(ARG_SALE_ITEM_NAME, saleProductName!!)
        }
        if (arguments.containsKey(ARG_SALE_ITEM_DUE_DATE)) {
            args.put(ARG_SALE_ITEM_DUE_DATE, arguments[ARG_SALE_ITEM_DUE_DATE].toString())
        }
        impl.go(AddReminderDialogView.VIEW_NAME, args, context)
    }

    fun handleDeleteReminder(saleItemReminderUid: Long) {
        GlobalScope.launch {
            reminderDao!!.invalidateReminder(saleItemReminderUid)
        }
    }

    fun handleClickSave() {

        val inventoryTransactionDao = repository.inventoryTransactionDao
        val saleDao = repository.saleDao

        if (updatedSaleItem != null) {
            updatedSaleItem!!.saleItemActive = true

            if (producerUid != 0L && productUid != 0L) {
                updatedSaleItem!!.saleItemProductUid = productUid
                updatedSaleItem!!.saleItemProducerUid = producerUid
            }

            if (updatedSaleItem!!.saleItemQuantity == 0) {
                updatedSaleItem!!.saleItemQuantity = 1
            }

            val thisP = this
            GlobalScope.launch {

                saleItemDao!!.updateAsync(updatedSaleItem!!)

                //Get all reminders.
                val remindersLive =
                        reminderDao.findBySaleItemUidLive(updatedSaleItem!!.saleItemUid)
                view.runOnUiThread(Runnable {
                    remindersLive.observe(thisP, thisP::handleReminderLive)
                })


            }
        }
    }

    private fun handleReminderLive(reminders : List<SaleItemReminder>?){
        if(!updatedSaleItem!!.saleItemSold || updatedSaleItem!!.saleItemPreorder) {
            for (everyReminder in reminders!!) {
                val days = everyReminder.saleItemReminderDays
                if(saleTitle == null){
                    saleTitle = ""
                }
                view.setReminderNotification(days, saleTitle!!, saleItemDueDate)
            }
        }
        view.finish()
    }

    fun handleAddReminder(days: Int) {
        val reminder = SaleItemReminder(days, saleItemUid, true)
        val reminderDao = repository.saleItemReminderDao
        GlobalScope.launch {
            try {
                val result =
                        reminderDao.findBySaleItemUidAndDaysAsync(saleItemUid, days)
                if (result.size > 0) {
                    //It has it already. Skipp it.
                } else {
                    try {
                        reminderDao.insertAsync(reminder)
                    } catch (e: Exception) {
                        println(e.message)
                    }
                }
            }catch (e:Exception){
                println(e.message)
            }
        }
    }
}
