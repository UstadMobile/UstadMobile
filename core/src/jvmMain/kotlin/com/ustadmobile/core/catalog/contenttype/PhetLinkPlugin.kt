package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import org.jsoup.Jsoup
import org.jsoup.select.Elements

import org.kodein.di.DI
import java.io.File

class PhetLinkPlugin(
    private var context: Any,
    private val endpoint: Endpoint,
    override val di: DI,
): ContentPlugin {

    override val pluginId: Int
        get() = PLUGIN_ID

    override val supportedMimeTypes: List<String>
        get() = TODO("Not yet implemented")
    override val supportedFileExtensions: List<String>
        get() = TODO("Not yet implemented")

    override suspend fun extractMetadata(
        uri: DoorUri,
        process: ContentJobProcessContext
    ): MetadataResult? {
        if(!uri.toString().startsWith("https://phet.colorado.edu/"))
            return null


        val localUri = process.getLocalOrCachedUri()
        val downloadedFile: File = localUri.toFile()
        val fileContent = downloadedFile.readText()
        val htmlContent = Jsoup.parse(fileContent)
        val htmlTitle = htmlContent.title()
        println(fileContent)
        println(htmlTitle)

        return MetadataResult(ContentEntryWithLanguage().apply {
            title = htmlTitle
            val headTags: Elements = htmlContent.getElementsByTag("head")
            val metaTags: Elements = headTags.get(0).getElementsByTag("meta")

            for (metaTag in metaTags) {
                val content: String = metaTag.attr("content")
                val name: String = metaTag.attr("name")
                if (name.equals("description")) {
                    this.description = content
                }
            }
        }, pluginId)
    }

    override suspend fun processJob(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progress: ContentJobProgressListener
    ): ProcessResult {
        TODO("Not yet implemented")
    }

    companion object {
        const val PLUGIN_ID = 102

    }

}