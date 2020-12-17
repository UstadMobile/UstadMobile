package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.SaleEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleWithCustomerAndLocation
import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.lib.db.entities.SaleItemWithProduct
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SaleDeliveryAndItems
import com.ustadmobile.lib.db.entities.SalePayment
import com.ustadmobile.lib.db.entities.SalePaymentWithSaleItems
import com.ustadmobile.lib.db.entities.PersonWithInventoryItemAndStock
import com.ustadmobile.lib.db.entities.InventoryItem
import kotlinx.coroutines.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import kotlinx.serialization.builtins.list

class SaleEditPresenter(context: Any,
                        arguments: Map<String, String>, view: SaleEditView, di: DI,
                        lifecycleOwner: DoorLifecycleOwner )
    : UstadEditPresenter<SaleEditView, SaleWithCustomerAndLocation>(context, arguments,
        view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    //Sale Item edit helper
    private val saleItemEditHelper = DefaultOneToManyJoinEditHelper<SaleItemWithProduct>(
            SaleItemWithProduct::saleItemUid,
            "SaleItem", SaleItemWithProduct.serializer().list,
            SaleItemWithProduct.serializer().list, this, SaleItemWithProduct::class) {
                saleItemUid = it }

    fun handleAddOrEditSaleItem(saleItem: SaleItemWithProduct) {
        saleItemEditHelper.onEditResult(saleItem)
    }

    fun handleRemoveSaleItem(saleItem: SaleItemWithProduct) {
        saleItemEditHelper.onDeactivateEntity(saleItem)
    }

    //Sale Delivery edit helper
    private val saleDeliveryEditHelper = DefaultOneToManyJoinEditHelper<SaleDeliveryAndItems>(
            SaleDeliveryAndItems::saleDeliveryUid,
            "SaleDeliveryAndItems", SaleDeliveryAndItems.serializer().list,
            SaleDeliveryAndItems.serializer().list, this, SaleDeliveryAndItems::class) {
                saleDeliveryUid = it }

    fun handleAddOrEditSaleDelivery(saleDelivery: SaleDeliveryAndItems) {
        saleDeliveryEditHelper.onEditResult(saleDelivery)
    }

    fun handleRemoveSaleDelivery(saleDelivery: SaleDeliveryAndItems) {
        saleDeliveryEditHelper.onDeactivateEntity(saleDelivery)
    }


    //Sale Payment edit helper
    private val salePaymentEditHelper = DefaultOneToManyJoinEditHelper<SalePaymentWithSaleItems>(
            SalePaymentWithSaleItems::salePaymentUid ,
            "SalePaymentWithSaleItems", SalePaymentWithSaleItems.serializer().list,
            SalePaymentWithSaleItems.serializer().list, this,
                SalePaymentWithSaleItems::class) { salePaymentUid = it }

    fun handleAddOrEditSalePayment(salePayment: SalePaymentWithSaleItems) {
        salePaymentEditHelper.onEditResult(salePayment)
    }

    fun handleRemoveSalePayment(salePayment: SalePaymentWithSaleItems) {
        salePaymentEditHelper.onDeactivateEntity(salePayment)
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.saleItemList = saleItemEditHelper.liveList
        view.saleDeliveryList = saleDeliveryEditHelper.liveList
        view.salePaymentList = salePaymentEditHelper.liveList
    }


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): SaleWithCustomerAndLocation? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val sale = withTimeout(2000){
            db.saleDao.findWithCustomerAndLocationByUidAsync(entityUid)
        }?:SaleWithCustomerAndLocation()

        val saleItemList = withTimeout(2000){
            db.saleItemDao.findAllBySaleListAsList(entityUid)
        }
        saleItemEditHelper.liveList.sendValue(saleItemList)

        val saleDeliveryList = withTimeout(2000){
            db.saleDeliveryDao.findAllBySaleAsList(entityUid)
        }

        val saleDeliveryWithItems = mutableListOf<SaleDeliveryAndItems>()
        for(eachDelivery in saleDeliveryList){

            var saleDeliveryAndItems = SaleDeliveryAndItems()
            saleDeliveryAndItems.createWithDelivery(eachDelivery)

            saleDeliveryWithItems.add(saleDeliveryAndItems)
        }
        saleDeliveryEditHelper.liveList.sendValue(saleDeliveryWithItems)

        val salePaymentList = withTimeout(2000){
            db.salePaymentDao.findAllBySaleAsList(entityUid)
        }
        var salePaymentWithItems = mutableListOf<SalePaymentWithSaleItems>()
        for(eachPayment in salePaymentList){

            var salePaymentAndItems = SalePaymentWithSaleItems()
            salePaymentAndItems.createWithPayment(eachPayment)

            salePaymentWithItems.add(salePaymentAndItems)
        }
        salePaymentEditHelper.liveList.sendValue(salePaymentWithItems)

        val totalCount = withTimeout(2000) {
            db.saleItemDao.findTotalBySale(entityUid)
        }?:0L

        val paymentTotal = withTimeout(2000){
            db.salePaymentDao.findTotalBySale(entityUid)
        }?:0L

        view.runOnUiThread(Runnable {
            view.orderTotal = totalCount
            view.paymentTotal = paymentTotal
        })

        return sale

    }

    override fun onLoadFromJson(bundle: Map<String, String>): SaleWithCustomerAndLocation? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: SaleWithCustomerAndLocation? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, SaleWithCustomerAndLocation.serializer(), entityJsonStr)
        }else {
            editEntity = SaleWithCustomerAndLocation()
        }

        saleDeliveryEditHelper.onLoadFromJsonSavedState(bundle)
        salePaymentEditHelper.onLoadFromJsonSavedState(bundle)


        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: SaleWithCustomerAndLocation) {
        GlobalScope.launch(doorMainDispatcher()) {
            val loggedInPersonUid = accountManager.activeAccount.personUid
            entity?.salePersonUid = accountManager.activeAccount.personUid
            entity?.saleLastUpdateDate = UMCalendarUtil.getDateInMilliPlusDays(0)

            if(entity.saleUid == 0L) {
                entity?.saleCreationDate = UMCalendarUtil.getDateInMilliPlusDays(0)
                entity.saleUid = repo.saleDao.insertAsync(entity)
            }else {
                repo.saleDao.updateAsync(entity)
            }

            val saleItemsToInsert = saleItemEditHelper.entitiesToInsert
            val saleItemstoDelete = saleItemEditHelper.primaryKeysToDeactivate
            val saleItemstoUpdate = saleItemEditHelper.entitiesToUpdate

            saleItemsToInsert.forEach {
                it.saleItemSaleUid = entity.saleUid
                it.saleItemUid = 0L
                it.saleItemUid = repo.saleItemDao.insertAsync(it)
            }
            saleItemstoUpdate.forEach {
                it.saleItemSaleUid = entity.saleUid
            }
            repo.saleItemDao.updateListAsync(saleItemstoUpdate)
            repo.saleItemDao.deactivateByUids(saleItemstoDelete)


            val deliveriesToInsert = saleDeliveryEditHelper.entitiesToInsert
            val deliveriesToUpdate = saleDeliveryEditHelper.entitiesToUpdate
            val deliveriesToDelete = saleDeliveryEditHelper.primaryKeysToDeactivate

            deliveriesToUpdate.forEach {
                it.saleDeliverySaleUid = entity.saleUid
                if(it.saleDeliveryUid == 0L) {
                    it.saleDeliveryUid = repo.saleDeliveryDao.insertAsync(it)
                }

                val saleDeliveryUid = it.saleDeliveryUid
                //1. get the amount and product
                var productCountSelected = 0
                for(producerSelection in it.deliveryDetails){
                    for(everyTransaction in producerSelection?.transactions?:
                                emptyList<PersonWithInventoryItemAndStock>()){
                        productCountSelected += everyTransaction.selectedStock
                    }
                }

                for(producerSelection in it.deliveryDetails) {
                    //2. Update productCountSelected InventoryItems with the deliveryUid and saleUid
                    val productUid = producerSelection.productUid
                    for(everyTransaction in producerSelection?.transactions?:
                                emptyList<PersonWithInventoryItemAndStock>()) {
                        InventoryItem().apply{
                            inventoryItemProductUid = productUid
                            inventoryItemLeUid = loggedInPersonUid
                            inventoryItemWeUid = everyTransaction.personUid
                            inventoryItemDateAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                            inventoryItemSaleUid = entity.saleUid
                            inventoryItemSaleDeliveryUid = saleDeliveryUid
                            inventoryItemQuantity = everyTransaction.selectedStock * -1L
                            inventoryItemUid = repo.inventoryItemDao.insertAsync(this)
                        }
                    }
                }
            }

            var deliveriesToUpdateDeliveriesOnly = mutableListOf<SaleDelivery>()
            for(everyDelivery in deliveriesToUpdate){
                deliveriesToUpdateDeliveriesOnly.add(everyDelivery)
            }
            repo.saleDeliveryDao.updateListAsync(deliveriesToUpdateDeliveriesOnly)
            repo.saleDeliveryDao.deactivateByUids(deliveriesToDelete)


            val paymentsToInsert = salePaymentEditHelper.entitiesToInsert
            val paymentsToUpdate = salePaymentEditHelper.entitiesToUpdate
            val paymentsToDelete = salePaymentEditHelper.primaryKeysToDeactivate

            val paymentsToUpdatePaymentsOnly = mutableListOf<SalePayment>()
            for(everyPayment in paymentsToUpdate){
                paymentsToUpdatePaymentsOnly.add(everyPayment)
            }
            val paymentsToInsertPaymentsOnly = mutableListOf<SalePayment>()
            for(everyPayment in paymentsToInsert){
                paymentsToUpdatePaymentsOnly.add(everyPayment)
            }
            paymentsToInsertPaymentsOnly.forEach {
                it.salePaymentSaleUid = entity.saleUid
                it.salePaymentUid = repo.salePaymentDao.insertAsync(it)
            }
            paymentsToUpdatePaymentsOnly.forEach {
                it.salePaymentSaleUid = entity.saleUid
                if(it.salePaymentUid == 0L) {
                    it.salePaymentUid = repo.salePaymentDao.insertAsync(it)
                }
            }
            repo.salePaymentDao.updateListAsync(paymentsToUpdatePaymentsOnly)
            repo.salePaymentDao.deactivateByUids(paymentsToDelete)


            view.finishWithResult(listOf(entity))

        }
    }

}