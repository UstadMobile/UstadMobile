package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.putEntityAsJson
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


    val productWithDeliveriesEditHelper = DefaultOneToManyJoinEditHelper<ProductDeliveryWithProductAndTransactions>(
            ProductDeliveryWithProductAndTransactions::productUid,
            "ProductDeliveryWithProductAndTransactions", ProductDeliveryWithProductAndTransactions.serializer().list,
            ProductDeliveryWithProductAndTransactions.serializer().list, this) { productUid = it }

    fun handleAddOrEditSaleItemWithProduct(saleItemWithProduct: ProductDeliveryWithProductAndTransactions) {
        productWithDeliveriesEditHelper.onEditResult(saleItemWithProduct)
    }

    fun handleRemoveSchedule(saleItemWithProduct: ProductDeliveryWithProductAndTransactions) {
        productWithDeliveriesEditHelper.onDeactivateEntity(saleItemWithProduct)
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //view.productWithDeliveries = productWithDeliveriesEditHelper.liveList
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): SaleDeliveryAndItems? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val saleUid = arguments[UstadView.ARG_SALE_UID]?.toLong() ?: 0L


         val saleDelivery = withTimeoutOrNull(2000) {
             db.saleDeliveryDao.findByUidAsync(entityUid)
         } ?: SaleDelivery()

        val saleDeliveryAndItems = SaleDeliveryAndItems().apply{
            delivery = saleDelivery
        }

         return saleDeliveryAndItems

    }

    override fun onLoadFromJson(bundle: Map<String, String>): SaleDeliveryAndItems? {
        super.onLoadFromJson(bundle)

        val loggedInPersonUid = accountManager.activeAccount.personUid

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: SaleDeliveryAndItems? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(SaleDeliveryAndItems.serializer(), entityJsonStr)
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


            val allProductsWithProducers = mutableListOf<ProductDeliveryWithProductAndTransactions>()
            //Build product and we selection list ie ProductDeliveryWithProductAndTransactions
            for ((productUid, quantity) in quantityMap) {
                print("hehe")

                //4. Get transactions
                val producersTransactions = withTimeout(2000) {
                    db.inventoryItemDao.getStockAndDeliveryListByProduct(productUid,
                            loggedInPersonUid, editEntity?.delivery?.saleDeliveryUid ?: 0L)
                }

                val product = withTimeout(2000){
                    db.productDao.findByUidAsync(productUid)
                }

                //5. Create product with transactions
                val productWithWE = ProductDeliveryWithProductAndTransactions().apply {
                    saleDelivery = editEntity.delivery
                    items = quantity
                    transactions = producersTransactions
                    productName = product?.productName
                }
                allProductsWithProducers.add(productWithWE)

            }

            view.runOnUiThread(Runnable {
//                view.productWithDeliveries = DoorMutableLiveData(allProductsWithProducers)
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
        val saleUid = arguments[UstadView.ARG_SALE_UID]?.toLong() ?: 0L
        GlobalScope.launch(doorMainDispatcher()) {
//            if(entity.saleDeliveryUid == 0L) {
//                entity.saleDeliveryDate = UMCalendarUtil.getDateInMilliPlusDays(0)
//                entity.saleDeliveryPersonUid = accountManager.activeAccount.personUid
//                entity.saleDeliverySaleUid = saleUid
//                entity.saleDeliveryUid = repo.saleDeliveryDao.insertAsync(entity)
//            }else {
//                repo.saleDeliveryDao.updateAsync(entity)
//            }
//
//            //TODO: Persist inventory selection

            view.finishWithResult(listOf(entity))
        }
    }


}