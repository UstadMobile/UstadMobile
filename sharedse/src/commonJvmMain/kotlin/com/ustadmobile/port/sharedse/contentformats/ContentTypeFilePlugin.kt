package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage

import java.io.File

/**
 * @author kileha3
 */
interface ContentTypeFilePlugin : ContentTypePlugin {

    /**
     * Get content entry from imported epub file
     * @param file file to be imported
     * @return constructed content entry from the file.
     */
    fun getContentEntry(file: File): ContentEntryWithLanguage?


    fun importMode(): Int

}
