package com.ustadmobile.core.contentformats

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.*

/**
 * A ContentImporter can :
 *
 *  Extract Metadata:
 *  Given a URI (e.g. to a file or valid link), extract Metadata as a ContentEntry entity as far as
 *  possible e.g. title, author, license, etc.
 *
 *  Import content:
 *  Given a ContentJobItem with a Uri, create a ContentEntryVersion (generate the manifest and save
 *  content to the cache).
 *
 */
abstract class ContentImporter(
    protected val learningSpace: LearningSpace,
)  {

    /**
     * This must be a unique integer. It can be used by components to remember what plugin to use
     * e.g. when extractMetadata is called, the plugin id that successfully extracted the metadata
     * will be saved to the ContentJobItem such that importContent will not have to guess which plugin
     * to use.
     */
    abstract val importerId: Int

    abstract val supportedMimeTypes: List<String>

    /**
     * A list of the file extensions that are supported by this plugin. They should be lowercase
     * without the dot. e.g. "pdf"
     */
    abstract val supportedFileExtensions: List<String>

    /**
     * A human readable format name (e.g. "PDF", Video(MP4, M4V, WEBM, QuickTime)". This is used to
     * create a list of supported content types.
     */
    abstract val formatName: String

    /**
     * The plugin should extract metadata from the given uri (if possible) and return a
     * MetadataResult if Metadata is retrieved, or null otherwise.
     *
     * @param uri - the URI from which the content data can be accessed
     * @param originalFilename if the URI does not include the original filename (e.g. as is
     *        the case for temporary uploads et.c), then the original filename as the user selected
     *        it.
     */
    abstract suspend fun extractMetadata(
        uri: DoorUri,
        originalFilename: String?
    ): MetadataResult?

    /**
     * The plugin should actually process the given ContentJobItem (e.g. import, download, etc).
     *
     * If a FatalContentJobException is thrown, no retry attempt will be made. If any other exception
     * is thrown, processJob may be retried
     *
     * @param jobItem ContentJobItemAndContentJob with the job item to process
     * @param progressListener simple progress listener
     */
    abstract suspend fun importContent(
        jobItem: ContentEntryImportJob,
        progressListener: ContentImportProgressListener,
    ) : ContentEntryVersion


    /**
     * Create the URL prefix for a content item in the form of:
     * https://endpointserer.com/api/content/contentEntryVersionUid/
     */
    protected fun createContentUrlPrefix(contentEntryVersionUid: Long): String {
        return learningSpace.url + ContentEntryVersion.PATH_POSTFIX + contentEntryVersionUid + "/"
    }


}