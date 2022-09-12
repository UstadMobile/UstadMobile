package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.contentjob.SupportedContent
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import io.ktor.http.*

abstract class PhetLinkTypePlugin : ContentTypePlugin {

    override val viewName: String
        get() = "PhetLinkTodo"

    override val mimeTypes: Array<String>
        get() = MIME_TYPES

    override val fileExtensions: Array<String>
        get() = arrayOf("")

    companion object {

        val MIME_TYPES = arrayOf("application/html")
    }

}