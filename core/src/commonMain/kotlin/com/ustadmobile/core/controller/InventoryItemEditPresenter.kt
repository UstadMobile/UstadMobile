package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.InventoryItemEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_PRODUCT_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.InventoryItem
import com.ustadmobile.lib.db.entities.PersonWithInventoryItemAndStock
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import kotlinx.serialization.builtins.list


class InventoryItemEditPresenter(context: Any,
                                 arguments: Map<String, String>, view: InventoryItemEditView, di: DI,
                                 lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<InventoryItemEditView, InventoryItem>(context, arguments, view, di,
        lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    val producerSelectionEditHelper = DefaultOneToManyJoinEditHelper<PersonWithInventoryItemAndStock>(
            PersonWithInventoryItemAndStock::personUid,
            "PersonWithInventoryItemAndStock", PersonWithInventoryItemAndStock.serializer().list,
            PersonWithInventoryItemAndStock.serializer().list, this) { personUid = it }

    fun handleAddOrEditPersonWithInventory(personWithInventory: PersonWithInventoryItemAndStock) {
        producerSelectionEditHelper.onEditResult(personWithInventory)
    }

    fun handleRemoveSchedule(personWithInventory: PersonWithInventoryItemAndStock) {
        producerSelectionEditHelper.onDeactivateEntity(personWithInventory)
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.producers = producerSelectionEditHelper.liveList
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): InventoryItem? {

        val loggedInPersonUid = accountManager.activeAccount.personUid
        val productUid = arguments[ARG_PRODUCT_UID]?.toLong()?: 0L

       val producers = withTimeout(2000){
            db.inventoryItemDao.getAllWeWithNewInventoryItem(loggedInPersonUid)
       }
        producerSelectionEditHelper.liveList.sendValue(producers)

        return InventoryItem()
    }

    override fun onLoadFromJson(bundle: Map<String, String>): InventoryItem? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: InventoryItem? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(InventoryItem.serializer(), entityJsonStr)
        }else {
            editEntity = InventoryItem()
        }

        view.producers = producerSelectionEditHelper.liveList

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: InventoryItem) {

        val loggedInPersonUid = accountManager.activeAccount.personUid
        val productUid = arguments[ARG_PRODUCT_UID]?.toLong()?: 0L

        GlobalScope.launch() {

            val itemsToUpdate = producerSelectionEditHelper.entitiesToUpdate
            val commonDate = UMCalendarUtil.getDateInMilliPlusDays(0)

            for(producerInventory in itemsToUpdate){
                val newInventory = InventoryItem().apply{
                    inventoryItemProductUid = productUid
                    inventoryItemLeUid = loggedInPersonUid
                    inventoryItemWeUid = producerInventory.personUid
                    inventoryItemDateAdded = commonDate
                    inventoryItemQuantity = producerInventory.selectedStock?.toLong()?:0L
                    inventoryItemUid = repo.inventoryItemDao.insertAsync(this)
                }
            }

            view.runOnUiThread(Runnable {
                view.finishWithResult(listOf(entity))
            })
        }
    }


}