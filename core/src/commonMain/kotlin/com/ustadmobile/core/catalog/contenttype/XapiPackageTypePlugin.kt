package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage

/**
 * Created by mike on 9/13/17.
 *
 *
 */

open class XapiPackageTypePlugin : ContentTypePlugin {

    override val viewName: String
        get() = XapiPackageContentView.VIEW_NAME

    override val mimeTypes:  Array<String>
        get() = MIME_TYPES

    override val fileExtensions:  Array<String>
        get() = FILE_EXTENSIONS

    override suspend fun extractMetadata(uri: String, context: Any): ContentEntryWithLanguage? {
        TODO("Not yet implemented")
    }

    override suspend fun importToContainer(uri: String, conversionParams: Map<String, String>, contentEntryUid: Long, mimeType: String, containerBaseDir: String, context: Any, db: UmAppDatabase, repo: UmAppDatabase, progressListener: (Int) -> Unit): Container {
        TODO("Not yet implemented")
    }


    companion object {

        val MIME_TYPES = arrayOf("application/tincan+zip", "application/zip")

        private val FILE_EXTENSIONS = arrayOf("zip")

        //As per spec - there should be one and only one tincan.xml file
        protected val XML_FILE_NAME = "tincan.xml"
    }

}
