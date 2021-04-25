package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.CategoryEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Category
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.kodein.di.DI


class CategoryEditPresenter(context: Any,
                            arguments: Map<String, String>, view: CategoryEditView, di: DI,
                            lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<CategoryEditView, Category>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Category? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val category = withTimeout(2000){
            db.categoryDao.findByUidAsync(entityUid)
        }?: Category()

        return category

    }

    override fun onLoadFromJson(bundle: Map<String, String>): Category? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: Category? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, Category.serializer(), entityJsonStr)
        }else {
            editEntity = Category()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: Category) {


        GlobalScope.launch(doorMainDispatcher()) {

            if(entity.categoryUid == 0L) {
                entity.categoryUid = repo.categoryDao.insertAsync(entity)
            }else {
                repo.categoryDao.updateAsync(entity)
            }

            view.finishWithResult(listOf(entity))

        }
    }


}