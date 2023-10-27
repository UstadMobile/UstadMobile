package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.tincan.TinCanXML
import java.util.zip.ZipInputStream
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.requireSourceAsDoorUri
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libcache.UstadCache
import org.kodein.di.DI
import org.xmlpull.v1.XmlPullParserFactory
import kotlinx.coroutines.*
import kotlinx.io.asInputStream
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class XapiZipContentImportPlugin(
    endpoint: Endpoint,
    override val di: DI,
    private val cache: UstadCache,
    uriHelper: UriHelper,
    uploader: ContentPluginUploader = DefaultContentPluginUploader(di),
) : AbstractContentImportPlugin(endpoint, uploader, uriHelper) {

    val viewName: String
        get() = XapiPackageContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
        get() = SupportedContent.XAPI_MIME_TYPES

    override val supportedFileExtensions: List<String>
        get() = SupportedContent.ZIP_EXTENSIONS


    override val pluginId: Int
        get() = PLUGIN_ID

    private val MAX_SIZE_LIMIT: Long = 100 * 1024 * 1024 //100MB


    override suspend fun extractMetadata(uri: DoorUri): MetadataResult? {
        val size = uriHelper.getSize(uri)
        if(size > MAX_SIZE_LIMIT){
            return null
        }

        val mimeType = uriHelper.getMimeType(uri)
        if (mimeType != null && !supportedMimeTypes.contains(mimeType)) {
            return null
        }
        return withContext(Dispatchers.Default) {
            return@withContext ZipInputStream(uriHelper.openSource(uri).asInputStream()).use {
                it.skipToEntry { it.name == TINCAN_FILENAME } ?: return@withContext null

                val xppFactory = XmlPullParserFactory.newInstance()
                val xpp = xppFactory.newPullParser()
                xpp.setInput(it, "UTF-8")
                val activity = TinCanXML.loadFromXML(xpp).launchActivity
                        ?: return@withContext null

                val entry = ContentEntryWithLanguage().apply {
                    contentFlags = ContentEntry.FLAG_IMPORTED
                    licenseType = ContentEntry.LICENSE_TYPE_OTHER
                    title = if (activity.name.isNullOrEmpty())
                        uriHelper.getFileName(uri)
                    else
                        activity.name
                    contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                    description = activity.desc
                    leaf = true
                    entryId = activity.id
                    sourceUrl = uri.uri.toString()
                }
                MetadataResult(entry, PLUGIN_ID)
            }
        }
    }

    override suspend fun addToCache(
        jobItem: ContentJobItemAndContentJob,
        progressListener: ContentJobProgressListener
    ): ContentEntryVersion {
        val jobUri = jobItem.contentJobItem.requireSourceAsDoorUri()
        val db: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_DB)

        val tinCanEntry = ZipInputStream(uriHelper.openSource(jobUri).asInputStream()).use {
            it.skipToEntry { it.name == TINCAN_FILENAME }
        } ?: throw FatalContentJobException("XapiImportPlugin: no tincan entry file")

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextIdAsync(
            ContentEntryVersion.TABLE_ID)

        val urlPrefix = createContentUrlPrefix(contentEntryVersionUid)

        val contentEntryVersion = ContentEntryVersion(
            cevUid = contentEntryVersionUid,
            cevContentType = ContentEntryVersion.TYPE_XAPI,
            cevContentEntryUid = jobItem.contentJobItem?.cjiContentEntryUid ?: 0L,
            cevUrl = "$urlPrefix${tinCanEntry.name}"
        )

        cache.storeZip(
            zipSource = uriHelper.openSource(jobUri),
            urlPrefix = urlPrefix,
            retain = true,
        )

        return contentEntryVersion
    }

    companion object {

        const val TINCAN_FILENAME = "tincan.xml"

        const val PLUGIN_ID = 8

    }
}