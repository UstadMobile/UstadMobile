package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.container.ContainerManager
import com.ustadmobile.port.sharedse.contentformats.epub.EpubTypePlugin
import com.ustadmobile.port.sharedse.contentformats.h5p.H5PTypePlugin

import java.io.File
import java.io.IOException
import java.util.Arrays
import java.util.HashMap
import java.util.zip.ZipException
import java.util.zip.ZipFile

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin.Companion.CONTENT_ENTRY
import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin.Companion.CONTENT_MIMETYPE

object ContentTypeUtil {

    private val CONTENT_PLUGINS = Arrays.asList(
            EpubTypePlugin(), H5PTypePlugin()
    )


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
                container.lastModified = System.currentTimeMillis()
                container.fileSize = file.length()
                container.mimeType = mimeType
                container.containerUid = appRepo.containerDao.insert(container)

                val containerManager = ContainerManager(container, appDatabase,
                        appRepo, baseDir)
                try {
                    containerManager.addEntriesFromZip(ZipFile(file),
                            ContainerManager.OPTION_COPY or ContainerManager.OPTION_UPDATE_TOTALS)
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
