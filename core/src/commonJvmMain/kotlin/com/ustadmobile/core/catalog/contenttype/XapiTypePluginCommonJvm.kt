package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.tincan.TinCanXML
import java.util.zip.ZipInputStream
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ContainerBuilder
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.openInputStream
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.DI
import io.ktor.client.*
import org.xmlpull.v1.XmlPullParserFactory
import kotlinx.coroutines.*
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class XapiTypePluginCommonJvm(
    context: Any,
    endpoint: Endpoint,
    override val di: DI,
    uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
) : ContentImportContentPlugin(endpoint, context, uploader) {

    val viewName: String
        get() = XapiPackageContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
        get() = SupportedContent.XAPI_MIME_TYPES

    override val supportedFileExtensions: List<String>
        get() = SupportedContent.ZIP_EXTENSIONS


    override val pluginId: Int
        get() = PLUGIN_ID

    private val MAX_SIZE_LIMIT: Long = 100 * 1024 * 1024


    override suspend fun extractMetadata(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        val size = uri.getSize(context, di)
        if(size > MAX_SIZE_LIMIT){
            return null
        }
        val mimeType = uri.guessMimeType(context, di)
        if (mimeType != null && !supportedMimeTypes.contains(mimeType)) {
            return null
        }
        return withContext(Dispatchers.Default) {
            val localUri = process.getLocalOrCachedUri()
            val inputStream = localUri.openInputStream(context)
            return@withContext ZipInputStream(inputStream).use {
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
                        uri.getFileName(context) else activity.name
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

    override suspend fun makeContainer(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progressListener: ContentJobProgressListener,
        containerStorageUri: DoorUri,
    ) : Container {
        val repo: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_REPO)

        return repo.containerBuilder(jobItem.contentJobItem?.cjiContentEntryUid ?: 0,
                supportedMimeTypes.first(), containerStorageUri)
            .addZip(process.getLocalOrCachedUri(), context)
            .build()
    }

    companion object {

        const val TINCAN_FILENAME = "tincan.xml"

        const val PLUGIN_ID = 8

    }
}