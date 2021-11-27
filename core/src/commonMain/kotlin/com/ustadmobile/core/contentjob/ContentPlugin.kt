package com.ustadmobile.core.contentjob

import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.DIAware

/**
 * Implementations of ContentPlugin manage how content is imported, downloaded, and uploaded. There
 * is generally one ContentPlugin implementation for each type of supported content import, and
 * there is another ContentPlugin that implements downloading a Container using a Torrent.
 */
interface ContentPlugin : DIAware {

    /**
     * This must be a unique integer. It can be used by components to remember what plugin to use
     * e.g. when extractMetadata is called, the plugin id that successfully extracted the metadata
     * will be saved to the ContentJobItem such that processJob will not have to guess which plugin
     * to use.
     */
    val pluginId: Int

    val supportedMimeTypes: List<String>

    val supportedFileExtensions: List<String>

    /**
     * The plugin should extract metadata from the given uri (if possible) and return a
     * MetadataResult if Metadata is retrieved, or null otherwise.
     */
    suspend fun extractMetadata(uri: DoorUri, process: ContentJobProcessContext): MetadataResult?

    /**
     * The plugin should actually process the given ContentJobItem (e.g. import, download, etc).
     *
     * If a FatalContentJobException is thrown, no retry attempt will be made. If any other exception
     * is thrown, processJob may be retried by ContentJobRunner
     */
    suspend fun processJob(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progress: ContentJobProgressListener
    ) : ProcessResult

}