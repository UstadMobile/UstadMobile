package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SaleItemDao
import com.ustadmobile.core.db.dao.SaleItemReminderDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.AddReminderDialogView
import com.ustadmobile.core.view.SaleItemDetailView
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_DETAIL_FROM_INVENTORY
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_DETAIL_PREORDER
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_DUE_DATE
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_NAME
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_PRODUCT_UID
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_UID
import com.ustadmobile.core.view.SelectProducerView.Companion.ARG_PRODUCER_UID
import com.ustadmobile.core.view.SelectProducersView
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
    internal var database: UmAppDatabase
    private var saleItemDao: SaleItemDao
    private var saleItemDaoDB: SaleItemDao
    private var reminderDao: SaleItemReminderDao

    private var currentSaleItem: SaleItem? = null
    private var updatedSaleItem: SaleItem? = null
    private var productUid: Long = 0
    private var producerUid: Long = 0
    private var saleItemUid: Long = 0
    private var saleItemDueDate: Long = 0
    private var saleTitle: String? = null
    private var saleProductName: String? = null
    private var fromArguments = false
    private var refreshSaleItem = true
    val thisP = this
    private var preOrder = false
    private var saleUid: Long = 0

    constructor(context: Any,
                arguments: Map<String, String?>,
                view: SaleItemDetailView)
            : super(context, arguments, view) {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        database = UmAccountManager.getActiveDatabase(context)

        saleItemDao = repository.saleItemDao
        saleItemDaoDB = database.saleItemDao
        reminderDao = repository.saleItemReminderDao
    }

    constructor(context: Any,
                arguments: Map<String, String?>,
                view: SaleItemDetailView,
                refresh: Boolean)
            : super(context, arguments, view) {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        database = UmAccountManager.getActiveDatabase(context)

        saleItemDao = repository.saleItemDao
        reminderDao = repository.saleItemReminderDao
        saleItemDaoDB = database.saleItemDao
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

        if(arguments.containsKey(SelectProducersView.ARG_SELECT_PRODUCERS_SALE_UID)){
            saleUid = (arguments[SelectProducersView.ARG_SELECT_PRODUCERS_SALE_UID]!!.toLong())
        }

        if (arguments.containsKey(ARG_SALE_ITEM_NAME)) {
            saleTitle = arguments[ARG_SALE_ITEM_NAME].toString()
        }

        if (arguments.containsKey(ARG_SALE_ITEM_DUE_DATE)) {
            saleItemDueDate = (arguments[ARG_SALE_ITEM_DUE_DATE]!!.toLong())
        }

        if(arguments.containsKey(ARG_SALE_ITEM_DETAIL_FROM_INVENTORY)){
            val fromInventory = arguments[ARG_SALE_ITEM_DETAIL_FROM_INVENTORY].toString().toLowerCase()
            if(fromInventory.equals("true")){
                fromArguments = true
                view.showQuantityTextView(fromArguments)
            }
        }

        if(arguments.containsKey(ARG_SALE_ITEM_DETAIL_PREORDER)){
            val fromInventory = arguments[ARG_SALE_ITEM_DETAIL_PREORDER].toString().toLowerCase()
            if(fromInventory.equals("true")){
                preOrder = true
                fromArguments = false
                view.showQuantityTextView(fromArguments)
            }
        }

        if (arguments.containsKey(ARG_SALE_ITEM_UID)) {
            saleItemUid = arguments[ARG_SALE_ITEM_UID]!!.toLong()
            initFromSaleItem(saleItemUid)
        } else {
            //Create the new SaleItem
            updatedSaleItem = SaleItem(productUid)
            updatedSaleItem!!.saleItemPreorder = preOrder
            updatedSaleItem!!.saleItemSaleUid = saleUid
            if(preOrder){
                updatedSaleItem!!.saleItemDueDate = UMCalendarUtil.getDateInMilliPlusDays(2)
                updatedSaleItem!!.saleItemPreorder = true
            }
            GlobalScope.launch {
                saleItemUid = saleItemDaoDB.insertAsync(updatedSaleItem!!)
                updatedSaleItem!!.saleItemUid = saleItemUid
                initFromSaleItem(saleItemUid)
            }
        }
    }

    private fun initFromSaleItem(saleItemUid: Long) {

        //Get the sale item entity
        GlobalScope.launch {

            //Observe Sale Item
            val saleItemLiveData = saleItemDao.findByUidLive(saleItemUid)

            GlobalScope.launch(Dispatchers.Main) {
                saleItemLiveData.observeWithPresenter(thisP, thisP::handleSaleItemChanged)
            }

            //Observe product so we can update the toolbar
            GlobalScope.launch {

                val saleProductLive = repository.saleProductDao.findByUidLive(productUid)
                GlobalScope.launch(Dispatchers.Main) {
                    saleProductLive.observeWithPresenter(thisP, thisP::handleSaleProductLive)
                }
            }

            //Observe the notifications
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

            if (updatedSaleItem == null || updatedSaleItem != changedSaleItem) {
                updatedSaleItem = changedSaleItem
            }

            if(updatedSaleItem!!.saleItemPreorder){
                view.showQuantityTextView(false)
            }

            view.updateSaleItemOnView(updatedSaleItem!!)


        }
    }


    private fun handleSaleProductLive(saleProduct : SaleProduct?){
        if(saleProduct!=null) {
            var productName: String? = ""
            productName = saleProduct!!.saleProductName
            saleProductName = productName
            view.updateProductTitleOnView(productName!!)
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

    fun setPreOrder(po: Boolean) {
        if (po) {
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

        args.put(ARG_SALE_ITEM_UID, saleItemUid.toString())
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
        }else {
            args.put(ARG_SALE_ITEM_DUE_DATE, updatedSaleItem!!.saleItemDueDate.toString())
        }

        impl.go(AddReminderDialogView.VIEW_NAME, args, context)
    }

    fun handleDeleteReminder(saleItemReminderUid: Long) {
        GlobalScope.launch {
            reminderDao!!.invalidateReminder(saleItemReminderUid)
        }
    }

    fun handleClickSave() {


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
                    remindersLive.observeWithPresenter(thisP, thisP::handleReminderLive)
                })
            }
            view.finish()
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
    }

    fun handleAddReminder(days: Int) {
        val reminder = SaleItemReminder(days, saleItemUid, true)
        val reminderDao = repository.saleItemReminderDao
        GlobalScope.launch {
            val result =
                    reminderDao.findBySaleItemUidAndDaysAsync(saleItemUid, days)
            if (result.size > 0) {
                //It has it already. Skipp it.
            } else {
                reminderDao.insertAsync(reminder)
            }
        }
    }
}
