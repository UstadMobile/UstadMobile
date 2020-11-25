package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.InventoryItemEditView
import com.ustadmobile.core.view.SaleDeliveryEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SaleItemWithProduct


import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.list
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI


class SaleDeliveryEditPresenter(context: Any,
                                arguments: Map<String, String>, view: SaleDeliveryEditView, di: DI,
                                lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<SaleDeliveryEditView, SaleDelivery>(context, arguments, view, di,
        lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    val saleItemWithProducersEditHelper = DefaultOneToManyJoinEditHelper<SaleItemWithProduct>(
            SaleItemWithProduct::saleItemUid,
            "SaleItemWithProduct", SaleItemWithProduct.serializer().list,
            SaleItemWithProduct.serializer().list, this) { saleItemUid = it }

    fun handleAddOrEditSaleItemWithProduct(saleItemWithProduct: SaleItemWithProduct) {
        saleItemWithProducersEditHelper.onEditResult(saleItemWithProduct)
    }

    fun handleRemoveSchedule(saleItemWithProduct: SaleItemWithProduct) {
        saleItemWithProducersEditHelper.onDeactivateEntity(saleItemWithProduct)
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.saleItems = saleItemWithProducersEditHelper.liveList
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): SaleDelivery? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val saleUid = arguments[UstadView.ARG_SALE_UID]?.toLong() ?: 0L


         val saleDelivery = withTimeoutOrNull(2000) {
             db.saleDeliveryDao.findByUidAsync(entityUid)
         } ?: SaleDelivery()

        val saleItems = withTimeout(2000){
            db.saleDeliveryDao.findAllSaleItemsByDelivery(saleUid)
        }
        saleItemWithProducersEditHelper.liveList.sendValue(saleItems)

         return saleDelivery

    }

    override fun onLoadFromJson(bundle: Map<String, String>): SaleDelivery? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: SaleDelivery? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(SaleDelivery.serializer(), entityJsonStr)
        }else {
            editEntity = SaleDelivery()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: SaleDelivery) {
        val saleUid = arguments[UstadView.ARG_SALE_UID]?.toLong() ?: 0L
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.saleDeliveryUid == 0L) {
                entity.saleDeliveryDate = UMCalendarUtil.getDateInMilliPlusDays(0)
                entity.saleDeliveryPersonUid = accountManager.activeAccount.personUid
                entity.saleDeliverySaleUid = saleUid
                entity.saleDeliveryUid = repo.saleDeliveryDao.insertAsync(entity)
            }else {
                repo.saleDeliveryDao.updateAsync(entity)
            }

            //TODO: Persist inventory selection

            view.finishWithResult(listOf(entity))
        }
    }


}