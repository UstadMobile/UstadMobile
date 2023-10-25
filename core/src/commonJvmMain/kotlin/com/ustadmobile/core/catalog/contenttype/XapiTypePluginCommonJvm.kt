package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.tincan.TinCanXML
import java.util.zip.ZipInputStream
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.DI
import org.xmlpull.v1.XmlPullParserFactory
import kotlinx.coroutines.*
import kotlinx.io.asInputStream

class XapiTypePluginCommonJvm(
    endpoint: Endpoint,
    override val di: DI,
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

    private val MAX_SIZE_LIMIT: Long = 100 * 1024 * 1024


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
        TODO()
        /*val repo: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_REPO)

        return repo.containerBuilder(jobItem.contentJobItem?.cjiContentEntryUid ?: 0,
                supportedMimeTypes.first(), containerStorageUri)
            .addZip(process.getLocalOrCachedUri(), context)
            .build()*/
    }

    companion object {

        const val TINCAN_FILENAME = "tincan.xml"

        const val PLUGIN_ID = 8

    }
}