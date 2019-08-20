package com.ustadmobile.core.container

import kotlinx.coroutines.runBlocking
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipEntrySource(val zipEntry: ZipEntry, val zipFile: ZipFile,
                     override val compression: Int = 0) : ContainerManagerCommon.EntrySource {
    override val length = zipEntry.size

    override val pathInContainer = zipEntry.name

    override val inputStream
        get() = zipFile.getInputStream(zipEntry)

    override val filePath = null

    override val md5Sum: ByteArray by lazy {
        val buffer = ByteArray(8 * 1024)
        var bytesRead = 0

        val inStream = inputStream
        val md5Digest = MessageDigest.getInstance("MD5")
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
            val entryList = zipFile.entries().toList().map { ZipEntrySource(it, zipFile) }.toTypedArray()
            containerManager.addEntries(*entryList)
        } catch (e: Exception) {
            throw e
        } finally {
            zipFile?.close()
        }
    }
}
