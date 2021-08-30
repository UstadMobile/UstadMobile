package com.ustadmobile.core.catalog.contenttype

import com.turn.ttorrent.tracker.Tracker
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.zip.ZipInputStream
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.contentjob.ext.processMetadata
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.torrent.UstadTorrentManager
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.openInputStream
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.direct
import org.kodein.di.on
import com.ustadmobile.lib.db.entities.ContentJobItem
import org.xmlpull.v1.XmlPullParserFactory


class XapiTypePluginCommonJvm(private var context: Any, private val endpoint: Endpoint, override val di: DI) : ContentPlugin {

    val viewName: String
        get() = XapiPackageContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
        get() = SupportedContent.XAPI_MIME_TYPES

    override val supportedFileExtensions: List<String>
        get() = SupportedContent.ZIP_EXTENSIONS


    override val pluginId: Int
        get() = TODO("Not yet implemented")

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val defaultContainerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val torrentDir: File by di.on(endpoint).instance(tag = DiTag.TAG_TORRENT_DIR)

    private val tracker: Tracker = di.direct.instance<Tracker>()

    private val ustadTorrentManager: UstadTorrentManager by di.on(endpoint).instance()

    override suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): MetadataResult? {
        val mimeType = uri.guessMimeType(context, di)
        if (mimeType != null && !supportedMimeTypes.contains(mimeType)) {
            return null
        }
        return withContext(Dispatchers.Default) {
            val localUri = process.getLocalUri(uri, context, di)
            val inputStream = localUri.openInputStream(context)
            return@withContext ZipInputStream(inputStream).use {
                it.skipToEntry { it.name == TINCAN_FILENAME } ?: return@withContext null

                val xppFactory = XmlPullParserFactory.newInstance()
                val xpp = xppFactory.newPullParser()
                xpp.setInput(it, "UTF-8")
                val activity = TinCanXML.loadFromXML(xpp).launchActivity
                        ?: throw IOException("TinCanXml from name has no launchActivity!")

                val entry = ContentEntryWithLanguage().apply {
                    contentFlags = ContentEntry.FLAG_IMPORTED
                    licenseType = ContentEntry.LICENSE_TYPE_OTHER
                    title = if (activity.name.isNullOrEmpty())
                        uri.getFileName(context) else activity.name
                    contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                    description = activity.desc
                    leaf = true
                    entryId = activity.id
                }
                MetadataResult(entry)
            }
        }
    }

    override suspend fun processJob(jobItem: ContentJobItem, process: ProcessContext, progress: ContentJobProgressListener): ProcessResult {
        val uri = jobItem.fromUri ?: return ProcessResult(404)
        val container = withContext(Dispatchers.Default) {

            val contentEntryUid = processMetadata(jobItem, process,context, endpoint)
            val localUri = process.getLocalUri(DoorUri.parse(uri), context, di)

            val container = Container().apply {
                containerContentEntryUid = contentEntryUid
                cntLastModified = System.currentTimeMillis()
                mimeType = supportedMimeTypes.first()
                containerUid = repo.containerDao.insertAsync(this)
                jobItem.cjiContainerUid = containerUid
            }
            val containerFolder = jobItem.toUri ?: defaultContainerDir.toURI().toString()
            val containerFolderUri = DoorUri.parse(containerFolder)

            repo.addEntriesToContainerFromZip(container.containerUid,
                    localUri,
                    ContainerAddOptions(storageDirUri = containerFolderUri), context)

            repo.addTorrentFileFromContainer(container.containerUid,
                    DoorUri.parse(torrentDir.toURI().toString()), tracker.announceUrl)

            ustadTorrentManager.addTorrent(container.containerUid)

            repo.containerDao.findByUid(container.containerUid)

        }

        return ProcessResult(200)
    }

    companion object {

        const val TINCAN_FILENAME = "tincan.xml"

    }
}