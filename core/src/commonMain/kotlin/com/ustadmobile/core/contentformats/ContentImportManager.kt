package com.ustadmobile.core.contentformats

import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerImportJob

/**
 * Manager that handles importing content
 */
@Deprecated("use ContentJobPlugin to process imports")
interface ContentImportManager {

    /**
     * Extract the ContentEntry object from the given file path. This is essentially the same as
     * extractContentEntryMetadataFromFile.
     *
     * @param uri to extract
     * @return ImportContentEntryMetaData object
     */
    suspend fun extractMetadata(uri: String) : ImportedContentEntryMetaData?

    /**
     * Queue the given file path to be imported, and (if required), then uploaded. This would
     * be done using a LiveDataWorkQueue and ImportJobRunner. On Android this should also start a
     * foreground service that can display the progress of the job for the user.
     *
     * @param uri that should be imported
     * @param metadata ImportedContentEntryMetaData for the entry to be imported
     * @param containerBaseDir path to container folder
     *
     * @return ImportJob (that is saved into the database)
     */
    suspend fun queueImportContentFromFile(uri: String, metadata: ImportedContentEntryMetaData,
                                           containerBaseDir: String, importMode: Int, conversionParams: Map<String, String>): ContainerImportJob


    /**
     * Import the given uri to a container. This may involve performing extra compression work.
     *
     * This can be called directly, but would more likely be called by ImportJobRunner.
     *
     * This would lookup the right plugin to use to do the import, and then use the
     * ContentTypePlugin#importToContainer to run the import
     */
    suspend fun importFileToContainer(uri: String, mimeType: String, contentEntryUid: Long,
                                      containerBaseDir: String, conversionParams: Map<String,String>,
                                      progressListener: (Int) -> Unit): Container?

    fun getMimeTypeSupported(): List<String>

    fun getExtSupported(): List<String>

}