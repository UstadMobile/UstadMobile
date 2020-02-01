package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.db.dao.SaleProductDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.NewInventoryItemView
import com.ustadmobile.core.view.SaleProductDetailView
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_ADD_INVENTORY_POST_SAVE
import com.ustadmobile.core.view.SelectProducersView
import com.ustadmobile.lib.db.entities.SaleProduct
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 *  Presenter for New InventoryItem  view
 **/
class NewInventoryItemPresenter(context: Any,
                                arguments: Map<String, String>?,
                                view: NewInventoryItemView,
                                val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                                private val repository: UmAppDatabase =
                                        UmAccountManager.getRepositoryForActiveAccount(context))
    : CommonHandlerPresenter<NewInventoryItemView>(context, arguments!!, view) {
    override fun handleCommonPressed(arg: Any, arg2: Any) {
        handleClickProduct(arg as Long)
    }

    override fun handleSecondaryPressed(arg: Any) {

    }

    fun handleClickNewProduct(){
        //Create the product then go
        val args = HashMap<String, String>()

        GlobalScope.launch {
            val currentSaleProduct = SaleProduct("", "", false, false)
            currentSaleProduct.saleProductDateAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
            currentSaleProduct.saleProductPersonAdded = loggedInPersonUid

            currentSaleProduct.saleProductUid = saleProductDao.insertAsync(currentSaleProduct!!)
            val productUid = currentSaleProduct.saleProductUid

            val product = saleProductDao.findByUidAsync(currentSaleProduct.saleProductUid)
            view.runOnUiThread(Runnable {
                args.put(SaleProductDetailView.ARG_SALE_PRODUCT_UID, productUid.toString())
                args.put(ARG_ADD_INVENTORY_POST_SAVE, "true")
                impl.go(SaleProductDetailView.VIEW_NAME, args, context)
                view.finish()
            })

        }


    }


    fun handleClickProduct(productUid: Long) {
        //TODO: Go to Inventory select producers thingi
        val args = HashMap<String, String>()
        args.put(SelectProducersView.ARG_SELECT_PRODUCERS_INVENTORY_ADDITION, "true")
        args.put(SelectProducersView.ARG_SELECT_PRODUCERS_SALE_PRODUCT_UID,
                productUid.toString())
        impl.go(SelectProducersView.VIEW_NAME, args, context)
        view.finish()

    }

    private var idToOrderInteger: MutableMap<Long, Int>? = null

    private var currentSortOrder = 0

    var searchQuery: String = "%%"

    private var saleProductDao: SaleProductDao

    var loggedInPersonUid : Long = 0

    init {
        //Initialise Daos, etc here.
        saleProductDao = repository.saleProductDao
        loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        idToOrderInteger = HashMap()
        //Update sort presets
        updateSortSpinnerPreset()

        getAndSetProvider(currentSortOrder)
    }

    fun handleClickAddNewSaleProduct(){
        //Create new, let it get to adding inventory post

    }


    /**
     * Upon clicking search -> should open up search experience.
     */
    fun handleSearchQuery(searchBit: String) {
        searchQuery = "%" + searchBit + "%"
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
        val factory = saleProductDao.findActiveProductsProvider(loggedInPersonUid,
                searchQuery)
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
        idToOrderInteger!!.put(presetAL.size.toLong(), SaleDao.SORT_ORDER_NAME_ASC)
        presetAL.add(impl.getString(MessageID.sorT_by_name_desc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SaleDao.SORT_ORDER_NAME_DESC)
        presetAL.add(impl.getString(MessageID.sale_list_sort_by_total_asc, context))

        val sortPresets = SaleListPresenter.arrayListToStringArray(presetAL)

        view.updateSortSpinner(sortPresets)
    }

}
