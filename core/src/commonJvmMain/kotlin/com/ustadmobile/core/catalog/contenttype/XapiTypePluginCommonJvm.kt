package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParserException
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class XapiTypePluginCommonJvm : XapiPackageTypePlugin() {

    override suspend fun extractMetadata(filePath: String): ContentEntryWithLanguage? {
        return withContext(Dispatchers.Default) {
            var contentEntry: ContentEntryWithLanguage? = null
            try {
                val file = File(filePath)
                ZipInputStream(FileInputStream(file)).use {
                    var zipEntry: ZipEntry? = null
                    while ({ zipEntry = it.nextEntry; zipEntry }() != null) {

                        val fileName = zipEntry?.name
                        if (fileName?.toLowerCase() == "tincan.xml") {
                            val xpp = UstadMobileSystemImpl.instance.newPullParser(it)
                            val activity = TinCanXML.loadFromXML(xpp).launchActivity
                                    ?: throw IOException("TinCanXml from ${file.name} has no launchActivity!")

                            contentEntry = ContentEntryWithLanguage().apply {
                                contentFlags = ContentEntry.FLAG_IMPORTED
                                licenseType = ContentEntry.LICENSE_TYPE_OTHER
                                title = if (activity.name.isNullOrEmpty())
                                    file.nameWithoutExtension else activity.name
                                contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                                description = activity.desc
                                leaf = true
                                entryId = activity.id
                            }
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

            val containerManager = ContainerManager(container, db, repo, containerBaseDir)

            addEntriesFromZipToContainer(file.absolutePath, containerManager, "")

            container
        }
    }
}