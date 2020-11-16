package com.ustadmobile.core.controller

import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.SaleEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SalePayment

import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI


class SaleEditPresenter(context: Any,
                        arguments: Map<String, String>, view: SaleEditView, di: DI,
                        lifecycleOwner: DoorLifecycleOwner )
    : UstadEditPresenter<SaleEditView, Sale>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    //Sale Item edit helper
    val saleItemEditHelper = DefaultOneToManyJoinEditHelper<SaleItem>(SaleItem::saleItemUid,
            "state_SaleItem_list", SaleItem.serializer().list,
            SaleItem.serializer().list, this) { saleItemUid = it }

    fun handleAddOrEditSaleItem(saleItem: SaleItem) {
        saleItemEditHelper.onEditResult(saleItem)
    }

    fun handleRemoveSchedule(saleItem: SaleItem) {
        saleItemEditHelper.onDeactivateEntity(saleItem)
    }

    //Sale Delivery edit helper
    val saleDeliveryEditHelper = DefaultOneToManyJoinEditHelper<SaleDelivery>(SaleDelivery::saleDeliveryUid,
            "state_SaleDelivery_list", SaleDelivery.serializer().list,
            SaleDelivery.serializer().list, this) { saleDeliveryUid = it }

    fun handleAddOrEditSaleDelivery(saleDelivery: SaleDelivery) {
        saleDeliveryEditHelper.onEditResult(saleDelivery)
    }

    fun handleRemoveSchedule(saleDelivery: SaleDelivery) {
        saleDeliveryEditHelper.onDeactivateEntity(saleDelivery)
    }


    //Sale Payment edit helper
    val salePaymentEditHelper = DefaultOneToManyJoinEditHelper<SalePayment>(SalePayment::salePaymentUid,
            "state_SalePayment_list", SalePayment.serializer().list,
            SalePayment.serializer().list, this) { salePaymentUid = it }

    fun handleAddOrEditSalePayment(salePayment: SalePayment) {
        salePaymentEditHelper.onEditResult(salePayment)
    }

    fun handleRemoveSchedule(salePayment: SalePayment) {
        salePaymentEditHelper.onDeactivateEntity(salePayment)
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.saleItemList = saleItemEditHelper.liveList
        view.saleDeliveryList = saleDeliveryEditHelper.liveList
        view.salePaymentList = salePaymentEditHelper.liveList
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Sale? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val sale = withTimeout(2000){
            db.saleDao.findByUidAsync(entityUid)
        }?:Sale()

        val saleItemList = withTimeout(2000){
            db.saleItemDao.findAllBySale(entityUid)
        }
        saleItemEditHelper.liveList.sendValue(saleItemList)

        val saleDeliveryList = withTimeout(2000){
            db.saleDeliveryDao.findAllBySale(entityUid)
        }
        saleDeliveryEditHelper.liveList.sendValue(saleDeliveryList)

        val salePaymentList = withTimeout(2000){
            db.salePaymentDao.findAllBySale(entityUid)
        }
        salePaymentEditHelper.liveList.sendValue(salePaymentList)

        val totalCountLive = withTimeout(2000){
            db.saleItemDao.findTotalBySaleLive(entityUid)
        }
        val totalCount = withTimeout(2000){
            db.saleItemDao.findTotalBySale(entityUid)
        }

        view.totalAmountLive = totalCountLive
        view.totalAmount = totalCount

        return sale

    }

    override fun onLoadFromJson(bundle: Map<String, String>): Sale? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: Sale? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(Sale.serializer(), entityJsonStr)
        }else {
            editEntity = Sale()
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

    override fun handleClickSave(entity: Sale) {
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