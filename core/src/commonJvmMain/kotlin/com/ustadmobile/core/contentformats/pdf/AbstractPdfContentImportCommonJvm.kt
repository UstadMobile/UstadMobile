package com.ustadmobile.core.contentformats.pdf

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.contentformats.ContentImporter
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.storeText
import com.ustadmobile.core.contentjob.SupportedContent
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.contententry.ContentConstants
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.requireSourceAsDoorUri
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.libcache.UstadCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileSystem
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json

abstract class AbstractPdfContentImportCommonJvm(
    endpoint: Endpoint,
    protected val cache: UstadCache,
    protected val uriHelper: UriHelper,
    protected val json: Json,
    protected val fileSystem: FileSystem = SystemFileSystem,
    protected val db: UmAppDatabase,
    protected val saveLocalUriAsBlobAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase,
    protected val getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase,
) : ContentImporter(endpoint){

    override val importerId: Int = PLUGINID
    override val supportedMimeTypes: List<String>
        get() = listOf("application/pdf")
    override val supportedFileExtensions: List<String>
        get() = SupportedContent.PDF_EXTENSIONS

    override val formatName: String
        get() = "PDF"

    override suspend fun importContent(
        jobItem: ContentEntryImportJob,
        progressListener: ContentImportProgressListener,
    ): ContentEntryVersion = withContext(Dispatchers.IO) {
        val jobUri = jobItem.requireSourceAsDoorUri()

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID)
        val urlPrefix = createContentUrlPrefix(contentEntryVersionUid)
        val pdfManifestUri = "content.pdf"
        val manifestUrl = "${urlPrefix}${ContentConstants.MANIFEST_NAME}"

        val manifestEntriesAndBlobs = saveLocalUriAsBlobAndManifestUseCase(
            listOf(
                SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                    blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                        localUri = jobUri.toString(),
                        entityUid = contentEntryVersionUid,
                        tableId = ContentEntryVersion.TABLE_ID,
                        mimeType = "application/pdf",
                    ),
                    manifestUri = "content.pdf",
                )
            )
        )

        val contentEntryVersion = ContentEntryVersion(
            cevUid = contentEntryVersionUid,
            cevContentType = ContentEntryVersion.TYPE_PDF,
            cevContentEntryUid = jobItem.cjiContentEntryUid,
            cevManifestUrl = manifestUrl,
            cevOpenUri = pdfManifestUri,
        )

        val manifest = ContentManifest(
            version = 1,
            metadata = emptyMap(),
            entries = manifestEntriesAndBlobs.map { it.manifestEntry }
        )

        cache.storeText(
            url = manifestUrl,
            text = json.encodeToString(ContentManifest.serializer(), manifest),
            mimeType = "application/json"
        )

        contentEntryVersion
    }

    companion object {

        const val PLUGINID = 111


    }

}