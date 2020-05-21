package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.ContentEntryEdit2View.Companion.CONTENT_ENTRY_PARENT_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json


class ContentEntryEdit2Presenter(context: Any,
                          arguments: Map<String, String>, view: ContentEntryEdit2View,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadEditPresenter<ContentEntryEdit2View, ContentEntryWithLanguage>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    enum class LicenceOptions(val optionVal: Int, val messageId: Int){
        LICENSE_TYPE_CC_BY(ContentEntry.LICENSE_TYPE_CC_BY, MessageID.licence_type_cc_by),
        LICENSE_TYPE_CC_BY_SA(ContentEntry.LICENSE_TYPE_CC_BY_SA, MessageID.licence_type_cc_by_sa),
        LICENSE_TYPE_CC_BY_SA_NC(ContentEntry.LICENSE_TYPE_CC_BY_SA_NC, MessageID.licence_type_cc_by_sa_nc),
        LICENSE_TYPE_CC_BY_NC(ContentEntry.LICENSE_TYPE_CC_BY_NC, MessageID.licence_type_cc_by_nc),
        ALL_RIGHTS_RESERVED(ContentEntry.ALL_RIGHTS_RESERVED, MessageID.licence_type_all_rights),
        LICENSE_TYPE_CC_BY_NC_SA(ContentEntry.LICENSE_TYPE_CC_BY_NC_SA, MessageID.licence_type_cc_by_nc_sa),
        LICENSE_TYPE_PUBLIC_DOMAIN(ContentEntry.LICENSE_TYPE_PUBLIC_DOMAIN, MessageID.licence_type_public_domain),
        LICENSE_TYPE_OTHER(ContentEntry.LICENSE_TYPE_OTHER, MessageID.licence_type_other)
    }

    data class UmStorageOptions(var messageId: Int,var label: String)


    open class StorageOptions(context: Any, val storage: UmStorageOptions): MessageIdOption(storage.messageId,context){
        override fun toString(): String {
            return storage.label
        }
    }

    class LicenceMessageIdOptions(licence: LicenceOptions,context: Any)
        : MessageIdOption(licence.messageId,context, licence.optionVal)


    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.licenceOptions = LicenceOptions.values().map { LicenceMessageIdOptions(it, context) }

        systemImpl.getStorageDirs(context, object : UmResultCallback<List<UMStorageDir>> {
            override fun onDone(result: List<UMStorageDir>?) {
                if(result != null){
                   view.runOnUiThread(Runnable {
                       view.setUpStorageOptions(result)
                   })
                }
            }
        })
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ContentEntryWithLanguage? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        return withTimeoutOrNull(2000) {
            db.contentEntryDao.findByEntryId(entityUid)
        } ?: ContentEntryWithLanguage()
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ContentEntryWithLanguage? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ContentEntryWithLanguage? = null
        editEntity = if(entityJsonStr != null) {
            Json.parse(ContentEntryWithLanguage.serializer(), entityJsonStr)
        }else {
            ContentEntryWithLanguage()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }


    override fun handleClickSave(entity: ContentEntryWithLanguage) {
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.contentEntryUid == 0L) {
                entity.contentEntryUid = repo.contentEntryDao.insertAsync(entity)
                val contentEntryJoin = ContentEntryParentChildJoin()
                contentEntryJoin.cepcjChildContentEntryUid = entity.contentEntryUid
                contentEntryJoin.cepcjParentContentEntryUid =
                        arguments[CONTENT_ENTRY_PARENT_UID]?.toLong()!!
                contentEntryJoin.cepcjUid = repo.contentEntryParentChildJoinDao.insertAsync(contentEntryJoin)
            }else {
                repo.contentEntryDao.updateAsync(entity)
            }
            view.saveContainerOnExit(entity.contentEntryUid,db, repo)
            //TODO: Call commitToDatabase on any onetomany join helpers
            view.finishWithResult(listOf(entity))
        }
    }

    companion object {
        const val ARG_SAVEDSTATE_CONTENT_ENTRY = "contentEntry"
    }

}