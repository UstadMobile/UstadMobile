package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.catalog.contenttype.*
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import java.io.File
import java.io.IOException
import java.util.zip.ZipException

/**
 * Class which handles entries and containers for all imported content
 *
 * @author kileha3
 */

val CONTENT_PLUGINS = listOf(EpubTypePluginCommonJvm(), XapiTypePluginCommonJvm(), H5PTypePluginCommonJvm(Any()), VideoTypePluginJvm())

val mimeTypeSupported: List<String> = CONTENT_PLUGINS.flatMap { it.mimeTypes.asList() }
val extSupported: List<String> = CONTENT_PLUGINS.flatMap { it.fileExtensions.asList() }

suspend fun extractContentEntryMetadataFromFile(file: File, db: UmAppDatabase, plugins: List<ContentTypePlugin> = CONTENT_PLUGINS): ImportedContentEntryMetaData? {
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



suspend fun importContainerFromFile(contentEntryUid: Long, mimeType: String?, containerBaseDir: String,
                                    file: File, db: UmAppDatabase, dbRepo: UmAppDatabase, importMode: Int, context: Any): Container {
    try {
        //return importer.importContentEntryFromFile(contentEntryUid, mimeType, containerBaseDir, file, db, dbRepo, importMode, context)
    } catch (e: ZipException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return Container()
}


suspend fun importContentEntryFromFile(file: File, db: UmAppDatabase, dbRepo: UmAppDatabase,
                                       containerBaseDir: String, context: Any, plugins: List<ContentTypePlugin> = CONTENT_PLUGINS): Pair<ContentEntry, Container>? {
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
