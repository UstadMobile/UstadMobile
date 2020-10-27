package com.ustadmobile.core.contentformats

import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.lib.db.entities.ContainerUploadJob

/**
 *
 */
interface ContentImportManager {

    /**
     * Extract the ContentEntry object from the given file path. This is essentially the same as
     * extractContentEntryMetadataFromFile.
     *
     * @param filePath the path to the file
     * @return ImportContentEntryMetaData object
     */
    suspend fun extractMetadata(filePath: String) : ImportedContentEntryMetaData?

    /**
     * Queue the given file path to be imported and then uploaded. On Android this should start a
     * foreground service that will run the import and upload. On JVM this can just be forked into
     * the background.
     *
     */
    suspend fun queueImportContentFromFile(filePath: String, metadata: ImportedContentEntryMetaData): ContainerUploadJob


    /**
     * Import the given file path to a container. This may involve performing extra compression work.
     *
     * This can be called directly, but would more likely be called by ImportJobRunner.
     *
     * This would lookup the right plugin to use to do the import, and then use the
     * ContentTypePlugin#importToContainer to run the import
     */
    suspend fun importFileToContainer(filePath: String, metadata: ImportedContentEntryMetaData,
                                      progressListener: (Int) -> Unit)



}