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
import com.ustadmobile.port.sharedse.contentformats.h5p.H5PTypePlugin
import com.ustadmobile.port.sharedse.contentformats.xapi.plugin.TinCanTypePlugin
import java.io.File
import java.io.IOException
import java.util.*
import java.util.zip.ZipException

/**
 * Class which handles entries and containers for all imported content
 *
 * @author kileha3
 */
object ContentTypeUtil {

    private val CONTENT_PLUGINS = listOf(EpubTypePlugin(), H5PTypePlugin(), TinCanTypePlugin())

    /**
     * Get generated content entry from the imported content
     */
    fun getContent(file: File): HashMap<String, Any?> {
        val content = HashMap<String, Any?>()
        for (plugin in CONTENT_PLUGINS) {
            val contentEntry = plugin.getContentEntry(file)
            if (contentEntry != null) {
                contentEntry.imported = true
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
    fun importContentEntryFromFile(context: Any, content: HashMap<String, Any?>, baseDir: String,
                                   file: File, callback: UmCallback<ContentEntry>) {

        Thread {

            val contentEntry = if (content.containsKey(CONTENT_ENTRY))
                content[CONTENT_ENTRY] as ContentEntry
            else
                null
            val mimeType = if (content.containsKey(CONTENT_MIMETYPE))
                content[CONTENT_MIMETYPE] as String
            else
                null

            if (contentEntry != null) {
                val appDatabase = UmAppDatabase.getInstance(context)
                val appRepo = UmAccountManager.getRepositoryForActiveAccount(context)
                contentEntry.imported = true

                val container = Container(contentEntry)
                container.cntLastModified = System.currentTimeMillis()
                container.fileSize = file.length()
                container.mimeType = mimeType
                container.containerUid = appRepo.containerDao.insert(container)

                val containerManager = ContainerManager(container, appDatabase,
                        appRepo, baseDir)
                try {
                    addEntriesFromZipToContainer(file.absolutePath, containerManager)
                    callback.onSuccess(contentEntry)
                } catch (e: ZipException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            } else {
                callback.onSuccess(null)
            }
        }.start()
    }

}
