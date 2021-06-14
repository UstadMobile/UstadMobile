package com.ustadmobile.core.catalog.contenttype

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
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.openInputStream
import com.ustadmobile.door.DoorUri
import org.xmlpull.v1.XmlPullParserFactory


class XapiTypePluginCommonJvm : XapiPackageTypePlugin() {

    override suspend fun extractMetadata(uri: String, context: Any): ContentEntryWithLanguage? {
        return withContext(Dispatchers.Default) {
            var contentEntry: ContentEntryWithLanguage? = null
            try {
                val doorUri = DoorUri.parse(uri)
                val inputStream = doorUri.openInputStream(context)

                ZipInputStream(inputStream).use {
                    var zipEntry: ZipEntry? = null
                    while ({ zipEntry = it.nextEntry; zipEntry }() != null) {

                        val fileName = zipEntry?.name
                        if (fileName?.toLowerCase() == "tincan.xml") {
                            val xppFactory = XmlPullParserFactory.newInstance()
                            val xpp = xppFactory.newPullParser()//UstadMobileSystemImpl.instance.newPullParser(it)
                            xpp.setInput(it, "UTF-8")
                            val activity = TinCanXML.loadFromXML(xpp).launchActivity
                                    ?: throw IOException("TinCanXml from name has no launchActivity!")

                            contentEntry = ContentEntryWithLanguage().apply {
                                contentFlags = ContentEntry.FLAG_IMPORTED
                                licenseType = ContentEntry.LICENSE_TYPE_OTHER
                                title =  if(activity.name.isNullOrEmpty())
                                    doorUri.getFileName(context) else activity.name
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

            repo.addEntriesToContainerFromZip(container.containerUid, doorUri,
                    ContainerAddOptions(storageDirUri = File(containerBaseDir).toDoorUri()), context)

            val containerWithSize = repo.containerDao.findByUid(container.containerUid) ?: container

            containerWithSize
        }
    }
}