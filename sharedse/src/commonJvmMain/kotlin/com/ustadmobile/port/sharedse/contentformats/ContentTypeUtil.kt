package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil.FILE
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil.H5P
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil.ZIPPED
import com.ustadmobile.port.sharedse.contentformats.epub.EpubTypeFilePlugin
import com.ustadmobile.port.sharedse.contentformats.h5p.H5PImporter
import com.ustadmobile.port.sharedse.contentformats.h5p.H5PTypeFilePlugin
import com.ustadmobile.port.sharedse.contentformats.video.VideoTypeFilePlugin
import com.ustadmobile.port.sharedse.contentformats.xapi.plugin.XapiPackageTypeFilePlugin
import java.io.File
import java.io.IOException
import java.util.zip.ZipException

/**
 * Class which handles entries and containers for all imported content
 *
 * @author kileha3
 */



internal val CONTENT_PLUGINS = listOf(EpubTypeFilePlugin(), XapiPackageTypeFilePlugin(), VideoTypeFilePlugin(), H5PTypeFilePlugin())

val mimeTypeSupported: List<String> = CONTENT_PLUGINS.flatMap { it.mimeTypes.asList() }
val extSupported: List<String> = CONTENT_PLUGINS.flatMap { it.fileExtensions.asList() }

suspend fun extractContentEntryMetadataFromFile(file: File, db: UmAppDatabase, plugins: List<ContentTypeFilePlugin> = CONTENT_PLUGINS): ImportedContentEntryMetaData? {
   /* plugins.forEach {
        val pluginResult = it.getContentEntry(file)
        val languageCode = pluginResult?.language?.iso_639_1_standard
        if (languageCode != null) {
            pluginResult?.language = db.languageDao.findByTwoCode(languageCode)
        }
        if (pluginResult != null) {
            pluginResult.contentFlags = ContentEntry.FLAG_IMPORTED
            return ImportedContentEntryMetaData(pluginResult, it.mimeTypes[0], file.toURI().toString(), it.importMode())
        }
    }*/

    return null
}

val importerMap = mapOf(ZIPPED to DefaultContainerImporter(isZipped = true),
        H5P to H5PImporter("workspace/"), FILE to DefaultContainerImporter(isZipped = false))


suspend fun importContainerFromFile(contentEntryUid: Long, mimeType: String?, containerBaseDir: String,
                                    file: File, db: UmAppDatabase, dbRepo: UmAppDatabase, importMode: Int, context: Any): Container {
    try {
        val importer = importerMap[importMode] ?: throw IllegalArgumentException("no file found")
        return importer.importContentEntryFromFile(contentEntryUid, mimeType, containerBaseDir, file, db, dbRepo, importMode, context)
    } catch (e: ZipException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return Container()
}


suspend fun importContentEntryFromFile(file: File, db: UmAppDatabase, dbRepo: UmAppDatabase,
                                       containerBaseDir: String, context: Any, plugins: List<ContentTypeFilePlugin> = CONTENT_PLUGINS): Pair<ContentEntry, Container>? {
    val (contentEntry, mimeType, fileUri, importMode) = extractContentEntryMetadataFromFile(file, db, plugins)
            ?: return null

   /* contentEntry.contentEntryUid = dbRepo.contentEntryDao.insert(contentEntry)
    val container = importContainerFromFile(contentEntry.contentEntryUid, mimeType, containerBaseDir, file,
            db, dbRepo, importMode, context)*/

    return Pair(contentEntry, Container())
}


object ContentTypeUtil {

    const val ZIPPED = 1

    const val FILE = 2

    const val H5P = 3

}
