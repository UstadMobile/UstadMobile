package com.ustadmobile.core.contentformats

import io.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.ImportJobRunner
import com.ustadmobile.core.util.LiveDataWorkQueue
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerImportJob
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

open class ContentImportManagerImpl(val contentPlugins: List<ContentTypePlugin>, val context: Any, val endpoint: Endpoint, final override val di: DI) : ContentImportManager, DIAware {

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = UmAppDatabase.TAG_DB)

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = UmAppDatabase.TAG_REPO)

    private val mimeTypeSupported: List<String> = contentPlugins.flatMap { it.mimeTypes.asList() }

    private val extSupported: List<String> = contentPlugins.flatMap { it.fileExtensions.asList() }

    init {

        LiveDataWorkQueue(db.containerImportJobDao.findJobs(),
                { item1, item2 -> item1.cijUid == item2.cijUid },
                mainDispatcher = doorMainDispatcher()) {
            try {
                it.cijJobStatus = JobStatus.RUNNING
                db.containerImportJobDao.updateStatus(it.cijJobStatus, it.cijUid)

                val runner = ImportJobRunner(it, endpointUrl = endpoint.url, di = di)
                runner.importContainer()
                var status = JobStatus.COMPLETE
                if(it.cijImportMode == ContainerImportJob.CLIENT_IMPORT_MODE){
                   status = runner.upload()
                }

                it.cijJobStatus = status
                db.containerImportJobDao.updateStatus(status, it.cijUid)
            }catch (e: Exception){
                db.containerImportJobDao.updateStatus(JobStatus.FAILED, it.cijUid)
            }
        }.also { workQueue ->
            GlobalScope.launch {
                workQueue.start()
            }
        }

    }

    //This can also container the LiveDataWorkQueue and host ImportJobRunner here in core.
    //You can replace the dependency on NetworkManagerBle with a dependency network status livedata
    override suspend fun extractMetadata(uri: String): ImportedContentEntryMetaData? {
        contentPlugins.forEach {
            val pluginResult = it.extractMetadata(uri, context)
            val languageCode = pluginResult?.language?.iso_639_1_standard
            if (languageCode != null) {
                pluginResult.language = db.languageDao.findByTwoCodeAsync(languageCode)
            }
            if (pluginResult != null) {
                pluginResult.contentFlags = ContentEntry.FLAG_IMPORTED
                return ImportedContentEntryMetaData(pluginResult, it.mimeTypes[0], uri)
            }
        }

        return null
    }

    override suspend fun queueImportContentFromFile(uri: String, metadata: ImportedContentEntryMetaData,
                                                    containerBaseDir: String, importMode: Int,
                                                    conversionParams: Map<String, String>): ContainerImportJob {
        return ContainerImportJob().apply {
            cijBytesSoFar = 0
            this.cijUri = uri
            this.cijContentEntryUid = metadata.contentEntry.contentEntryUid
            this.cijMimeType = metadata.mimeType
            this.cijContainerBaseDir = containerBaseDir
            this.cijJobStatus = JobStatus.QUEUED
            this.cijImportMode = importMode
            //This used to use encodeToString(JsonObject), but there is a bug that makes this crash
            //when obfuscation is enabled.
            //Previously: Json.encodeToString(JsonObject.serializer(), conversionParams.convertToJsonObject())
            this.cijConversionParams = Json.encodeToString(
                MapSerializer(String.serializer(), String.serializer()), conversionParams)
            cijUid = db.containerImportJobDao.insertAsync(this)
        }
    }

    override suspend fun importFileToContainer(uri: String, mimeType: String,
                                               contentEntryUid: Long, containerBaseDir: String,
                                               conversionParams: Map<String, String>,
                                               progressListener: (Int) -> Unit): Container? {
        contentPlugins.forEach {

            it.mimeTypes.find { pluginMimeType -> pluginMimeType == mimeType }
                    ?: return@forEach

            Napier.v("Importing $uri for ContentEntry UID# $contentEntryUid using plugin: $it", tag = LOG_TAG)
            return it.importToContainer(uri, conversionParams, contentEntryUid, mimeType,
                    containerBaseDir, context, db, repo, progressListener).also {
                        Napier.v("Importing $uri for ContentEntry UID# $contentEntryUid completed")
            }
        }
        return null
    }

    override fun getMimeTypeSupported(): List<String> {
        return mimeTypeSupported
    }

    override fun getExtSupported(): List<String> {
        return extSupported
    }

    companion object {

        const val LOG_TAG = "ContentImportMgr"
    }

}