package com.ustadmobile.core.contentformats.xapi

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.contentformats.ContentImporter
import com.ustadmobile.core.tincan.TinCanXML
import java.util.zip.ZipInputStream
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.requireSourceAsDoorUri
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libcache.UstadCache
import org.xmlpull.v1.XmlPullParserFactory
import kotlinx.coroutines.*
import kotlinx.io.asInputStream

class XapiZipContentImporter(
    endpoint: Endpoint,
    private val db: UmAppDatabase,
    private val cache: UstadCache,
    private val uriHelper: UriHelper,
) : ContentImporter(endpoint) {

    val viewName: String
        get() = XapiPackageContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
        get() = SupportedContent.XAPI_MIME_TYPES

    override val supportedFileExtensions: List<String>
        get() = SupportedContent.ZIP_EXTENSIONS

    override val formatName: String
        get() = "Experience API (TinCan) Zip"

    override val importerId: Int
        get() = PLUGIN_ID

    private val MAX_SIZE_LIMIT: Long = 100 * 1024 * 1024 //100MB


    override suspend fun extractMetadata(
        uri: DoorUri,
        originalFilename: String?,
    ): MetadataResult? {
        val size = uriHelper.getSize(uri)
        if(size > MAX_SIZE_LIMIT){
            return null
        }

        val mimeType = uriHelper.getMimeType(uri)
        if (mimeType != null && !supportedMimeTypes.contains(mimeType)) {
            return null
        }
        return withContext(Dispatchers.IO) {
            var isTinCan = false
            try {
                return@withContext ZipInputStream(uriHelper.openSource(uri).asInputStream()).use {
                    it.skipToEntry { it.name == TINCAN_FILENAME } ?: return@withContext null
                    isTinCan = true

                    val xppFactory = XmlPullParserFactory.newInstance()
                    val xpp = xppFactory.newPullParser()
                    xpp.setInput(it, "UTF-8")
                    val activity = TinCanXML.loadFromXML(xpp).launchActivity
                        ?: throw IllegalArgumentException("Could not load launch activity")

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
            }catch(e: Throwable) {
                if(isTinCan) {
                    //This is a zip file with a tincan.xml file, but something went wrong.
                    throw InvalidContentException("Invalid tincan.xml: ${e.message}", e)
                }else {
                    throw e
                }
            }
        }
    }

    override suspend fun importContent(
        jobItem: ContentEntryImportJob,
        progressListener: ContentImportProgressListener
    ): ContentEntryVersion {
        val jobUri = jobItem.requireSourceAsDoorUri()
        val tinCanEntry = ZipInputStream(uriHelper.openSource(jobUri).asInputStream()).use {
            it.skipToEntry { it.name == TINCAN_FILENAME }
        } ?: throw FatalContentJobException("XapiImportPlugin: no tincan entry file")

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextIdAsync(
            ContentEntryVersion.TABLE_ID)

        val urlPrefix = createContentUrlPrefix(contentEntryVersionUid)

        val contentEntryVersion = ContentEntryVersion(
            cevUid = contentEntryVersionUid,
            cevContentType = ContentEntryVersion.TYPE_XAPI,
            cevContentEntryUid = jobItem.cjiContentEntryUid,
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