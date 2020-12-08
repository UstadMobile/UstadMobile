package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeParseList
import com.ustadmobile.core.view.LocationEditView
import com.ustadmobile.core.view.SalePaymentEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.SalePayment
import com.ustadmobile.lib.db.entities.SalePaymentWithSaleItems

import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI


class SalePaymentEditPresenter(context: Any,
                               arguments: Map<String, String>, view: SalePaymentEditView, di: DI,
                               lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<SalePaymentEditView, SalePaymentWithSaleItems>(context, arguments, view,
        di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

    }

    override fun onLoadFromJson(bundle: Map<String, String>): SalePaymentWithSaleItems? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: SalePaymentWithSaleItems? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, SalePaymentWithSaleItems.serializer(), entityJsonStr)
        }else {
            editEntity = SalePaymentWithSaleItems()
        }
        //1. Get all saleItems from JSON
        val saleItems = editEntity.saleItems


        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: SalePaymentWithSaleItems) {

        //Total amount possible given

        var totalSaleAmount = 0F
        for(eachItem in entity?.saleItems){
            totalSaleAmount += eachItem.saleItemQuantity * eachItem.saleItemPricePerPiece
        }
        if(entity.salePaymentPaidAmount >  (totalSaleAmount - entity.saleDiscount)){
            view.showSnackBar(systemImpl.getString(MessageID.selected_payment_higher, context))
        }else {
            entity.salePaymentPaidDate = UMCalendarUtil.getDateInMilliPlusDays(0)
            view.finishWithResult(listOf(entity))
        }
    }



}