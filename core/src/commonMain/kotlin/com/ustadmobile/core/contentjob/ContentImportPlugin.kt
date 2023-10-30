package com.ustadmobile.core.contentjob

import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.DIAware

/**
 * A ContentImportPlugin can :
 *
 *  Extract Metadata:
 *  Given a URI (e.g. to a file or valid link), extract Metadata as a ContentEntry entity as far as
 *  possible e.g. title, author, license, etc.
 *
 *  Import content:
 *  Given a ContentJobItem with a Uri. This normally means:
 *     a) importing content from the given file/link by saving it into the Http Cache and creating
 *        a ContentEntryVersion entity. This can include further processing e.g. extra compression
 *     b) if the content is imported on a client device, then upload the cached entries to the
 *        endpoint
 *
 *  The processJob function MAY insert additional jobs e.g. if importing a link
 *  http://somewebsite.com/category, then it might insert jobs to import
 *  http://somewebsite.com/category/item1, http://somewebsite.com/category/item2 etc. Those jobs
 *  will then be processed as part of the job itself.
 */
interface ContentImportPlugin : DIAware {

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
     *
     * @param uri - the URI from which the content data can be accessed
     * @param originalFilename if the URI does not include the original filename (e.g. as is
     *        the case for temporary uploads et.c), then the original filename as the user selected
     *        it.
     */
    suspend fun extractMetadata(
        uri: DoorUri,
        originalFilename: String?
    ): MetadataResult?

    /**
     * The plugin should actually process the given ContentJobItem (e.g. import, download, etc).
     *
     * If a FatalContentJobException is thrown, no retry attempt will be made. If any other exception
     * is thrown, processJob may be retried by ContentJobRunner
     *
     * @param jobItem ContentJobItemAndContentJob with the job item to process
     * @param progressListener simple progress listener
     * @param transactionRunner mutex-based transaction runner to avoid potential conflicts - see
     *        ContentJobItemTransactionRunner
     */
    suspend fun processJob(
        jobItem: ContentJobItemAndContentJob,
        progressListener: ContentJobProgressListener,
        transactionRunner: ContentJobItemTransactionRunner,
    ) : ProcessResult

}