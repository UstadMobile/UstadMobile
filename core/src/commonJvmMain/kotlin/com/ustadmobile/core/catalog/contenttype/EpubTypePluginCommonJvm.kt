package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.alternative
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libcache.UstadCache
import org.kodein.di.DI
import org.xmlpull.v1.XmlPullParserFactory
import java.util.*
import java.util.zip.ZipInputStream
import kotlinx.coroutines.*
import kotlinx.io.asInputStream
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class EpubTypePluginCommonJvm(
    endpoint: Endpoint,
    override val di: DI,
    private val cache: UstadCache,
    uriHelper: UriHelper,
    uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
) : AbstractContentImportPlugin(endpoint, uploader, uriHelper) {

    val viewName: String
        get() = EpubContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
        get() = SupportedContent.EPUB_MIME_TYPES

    override val supportedFileExtensions: List<String>
        get() = SupportedContent.EPUB_EXTENSIONS

    override val pluginId: Int
        get() = PLUGIN_ID

    private fun ZipInputStream.findFirstOpfPath(): String? {
        val xppFactory = XmlPullParserFactory.newInstance()
        skipToEntry { entry -> entry.name == OCF_CONTAINER_PATH } ?: return null

        val ocfContainer = OcfDocument()
        val xpp = xppFactory.newPullParser()
        xpp.setInput(this, "UTF-8")
        ocfContainer.loadFromParser(xpp)

        return ocfContainer.rootFiles.firstOrNull()?.fullPath
    }

    override suspend fun extractMetadata(uri: DoorUri): MetadataResult? {
        val mimeType = uriHelper.getMimeType(uri)
        if(mimeType != null && !supportedMimeTypes.contains(mimeType)){
            return null
        }
        return withContext(Dispatchers.Default) {
            val xppFactory = XmlPullParserFactory.newInstance()
            try {
                val opfPath = ZipInputStream(uriHelper.openSource(uri).asInputStream()).use {
                    it.findFirstOpfPath()
                } ?: return@withContext null

                return@withContext  ZipInputStream(uriHelper.openSource(uri).asInputStream()).use {

                    it.skipToEntry { it.name == opfPath } ?: return@use null

                    val xpp = xppFactory.newPullParser()
                    xpp.setInput(it, "UTF-8")
                    val opfDocument = OpfDocument()
                    opfDocument.loadFromOPF(xpp)

                    val entry = ContentEntryWithLanguage().apply {
                        contentFlags = ContentEntry.FLAG_IMPORTED
                        contentTypeFlag = ContentEntry.TYPE_EBOOK
                        licenseType = ContentEntry.LICENSE_TYPE_OTHER
                        title = if (opfDocument.title.isNullOrEmpty())
                            uriHelper.getFileName(uri)
                        else
                            opfDocument.title
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


    override suspend fun addToCache(
        jobItem: ContentJobItemAndContentJob,
        progressListener: ContentJobProgressListener,
    ): ContentEntryVersion {
        val jobUri = jobItem.contentJobItem?.sourceUri?.let { DoorUri.parse(it) }
            ?: throw IllegalArgumentException("no sourceUri")
        val db: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_DB)

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID)
        val urlPrefix = endpoint.url + ContentEntryVersion.PATH_POSTFIX + contentEntryVersionUid + "/"

        val opfPath = ZipInputStream(uriHelper.openSource(jobUri).asInputStream()).use {
            it.findFirstOpfPath()
        }

        val contentEntryVersion = ContentEntryVersion(
            cevUid = contentEntryVersionUid,
            cevContentType = ContentEntryVersion.TYPE_EPUB,
            cevUrl = "$urlPrefix$opfPath"
        )

        cache.storeZip(
            zipSource = uriHelper.openSource(jobUri),
            urlPrefix = urlPrefix,
            retain = true,
        )

        return contentEntryVersion
    }

    companion object {

        private const val OCF_CONTAINER_PATH = "META-INF/container.xml"

        const val PLUGIN_ID = 2
    }
}
