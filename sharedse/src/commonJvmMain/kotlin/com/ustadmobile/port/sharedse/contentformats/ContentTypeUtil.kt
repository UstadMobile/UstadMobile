package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin.Companion.CONTENT_ENTRY
import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin.Companion.CONTENT_MIMETYPE
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.contentformats.epub.EpubTypePlugin
import com.ustadmobile.port.sharedse.contentformats.xapi.plugin.TinCanTypePlugin
import java.io.File
import java.io.IOException
import java.util.*
import java.util.zip.ZipException


/**
 *
 */
data class ImportedContentEntryMetaData(var contentEntry: ContentEntry, var mimeType: String)

/**
 * Class which handles entries and containers for all imported content
 *
 * @author kileha3
 */
object ContentTypeUtil {

    private val CONTENT_PLUGINS = listOf(EpubTypePlugin(), TinCanTypePlugin())

    fun extractContentEntryMetadataFromFile(file: File, plugins: List<ContentTypePlugin> = CONTENT_PLUGINS): ImportedContentEntryMetaData? {
        plugins.forEach {
            val pluginResult = it.getContentEntry(file)
            if(pluginResult != null) {
                pluginResult.contentFlags = ContentEntry.FLAG_IMPORTED
                return ImportedContentEntryMetaData(pluginResult, it.mimeTypes[0])
            }
        }

        return null
    }

    suspend fun importContainerFromZippedFile(contentEntryUid: Long, mimeType: String?, containerBaseDir: String,
                                              file: File, db: UmAppDatabase, dbRepo: UmAppDatabase): Container {

        val container = Container().apply {
            containerContentEntryUid = contentEntryUid
        }

        container.cntLastModified = System.currentTimeMillis()
        container.fileSize = file.length()
        container.mimeType = mimeType
        container.containerUid = dbRepo.containerDao.insert(container)

        val containerManager = ContainerManager(container, db, dbRepo, containerBaseDir)
        try {
            addEntriesFromZipToContainer(file.absolutePath, containerManager)
        } catch (e: ZipException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return container
    }

    suspend fun importContentEntryFromFile(file: File, db: UmAppDatabase, dbRepo: UmAppDatabase,
                                           containerBaseDir: String, plugins: List<ContentTypePlugin> = CONTENT_PLUGINS): Pair<ContentEntry, Container>? {
        val (contentEntry, mimeType) = extractContentEntryMetadataFromFile(file, plugins) ?: return null

        contentEntry.contentEntryUid = dbRepo.contentEntryDao.insert(contentEntry)
        val container = importContainerFromZippedFile(contentEntry.contentEntryUid, mimeType, containerBaseDir, file,
                db, dbRepo)

        return Pair(contentEntry, container)
    }

    /**
     * Get generated content entry from the imported content
     */
    @Deprecated("This should be a toplevel function and should just return a nullable ContentEntry")
    fun getContent(file: File): HashMap<String, Any?> {
        val content = HashMap<String, Any?>()
        for (plugin in CONTENT_PLUGINS) {
            val contentEntry = plugin.getContentEntry(file)
            if (contentEntry != null) {
                contentEntry.contentFlags = ContentEntry.FLAG_IMPORTED
                content[CONTENT_ENTRY] = contentEntry
                content[CONTENT_MIMETYPE] = plugin.mimeTypes[0]
                break
            }
        }

        return content
    }

    /**
     * Import actual content to the database
     */
    suspend fun importContentEntryFromFile(context: Any, contentEntry: ContentEntry,mimeType: String?, baseDir: String, file: File): ContentEntry{

        val appDatabase = UmAppDatabase.getInstance(context)
        val appRepo = UmAccountManager.getRepositoryForActiveAccount(context)

        val container = Container(contentEntry)
        container.cntLastModified = System.currentTimeMillis()
        container.fileSize = file.length()
        container.mimeType = mimeType
        container.containerUid = appRepo.containerDao.insert(container)

        val containerManager = ContainerManager(container, appDatabase,
                appRepo, baseDir)
        try {
            addEntriesFromZipToContainer(file.absolutePath, containerManager)
        } catch (e: ZipException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return contentEntry
    }

}
