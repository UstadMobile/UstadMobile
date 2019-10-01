package com.ustadmobile.core.container

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_GZIP
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_NONE
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import com.ustadmobile.lib.util.Base64Coder
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.io.InputStream
import kotlinx.io.OutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.copyTo

actual class ContainerManager actual constructor(container: Container,
                                                 db: UmAppDatabase,
                                                 dbRepo: UmAppDatabase,
                                                 newFilePath: String?,
                                                 pathToEntryMap: MutableMap<String, ContainerEntryWithContainerEntryFile>)

    : ContainerManagerCommon(container, db, dbRepo, newFilePath, pathToEntryMap) {

    val newFileDir = if (newFilePath != null) {
        File(newFilePath)
    } else {
        null
    }

    val mutex = Mutex()

    open class FileEntrySource(private val file: File, override val pathInContainer: String,
                               override val compression: Int = 0) : EntrySource {
        override val length: Long
            get() = file.length()

        override val inputStream: InputStream
            get() = FileInputStream(file)

        override val filePath: String?
            get() = file.getAbsolutePath()

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


    @UseExperimental(ExperimentalUnsignedTypes::class)
    actual override suspend fun addEntries(addOptions: AddEntryOptions?, vararg entries: EntrySource) {
        val addOpts = addOptions ?: AddEntryOptions()
        if (newFileDir == null)
            throw RuntimeException("Cannot add files to container ${container.containerUid} with null newFileDir")

        val entryMd5Sums = entries.map { Base64Coder.encodeToString(it.md5Sum) }
        mutex.withLock {

            val existingFiles = db.containerEntryFileDao.findEntriesByMd5Sums(entryMd5Sums)
                    .map { it.cefMd5!! to it }.toMap()

            val newContainerEntries = mutableListOf<ContainerEntryWithContainerEntryFile>()

            val entriesParted = entries.partition { Base64Coder.encodeToString(it.md5Sum) in existingFiles.keys }

            //delete any ContainerEntry that is being overwritten
            val newEntryPaths = entries.map { it.pathInContainer }
            db.containerEntryDao.deleteList(
                    pathToEntryMap.filter { it.key in newEntryPaths }.map { it.value })

            //for all entries that we already have
            newContainerEntries.addAll(entriesParted.first.map {
                ContainerEntryWithContainerEntryFile(it.pathInContainer, container,
                        existingFiles[Base64Coder.encodeToString(it.md5Sum)]!!)
            })

            entriesParted.second.forEach {
                val md5HexStr = it.md5Sum.joinToString(separator = "") { it.toUByte().toString(16) }
                val destFile = File(newFileDir, md5HexStr)
                val currentFilePath = it.filePath
                val isExcludedFromGzip = EXCLUDED_GZIP_TYPES.any { gzipIt -> it.pathInContainer.endsWith(gzipIt) }
                val shouldGzipNow = if (it.compression == COMPRESSION_GZIP) false else !isExcludedFromGzip
                val compressionSetting = if (isExcludedFromGzip) COMPRESSION_NONE else COMPRESSION_GZIP
                //TODO: check for any paths that are being overwritten

                if (addOpts.moveExistingFiles && currentFilePath != null) {
                    if (!File(currentFilePath).renameTo(destFile)) {
                        throw IOException("Could not rename input file : $currentFilePath")
                    }
                } else {
                    //copy it
                    GlobalScope.async {
                        var destOutStream = null as OutputStream?
                        var inStream = null as InputStream?
                        try {
                            inStream = it.inputStream
                            destOutStream = FileOutputStream(destFile)
                            destOutStream = if (shouldGzipNow) GZIPOutputStream(destOutStream) else destOutStream
                            inStream.copyTo(destOutStream)
                        } catch (e: IOException) {
                            throw e
                        } finally {
                            destOutStream?.close()
                            inStream?.close()
                            destOutStream?.close()
                        }
                    }.await()
                }

                val containerEntryFile = ContainerEntryFile(Base64Coder.encodeToString(it.md5Sum),
                        it.length, destFile.length(), compressionSetting, getSystemTimeInMillis())
                containerEntryFile.cefPath = destFile.absolutePath
                containerEntryFile.cefUid = db.containerEntryFileDao.insert(containerEntryFile)
                newContainerEntries.add(ContainerEntryWithContainerEntryFile(it.pathInContainer, container,
                        containerEntryFile))
            }

            db.containerEntryDao.insertAndSetIds(newContainerEntries)

            pathToEntryMap.putAll(newContainerEntries.map { it.cePath!! to it }.toMap())

            if (!addOpts.dontUpdateTotals) {
                container.fileSize = pathToEntryMap.values.fold(0L, { count, next -> count + next.containerEntryFile!!.ceCompressedSize })
                container.cntNumEntries = pathToEntryMap.size
                dbRepo.containerDao.updateContainerSizeAndNumEntries(container.containerUid)
            }
        }
    }

    actual fun getInputStream(containerEntry: ContainerEntryWithContainerEntryFile): InputStream {
        val fileIn = FileInputStream(File(containerEntry.containerEntryFile!!.cefPath!!))
        return if (containerEntry.containerEntryFile?.compression == COMPRESSION_GZIP) {
            GZIPInputStream(fileIn)
        } else {
            fileIn
        }
    }

    actual fun getEntry(pathInContainer: String): ContainerEntryWithContainerEntryFile? {
        return pathToEntryMap[pathInContainer]
    }

    actual suspend fun linkExistingItems(itemsToDownload: List<ContainerEntryWithMd5>): List<ContainerEntryWithMd5> {
        mutex.withLock {
            val existingEntryMd5List = pathToEntryMap.values.map { it.containerEntryFile!!.cefMd5 }
            val md5ToFilesToEntryToDownloadMap = itemsToDownload.map { it.cefMd5!! to it }.toMap()
            val existingEntryFiles = db.containerEntryFileDao.findEntriesByMd5Sums(
                    md5ToFilesToEntryToDownloadMap.keys.toList())
            val md5ToExistingEntryFilesMap = existingEntryFiles.map { it.cefMd5 to it }.toMap()
            val itemsToDownloadPartitioned = itemsToDownload.partition {
                it.cefMd5 in existingEntryMd5List || it.cefMd5 in md5ToExistingEntryFilesMap.keys }

            //these are the items that we already have here after searching by md5
            var linksToInsert = itemsToDownloadPartitioned.first.map {
                ContainerEntryWithContainerEntryFile(it.cePath!!,container,
                        md5ToExistingEntryFilesMap[it.cefMd5]!!)
            }
            linksToInsert = linksToInsert.filter { it.containerEntryFile!!.cefMd5 !in existingEntryMd5List }
            db.containerEntryDao.insertAndSetIds(linksToInsert)
            pathToEntryMap.putAll(linksToInsert.map { it.cePath!! to it }.toMap())

            //return the items remaining (e.g. those that actually need downloaded)
            return itemsToDownloadPartitioned.second
        }
    }

    companion object {
        val EXCLUDED_GZIP_TYPES: List<String> = listOf(".webm", ".mp4")
    }

}