package com.ustadmobile.test.util.ext

import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addFileToContainer
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.db.entities.Container
import org.kodein.di.DI
import java.io.File

suspend fun insertContainerFromResources(db: UmAppDatabase, repo: UmAppDatabase, containerTmpDir: File, resTmpDir: File, di: DI, vararg resourceNames: String) : Container{
    val container = Container().apply {
        containerUid = repo.containerDao.insert(this)
    }

    resourceNames.forEach { resourceName ->
        val filename = resourceName.substringAfterLast("/")
        val entryTmpFile = File(resTmpDir, filename)
        db::class.java.getResourceAsStream(resourceName).writeToFile(entryTmpFile)
        repo.addFileToContainer(container.containerUid, entryTmpFile.toDoorUri(),
                entryTmpFile.name, Any(), di, ContainerAddOptions(storageDirUri = containerTmpDir.toDoorUri()))
    }

    return container
}