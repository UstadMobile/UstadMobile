package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.ProductEditView
import com.ustadmobile.core.view.SaleItemEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.lib.db.entities.SaleItemWithProduct
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount

import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_PRODUCT_UID
import org.kodein.di.DI


class SaleItemEditPresenter(context: Any,
                            arguments: Map<String, String>, view: SaleItemEditView, di: DI,
                            lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<SaleItemEditView, SaleItemWithProduct>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): SaleItemWithProduct? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val productUid = arguments[ARG_PRODUCT_UID]?.toLong() ?:0L

        val product = withTimeout(2000){
            db.productDao.findByUidAsync(productUid)
        }?:Product()

        val productWithInventoryCount = withTimeout(2000){
            db.productDao.findProductWithInventoryCountAsync(productUid,
                    accountManager.activeAccount.personUid)
        }?:ProductWithInventoryCount()


        return withTimeout(2000){
            db.saleItemDao.findWithProductByUidAsync(entityUid,
                    accountManager.activeAccount.personUid)
        }?:SaleItemWithProduct().apply{
            saleItemProduct = productWithInventoryCount
            deliveredCount = productWithInventoryCount.stock
            saleItemProductUid = productWithInventoryCount.productUid
        }
    }

    override fun onLoadFromJson(bundle: Map<String, String>): SaleItemWithProduct? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: SaleItemWithProduct? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, SaleItemWithProduct.serializer(), entityJsonStr)
        }else {
            editEntity = SaleItemWithProduct()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: SaleItemWithProduct) {

        if(arguments.containsKey(UstadView.ARG_CREATE_SALE) &&
                arguments[UstadView.ARG_CREATE_SALE].equals("true")){
            //Create a new sale
            view.goToNewSale(entity)
        }else{
            view.finishWithResult(listOf(entity))
        }
    }


}