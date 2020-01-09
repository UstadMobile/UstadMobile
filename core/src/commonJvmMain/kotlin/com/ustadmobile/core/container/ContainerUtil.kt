package com.ustadmobile.core.container

import kotlinx.coroutines.runBlocking
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipEntrySource(val zipEntry: ZipEntry, val zipFile: ZipFile,
                     override val compression: Int = 0) : ContainerManagerCommon.EntrySource {
    override val length = zipEntry.size

    override val pathsInContainer = listOf(zipEntry.name)

    override val inputStream by lazy { zipFile.getInputStream(zipEntry) }

    override val filePath = null

    override val md5Sum: ByteArray by lazy {
        val buffer = ByteArray(8 * 1024)
        var bytesRead = 0

        val md5Digest = MessageDigest.getInstance("MD5")
        zipFile.getInputStream(zipEntry).use { inStream ->
            while (inStream.read(buffer).also { bytesRead = it } != -1) {
                md5Digest.update(buffer, 0, bytesRead)
            }
        }

        md5Digest.digest()
    }

    override fun dispose() {
        inputStream.close()
    }
}


actual fun addEntriesFromZipToContainer(zipPath: String, containerManager: ContainerManager) {
    runBlocking {
        var zipFile = null as ZipFile?
        try {
            zipFile = ZipFile(zipPath)
            val entryList = zipFile.entries().toList()
                    .filter { !it.isDirectory() }
                    .map { ZipEntrySource(it, zipFile) }.toTypedArray()
            containerManager.addEntries(*entryList)
        } catch (e: Exception) {
            throw e
        } finally {
            zipFile?.close()
        }
    }
}
