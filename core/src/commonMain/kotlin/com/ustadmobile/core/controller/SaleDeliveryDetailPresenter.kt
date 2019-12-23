package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.SaleDeliveryDetailView
import com.ustadmobile.core.view.SaleDeliveryDetailView.Companion.ARG_SALE_DELIVERY_SALE_UID
import com.ustadmobile.core.view.SaleDeliveryDetailView.Companion.ARG_SALE_DELIVERY_UID
import com.ustadmobile.lib.db.entities.PersonWithInventory
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SaleItemListDetail
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 *  Presenter for SaleDeliveryDetail view
 **/
class SaleDeliveryDetailPresenter(context: Any,
                                  arguments: Map<String, String>?,
                                  view: SaleDeliveryDetailView,
                                  val systemImpl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                                  private val repository: UmAppDatabase =
                                          UmAccountManager.getRepositoryForActiveAccount(context))
    : CommonInventorySelectionPresenter<SaleDeliveryDetailView>(context, arguments!!, view) {


    private var saleUid : Long = 0
    private var saleDeliveryUid : Long = 0
    private lateinit var saleDelivery: SaleDelivery
    private var currentSignSvg: String? = null

    internal var saleItemToWeCounter: HashMap<Long, HashMap<Long, Int>>
    var saleItemToProducerSelection : HashMap<SaleItemListDetail, List<PersonWithInventory>>

    val inventoryTransactionDao = repository.inventoryTransactionDao
    val inventoryItemDao = repository.inventoryItemDao
    val saleDeliveryDao = repository.saleDeliveryDao
    val saleItemDao = repository.saleItemDao
    val saleDao = repository.saleDao

    init {
        //Initialise Daos, etc here.
        saleItemToWeCounter = HashMap()
        saleItemToProducerSelection = HashMap()
    }



    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if(arguments.containsKey(ARG_SALE_DELIVERY_SALE_UID)){
            saleUid = arguments[ARG_SALE_DELIVERY_SALE_UID].toString().toLong()
        }

        if(arguments.containsKey(ARG_SALE_DELIVERY_UID)){
            saleDeliveryUid = arguments[ARG_SALE_DELIVERY_UID].toString().toLong()

            GlobalScope.launch {
                saleDelivery = saleDeliveryDao.findByUidAsync(saleDeliveryUid)!!
                if(saleDelivery == null){
                    saleDelivery = SaleDelivery()
                    saleDelivery.saleDeliveryDate = UMCalendarUtil.getDateInMilliPlusDays(0)
                    saleDelivery.saleDeliverySaleUid = saleUid
                    saleDelivery.saleDeliveryActive = false
                    saleDelivery.saleDeliveryUid = saleDeliveryDao.insertAsync(saleDelivery)
                }
                initFromSaleDelivery()
            }
        }else {
            saleDelivery = SaleDelivery()
            saleDelivery.saleDeliverySaleUid = saleUid
            saleDelivery.saleDeliveryActive = false
            saleDelivery.saleDeliveryDate = UMCalendarUtil.getDateInMilliPlusDays(0)

            GlobalScope.launch {
                saleDelivery.saleDeliveryUid = saleDeliveryDao.insertAsync(saleDelivery)
                initFromSaleDelivery()
            }
        }

    }

    private fun initFromSaleDelivery(){
        view.runOnUiThread(Runnable {
            view.updateSaleDeliveryOnView(saleDelivery)
        })

        val loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
        GlobalScope.launch {
            val saleItems = saleItemDao.findAllSaleItemListDetailActiveBySaleList(saleUid)

            //Get all producer selection for this salItem
            val saleItemsWithProducers : HashMap<SaleItemListDetail, List<PersonWithInventory>>
            saleItemsWithProducers = HashMap()

            for (item in saleItems){
                //Get producers
                val producers : List<PersonWithInventory>

                val saleProductUid = item.saleItemProductUid
                val saleUid = item.saleItemSaleUid
                val saleItemUid = item.saleItemUid
                producers = inventoryItemDao.findStockBySaleItemAndSale(
                        saleUid, saleItemUid, saleProductUid, loggedInPersonUid)
                saleItemsWithProducers.put(item, producers)
            }

            view.runOnUiThread(Runnable {
                view.setUpAllViews(saleItemsWithProducers)

            })
        }
    }


    override fun updateWeCount(weUid: Long, count: Int, saleItemUid: Long){
        if(saleItemToWeCounter.containsKey(saleItemUid)){
            val existing = saleItemToWeCounter[saleItemUid]
            existing!!.put(weUid, count)
        }else{
            val newMap = HashMap<Long, Int>()
            newMap.put(weUid, count)
            saleItemToWeCounter.put(saleItemUid, newMap)
        }

    }

    private fun calculateTotalCountForSaleItem(saleItemUid: Long):Int{
        var count = 0

        if(saleItemToWeCounter.containsKey(saleItemUid)) {
            val weToCount = saleItemToWeCounter[saleItemUid]
            for (weUid in weToCount!!.keys) {
                count += weToCount[weUid]!!
            }

        }
        return count
    }

    fun handleClickAccept(){


        //1. Save the signature
        saveSignature()
        //2. Loop over inventory assignment and create transactions if needed and also assign deliveries
        //TODO

        //3. Persist and close
        GlobalScope.launch {
            saleDelivery.saleDeliveryActive = true
            saleDeliveryDao.updateAsync(saleDelivery!!)
            view.finish()
        }
    }

    fun saveSignature() {
        if (saleDelivery != null) {
            if (currentSignSvg != null && !currentSignSvg!!.isEmpty()) {
                saleDelivery!!.saleDeliverySignature = currentSignSvg!!
            }else{
                saleDelivery!!.saleDeliverySignature = ""
            }
        }
    }

    fun handleClickClear(){
        view.finish()
    }

    fun updateSignatureSvg(signSvg: String) {
        currentSignSvg = signSvg
    }




}
