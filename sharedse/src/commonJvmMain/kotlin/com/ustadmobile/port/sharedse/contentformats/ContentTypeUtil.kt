package com.ustadmobile.port.sharedse.contentformats

import com.sun.xml.internal.ws.util.StringUtils
import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin.Companion.CONTENT_ENTRY
import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin.Companion.CONTENT_MIMETYPE
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.ContainerManagerCommon
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil.FILE
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil.ZIPPED
import com.ustadmobile.port.sharedse.contentformats.epub.EpubTypeFilePlugin
import com.ustadmobile.port.sharedse.contentformats.video.VideoTypeFilePlugin
import com.ustadmobile.port.sharedse.contentformats.xapi.plugin.XapiPackageTypeFilePlugin
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
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


private val CONTENT_PLUGINS = listOf(EpubTypeFilePlugin(), XapiPackageTypeFilePlugin(), VideoTypeFilePlugin())

suspend fun extractContentEntryMetadataFromFile(file: File, db: UmAppDatabase, plugins: List<ContentTypeFilePlugin> = CONTENT_PLUGINS): ImportedContentEntryMetaData? {
    plugins.forEach {
        val pluginResult = it.getContentEntry(file)
        val languageCode = pluginResult?.language?.iso_639_1_standard
        if (languageCode != null) {
            pluginResult?.language = db.languageDao.findByTwoCode(languageCode)
        }
        if (pluginResult != null) {
            pluginResult.contentFlags = ContentEntry.FLAG_IMPORTED
            return ImportedContentEntryMetaData(pluginResult, it.mimeTypes[0], file, it.importMode())
        }
    }

    return null
}

suspend fun importContainerFromFile(contentEntryUid: Long, mimeType: String?, containerBaseDir: String,
                                    file: File, db: UmAppDatabase, dbRepo: UmAppDatabase, importMode: Int): Container {

    val container = Container().apply {
        containerContentEntryUid = contentEntryUid
    }

    container.cntLastModified = System.currentTimeMillis()
    container.fileSize = file.length()
    container.mimeType = mimeType
    container.containerUid = dbRepo.containerDao.insert(container)

    val containerManager = ContainerManager(container, db, dbRepo, containerBaseDir)
    try {
        when (importMode) {
            ZIPPED -> addEntriesFromZipToContainer(file.absolutePath, containerManager)
            FILE -> containerManager.addEntries(ContainerManager.FileEntrySource(file, file.name))
        }

    } catch (e: ZipException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return container
}


suspend fun importContentEntryFromFile(file: File, db: UmAppDatabase, dbRepo: UmAppDatabase,
                                       containerBaseDir: String, plugins: List<ContentTypeFilePlugin> = CONTENT_PLUGINS): Pair<ContentEntry, Container>? {
    val (contentEntry, mimeType, file, importMode) = extractContentEntryMetadataFromFile(file, db, plugins)
            ?: return null

    contentEntry.contentEntryUid = dbRepo.contentEntryDao.insert(contentEntry)
    val container = importContainerFromFile(contentEntry.contentEntryUid, mimeType, containerBaseDir, file,
            db, dbRepo, importMode)

    return Pair(contentEntry, container)
}


interface ContainerImporter {

    suspend fun importContentEntryFromFiles(file: File, db: UmAppDatabase, dbRepo: UmAppDatabase,
                                   containerBaseDir: String, plugins: List<ContentTypeFilePlugin> = CONTENT_PLUGINS): Pair<ContentEntry, Container>?
}

open class DefaultContainerImporter(var prefixContainer: String = "") : ContainerImporter {

    override suspend fun importContentEntryFromFiles(file: File, db: UmAppDatabase, dbRepo: UmAppDatabase, containerBaseDir: String, plugins: List<ContentTypeFilePlugin>): Pair<ContentEntry, Container>? {
        return importContentEntryFromFile(file, db, dbRepo, prefixContainer + containerBaseDir, plugins)
    }

}

class H5PImporter(prefixContainer: String): DefaultContainerImporter(prefixContainer) {

    override suspend fun importContentEntryFromFiles(file: File, db: UmAppDatabase, dbRepo: UmAppDatabase, containerBaseDir: String, plugins: List<ContentTypeFilePlugin>): Pair<ContentEntry, Container>? {
        val pair = importContentEntryFromFile(file, db, dbRepo, containerBaseDir, plugins) ?: return null

        // TODO add the files from resources



        // generate tincan.xml
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <tincan xmlns="http://projecttincan.com/tincan.xsd">
                <activities>
                    <activity id="${pair.first.entryId}" type="http://adlnet.gov/expapi/activities/module">
                        <name>${pair.first.title}</name>
                        <description lang="en-US">${pair.first.description}</description>
                        <launch lang="en-us">index.html</launch>
                    </activity>
                </activities>
            </tincan>
        """.trimIndent()

        // generate index.html
        """
            <html>
            <head>
              <link type="text/css" rel="stylesheet" media="all" href="dist/styles/h5p.css" />
              <meta charset="utf-8" />
              <script type="text/javascript" src="dist/main.bundle.js"></script>
            </head>
            <body>
              <div id="h5p-container"></div>
             <script type="text/javascript">
                const {
                  H5P	
                } = H5PStandalone;
                new H5P(document.getElementById('h5p-container'), '${pair.first.entryId}', {
                  frameJs: 'dist/frame.bundle.js',
                  frameCss: 'dist/styles/h5p.css'
                });
               
              </script>
            </body>
            </html>
            
        """.trimIndent()

        // TODO add to container
        val containerManager = ContainerManager(pair.second, db, dbRepo, containerBaseDir)
        containerManager.addEntries()


        return pair
    }


}



/**
 *
 */
data class ImportedContentEntryMetaData(var contentEntry: ContentEntryWithLanguage, var mimeType: String, var file: File, var importMode: Int)

object ContentTypeUtil {

    const val ZIPPED = 1

    const val FILE = 2

    const val H5P = 3
}
