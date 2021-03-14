package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.LanguageEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Language

import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.LeavingReason


class LanguageEditPresenter(context: Any,
        arguments: Map<String, String>, view: LanguageEditView,
        lifecycleOwner: DoorLifecycleOwner,
        di: DI)
    : UstadEditPresenter<LanguageEditView, Language>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Language? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        return db.onRepoWithFallbackToDb(2000) {
            it.takeIf { entityUid != 0L }?.languageDao?.findByUidAsync(entityUid)
        }?: Language()
    }

    override fun onLoadFromJson(bundle: Map<String, String>): Language? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]

        return if(entityJsonStr != null) {
            safeParse(di, Language.serializer(), entityJsonStr)
        }else {
            Language()
        }
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, Language.serializer(),
                entityVal)
    }

    override fun handleClickSave(entity: Language) {
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.name.isNullOrEmpty()){
                view.langNameError = systemImpl.getString(MessageID.field_required_prompt, context)
                return@launch
            }

            if(entity.langUid == 0L) {
                entity.langUid = repo.languageDao.insertAsync(entity)
            }else {
                repo.languageDao.updateAsync(entity)
            }

            view.finishWithResult(listOf(entity))
        }
    }

}