package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.ContentImportManager
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMUUID
import com.ustadmobile.core.util.ext.convertToJsonObject
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.ContentEntryEdit2View.Companion.ARG_IMPORTED_METADATA
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.kodein.di.DI
import org.kodein.di.instanceOrNull
import org.kodein.di.on


class ContentEntryEdit2Presenter(context: Any,
                                 arguments: Map<String, String>, view: ContentEntryEdit2View,
                                 lifecycleOwner: DoorLifecycleOwner,
                                 di: DI)
    : UstadEditPresenter<ContentEntryEdit2View, ContentEntryWithLanguage>(context, arguments, view, di, lifecycleOwner) {

    private val contentImportManager: ContentImportManager?
            by on(accountManager.activeAccount).instanceOrNull<ContentImportManager>()

    enum class LicenceOptions(val optionVal: Int, val messageId: Int) {
        LICENSE_TYPE_CC_BY(ContentEntry.LICENSE_TYPE_CC_BY, MessageID.licence_type_cc_by),
        LICENSE_TYPE_CC_BY_SA(ContentEntry.LICENSE_TYPE_CC_BY_SA, MessageID.licence_type_cc_by_sa),
        LICENSE_TYPE_CC_BY_SA_NC(ContentEntry.LICENSE_TYPE_CC_BY_SA_NC, MessageID.licence_type_cc_by_sa_nc),
        LICENSE_TYPE_CC_BY_NC(ContentEntry.LICENSE_TYPE_CC_BY_NC, MessageID.licence_type_cc_by_nc),
        ALL_RIGHTS_RESERVED(ContentEntry.ALL_RIGHTS_RESERVED, MessageID.licence_type_all_rights),
        LICENSE_TYPE_CC_BY_NC_SA(ContentEntry.LICENSE_TYPE_CC_BY_NC_SA, MessageID.licence_type_cc_by_nc_sa),
        LICENSE_TYPE_PUBLIC_DOMAIN(ContentEntry.LICENSE_TYPE_PUBLIC_DOMAIN, MessageID.licence_type_public_domain),
        LICENSE_TYPE_OTHER(ContentEntry.LICENSE_TYPE_OTHER, MessageID.other),
        LICENSE_TYPE_CC0(ContentEntry.LICENSE_TYPE_CC_0, MessageID.license_type_cc_0)
    }

    data class UmStorageOptions(var messageId: Int, var label: String)

    private var parentEntryUid: Long = 0


    open class StorageOptions(context: Any, val storage: UmStorageOptions) : MessageIdOption(storage.messageId, context) {
        override fun toString(): String {
            return storage.label
        }
    }

    class LicenceMessageIdOptions(licence: LicenceOptions, context: Any)
        : MessageIdOption(licence.messageId, context, licence.optionVal)

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.licenceOptions = LicenceOptions.values().map { LicenceMessageIdOptions(it, context) }
        parentEntryUid = arguments[ARG_PARENT_ENTRY_UID]?.toLong() ?: 0
        GlobalScope.launch(doorMainDispatcher()) {
            view.storageOptions = systemImpl.getStorageDirsAsync(context)
        }
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ContentEntryWithLanguage? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0
        val isLeaf = arguments[ARG_LEAF]?.toBoolean()
        return withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.contentEntryDao?.findEntryWithLanguageByEntryIdAsync(entityUid)
        } ?: ContentEntryWithLanguage().apply {
            leaf = isLeaf ?: (contentFlags != ContentEntry.FLAG_IMPORTED)
        }
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ContentEntryWithLanguage? {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        val metaDataStr = bundle[ARG_IMPORTED_METADATA]
        if (metaDataStr != null) {
            view.entryMetaData = safeParse(di, ImportedContentEntryMetaData.serializer(), metaDataStr)
        }
        var editEntity: ContentEntryWithLanguage? = null
        editEntity = if (entityJsonStr != null) {
            safeParse(di, ContentEntryWithLanguage.serializer(), entityJsonStr)
        } else {
            ContentEntryWithLanguage()
        }
        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = view.entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null, entityVal)
        savedState.putEntityAsJson(ARG_IMPORTED_METADATA, ImportedContentEntryMetaData.serializer(), view.entryMetaData)
    }


    override fun handleClickSave(entity: ContentEntryWithLanguage) {
        view.titleErrorEnabled = false
        view.fileImportErrorVisible = false
        GlobalScope.launch(doorMainDispatcher()) {
            val canCreate = isImportValid(entity)

            if (canCreate) {
                entity.licenseName = view.licenceOptions?.firstOrNull { it.code == entity.licenseType }.toString()
                if (entity.contentEntryUid == 0L) {
                    entity.contentEntryUid = repo.contentEntryDao.insertAsync(entity)

                    if (entity.entryId == null) {
                        entity.entryId = accountManager.activeAccount.endpointUrl +
                                "${entity.contentEntryUid}/${UMUUID.randomUUID()}"
                        repo.contentEntryDao.updateAsync(entity)
                    }
                    val contentEntryJoin = ContentEntryParentChildJoin().apply {
                        cepcjChildContentEntryUid = entity.contentEntryUid
                        cepcjParentContentEntryUid = parentEntryUid
                    }
                    repo.contentEntryParentChildJoinDao.insertAsync(contentEntryJoin)
                } else {
                    repo.contentEntryDao.updateAsync(entity)
                }

                val language = entity.language
                if (language != null && language.langUid == 0L) {
                    repo.languageDao.insertAsync(language)
                }

                val metaData = view.entryMetaData
                val uri = metaData?.uri
                val videoDimensions = view.videoDimensions
                val conversionParams = mapOf("compress" to view.compressionEnabled.toString(),
                        "dimensions" to "${videoDimensions.first}x${videoDimensions.second}")
                if (metaData != null && uri != null) {

                    if (uri.startsWith("file://")) {

                        metaData.contentEntry = entity
                        contentImportManager?.queueImportContentFromFile(uri, metaData,
                                view.storageOptions?.get(view.selectedStorageIndex)?.dirURI.toString(),
                                conversionParams)

                        view.finishWithResult(listOf(entity))
                        return@launch


                    } else {

                        var client: HttpResponse? = null
                        try {

                            client = defaultHttpClient().post<HttpStatement>() {
                                url(UMFileUtil.joinPaths(accountManager.activeAccount.endpointUrl,
                                        "/import/downloadLink/"))
                                parameter("parentUid", parentEntryUid)
                                parameter("scraperType", view.entryMetaData?.scraperType)
                                parameter("url", view.entryMetaData?.uri)
                                parameter("conversionParams",
                                        Json.stringify(JsonObject.serializer(),
                                                conversionParams.convertToJsonObject()))
                                header("content-type", "application/json")
                                body = entity
                            }.execute()

                        }catch (e: Exception){
                            view.showSnackBar("${systemImpl.getString(MessageID.error, 
                                    context)}: ${e.message ?: ""}", {})
                            return@launch
                        }

                        if (client.status.value != 200) {
                            view.showSnackBar(systemImpl.getString(MessageID.error,
                                    context), {})
                            return@launch
                        }

                        view.finishWithResult(listOf(entity))
                        return@launch

                    }
                }

                view.finishWithResult(listOf(entity))

            } else {
                view.titleErrorEnabled = entity.title == null
                view.fileImportErrorVisible = entity.title != null && entity.leaf
                        && view.entryMetaData?.uri == null
            }
        }
    }

    fun isImportValid(entity: ContentEntryWithLanguage): Boolean{
        return entity.title != null && (!entity.leaf || entity.contentEntryUid != 0L ||
                (entity.contentEntryUid == 0L && view.entryMetaData?.uri != null))
    }

    fun handleFileSelection(filePath: String) {
        GlobalScope.launch(doorMainDispatcher()) {
            val metadata = contentImportManager?.extractMetadata(filePath)
            view.entryMetaData = metadata
            when (metadata) {
                null -> {
                    view.showSnackBar(systemImpl.getString(MessageID.import_link_content_not_supported, context))
                }
            }

            val entry = metadata?.contentEntry
            val entryUid = arguments[ARG_ENTITY_UID]
            if (entry != null) {
                if (entryUid != null) entry.contentEntryUid = entryUid.toString().toLong()
                view.fileImportErrorVisible = false
                view.entity = entry
                if(metadata.mimeType.startsWith("video/")){
                    view.videoUri = filePath
                }
            }
            view.loading = false
        }
    }

}