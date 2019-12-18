package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.InventoryItemDao
import com.ustadmobile.core.db.dao.InventoryItemDao.Companion.SORT_ORDER_LEAST_RECENT
import com.ustadmobile.core.db.dao.InventoryItemDao.Companion.SORT_ORDER_MOST_RECENT
import com.ustadmobile.core.db.dao.InventoryItemDao.Companion.SORT_ORDER_NAME_ASC
import com.ustadmobile.core.db.dao.InventoryItemDao.Companion.SORT_ORDER_NAME_DESC
import com.ustadmobile.core.db.dao.InventoryItemDao.Companion.SORT_ORDER_STOCK_ASC
import com.ustadmobile.core.db.dao.InventoryItemDao.Companion.SORT_ORDER_STOCK_DESC
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.InventoryDetailView
import com.ustadmobile.core.view.InventoryDetailView.Companion.ARG_INVENTORY_DETAIL_SALE_PRODUCT_UID
import com.ustadmobile.core.view.InventoryListView
import com.ustadmobile.core.view.SelectProducersView.Companion.ARG_SELECT_PRODUCERS_INVENTORY_ADDITION
import com.ustadmobile.core.view.SelectSaleProductView
import com.ustadmobile.core.view.SelectSaleProductView.Companion.ARG_INVENTORY_MODE
import com.ustadmobile.lib.db.entities.SaleProductWithInventoryCount

/**
 *  Presenter for InventoryListPresenter view
 **/
class InventoryListPresenter(context: Any,
                             arguments: Map<String, String>?,
                             view: InventoryListView,
                             val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                             private val repository: UmAppDatabase =
                                     UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<InventoryListView>(context, arguments!!, view) {


    private var idToOrderInteger: MutableMap<Long, Int>? = null

    private var currentSortOrder = 0

    private var rvDao: InventoryItemDao = repository.inventoryItemDao

    private lateinit var factory: DataSource.Factory<Int, SaleProductWithInventoryCount>

    var searchQuery: String = "%%"

    var loggedInPersonUid : Long = 0L
    fun setQuerySearch(query:String){
        searchQuery = "%$query%"
    }

    init {
        //Initialise Daos, etc here.
        loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        idToOrderInteger = HashMap()
        //Update sort presets
        updateSortSpinnerPreset()
        getAndSetProvider(currentSortOrder)
    }


    fun handleSortChanged(order: Long) {
        var order = order
        order = order + 1

        if (idToOrderInteger!!.containsKey(order)) {
            currentSortOrder = idToOrderInteger!![order]!!
            getAndSetProvider(currentSortOrder)
        }
    }

    private fun getAndSetProvider(sortCode: Int) {

        factory = rvDao.findAllInventoryByProduct(loggedInPersonUid, searchQuery, sortCode)
        view.setListProvider(factory)
    }

    /**
     * Updates the sort by drop down (spinner) on the Class list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private fun updateSortSpinnerPreset() {
        val presetAL = ArrayList<String>()
        val impl = UstadMobileSystemImpl.instance

        idToOrderInteger = HashMap()

        presetAL.add(impl.getString(MessageID.sort_by_name_asc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_NAME_ASC)

        presetAL.add(impl.getString(MessageID.sort_by_name_desc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_NAME_DESC)

        presetAL.add(impl.getString(MessageID.sort_stock_asc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_STOCK_ASC)

        presetAL.add(impl.getString(MessageID.sort_stock_desc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_STOCK_DESC)

        presetAL.add(impl.getString(MessageID.sort_most_recent, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_MOST_RECENT)

        presetAL.add(impl.getString(MessageID.sort_lease_recent, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_LEAST_RECENT)

        val sortPresets = SaleListPresenter.arrayListToStringArray(presetAL)

        view.updateSortSpinner(sortPresets)
    }

    fun updateProviders(){
        getAndSetProvider(currentSortOrder)
    }


    fun handleClickSaleProductInventory(saleProductUid: Long){
        val args = HashMap<String, String>()
        args.put(ARG_INVENTORY_DETAIL_SALE_PRODUCT_UID, saleProductUid.toString())
        impl.go(InventoryDetailView.VIEW_NAME, args, context)
    }

    fun handleClickAddItems(){
        val args = HashMap<String, String>()
        args.put(ARG_INVENTORY_MODE, "true")
        args.put(ARG_SELECT_PRODUCERS_INVENTORY_ADDITION, "true")
        impl.go(SelectSaleProductView.VIEW_NAME, args, context)
    }

}
