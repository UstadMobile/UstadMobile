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
import com.ustadmobile.core.util.ext.displayFilename
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * DirectoryContentImporter handles directory-based content import,
 * processes each file within a directory, and delegates to other importers for specific file types.
 *
 * @param otherContentImportersList all other ContentImporters that are not DirectoryContentImporter
 *        e.g. importers that would be used to process any files or subdirectories found in a directory.
 *        Normally we would simply use contentImportersManager as a dependency, unfortunately, this
 *        would result in a circular DI dependency where DirectoryContentImporter would depend on
 *        ContentImportersManager and ContentImportersManager depends on DirectoryContentImporter
 * @param contentImportersManagerFactory function to create a ContentImportersManager
 * @param listDirectoryUriUseCase A use case to list files within a directory URI.
 *
 */
class DirectoryContentImporter(
    endpoint: Endpoint,
    private val db: UmAppDatabase,
    private val otherContentImportersList: List<ContentImporter>,
    private val contentImportersManagerFactory: (List<ContentImporter>) -> ContentImportersManager = ::ContentImportersManager,
    private val enqueueContentEntryImportUseCase: EnqueueContentEntryImportUseCase,
    private val listDirectoryUriUseCase: ListDirectoryUriUseCase
) : ContentImporter(endpoint) {

    override val importerId: Int = PLUGINID
    override val supportedMimeTypes: List<String> =
        listOf("application/pdf", "video/mp4", "image/jpeg", "image/png")
    override val supportedFileExtensions: List<String> = listOf("pdf", "mp4", "jpg", "png")
    override val formatName: String = "Directory"

    /**
     * Extracts metadata for a directory URI.
     * Creates a MetadataResult for the directory entry, setting metadata fields like title, leaf status, and source URL.
     */
    override suspend fun extractMetadata(uri: DoorUri, originalFilename: String?): MetadataResult? =
        withContext(Dispatchers.IO) {
            if (listDirectoryUriUseCase.isDirectory(uri.toString())) {
                try {
                    MetadataResult(
                        entry = ContentEntryWithLanguage().apply {
                            title =
                                originalFilename ?: uri.getFileName(this@DirectoryContentImporter)
                            leaf = false
                            sourceUrl = uri.toString()
                        },
                        importerId = importerId,
                        originalFilename = originalFilename
                    )
                } catch (e: Throwable) {
                    Napier.w(throwable = e) { "DirectoryContentImporter: error extracting metadata for $uri" }
                    throw InvalidContentException("Invalid Directory: ${e.message}", e)
                }
            } else {
                Napier.i { "Invalid directory: $uri" }
                null
            }
        }

    /**
     * Imports the content of a directory, iterating through each file, extracting metadata, and enqueuing content entries.
     */

    override suspend fun importContent(
        jobItem: ContentEntryImportJob,
        progressListener: ContentImportProgressListener
    ): ContentEntryVersion = withContext(Dispatchers.IO) {

        // List all files within the directory URI
        val fileUris = listDirectoryUriUseCase(jobItem.sourceUri.toString())

        // Create a manager for handling content import based on file type, avoiding circular dependency
        val contentImportersManager = contentImportersManagerFactory(
            otherContentImportersList + this@DirectoryContentImporter
        )
        for (entry in fileUris) {
            try {
                // Attempt to extract metadata for each file entry
                val metadataResult = contentImportersManager.extractMetadata(
                    entry.uri,
                    entry.fileName
                )
                if (metadataResult != null) {
                    // Enqueue job item for files that have metadata
                    val contentJobItem = ContentEntryImportJob(
                        sourceUri = entry.uri.toString(),
                        cjiOriginalFilename = entry.fileName,
                        cjiPluginId = metadataResult.importerId,
                        cjiParentContentEntryUid = jobItem.cjiContentEntryUid
                    )
                    enqueueContentEntryImportUseCase.invoke(contentJobItem)
                } else {
                    Napier.w("Unsupported file type or failed to extract metadata for: ${entry}")
                }
            } catch (e: Throwable) {
                Napier.w("Exception For Entry: ${entry}", e)
            }
        }

        // Create the directory content entry version after processing
        val directoryContentEntryVersion = ContentEntryVersion(
            cevUid = db.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID),
            cevContentType = ContentEntryVersion.TYPE_DIRECTORY,
            cevContentEntryUid = jobItem.cjiContentEntryUid,
            cevManifestUrl = "",
        )
        directoryContentEntryVersion
    }

    companion object {
        const val PLUGINID = 202
    }
}
