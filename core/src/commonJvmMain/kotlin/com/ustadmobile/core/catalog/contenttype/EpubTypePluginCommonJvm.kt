package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ContainerBuilder
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.util.ext.alternative
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.openInputStream
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.DI
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.util.*
import io.ktor.client.*
import java.util.zip.ZipInputStream
import kotlinx.coroutines.*
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class EpubTypePluginCommonJvm(
    context: Any,
    endpoint: Endpoint,
    override val di: DI,
    uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
) : ContentImportContentPlugin(endpoint, context, uploader) {

    val viewName: String
        get() = EpubContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
        get() = SupportedContent.EPUB_MIME_TYPES

    override val supportedFileExtensions: List<String>
        get() = SupportedContent.EPUB_EXTENSIONS

    override val pluginId: Int
        get() = PLUGIN_ID


    override suspend fun extractMetadata(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        val mimeType = uri.guessMimeType(context, di)
        if(mimeType != null && !supportedMimeTypes.contains(mimeType)){
            return null
        }
        return withContext(Dispatchers.Default) {
            val xppFactory = XmlPullParserFactory.newInstance()
            try {
                val localUri = process.getLocalOrCachedUri()
                val opfPath = ZipInputStream(localUri.openInputStream(context)).use {
                    it.skipToEntry { entry -> entry.name == OCF_CONTAINER_PATH } ?: return@use null

                    val ocfContainer = OcfDocument()
                    val xpp = xppFactory.newPullParser()
                    xpp.setInput(it, "UTF-8")
                    ocfContainer.loadFromParser(xpp)

                    return@use ocfContainer.rootFiles.firstOrNull()?.fullPath
                } ?: return@withContext null

                return@withContext  ZipInputStream(localUri.openInputStream(context)).use {

                    it.skipToEntry { it.name == opfPath } ?: return@use null

                    val xpp = xppFactory.newPullParser()
                    xpp.setInput(it, "UTF-8")
                    val opfDocument = OpfDocument()
                    opfDocument.loadFromOPF(xpp)

                    val entry = ContentEntryWithLanguage().apply {
                        contentFlags = ContentEntry.FLAG_IMPORTED
                        contentTypeFlag = ContentEntry.TYPE_EBOOK
                        licenseType = ContentEntry.LICENSE_TYPE_OTHER
                        title = if (opfDocument.title.isNullOrEmpty()) localUri.getFileName(context)
                        else opfDocument.title
                        author = opfDocument.getCreator(0)?.creator
                        description = opfDocument.description
                        leaf = true
                        sourceUrl = uri.uri.toString()
                        entryId = opfDocument.id.alternative(UUID.randomUUID().toString())
                        val languageCode = opfDocument.getLanguage(0)
                        if (languageCode != null) {
                            this.language = Language().apply {
                                iso_639_1_standard = languageCode
                            }
                        }
                    }
                    return@use MetadataResult(entry, PLUGIN_ID)
                }
            } catch (e: Exception) {
                null
            }
        }
    }


    override suspend fun makeContainer(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progressListener: ContentJobProgressListener,
        containerStorageUri: DoorUri,
    ): Container {
        val repo: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_REPO)

        return repo.containerBuilder(jobItem.contentJobItem?.cjiContentEntryUid ?: 0,
                supportedMimeTypes.first(), containerStorageUri)
            .addZip(process.getLocalOrCachedUri(), context)
            .build()
    }

    companion object {

        private const val OCF_CONTAINER_PATH = "META-INF/container.xml"

        const val PLUGIN_ID = 2
    }
}
