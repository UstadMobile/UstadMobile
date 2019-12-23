package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.InventoryItemDao
import com.ustadmobile.core.db.dao.InventoryTransactionDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallbackUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.SaleItemDetailView
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_DETAIL_FROM_INVENTORY
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_UID
import com.ustadmobile.core.view.SelectProducerView
import com.ustadmobile.core.view.SelectProducersView
import com.ustadmobile.core.view.SelectProducersView.Companion.ARG_SELECT_PRODUCERS_INVENTORY_ADDITION
import com.ustadmobile.core.view.SelectProducersView.Companion.ARG_SELECT_PRODUCERS_INVENTORY_SELECTION
import com.ustadmobile.core.view.SelectProducersView.Companion.ARG_SELECT_PRODUCERS_SALE_ITEM_UID
import com.ustadmobile.core.view.SelectProducersView.Companion.ARG_SELECT_PRODUCERS_SALE_PRODUCT_UID
import com.ustadmobile.core.view.SelectProducersView.Companion.ARG_SELECT_PRODUCERS_SALE_UID
import com.ustadmobile.lib.db.entities.InventoryItem
import com.ustadmobile.lib.db.entities.InventoryTransaction
import com.ustadmobile.lib.db.entities.SaleItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * Presenter for SelectProducer view
 */
class SelectProducersPresenter(context: Any,
                               arguments: Map<String, String>?,
                               view: SelectProducersView)
    : CommonInventorySelectionPresenter<SelectProducersView>(context, arguments!!, view) {


    internal var repository: UmAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context)
    private val providerDao: InventoryItemDao
    private val transactionDao: InventoryTransactionDao
    private var saleItemUid: Long = 0L
    private var saleUid: Long = 0L
    private var saleProductUid: Long = 0L

    private var idToOrderInteger: HashMap<Long, Int>? = null

    internal var weToCount: HashMap<Long, Int>

    var inventoryAddition = false

    val loggedInPersonUid : Long


    init {

        //Get provider Dao
        providerDao = repository.inventoryItemDao
        transactionDao = repository.inventoryTransactionDao

        if (arguments!!.containsKey(ARG_SELECT_PRODUCERS_SALE_ITEM_UID)) {
            saleItemUid = (arguments.get(ARG_SELECT_PRODUCERS_SALE_ITEM_UID)!!.toLong())
        }

        if (arguments.containsKey(ARG_SELECT_PRODUCERS_SALE_PRODUCT_UID)) {
            saleProductUid = (arguments.get(ARG_SELECT_PRODUCERS_SALE_PRODUCT_UID)!!.toLong())
        }

        if (arguments.containsKey(ARG_SELECT_PRODUCERS_SALE_UID)) {
            saleUid = (arguments.get(ARG_SELECT_PRODUCERS_SALE_UID)!!.toLong())
        }

        if (arguments.containsKey(ARG_SELECT_PRODUCERS_INVENTORY_ADDITION)) {
            val addition = (arguments.get(ARG_SELECT_PRODUCERS_INVENTORY_ADDITION)!!.toString())
            if(addition.toLowerCase().equals("true")){
                inventoryAddition = true
            }
        }

        if (arguments.containsKey(ARG_SELECT_PRODUCERS_INVENTORY_SELECTION)) {
            val selection = arguments.getOrElse(ARG_SELECT_PRODUCERS_INVENTORY_SELECTION)
                { "false" }.toString()
            if(selection.toLowerCase().equals("true")){
                inventorySelection = true
            }
        }

        loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
        weToCount = HashMap()

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        idToOrderInteger = HashMap<Long, Int>()
        updateSortSpinnerPreset()
    }

    fun getAndSetProvider(sortCode: Int) {
        GlobalScope.launch {
            when (sortCode) {
                SORT_ORDER_NAME_ASC -> {
                    val peopleWithInventory = providerDao.findStockByPersonAsc(saleProductUid,
                            loggedInPersonUid)
                    view.runOnUiThread(Runnable {
                        view.updateProducersOnView(peopleWithInventory)
                    })
                }
                SORT_ORDER_NAME_DESC -> {
                    val peopleWithInventory = providerDao.findStockByPersonAsc(saleProductUid,
                            loggedInPersonUid)
                    view.runOnUiThread(Runnable {
                        view.updateProducersOnView(peopleWithInventory)
                    })
                }
                SORT_ORDER_STOCK_ASC -> {
                    val peopleWithInventory = providerDao.findStockByPersonAsc(saleProductUid,
                            loggedInPersonUid)
                    view.runOnUiThread(Runnable {
                        view.updateProducersOnView(peopleWithInventory)
                    })
                }
                SORT_ORDER_STOCK_DESC -> {
                    val peopleWithInventory = providerDao.findStockByPersonAsc(saleProductUid,
                            loggedInPersonUid)
                    view.runOnUiThread(Runnable {
                        view.updateProducersOnView(peopleWithInventory)
                    })
                }
                else -> {
                    val peopleWithInventory = providerDao.findStockByPersonAsc(saleProductUid,
                            loggedInPersonUid)
                    view.runOnUiThread(Runnable {
                        view.updateProducersOnView(peopleWithInventory)
                    })
                }
            }
        }
    }


    override fun updateWeCount(weUid: Long, count: Int, saleItemUid: Long){
        //Don't need to do anything with saleItemUid > usually 0 here. 
        weToCount.put(weUid, count)
    }

    private fun calculateTotalCount():Int{
        var count = 0

        for(weUid in weToCount.keys){
            count += weToCount[weUid]!!
        }

        return count
    }


    fun handleClickSave(){

        if(inventoryAddition) {
            handleClickSaveInventory()
        }
        if(inventorySelection){
            handleClickSelectInventory()
        }

    }

    private fun handleClickSaveInventory(){

        val addedDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
        val addedDate = UMCalendarUtil.getToday000000()
        GlobalScope.launch {
            for(weUid in weToCount.keys) {
            val count = weToCount[weUid]
                val newInventoryItem = InventoryItem(saleProductUid, loggedInPersonUid, weUid,
                        addedDateTime)
                newInventoryItem.inventoryItemDayAdded = addedDate
                providerDao.insertInventoryItem(newInventoryItem, count!!, loggedInPersonUid)
            }
            view.finish()
        }

    }

    private fun handleClickSelectInventory(){

        val saleItemDao = repository.saleItemDao
        val dateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
        val date = UMCalendarUtil.getToday000000()
        GlobalScope.launch {


            val totalCount = calculateTotalCount()
            var saleItem: SaleItem ? = null
            if(saleItemUid == 0L){
                saleItem = SaleItem()
                saleItem.saleItemCreationDate = UMCalendarUtil.getDateInMilliPlusDays(0)
                saleItem.saleItemUid = saleItemDao.insertAsync(saleItem)
                saleItemUid = saleItem.saleItemUid
            }else{
                saleItem = saleItemDao.findByUid(saleItemUid)
            }
            if(saleItem != null) {
                saleItem.saleItemSaleUid = saleUid
                saleItem.saleItemQuantity = totalCount
                saleItem.saleItemDueDate = 0
                saleItem.saleItemProductUid = saleProductUid
                // Multiple saleItem.saleItemProducerUid
                saleItem.saleItemActive = false
                saleItem.saleItemSold = true
                saleItem.saleItemPreorder = false

                saleItemDao.updateAsync(saleItem)
                saleItem.saleItemUid = saleItemUid
            }


            for (weUid in weToCount.keys) {
                val count = weToCount.get(weUid)!!

                // Get count number of unique InventoryItems and build Transactions for them.
                val availableItems = providerDao.findAvailableInventoryItemsByProductLimit(
                        saleProductUid, count, loggedInPersonUid, weUid)
                if(availableItems.count() != count){
                    //ERROR: We are asking for more than we have.
                    throw Exception("Asked for more than required")
                }
                for(item in availableItems){
                    val newInventoryTransaction = InventoryTransaction(item.inventoryItemUid,
                            loggedInPersonUid, saleUid, dateTime)
                    newInventoryTransaction.inventoryTransactionDay = date
                    newInventoryTransaction.inventoryTransactionActive = false
                    newInventoryTransaction.inventoryTransactionSaleItemUid = saleItemUid

                    transactionDao.insertAsync(newInventoryTransaction)
                }
            }


            val args = HashMap<String, String>()
            val impl = UstadMobileSystemImpl.instance
            args[SaleItemDetailView.ARG_SALE_ITEM_PRODUCT_UID] = saleProductUid.toString()
            args[SelectProducerView.ARG_PRODUCER_UID] = loggedInPersonUid.toString()
            args[ARG_SALE_ITEM_DETAIL_FROM_INVENTORY] = "true"
            args[ARG_SALE_ITEM_UID] = saleItemUid.toString()

            impl.go(SaleItemDetailView.VIEW_NAME, args, context)

            view.finish()
        }
    }


    /**
     * Updates the sort by drop down (spinner) on the Class list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private fun updateSortSpinnerPreset() {
        val presetAL = ArrayList<String>()
        val impl = UstadMobileSystemImpl.instance

        idToOrderInteger = HashMap<Long, Int>()

        presetAL.add(impl.getString(MessageID.sort_by_name_asc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_NAME_ASC)
        presetAL.add(impl.getString(MessageID.sorT_by_name_desc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_NAME_DESC)
        presetAL.add(impl.getString(MessageID.sort_by_inventory_asc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_STOCK_ASC)
        presetAL.add(impl.getString(MessageID.sort_by_inventory_desc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_STOCK_DESC)


        val sortPresets = arrayListToStringArray(presetAL)

        view.updateSpinner(sortPresets)
    }

    /**
     * Common method to convert Array List to String Array
     *
     * @param presetAL The array list of string type
     * @return  String array
     */
    private fun arrayListToStringArray(presetAL: ArrayList<String>): Array<String?> {
        val objectArr = presetAL.toTypedArray()
        val strArr = arrayOfNulls<String>(objectArr.size)
        for (j in objectArr.indices) {
            strArr[j] = objectArr[j]
        }
        return strArr
    }

    fun handleChangeSortOrder(order: Long) {
        var orderPlusOne = order
        orderPlusOne += 1

        if (idToOrderInteger!!.containsKey(orderPlusOne)) {
            val sortCode = idToOrderInteger!!.get(orderPlusOne)
            if (sortCode != null) {
                getAndSetProvider(sortCode)
            }
        }

    }

    companion object {
        private val SORT_ORDER_NAME_ASC = 1
        private val SORT_ORDER_NAME_DESC = 2
        private val SORT_ORDER_STOCK_ASC = 3
        private val SORT_ORDER_STOCK_DESC = 4


    }
}
