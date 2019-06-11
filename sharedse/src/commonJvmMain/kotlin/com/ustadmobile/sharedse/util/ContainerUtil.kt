package com.ustadmobile.sharedse.util

import com.ustadmobile.sharedse.container.ContainerManager
import com.ustadmobile.sharedse.security.getMessageDigestInstance
import kotlinx.coroutines.runBlocking
import kotlinx.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipEntrySource(val zipEntry: ZipEntry, val zipFile: ZipFile) : ContainerManager.EntrySource {
    override val length = zipEntry.size

    override val pathInContainer = zipEntry.name

    override val inputStream = zipFile.getInputStream(zipEntry)

    override val filePath = null

    override val md5Sum: ByteArray by lazy {
        val buffer = ByteArray(8*1024)
        var bytesRead = 0

        val inStream = inputStream
        val md5Digest = getMessageDigestInstance("MD5")
        while (inStream.read(buffer).also { bytesRead = it } != -1) {
            md5Digest.update(buffer, 0, bytesRead)
        }

        md5Digest.digest()
    }
}

actual fun addEntriesFromZipToContainer(zipPath: String, containerManager: ContainerManager) {
    runBlocking {
        var zipFile = null as ZipFile?
        try {
            zipFile = ZipFile(zipPath)
            containerManager.addEntries( *zipFile.entries().toList().map { ZipEntrySource(it, zipFile) as ContainerManager.EntrySource }.toTypedArray())
        }catch(e: Exception) {
            throw e
        }finally {
            zipFile?.close()
        }
    }
}
