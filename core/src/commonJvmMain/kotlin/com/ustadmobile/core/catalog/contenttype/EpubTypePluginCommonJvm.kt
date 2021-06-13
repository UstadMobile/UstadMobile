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
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZip
import java.io.File
import java.io.IOException
import java.util.*
import java.util.zip.ZipInputStream
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument
import com.ustadmobile.core.io.ext.skipToEntry
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.openInputStream
import org.xmlpull.v1.XmlPullParserFactory

class EpubTypePluginCommonJvm() : EpubTypePlugin() {

    override suspend fun extractMetadata(uri: String, context: Any): ContentEntryWithLanguage? {
        return withContext(Dispatchers.Default) {
            val xppFactory = XmlPullParserFactory.newInstance()
            try {

                val doorUri = DoorUri.parse(uri)
                val inputStream = doorUri.openInputStream(context)

                val opfPath: String = ZipInputStream(inputStream).use {
                    val metaDataEntry = it.skipToEntry { it.name == "META-INF/container.xml" }
                    if(metaDataEntry != null) {
                        val ocfContainer = OcfDocument()
                        val xpp = xppFactory.newPullParser()
                        xpp.setInput(it, "UTF-8")
                        ocfContainer.loadFromParser(xpp)

                        ocfContainer.rootFiles.firstOrNull()?.fullPath
                    }else {
                        null
                    }
                } ?: return@withContext null

                return@withContext ZipInputStream(doorUri.openInputStream(context)).use {
                    val entry = it.skipToEntry { it.name == opfPath } ?: return@use null

                    val xpp = xppFactory.newPullParser()
                    xpp.setInput(it, "UTF-8")
                    val opfDocument = OpfDocument()
                    opfDocument.loadFromOPF(xpp)

                    ContentEntryWithLanguage().apply {
                        contentFlags = ContentEntry.FLAG_IMPORTED
                        contentTypeFlag = ContentEntry.TYPE_EBOOK
                        licenseType = ContentEntry.LICENSE_TYPE_OTHER
                        title = opfDocument.title
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
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            }

            null
        }
    }

    override suspend fun importToContainer(uri: String, conversionParams: Map<String, String>,
                                           contentEntryUid: Long, mimeType: String,
                                           containerBaseDir: String, context: Any,
                                           db: UmAppDatabase, repo: UmAppDatabase,
                                           progressListener: (Int) -> Unit): Container {

        return withContext(Dispatchers.Default) {

            val doorUri = DoorUri.parse(uri)
            val container = Container().apply {
                containerContentEntryUid = contentEntryUid
                cntLastModified = System.currentTimeMillis()
                this.mimeType = mimeType
                containerUid = repo.containerDao.insert(this)
            }

            repo.addEntriesToContainerFromZip(container.containerUid,
                    doorUri,
                    ContainerAddOptions(storageDirUri = File(containerBaseDir).toDoorUri()), context)

            val containerWithSize = repo.containerDao.findByUid(container.containerUid) ?: container

            containerWithSize
        }
    }
}