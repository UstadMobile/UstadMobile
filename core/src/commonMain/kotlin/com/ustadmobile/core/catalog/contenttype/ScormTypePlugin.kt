package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage


/**
 * Created by mike on 1/6/18.
 */

open class ScormTypePlugin : ContentTypePlugin {

    override val viewName: String
        get() = "ScormViewTodo"

    override val mimeTypes: Array<String>
        get() = arrayOf(*MIME_TYPES)

    override val fileExtensions: Array<String>
        get() = arrayOf("zip")

    override suspend fun extractMetadata(uri: String, context: Any): ContentEntryWithLanguage? {
        TODO("Not yet implemented")
    }

    override suspend fun importToContainer(uri: String, conversionParams: Map<String, String>, contentEntryUid: Long, mimeType: String, containerBaseDir: String, context: Any, db: UmAppDatabase, repo: UmAppDatabase, progressListener: (Int) -> Unit): Container {
        TODO("Not yet implemented")
    }

    companion object {

        val MIME_TYPES = arrayOf("application/scorm+zip")
    }

}
