package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ProductEditView
import com.ustadmobile.core.view.SaleItemEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.lib.db.entities.SaleItemWithProduct

import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
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

        val saleItemWithProduct = withTimeout(2000){
            db.saleItemDao.findWithProductByUidAsync(entityUid)
        }?:SaleItemWithProduct()

        return saleItemWithProduct
    }

    override fun onLoadFromJson(bundle: Map<String, String>): SaleItemWithProduct? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: SaleItemWithProduct? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(SaleItemWithProduct.serializer(), entityJsonStr)
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

        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.saleItemUid == 0L) {
                entity.saleItemUid = repo.saleItemDao.insertAsync(entity)
            }else {
                repo.saleItemDao.updateAsync(entity)
            }

            view.finishWithResult(listOf(entity))
        }
    }


}