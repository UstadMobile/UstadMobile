package com.ustadmobile.core.contentformats.directory

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.contentformats.ContentImporter
import com.ustadmobile.core.contentformats.ContentImportersManager
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.cachestoragepath.getLocalUriIfRemote
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueContentEntryImportUseCase
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.requireSourceAsDoorUri
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * DirectoryContentImporter is a ContentImporter responsible for handling directory-based content.
 * It extracts metadata from directories and processes different content types.
 */
class DirectoryContentImporter(
    endpoint: Endpoint,
    private val db: UmAppDatabase,
    private val getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase,
    private val contentImportersManager: ContentImportersManager,
    private val enqueueContentEntryImportUseCase: EnqueueContentEntryImportUseCase,
    private val uriHelper: UriHelper,
) : ContentImporter(endpoint) {

    override val importerId: Int = 2
    override val supportedMimeTypes: List<String> =
        listOf("application/pdf", "video/mp4", "image/jpeg", "image/png")
    override val supportedFileExtensions: List<String> = listOf("pdf", "mp4", "jpg", "png")
    override val formatName: String = "Directory"

    override suspend fun extractMetadata(uri: DoorUri, originalFilename: String?): MetadataResult =
        withContext(Dispatchers.IO) {
            // Validate if the path is a directory
            val directoryUri = getStoragePathForUrlUseCase.getLocalUriIfRemote(uri)
            val localDirectory = directoryUri.toFile()
            if (!localDirectory.exists() || !localDirectory.isDirectory) {
                throw InvalidContentException("Provided path is not a valid directory: ${localDirectory.path}")
            }

            // If valid, create and return metadata
            try {
                MetadataResult(
                    entry = ContentEntryWithLanguage().apply {
                        title = originalFilename ?: localDirectory.name
                        leaf = false
                        sourceUrl = directoryUri.toString()
                    },
                    importerId = importerId,
                    originalFilename = originalFilename
                )
            } catch (e: Throwable) {
                Napier.w(throwable = e) { "DirectoryContentImporter: error extracting metadata for $uri" }
                throw InvalidContentException("Invalid Directory: ${e.message}", e)
            }
        }

    override suspend fun importContent(
        jobItem: ContentEntryImportJob,
        progressListener: ContentImportProgressListener
    ): ContentEntryVersion = withContext(Dispatchers.IO) {
        val directoryUri =
            getStoragePathForUrlUseCase.getLocalUriIfRemote(jobItem.requireSourceAsDoorUri())
        val localDirectory = directoryUri.toFile()
        val entries = localDirectory.listFiles() ?: arrayOf()

        for (entry in entries) {
            // Extract metadata for the current file
            val metadataResult = contentImportersManager.extractMetadata(
                entry.toDoorUri(),
                entry.name
            )

            if (metadataResult != null) {
                // If metadata extraction was successful, enqueue the import
                val contentJobItem = ContentEntryImportJob(
                    sourceUri = entry.toDoorUri().toString(),
                    cjiOriginalFilename = entry.name,
                    cjiPluginId = metadataResult.importerId,
                    cjiParentContentEntryUid = jobItem.cjiContentEntryUid

                )
                enqueueContentEntryImportUseCase.invoke(contentJobItem = contentJobItem)

            } else {
                Napier.w("Unsupported file type or failed to extract metadata for: ${entry.name}")
            }
        }

        val directoryContentEntryVersion = ContentEntryVersion(
            cevUid = db.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID),
            cevContentType = ContentEntryVersion.TYPE_DIRECTORY,
            cevContentEntryUid = jobItem.cjiContentEntryUid,
            cevManifestUrl = "",
            cevOpenUri = directoryUri.toString(),
            cevOriginalSize = uriHelper.getSize(directoryUri),
        )

        directoryContentEntryVersion
    }
}
