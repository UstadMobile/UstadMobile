package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.SalePaymentEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.SalePaymentWithSaleItems
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
            editEntity.salePaymentPaidDate = UMCalendarUtil.getDateInMilliPlusDays(0)
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
            if(entity.salePaymentPaidDate == 0L){
                entity.salePaymentPaidDate = UMCalendarUtil.getDateInMilliPlusDays(0)
            }
            view.finishWithResult(listOf(entity))
        }
    }



}