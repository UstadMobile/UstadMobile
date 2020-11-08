package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.util.getAssetFromResource
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.content
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

val licenseMap = mapOf(
        "CC-BY" to ContentEntry.LICENSE_TYPE_CC_BY,
        "CC BY-SA" to ContentEntry.LICENSE_TYPE_CC_BY_SA,
        "CC BY-ND" to ContentEntry.LICENSE_TYPE_CC_BY,
        "CC BY-NC" to ContentEntry.LICENSE_TYPE_OTHER,
        "CC BY-NC-SA" to ContentEntry.LICENSE_TYPE_CC_BY_NC_SA,
        "CC CC-BY-NC-CD" to ContentEntry.LICENSE_TYPE_OTHER,
        "CC0 1.0" to ContentEntry.LICENSE_TYPE_CC_0,
        "GNU GPL" to ContentEntry.LICENSE_TYPE_OTHER,
        "PD" to ContentEntry.LICENSE_TYPE_PUBLIC_DOMAIN,
        "ODC PDDL" to ContentEntry.LICENSE_TYPE_OTHER,
        "CC PDM" to ContentEntry.LICENSE_TYPE_OTHER,
        "C" to ContentEntry.ALL_RIGHTS_RESERVED,
        "U" to ContentEntry.LICENSE_TYPE_OTHER
)

class H5PTypePluginCommonJvm(): H5PTypePlugin() {

    override suspend fun extractMetadata(filePath: String): ContentEntryWithLanguage? {
        return withContext(Dispatchers.Default){
            var contentEntry: ContentEntryWithLanguage? = null
            try {
                val file = File(filePath)
                ZipInputStream(FileInputStream(file)).use {
                    var zipEntry: ZipEntry? = null
                    while ({ zipEntry = it.nextEntry; zipEntry }() != null) {

                        val fileName = zipEntry?.name
                        if (fileName?.toLowerCase() == "h5p.json") {

                            val data = String(it.readBytes())

                            val json = Json.parseJson(data)

                            // take the name from the role Author otherwise take last one
                            var author: String? = ""
                            var name: String? = ""
                            json.jsonObject["authors"]?.jsonArray?.forEach {
                                name = it.jsonObject["name"]?.content ?: ""
                                val role = it.jsonObject["role"]?.content ?: ""
                                if (role == "Author") {
                                    author = name
                                }
                            }
                            if (author.isNullOrEmpty()) {
                                author = name
                            }

                            contentEntry = ContentEntryWithLanguage().apply {
                                contentFlags = ContentEntry.FLAG_IMPORTED
                                contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                                licenseType = licenseMap[json.jsonObject["license"] ?: ""]
                                        ?: ContentEntry.LICENSE_TYPE_OTHER
                                title = if(json.jsonObject["title"]?.content.isNullOrEmpty())
                                    file.nameWithoutExtension else json.jsonObject["title"]?.content
                                this.author = author
                                leaf = true
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
                                           contentEntryUid: Long, mimeType: String, containerBaseDir: String,
                                           context: Any,
                                           db: UmAppDatabase, repo: UmAppDatabase,
                                           progressListener: (Int) -> Unit): Container {
        val file = File(filePath)
        val container = Container().apply {
            containerContentEntryUid = contentEntryUid
            cntLastModified = System.currentTimeMillis()
            fileSize = file.length()
            this.mimeType = mimeType
            containerUid = repo.containerDao.insert(this)
        }

        val entry = db.contentEntryDao.findByUid(contentEntryUid)
        val containerManager = ContainerManager(container, db, repo, containerBaseDir)

        addEntriesFromZipToContainer(file.absolutePath, containerManager, "workspace/")

        val tmpFolder: File = File.createTempFile("res", "")
        tmpFolder.delete()
        tmpFolder.mkdirs()

        val distIn = getAssetFromResource("/com/ustadmobile/sharedse/h5p/dist.zip", context)
                ?: return container
        val tempDistFile = File(tmpFolder, "dist.zip")
        val outputStream = FileOutputStream(tempDistFile)
        UMIOUtils.readFully(distIn, outputStream)
        UMIOUtils.closeInputStream(distIn)
        UMIOUtils.closeOutputStream(outputStream)

        addEntriesFromZipToContainer(tempDistFile.absolutePath, containerManager, "workspace/")

        // generate tincan.xml
        val tinCan = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tincan xmlns="http://projecttincan.com/tincan.xsd">
                <activities>
                    <activity id="${entry?.entryId ?: ""}" type="http://adlnet.gov/expapi/activities/module">
                        <name>${entry?.title ?: "" }</name>
                        <description lang="en-US">${entry?.description ?: ""}</description>
                        <launch lang="en-us">index.html</launch>
                    </activity>
                </activities>
            </tincan>
        """.trimIndent()

        val tinCanFile = File(tmpFolder, "tincan.xml")
        tinCanFile.writeText(tinCan)
        containerManager.addEntries(ContainerManager.FileEntrySource(tinCanFile, tinCanFile.name))

        // generate index.html
        val indexInput =  getAssetFromResource("/com/ustadmobile/sharedse/h5p/index.html", context)
                ?: return container
        val indexFile = File(tmpFolder, "index.html")
        val outStream = FileOutputStream(indexFile)
        UMIOUtils.readFully(indexInput, outStream)
        UMIOUtils.closeInputStream(indexInput)
        UMIOUtils.closeOutputStream(outStream)
        containerManager.addEntries(ContainerManager.FileEntrySource(indexFile, indexFile.name))
        tmpFolder.deleteRecursively()


        return container

    }
}