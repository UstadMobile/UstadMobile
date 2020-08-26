package com.ustadmobile.test.util.ext

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream

suspend fun UmAppDatabase.insertContainerFromResources(containerTmpDir: File, resTmpDir: File, vararg resourceNames: String) : Pair<Container, ContainerManager>{
    val container = Container().apply {
        containerUid = containerDao.insert(this)
    }

    val containerManager = ContainerManager(container, this, this,
            containerTmpDir.absolutePath)

    resourceNames.forEach { resourceName ->
        val filename = resourceName.substringAfterLast("/")
        this::class.java.getResourceAsStream(resourceName).use { inStream ->
            val entryTmpFile = File(resTmpDir, filename)
            FileOutputStream(entryTmpFile).use { outStream ->
                inStream.copyTo(outStream)
                outStream.flush()
                containerManager.addEntries(ContainerManager.FileEntrySource(entryTmpFile,
                            entryTmpFile.name))
            }
        }
    }

    return container to containerManager
}