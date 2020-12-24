package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.SaleDeliveryEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SaleItemWithProduct
import com.ustadmobile.lib.db.entities.SaleDeliveryAndItems
import com.ustadmobile.lib.db.entities.ProductDeliveryWithProductAndTransactions
import kotlinx.serialization.builtins.list
import com.ustadmobile.door.DoorMutableLiveData
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI


class SaleDeliveryEditPresenter(context: Any,
                                arguments: Map<String, String>, view: SaleDeliveryEditView, di: DI,
                                lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<SaleDeliveryEditView, SaleDeliveryAndItems>(context, arguments, view, di,
        lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON


    override fun onLoadFromJson(bundle: Map<String, String>): SaleDeliveryAndItems? {
        super.onLoadFromJson(bundle)

        val loggedInPersonUid = accountManager.activeAccount.personUid

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: SaleDeliveryAndItems? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, SaleDeliveryAndItems.serializer(), entityJsonStr)
        }else {
            editEntity = SaleDeliveryAndItems()
        }

        //1. Get all saleItems from JSON
        val saleItems = editEntity.saleItems
        //2. get distinct product uids:
        val distinctSaleItemsByProduct = saleItems.distinctBy{it.saleItemProductUid}
        //3. Get counts for every product
        val quantityMap = distinctSaleItemsByProduct.map{
            product -> product.saleItemProductUid to saleItems.filter{
                it.saleItemProductUid == product.saleItemProductUid}.sumBy{it.saleItemQuantity}
        }.toMap()


        GlobalScope.launch {


            var allProductsWithProducers = mutableListOf<ProductDeliveryWithProductAndTransactions>()
            //Build product and we selection list ie ProductDeliveryWithProductAndTransactions
            for ((productUid, quantity) in quantityMap) {

                // If already in json, use that, else get it from DB
                if(!editEntity.deliveryDetails.isEmpty()){
                    allProductsWithProducers = editEntity.deliveryDetails.toMutableList()
                }else {

                    //4. Get transactions
                    var producersTransactions = withTimeout(2000) {
                        db.inventoryItemDao.getStockAndDeliveryListByProduct(productUid,
                                loggedInPersonUid, editEntity?.saleDeliveryUid ?: 0L)
                    }

                    val product = withTimeout(2000) {
                        db.productDao.findByUidAsync(productUid)
                    }

                    //5. Create product with transactions
                    val productWithWE = ProductDeliveryWithProductAndTransactions().apply {
                        numItemsExpected = quantity
                        transactions = producersTransactions
                        productName = product?.productName
                        this.productUid = product?.productUid ?: 0L
                    }
                    allProductsWithProducers.add(productWithWE)

                }
            }

            view.runOnUiThread(Runnable {
                view.productWithDeliveriesList = allProductsWithProducers
            })
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: SaleDeliveryAndItems) {
        val deliveryDetailsOnView = view.productWithDeliveriesList

        //Do validation
        var validated: Boolean = true
        for(product in deliveryDetailsOnView){
            val totalSelectedStock = product.transactions?.sumBy{it.selectedStock}
            if(totalSelectedStock?:0 > product.numItemsExpected){
                //Alert
                view.runOnUiThread(Runnable {
                    view.showSnackBar(systemImpl.getString(
                            MessageID.selected_more_than_required, context))
                    validated = false
                })
            }
        }

        if(validated) {
            entity.deliveryDetails = deliveryDetailsOnView
            entity.saleDeliveryDate = UMCalendarUtil.getDateInMilliPlusDays(0)
            view.finishWithResult(listOf(entity))
        }

    }


}