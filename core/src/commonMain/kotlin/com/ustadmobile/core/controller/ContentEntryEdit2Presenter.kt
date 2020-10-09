package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.networkmanager.ContainerUploadManager
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMUUID

import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.ContentEntryEdit2View.Companion.ARG_IMPORTED_METADATA
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpStatement
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instanceOrNull
import org.kodein.di.on


class ContentEntryEdit2Presenter(context: Any,
                                 arguments: Map<String, String>, view: ContentEntryEdit2View,
                                 lifecycleOwner: DoorLifecycleOwner,
                                 di: DI)
    : UstadEditPresenter<ContentEntryEdit2View, ContentEntryWithLanguage>(context, arguments, view, di, lifecycleOwner) {

    private val containerUploadManager: ContainerUploadManager?
            by on(accountManager.activeAccount).instanceOrNull<ContainerUploadManager>()

    private val containerDownloadManager: ContainerDownloadManager?
            by on(accountManager.activeAccount).instanceOrNull<ContainerDownloadManager>()

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

    private var storageOptions: List<UMStorageDir>? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.licenceOptions = LicenceOptions.values().map { LicenceMessageIdOptions(it, context) }
        parentEntryUid = arguments[ARG_PARENT_ENTRY_UID]?.toLong() ?: 0
        systemImpl.getStorageDirs(context, object : UmResultCallback<List<UMStorageDir>> {
            override fun onDone(result: List<UMStorageDir>?) {
                storageOptions = result
                if (result != null) {
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
            db.takeIf { entityUid != 0L }?.contentEntryDao?.findEntryWithLanguageByEntryIdAsync(entityUid)
        } ?: ContentEntryWithLanguage().apply {
            leaf = isLeaf ?: (contentFlags != ContentEntry.FLAG_IMPORTED)
        }
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ContentEntryWithLanguage? {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        val metaDataStr = bundle[ARG_IMPORTED_METADATA]
        if(metaDataStr != null){
            view.entryMetaData = Json.parse(ImportedContentEntryMetaData.serializer(), metaDataStr)
        }
        var editEntity: ContentEntryWithLanguage? = null
        editEntity = if (entityJsonStr != null) {
            Json.parse(ContentEntryWithLanguage.serializer(), entityJsonStr)
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
            val canCreate = entity.title != null && (!entity.leaf || entity.contentEntryUid != 0L ||
                    (entity.contentEntryUid == 0L && view.entryMetaData?.uri != null))

            if (canCreate) {
                entity.licenseName = view.licenceOptions?.firstOrNull { it.code == entity.licenseType }.toString()
                if (entity.contentEntryUid == 0L) {
                    entity.contentEntryUid = repo.contentEntryDao.insertAsync(entity)

                    if(entity.entryId == null){
                        entity.entryId = "${systemImpl.getAppConfigString(AppConfig.KEY_API_URL, null, context)}" +
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

                if (view.entryMetaData?.uri != null) {

                    if (view.entryMetaData?.uri?.startsWith("file:/") == true) {

                        val container = view.saveContainerOnExit(entity.contentEntryUid,
                                storageOptions?.get(view.selectedStorageIndex)?.dirURI.toString(), db, repo)

                        if (container != null && containerUploadManager != null) {

                            val downloadJob = DownloadJob(entity.contentEntryUid, getSystemTimeInMillis())
                            downloadJob.djStatus = JobStatus.COMPLETE
                            downloadJob.timeRequested = getSystemTimeInMillis()
                            downloadJob.bytesDownloadedSoFar = container.fileSize
                            downloadJob.totalBytesToDownload = container.fileSize
                            downloadJob.djUid = repo.downloadJobDao.insertAsync(downloadJob).toInt()

                            val downloadJobItem = DownloadJobItem(downloadJob, entity.contentEntryUid,
                                    container.containerUid, container.fileSize)
                            downloadJobItem.djiUid = repo.downloadJobItemDao.insertAsync(downloadJobItem).toInt()
                            downloadJobItem.djiStatus = JobStatus.COMPLETE
                            downloadJobItem.downloadedSoFar = container.fileSize

                            containerDownloadManager?.handleDownloadJobItemUpdated(downloadJobItem)

                            val uploadJob = ContainerUploadJob().apply {
                                this.jobStatus = JobStatus.NOT_QUEUED
                                this.cujContainerUid = container.containerUid
                                this.cujUid = db.containerUploadJobDao.insertAsync(this)
                            }

                            containerUploadManager?.enqueue(uploadJob.cujUid)

                            view.finishWithResult(listOf(entity))
                            return@launch

                        }

                    } else {

                        val client = defaultHttpClient().post<HttpStatement>() {
                            url(UMFileUtil.joinPaths(accountManager.activeAccount.endpointUrl, "/import/downloadLink/"))
                            parameter("parentUid", parentEntryUid)
                            parameter("scraperType", view.entryMetaData?.scraperType)
                            parameter("url", view.entryMetaData?.uri)
                            header("content-type", "application/json")
                            body = entity
                        }.execute()


                        if (client.status.value != 200) {
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

}