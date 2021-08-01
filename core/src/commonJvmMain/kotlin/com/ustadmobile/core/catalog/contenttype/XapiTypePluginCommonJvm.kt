package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParserException
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZip
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.ContentPlugin
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.contentjob.ProcessResult
import com.ustadmobile.core.contentjob.SupportedContent
import com.ustadmobile.core.io.ext.skipToEntry
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.openInputStream
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.lib.db.entities.ContentJobItem
import org.xmlpull.v1.XmlPullParserFactory
import java.lang.IllegalArgumentException


class XapiTypePluginCommonJvm(private var context: Any, private val endpoint: Endpoint, override val di: DI) : ContentPlugin {

    val viewName: String
        get() = XapiPackageContentView.VIEW_NAME

    override val supportedMimeTypes:  List<String>
        get() = SupportedContent.XAPI_MIME_TYPES

    override val supportedFileExtensions:  List<String>
        get() = SupportedContent.ZIP_EXTENSIONS

    override val jobType: Int
        get() = TODO("Not yet implemented")

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    val defaultContainerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    override suspend fun canProcess(doorUri: DoorUri, process: ProcessContext): Boolean {
        return findTincanEntry(doorUri)
    }

    override suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): ContentEntryWithLanguage? {
        return withContext(Dispatchers.Default) {
            val inputStream = uri.openInputStream(context)
            return@withContext ZipInputStream(inputStream).use {
                it.skipToEntry { it.name == TINCAN_FILENAME } ?: throw IllegalArgumentException("no h5p file")

                val xppFactory = XmlPullParserFactory.newInstance()
                val xpp = xppFactory.newPullParser()
                xpp.setInput(it, "UTF-8")
                val activity = TinCanXML.loadFromXML(xpp).launchActivity
                        ?: throw IOException("TinCanXml from name has no launchActivity!")

                ContentEntryWithLanguage().apply {
                    contentFlags = ContentEntry.FLAG_IMPORTED
                    licenseType = ContentEntry.LICENSE_TYPE_OTHER
                    title =  if(activity.name.isNullOrEmpty())
                        uri.getFileName(context) else activity.name
                    contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                    description = activity.desc
                    leaf = true
                    entryId = activity.id
                }
            }
        }
    }

    override suspend fun processJob(jobItem: ContentJobItem, process: ProcessContext): ProcessResult {
        val uri = jobItem.fromUri ?: return ProcessResult(404)
        val doorUri = DoorUri.parse(uri)
        val container = Container().apply {
            containerContentEntryUid = jobItem.cjiContentEntryUid
            cntLastModified = System.currentTimeMillis()
            mimeType = supportedMimeTypes.first()
            containerUid = repo.containerDao.insertAsync(this)
        }
        val containerFolder = jobItem.toUri ?: defaultContainerDir.toURI().toString()
        val containerFolderUri = DoorUri.parse(containerFolder)

        repo.addEntriesToContainerFromZip(container.containerUid,
                doorUri,
                ContainerAddOptions(storageDirUri = containerFolderUri), context)

        repo.containerDao.findByUid(container.containerUid)

        return ProcessResult(200)
    }

    suspend fun findTincanEntry(doorUri: DoorUri): Boolean {
        return withContext(Dispatchers.Default) {
            val inputStream = doorUri.openInputStream(context)
            return@withContext ZipInputStream(inputStream).use {
                it.skipToEntry { entry -> entry.name == TINCAN_FILENAME } != null
            }
        }
    }

    companion object {

        const val TINCAN_FILENAME = "tincan.xml"

    }
}