package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.LocationEditView
import com.ustadmobile.core.view.SalePaymentEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.SalePayment

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
    : UstadEditPresenter<SalePaymentEditView, SalePayment>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): SalePayment? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val salePayment = withTimeoutOrNull(2000){
            db.salePaymentDao.findByUidAsync(entityUid)
        } ?: SalePayment()
        return salePayment

    }

    override fun onLoadFromJson(bundle: Map<String, String>): SalePayment? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: SalePayment? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(SalePayment.serializer(), entityJsonStr)
        }else {
            editEntity = SalePayment()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: SalePayment) {

        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.salePaymentUid == 0L) {
                entity.salePaymentUid = repo.salePaymentDao.insertAsync(entity)
            }else {
                repo.salePaymentDao.updateAsync(entity)
            }

            view.finishWithResult(listOf(entity))
        }
    }



}