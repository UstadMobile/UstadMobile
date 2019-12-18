package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.InventoryItemDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.dao.InventoryTransactionDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.InventoryDetailView
import com.ustadmobile.core.view.InventoryDetailView.Companion.ARG_INVENTORY_DETAIL_SALE_PRODUCT_UID
import com.ustadmobile.core.view.SaleDetailView
import com.ustadmobile.lib.db.entities.InventoryTransactionDetail
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.log

/**
 *  Presenter for MyWomenEntrepreneurs view
 **/
class InventoryDetailPresenter(context: Any,
                               arguments: Map<String, String>?,
                               view: InventoryDetailView,
                               val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                               private val repository: UmAppDatabase =
                                       UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<InventoryDetailView>(context, arguments!!, view) {


    private var currentSortOrder = 0

    private lateinit var rvDao: InventoryTransactionDao
    private lateinit var inventoryDao: InventoryItemDao

    private var saleProductUid: Long = 0

    private lateinit var factory: DataSource.Factory<Int, InventoryTransactionDetail>

    val loggedInPersonUid : Long

    init {
        //Initialise Daos, etc here.
        rvDao = repository.inventoryTransactionDao

        inventoryDao = repository.inventoryItemDao

        if (arguments!!.containsKey(ARG_INVENTORY_DETAIL_SALE_PRODUCT_UID)) {
            saleProductUid = (arguments.get(ARG_INVENTORY_DETAIL_SALE_PRODUCT_UID)!!.toLong())
        } else {
            //Create a new SaleItem? - shouldn't happen.
            //throw exception.
        }

        loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        getAndSetProvider(currentSortOrder)
    }


    private fun getAndSetProvider(sortCode: Int) {
        factory = rvDao.findAllInventoryByProduct(saleProductUid, loggedInPersonUid)
        view.setListProvider(factory)

        //Image
        GlobalScope.launch {
            val saleProductPicture =
                    repository.saleProductPictureDao.findBySaleProductUidAsync2(saleProductUid)
            if(saleProductPicture != null){
                val picPath = repository.saleProductPictureDao.getAttachmentPath(saleProductPicture)
                view.updateImageOnView(picPath!!, true)
            }

            //Title
            val saleProduct = repository.saleProductDao.findByUidAsync(saleProductUid)
            view.updateToolbar(saleProduct!!.getNameLocale(impl.getLocale(context)))
        }


        GlobalScope.launch {
            val remaining = inventoryDao.findStockForSaleProduct(saleProductUid,
                    loggedInPersonUid)
            view.updateTotalInventoryCount(remaining)
        }

    }

    fun handleClickSaleTransaction(saleUid:Long){
        val args = HashMap<String, String>()
        args.put(SaleDetailView.ARG_SALE_UID, saleUid.toString())
        impl.go(SaleDetailView.VIEW_NAME, args, context)
    }

    fun handleClickInventoryTransaction(leUid: Long, date: Long){
        //TODO : this
    }

    companion object{

    }

}
