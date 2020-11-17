package com.ustadmobile.core.controller

import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.SaleEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleWithCustomerAndLocation
import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.lib.db.entities.SaleItemWithProduct
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SalePayment
import kotlinx.serialization.builtins.list
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI


class SaleEditPresenter(context: Any,
                        arguments: Map<String, String>, view: SaleEditView, di: DI,
                        lifecycleOwner: DoorLifecycleOwner )
    : UstadEditPresenter<SaleEditView, SaleWithCustomerAndLocation>(context, arguments,
        view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    //Sale Item edit helper
    val saleItemEditHelper = DefaultOneToManyJoinEditHelper<SaleItemWithProduct>(SaleItemWithProduct::saleItemUid,
            "SaleItem", SaleItemWithProduct.serializer().list,
            SaleItemWithProduct.serializer().list, this) { saleItemUid = it }

    fun handleAddOrEditSaleItem(saleItem: SaleItemWithProduct) {
        saleItemEditHelper.onEditResult(saleItem)
    }

    fun handleRemoveSaleItem(saleItem: SaleItemWithProduct) {
        saleItemEditHelper.onDeactivateEntity(saleItem)
    }

    //Sale Delivery edit helper
    val saleDeliveryEditHelper = DefaultOneToManyJoinEditHelper<SaleDelivery>(SaleDelivery::saleDeliveryUid,
            "SaleDelivery", SaleDelivery.serializer().list,
            SaleDelivery.serializer().list, this) { saleDeliveryUid = it }

    fun handleAddOrEditSaleDelivery(saleDelivery: SaleDelivery) {
        saleDeliveryEditHelper.onEditResult(saleDelivery)
    }

    fun handleRemoveSaleDelivery(saleDelivery: SaleDelivery) {
        saleDeliveryEditHelper.onDeactivateEntity(saleDelivery)
    }


    //Sale Payment edit helper
    val salePaymentEditHelper = DefaultOneToManyJoinEditHelper<SalePayment>(SalePayment::salePaymentUid,
            "SalePayment", SalePayment.serializer().list,
            SalePayment.serializer().list, this) { salePaymentUid = it }

    fun handleAddOrEditSalePayment(salePayment: SalePayment) {
        salePaymentEditHelper.onEditResult(salePayment)
    }

    fun handleRemoveSalePayment(salePayment: SalePayment) {
        salePaymentEditHelper.onDeactivateEntity(salePayment)
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

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
        saleDeliveryEditHelper.liveList.sendValue(saleDeliveryList)

        val salePaymentList = withTimeout(2000){
            db.salePaymentDao.findAllBySaleAsList(entityUid)
        }
        salePaymentEditHelper.liveList.sendValue(salePaymentList)

        //TODO: Undo fix
        val totalCountLive = withTimeout(2000){
            db.saleItemDao.findTotalBySaleLive(entityUid)
        }
        view.totalAmountLive = totalCountLive


//        view.runOnUiThread(Runnable {
//            val totalCount =  db.saleItemDao.findTotalBySale(entityUid)
//            view.totalAmount = totalCount
//        })


        return sale

    }

    override fun onLoadFromJson(bundle: Map<String, String>): SaleWithCustomerAndLocation? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: SaleWithCustomerAndLocation? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(SaleWithCustomerAndLocation.serializer(), entityJsonStr)
        }else {
            editEntity = SaleWithCustomerAndLocation()
        }

        view.salePaymentList = salePaymentEditHelper.liveList
        view.saleDeliveryList = saleDeliveryEditHelper.liveList
        view.salePaymentList = salePaymentEditHelper.liveList

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
            if(entity.saleUid == 0L) {
                entity.saleUid = repo.saleDao.insertAsync(entity)
            }else {
                repo.saleDao.updateAsync(entity)
            }

            val saleItemsToInsert = saleItemEditHelper.entitiesToInsert
            val saleItemstoDelete = saleItemEditHelper.primaryKeysToDeactivate
            val saleItemstoUpdate = saleItemEditHelper.entitiesToUpdate

            saleItemsToInsert.forEach {
                it.saleItemSaleUid = entity.saleUid
                it.saleItemUid = repo.saleItemDao.insertAsync(it)
            }
            repo.saleItemDao.updateListAsync(saleItemstoUpdate)
            repo.saleItemDao.deactivateByUids(saleItemstoDelete)


            val deliveriesToInsert = saleDeliveryEditHelper.entitiesToInsert
            val deliveriesToUpdate = saleDeliveryEditHelper.entitiesToUpdate
            val deliveriesToDelete = saleDeliveryEditHelper.primaryKeysToDeactivate

            deliveriesToInsert.forEach {
                it.saleDeliverySaleUid = entity.saleUid
                it.saleDeliveryUid = repo.saleDeliveryDao.insertAsync(it)
            }
            repo.saleDeliveryDao.updateListAsync(deliveriesToUpdate)
            repo.saleDeliveryDao.deactivateByUids(deliveriesToDelete)


            val paymentsToInsert = salePaymentEditHelper.entitiesToInsert
            val paymentsToUpdate = salePaymentEditHelper.entitiesToUpdate
            val paymentsToDelete = salePaymentEditHelper.primaryKeysToDeactivate

            paymentsToInsert.forEach {
                it.salePaymentSaleUid = entity.saleUid
                it.salePaymentUid = repo.salePaymentDao.insertAsync(it)
            }
            repo.salePaymentDao.updateListAsync(paymentsToUpdate)
            repo.salePaymentDao.deactivateByUids(paymentsToDelete)

            //TODO: this
            //onFinish(SaleDetailView.VIEW_NAME, entity.saleUid, entity)
        }
    }

    companion object {


    }

}