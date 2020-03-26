package com.ustadmobile.port.android.view.util

import android.content.Context
import android.content.Intent
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID

/**
 * This ActivityResultContract will link to the edit screen for a given entity
 *
 * @param editActivityClass the Activity that implements the edit view for this entity
 * @param pkGetter a function (e.g. method reference to the primary key)
 */
class CrudEditActivityResultContract<T>(context: Context, entityClass: Class<T>,
                                                 val editActivityClass: Class<*>,
                                                 val pkGetter: (T) -> Long)
    : AbstractCrudActivityResultContract<CrudEditActivityResultContract.CrudEditInput<T>, T>(context, entityClass) {

    /**
     * @param entity the existing Entity that is to be edited, or null if a new entity is to be created
     * @param persistenceMode DB or JSON persistence mode.
     * If persistence mode is JSON, the body of entity will be passed to the edit view as a JSON string
     * using ARG_ENTITY_JSON in the extras bundle of the intent.
     * If persistence mode is DB, the primary key of the entity will be passed to the edit view as
     * using ARG_ENTITY_UID
     */
    data class CrudEditInput<T>(val entity: T?,
                                val persistenceMode: UstadSingleEntityPresenter.PersistenceMode,
                                val extraArgs: Map<String, String> = mapOf())

    override fun createIntent(input: CrudEditInput<T>): Intent {
        val intentArgs = input.extraArgs.toMutableMap()
        val entityVal = input.entity
        if(input.persistenceMode == UstadSingleEntityPresenter.PersistenceMode.JSON && entityVal != null) {
            intentArgs[ARG_ENTITY_JSON] = defaultGson().toJson(entityVal)
        }else if(input.persistenceMode == UstadSingleEntityPresenter.PersistenceMode.DB && entityVal != null) {
            intentArgs[ARG_ENTITY_UID] = pkGetter(entityVal).toString()
        }

        return Intent(context, editActivityClass).apply {
            putExtras(intentArgs.toBundle())
        }
    }

}