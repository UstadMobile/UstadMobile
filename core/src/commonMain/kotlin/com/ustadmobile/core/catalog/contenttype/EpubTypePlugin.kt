package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage

/**
 * Created by mike on 9/9/17.
 */

open class EpubTypePlugin : ContentTypePlugin {

    override val viewName: String
        get() = EpubContentView.VIEW_NAME

    override val mimeTypes: Array<String>
        get() = arrayOf(*MIME_TYPES)

    override val fileExtensions: Array<String>
        get() = arrayOf(*EXTENSIONS)

    override suspend fun extractMetadata(uri: String, context: Any): ContentEntryWithLanguage? {
        throw IllegalStateException("Not implemented in default")
    }

    override suspend fun importToContainer(uri: String, conversionParams: Map<String, String>, contentEntryUid: Long, mimeType: String, containerBaseDir: String, context: Any, db: UmAppDatabase, repo: UmAppDatabase, progressListener: (Int) -> Unit): Container {
        throw IllegalStateException("Not implemented in default")
    }

    companion object {

        val MIME_TYPES = arrayOf("application/epub+zip")

        val EXTENSIONS = arrayOf("epub")

        val OCF_CONTAINER_PATH = "META-INF/container.xml"
    }

}
