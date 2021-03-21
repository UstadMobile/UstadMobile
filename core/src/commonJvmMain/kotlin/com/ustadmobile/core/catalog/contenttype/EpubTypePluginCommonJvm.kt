package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.alternative
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParserException
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZip
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.door.ext.toDoorUri

class EpubTypePluginCommonJvm : EpubTypePlugin() {

    override suspend fun extractMetadata(filePath: String): ContentEntryWithLanguage? {
        return withContext(Dispatchers.Default) {
            var contentEntry: ContentEntryWithLanguage? = null
            try {
                val file = File(filePath.removePrefix("file://"))
                ZipInputStream(FileInputStream(file)).use {
                    var zipEntry: ZipEntry? = null
                    while ({ zipEntry = it.nextEntry; zipEntry }() != null) {

                        val fileName = zipEntry?.name
                        if (fileName!!.contains(".opf")) {
                            val xpp = UstadMobileSystemImpl.instance.newPullParser(it)
                            val opfDocument = OpfDocument()
                            opfDocument.loadFromOPF(xpp)
                            val contentEntryVal = ContentEntryWithLanguage()
                            contentEntryVal.contentFlags = ContentEntry.FLAG_IMPORTED
                            contentEntryVal.contentTypeFlag = ContentEntry.TYPE_EBOOK
                            contentEntryVal.licenseType = ContentEntry.LICENSE_TYPE_OTHER
                            contentEntryVal.title = if(opfDocument.title.isNullOrEmpty()) file.nameWithoutExtension else opfDocument.title
                            contentEntryVal.author = opfDocument.getCreator(0)?.creator
                            contentEntryVal.description = opfDocument.description
                            contentEntryVal.leaf = true
                            contentEntryVal.entryId = opfDocument.id.alternative(UUID.randomUUID().toString())
                            val languageCode = opfDocument.getLanguage(0)
                            if (languageCode != null) {
                                val language = Language()
                                language.iso_639_1_standard = languageCode
                                contentEntryVal.language = language
                            }
                            contentEntry = contentEntryVal
                            break
                        }

                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            }

            contentEntry
        }
    }

    override suspend fun importToContainer(filePath: String, conversionParams: Map<String, String>,
                                           contentEntryUid: Long, mimeType: String,
                                           containerBaseDir: String, context: Any,
                                           db: UmAppDatabase, repo: UmAppDatabase,
                                           progressListener: (Int) -> Unit): Container {

        return withContext(Dispatchers.Default) {

            val file = File(filePath)
            val container = Container().apply {
                containerContentEntryUid = contentEntryUid
                cntLastModified = System.currentTimeMillis()
                fileSize = file.length()
                this.mimeType = mimeType
                containerUid = repo.containerDao.insert(this)
            }

            repo.addEntriesToContainerFromZip(container.containerUid,
                    File(filePath).toDoorUri(),
                    ContainerAddOptions(storageDirUri = File(containerBaseDir).toDoorUri()))

            container
        }
    }
}