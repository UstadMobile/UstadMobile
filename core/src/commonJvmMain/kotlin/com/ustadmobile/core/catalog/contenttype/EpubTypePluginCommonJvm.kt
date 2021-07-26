package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.alternative
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DIAware
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZip
import java.io.File
import java.util.*
import java.util.zip.ZipInputStream
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument
import com.ustadmobile.core.contentjob.ContentPlugin
import com.ustadmobile.core.contentjob.ProcessResult
import com.ustadmobile.core.contentjob.SupportedContent
import com.ustadmobile.core.io.ext.skipToEntry
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.openInputStream
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.*
import org.xmlpull.v1.XmlPullParserFactory

class EpubTypePluginCommonJvm(private val endpoint: Endpoint, override val di: DI) : DIAware, ContentPlugin {

    val viewName: String
        get() = EpubContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
        get() = listOf(*SupportedContent.EPUB_MIME_TYPES)

    override val supportedFileExtensions: List<String>
        get() = listOf(*SupportedContent.EPUB_EXTENSIONS)

    override val jobType: Int
        get() = TODO("Not yet implemented")


    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    override suspend fun canProcess(doorUri: DoorUri): Boolean {
        return findOpfPath(doorUri) != null
    }

    override suspend fun extractMetadata(uri: DoorUri): ContentEntryWithLanguage? {
        val opfPath = findOpfPath(uri) ?: return null
        return withContext(Dispatchers.Default) {
            val xppFactory = XmlPullParserFactory.newInstance()
            try {
                ZipInputStream(uri.openInputStream(context)).use {
                    it.skipToEntry { it.name == opfPath } ?: return@use null

                    val xpp = xppFactory.newPullParser()
                    xpp.setInput(it, "UTF-8")
                    val opfDocument = OpfDocument()
                    opfDocument.loadFromOPF(xpp)

                    ContentEntryWithLanguage().apply {
                        contentFlags = ContentEntry.FLAG_IMPORTED
                        contentTypeFlag = ContentEntry.TYPE_EBOOK
                        licenseType = ContentEntry.LICENSE_TYPE_OTHER
                        title = if (opfDocument.title.isNullOrEmpty()) uri.getFileName(context)
                        else opfDocument.title
                        author = opfDocument.getCreator(0)?.creator
                        description = opfDocument.description
                        leaf = true
                        entryId = opfDocument.id.alternative(UUID.randomUUID().toString())
                        val languageCode = opfDocument.getLanguage(0)
                        if (languageCode != null) {
                            this.language = Language().apply {
                                iso_639_1_standard = languageCode
                            }
                        }
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    override suspend fun processJob(jobItem: ContentJobItem): ProcessResult {
        val container = withContext(Dispatchers.Default) {
            val uri = jobItem.fromUri ?: return@withContext
            val doorUri = DoorUri.parse(uri)
            val container = Container().apply {
                containerContentEntryUid = jobItem.cjiContentEntryUid
                cntLastModified = System.currentTimeMillis()
                this.mimeType = supportedMimeTypes.first()
                containerUid = repo.containerDao.insert(this)
                jobItem.cjiContainerUid = containerUid
            }

            repo.addEntriesToContainerFromZip(container.containerUid,
                    doorUri,
                    ContainerAddOptions(storageDirUri = File(jobItem.cjiContainerBaseDir).toDoorUri()), context)

            val containerWithSize = repo.containerDao.findByUid(container.containerUid) ?: container

            containerWithSize
        }
        return ProcessResult(200)
    }


    suspend fun findOpfPath(uri: DoorUri): String? {
        return withContext(Dispatchers.Default) {
            val xppFactory = XmlPullParserFactory.newInstance()
            try {
                val inputStream = uri.openInputStream(context)

                return@withContext ZipInputStream(inputStream).use {
                    it.skipToEntry { entry -> entry.name == OCF_CONTAINER_PATH } ?: return@use

                    val ocfContainer = OcfDocument()
                    val xpp = xppFactory.newPullParser()
                    xpp.setInput(it, "UTF-8")
                    ocfContainer.loadFromParser(xpp)

                    ocfContainer.rootFiles.firstOrNull()?.fullPath
                }
            } catch (e: Exception) {

            }
        }
    }

    companion object {

        private const val OCF_CONTAINER_PATH = "META-INF/container.xml"
    }
}