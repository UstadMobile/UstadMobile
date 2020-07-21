package com.ustadmobile.core.controller

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.networkmanager.ContainerUploadManager
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instanceOrNull


class ContentEntryEdit2Presenter(context: Any,
                                 arguments: Map<String, String>, view: ContentEntryEdit2View,
                                 lifecycleOwner: DoorLifecycleOwner,
                                 di: DI)
    : UstadEditPresenter<ContentEntryEdit2View, ContentEntryWithLanguage>(context, arguments, view,  di, lifecycleOwner) {

    private val containerUploadManager: ContainerUploadManager? by instanceOrNull<ContainerUploadManager>()

    enum class LicenceOptions(val optionVal: Int, val messageId: Int){
        LICENSE_TYPE_CC_BY(ContentEntry.LICENSE_TYPE_CC_BY, MessageID.licence_type_cc_by),
        LICENSE_TYPE_CC_BY_SA(ContentEntry.LICENSE_TYPE_CC_BY_SA, MessageID.licence_type_cc_by_sa),
        LICENSE_TYPE_CC_BY_SA_NC(ContentEntry.LICENSE_TYPE_CC_BY_SA_NC, MessageID.licence_type_cc_by_sa_nc),
        LICENSE_TYPE_CC_BY_NC(ContentEntry.LICENSE_TYPE_CC_BY_NC, MessageID.licence_type_cc_by_nc),
        ALL_RIGHTS_RESERVED(ContentEntry.ALL_RIGHTS_RESERVED, MessageID.licence_type_all_rights),
        LICENSE_TYPE_CC_BY_NC_SA(ContentEntry.LICENSE_TYPE_CC_BY_NC_SA, MessageID.licence_type_cc_by_nc_sa),
        LICENSE_TYPE_PUBLIC_DOMAIN(ContentEntry.LICENSE_TYPE_PUBLIC_DOMAIN, MessageID.licence_type_public_domain),
        LICENSE_TYPE_OTHER(ContentEntry.LICENSE_TYPE_OTHER, MessageID.other)
    }

    data class UmStorageOptions(var messageId: Int,var label: String)

    private var parentEntryUid:Long = 0


    open class StorageOptions(context: Any, val storage: UmStorageOptions): MessageIdOption(storage.messageId,context){
        override fun toString(): String {
            return storage.label
        }
    }

    class LicenceMessageIdOptions(licence: LicenceOptions,context: Any)
        : MessageIdOption(licence.messageId,context, licence.optionVal)

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private var storageOptions: List<UMStorageDir>? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.licenceOptions = LicenceOptions.values().map { LicenceMessageIdOptions(it, context) }
        parentEntryUid = arguments[ARG_PARENT_ENTRY_UID]?.toLong()?:0
        systemImpl.getStorageDirs(context, object : UmResultCallback<List<UMStorageDir>> {
            override fun onDone(result: List<UMStorageDir>?) {
                storageOptions = result
                if(result != null){
                   view.runOnUiThread(Runnable {
                       view.storageOptions = result
                   })
                }
            }
        })
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ContentEntryWithLanguage? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0
        val isLeaf = arguments[ARG_LEAF]?.toBoolean()
        return withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.contentEntryDao?.findEntryWithLanguageByEntryId(entityUid)
        } ?: ContentEntryWithLanguage().apply {
            leaf = isLeaf ?: (contentFlags != ContentEntry.FLAG_IMPORTED)
        }
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
        val entityVal = view.entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null, entityVal)
    }


    override fun handleClickSave(entity: ContentEntryWithLanguage) {
        view.titleErrorEnabled = false
        view.fileImportErrorVisible = false
        GlobalScope.launch(doorMainDispatcher()) {
            val canCreate = entity.title != null && (!entity.leaf || entity.contentEntryUid != 0L ||
                    (entity.contentEntryUid == 0L && view.selectedFileUri != null))

            if(canCreate){
                entity.licenseName = view.licenceOptions?.firstOrNull { it.code == entity.licenseType }.toString()
                if(entity.contentEntryUid == 0L) {
                    entity.contentEntryUid = repo.contentEntryDao.insertAsync(entity)
                    val contentEntryJoin = ContentEntryParentChildJoin().apply {
                        cepcjChildContentEntryUid = entity.contentEntryUid
                        cepcjParentContentEntryUid = parentEntryUid
                    }
                    repo.contentEntryParentChildJoinDao.insertAsync(contentEntryJoin)
                }else {
                    repo.contentEntryDao.updateAsync(entity)
                }

                val language = entity.language
                if(language != null && language.langUid == 0L){
                    repo.languageDao.insertAsync(language)
                }

                if(entity.leaf && view.selectedFileUri != null) {
                    val container = view.saveContainerOnExit(entity.contentEntryUid,
                            storageOptions?.get(view.selectedStorageIndex)?.dirURI.toString(), db, repo)

                    if (container != null && containerUploadManager != null) {

                        val uploadJob = ContainerUploadJob().apply {
                            this.jobStatus = JobStatus.NOT_QUEUED
                            this.cujContainerUid = container.containerUid
                            this.cujUid = db.containerUploadJobDao.insert(this)
                        }

                        containerUploadManager?.enqueue(uploadJob.cujUid)
                    }
                }
                view.finishWithResult(listOf(entity))
            }else{
                view.titleErrorEnabled = entity.title == null
                view.fileImportErrorVisible = entity.title != null && entity.leaf
                        && view.selectedFileUri == null
            }
        }
    }

}