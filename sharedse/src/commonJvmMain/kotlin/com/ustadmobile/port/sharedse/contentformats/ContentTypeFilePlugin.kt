package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage

import java.io.File
import java.net.URI

/**
 * @author kileha3
 */
interface ContentTypeFilePlugin : ContentTypePlugin {

    /**
     * Get content entry from imported epub file
     * @param uri file to be imported
     * @return constructed content entry from the file.
     */
    fun getContentEntry(uri: String): ContentEntryWithLanguage?


    fun importMode(): Int

}
