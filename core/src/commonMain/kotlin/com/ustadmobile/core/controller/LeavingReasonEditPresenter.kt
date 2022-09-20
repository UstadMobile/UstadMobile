package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.LeavingReasonEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.LeavingReason
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class LeavingReasonEditPresenter(context: Any,
        arguments: Map<String, String>, view: LeavingReasonEditView,
        lifecycleOwner: LifecycleOwner,
        di: DI)
    : UstadEditPresenter<LeavingReasonEditView, LeavingReason>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): LeavingReason? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        return db.onRepoWithFallbackToDb(2000) {
            it.takeIf { entityUid != 0L }?.leavingReasonDao?.findByUidAsync(entityUid)
        }?: LeavingReason()
    }

    override fun onLoadFromJson(bundle: Map<String, String>): LeavingReason? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]

        return if(entityJsonStr != null) {
            safeParse(di, LeavingReason.serializer(), entityJsonStr)
        }else {
            LeavingReason()
        }
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json, LeavingReason.serializer(), entity)
    }

    override fun handleClickSave(entity: LeavingReason) {
        GlobalScope.launch(doorMainDispatcher()) {

            if(entity.leavingReasonTitle.isNullOrEmpty()){
                view.reasonTitleError = systemImpl.getString(MessageID.field_required_prompt, context)
                return@launch
            }

            if(entity.leavingReasonUid == 0L) {
                entity.leavingReasonUid = repo.leavingReasonDao.insertAsync(entity)
            }else {
                repo.leavingReasonDao.updateAsync(entity)
            }

            finishWithResult(safeStringify(di,
                ListSerializer(LeavingReason.serializer()), listOf(entity)))
        }
    }

}