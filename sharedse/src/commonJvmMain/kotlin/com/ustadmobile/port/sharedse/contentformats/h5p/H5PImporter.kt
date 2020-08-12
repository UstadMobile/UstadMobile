package com.ustadmobile.port.sharedse.contentformats.h5p

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.getAssetFromResource
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.contentformats.DefaultContainerImporter
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import java.io.File


class H5PImporter(prefixContainer: String) : DefaultContainerImporter(prefixContainer, true) {

    override suspend fun importContentEntryFromFile(contentEntryUid: Long, mimeType: String?,
                                                    containerBaseDir: String, file: File,
                                                    db: UmAppDatabase, dbRepo: UmAppDatabase,
                                                    importMode: Int, context: Any): Container {

        val container = super.importContentEntryFromFile(contentEntryUid, mimeType, containerBaseDir, file, db, dbRepo, importMode, context)
        val entry = db.contentEntryDao.findByUid(contentEntryUid)
        val containerManager = ContainerManager(container, db, dbRepo, containerBaseDir)

        val tmpFolder = UmFileUtilSe.makeTempDir("res", "")

        val distIn = getAssetFromResource("/com/ustadmobile/sharedse/h5p/dist.zip", context)
                ?: return container
        val tempDistFile = File(tmpFolder, "dist.zip")
        tempDistFile.copyInputStreamToFile(distIn)
        addEntriesFromZipToContainer(tempDistFile.absolutePath, containerManager)

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
        indexFile.copyInputStreamToFile(indexInput)
        containerManager.addEntries(ContainerManager.FileEntrySource(indexFile, indexFile.name))
        tmpFolder.deleteRecursively()


        return container
    }

}
