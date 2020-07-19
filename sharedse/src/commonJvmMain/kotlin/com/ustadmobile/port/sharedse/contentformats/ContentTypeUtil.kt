package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin.Companion.CONTENT_ENTRY
import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin.Companion.CONTENT_MIMETYPE
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.port.sharedse.contentformats.epub.EpubTypePlugin
import com.ustadmobile.port.sharedse.contentformats.xapi.plugin.TinCanTypePlugin
import java.io.File
import java.io.IOException
import java.util.*
import java.util.zip.ZipException

import java.util.regex.Pattern

/**
 * Class which handles entries and containers for all imported content
 *
 * @author kileha3
 */


private val CONTENT_PLUGINS = listOf(EpubTypePlugin(), TinCanTypePlugin(), VideoTypePlugin())

suspend fun extractContentEntryMetadataFromFile(file: File, db: UmAppDatabase, plugins: List<ContentTypePlugin> = CONTENT_PLUGINS): ImportedContentEntryMetaData? {
    plugins.forEach {
        val pluginResult = it.getContentEntry(file)
        val languageCode = pluginResult?.language?.iso_639_1_standard
        if (languageCode != null) {
            pluginResult?.language = db.languageDao.findByTwoCode(languageCode)
        }
        if (pluginResult != null) {
            pluginResult.contentFlags = ContentEntry.FLAG_IMPORTED
            return ImportedContentEntryMetaData(pluginResult, it.mimeTypes[0], file, it.isZipped())
        }
    }

    return null
}

suspend fun importContainerFromFile(contentEntryUid: Long, mimeType: String?, containerBaseDir: String,
                                    file: File, db: UmAppDatabase, dbRepo: UmAppDatabase, isZipped: Boolean): Container {

    val container = Container().apply {
        containerContentEntryUid = contentEntryUid
    }

    container.cntLastModified = System.currentTimeMillis()
    container.fileSize = file.length()
    container.mimeType = mimeType
    container.containerUid = dbRepo.containerDao.insert(container)

    val containerManager = ContainerManager(container, db, dbRepo, containerBaseDir)
    try {
        if (isZipped) {
            addEntriesFromZipToContainer(file.absolutePath, containerManager)
        } else {
            containerManager.addEntries(ContainerManager.FileEntrySource(file, file.name))
        }

    } catch (e: ZipException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return container
}


suspend fun importContentEntryFromFile(file: File, db: UmAppDatabase, dbRepo: UmAppDatabase,
                                       containerBaseDir: String, plugins: List<ContentTypePlugin> = CONTENT_PLUGINS): Pair<ContentEntry, Container>? {
    val (contentEntry, mimeType, file, isZipped) = extractContentEntryMetadataFromFile(file, db, plugins)
            ?: return null

    contentEntry.contentEntryUid = dbRepo.contentEntryDao.insert(contentEntry)
    val container = importContainerFromFile(contentEntry.contentEntryUid, mimeType, containerBaseDir, file,
            db, dbRepo, isZipped)

    return Pair(contentEntry, container)
}

/**
 *
 */
data class ImportedContentEntryMetaData(var contentEntry: ContentEntryWithLanguage, var mimeType: String, var file: File, var isZipped: Boolean)
