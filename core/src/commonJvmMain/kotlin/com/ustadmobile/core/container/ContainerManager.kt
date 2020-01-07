package com.ustadmobile.core.container

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_GZIP
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_NONE
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
import java.io.BufferedInputStream
import java.util.zip.ZipOutputStream
import java.io.BufferedOutputStream
import java.util.zip.ZipEntry
import kotlin.io.copyTo
import com.ustadmobile.core.util.ext.*

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

    open class FileEntrySource(private val file: File, private val pathInContainer: String,
                               override val compression: Int = 0) : EntrySource {
        override val length: Long
            get() = file.length()

        override val inputStream: InputStream by lazy { FileInputStream(file) }

        override val filePath: String?
            get() = file.getAbsolutePath()

        override val pathsInContainer: List<String>
            get() = listOf(pathInContainer)

        override val md5Sum: ByteArray by lazy {
            val buffer = ByteArray(8 * 1024)
            var bytesRead = 0

            val md5Digest = MessageDigest.getInstance("MD5")
            FileInputStream(file).use { inStream ->
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


    /**
     * Adds a set of entries to this container. This is designed to work efficiently with
     * ConcatenatedInputStream. It requires the list of entries and md5s to be inserted to known
     * in advance, but can use a function to iterate over available entrysources in the order
     * they are read.
     *
     * The paths that are going to be added must be known in advance.
     * @param addOptions optional can be null.
     * @param newPathsToMd5Map a map in the form of String -> Md5 byte array. This must be known
     * in advance so that we can determine which entries are already available and which need
     * added
     * @param provider a function that provides an EntrySource. Once it has run out of EntrySources
     * then it should return null
     */
    actual override suspend fun addEntries(addOptions: AddEntryOptions?,
                                           newPathsToMd5Map: Map<String, ByteArray>,
                                           provider: suspend () -> EntrySource?) {
        val addOpts = addOptions ?: AddEntryOptions()
        if (newFileDir == null)
            throw RuntimeException("Cannot add files to container ${container.containerUid} with null newFileDir")

        val entryMd5Sums = newPathsToMd5Map.values.map { it.encodeBase64() }
        mutex.withLock {
            val existingFiles = db.containerEntryFileDao.findEntriesByMd5Sums(entryMd5Sums)
                    .map { it.cefMd5!! to it }.toMap()

            val newContainerEntries = mutableListOf<ContainerEntryWithContainerEntryFile>()

            //delete any ContainerEntry that is being overwritten
            val newEntryPaths = newPathsToMd5Map.keys
            val overwrittenEntriesToDelete = pathToEntryMap.filter { it.key in newEntryPaths }
                    .map { it.value }
            db.containerEntryDao.deleteList(overwrittenEntriesToDelete)


            var nextEntrySource: EntrySource? = null
            while(provider.invoke().also { nextEntrySource = it} != null) {
                val nextEntry = nextEntrySource!!
                val md5StrBase64 = nextEntry.md5Sum.encodeBase64()
                val existingFile = existingFiles[md5StrBase64]
                if(existingFile != null) {
                    newContainerEntries.addAll(
                        nextEntry.pathsInContainer.map { path ->
                            ContainerEntryWithContainerEntryFile(path, container, existingFile)
                        }
                    )
                }else {
                    val destFile = File(newFileDir, nextEntry.md5Sum.toHexString())
                    val currentFilePath = nextEntry.filePath
                    val isExcludedFromGzip = EXCLUDED_GZIP_TYPES.any { gzipIt -> nextEntry.pathsInContainer.any { it.endsWith(gzipIt) }}
                    val shouldGzipNow = if (nextEntry.compression == COMPRESSION_GZIP) {
                        false
                    } else {
                        !isExcludedFromGzip
                    }

                    val compressionSetting = if (isExcludedFromGzip) COMPRESSION_NONE else COMPRESSION_GZIP
                    //TODO: check for any paths that are being overwritten

                    if (addOpts.moveExistingFiles && currentFilePath != null) {
                        if (!File(currentFilePath).renameTo(destFile)) {
                            throw IOException("Could not rename input file : $currentFilePath")
                        }
                    } else {
                        //copy it
                        var destOutStream = null as OutputStream?
                        var inStream = null as InputStream?
                        try {
                            inStream = nextEntry.inputStream
                            destOutStream = FileOutputStream(destFile)
                            destOutStream = if (shouldGzipNow) GZIPOutputStream(destOutStream) else destOutStream
                            inStream.copyTo(destOutStream)
                        } catch (e: IOException) {
                            throw e
                        } finally {
                            destOutStream?.close()
                            nextEntry.dispose()
                            destOutStream?.close()
                        }
                    }

                    val containerEntryFile = ContainerEntryFile(md5StrBase64,
                            nextEntry.length, destFile.length(), compressionSetting, getSystemTimeInMillis())
                    containerEntryFile.cefPath = destFile.absolutePath
                    containerEntryFile.cefUid = db.containerEntryFileDao.insert(containerEntryFile)
                    newContainerEntries.addAll(nextEntry.pathsInContainer.map { path ->
                        ContainerEntryWithContainerEntryFile(path, container, containerEntryFile)
                    })
                }
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




    //TODO: modify this to use a function which would use a function that provides a next entry source
    @UseExperimental(ExperimentalUnsignedTypes::class)
    actual override suspend fun addEntries(addOptions: AddEntryOptions?, vararg entries: EntrySource) {
        val addOpts = addOptions ?: AddEntryOptions()
        if (newFileDir == null)
            throw RuntimeException("Cannot add files to container ${container.containerUid} with null newFileDir")

        var currentEntry = 0
        val pathToMd5Map: Map<String, ByteArray> = entries.flatMap { entry ->
            entry.pathsInContainer.map { path -> path to entry.md5Sum }
        }.toMap()
        addEntries(addOptions, pathToMd5Map) {
            if(currentEntry < entries.size) {
                entries[currentEntry++]
            }else {
                null
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
            val remainingItemsToDownload = itemsToDownload.filter { !pathToEntryMap.containsKey(it.cePath) }
            val existingEntryMd5List = pathToEntryMap.values.map { it.containerEntryFile!!.cefMd5 }
            val md5ToFilesToEntryToDownloadMap = remainingItemsToDownload.map { it.cefMd5!! to it }.toMap()
            val existingEntryFiles = db.containerEntryFileDao.findEntriesByMd5Sums(
                    md5ToFilesToEntryToDownloadMap.keys.toList())
            val md5ToExistingEntryFilesMap = existingEntryFiles.map { it.cefMd5 to it }.toMap()
            val itemsToDownloadPartitioned = remainingItemsToDownload.partition {
                it.cefMd5 in existingEntryMd5List || it.cefMd5 in md5ToExistingEntryFilesMap.keys }

            //these are the items that we already have here after searching by md5
            val linksToInsert = itemsToDownloadPartitioned.first.map {
                ContainerEntryWithContainerEntryFile(it.cePath!!,container,
                        md5ToExistingEntryFilesMap[it.cefMd5]!!)
            }
            db.containerEntryDao.insertAndSetIds(linksToInsert)
            pathToEntryMap.putAll(linksToInsert.map { it.cePath!! to it }.toMap())

            //return the items remaining (e.g. those that actually need downloaded)
            return itemsToDownloadPartitioned.second
        }
    }

    actual override fun exportContainer(zipFile: String,progressListener: ExportProgressListener?){
        GlobalScope.launch{
            destinationZipFile = zipFile
            if(File(destinationZipFile).exists()){
                File(destinationZipFile).delete()
            }

            val bytesToRead = ByteArray(BUFFER_SIZE)
            var totalFilesProcessed = 0

            ZipOutputStream(BufferedOutputStream(FileOutputStream(destinationZipFile))).use {zipOut ->
                zipOut.use {
                    for ((fileName, containerEntryWithFile) in pathToEntryMap) {
                        val entryWithFile = containerEntryWithFile.containerEntryFile!!
                        val fileInputStream = FileInputStream(entryWithFile.cefPath!!)
                        val inputStream = if(entryWithFile.compression == COMPRESSION_GZIP) GZIPInputStream(fileInputStream)
                        else BufferedInputStream(fileInputStream, BUFFER_SIZE)

                        inputStream.use { fStream ->
                            BufferedInputStream(fStream).use { iStream ->
                                val entry = ZipEntry(fileName)
                                entry.time = entryWithFile.lastModified
                                entry.size = entryWithFile.ceTotalSize
                                zipOut.putNextEntry(entry)
                                totalFilesProcessed += 1
                                if(progressListener != null){
                                    progressListener.onProcessing(((totalFilesProcessed/pathToEntryMap.size.toFloat()) * 100).toInt())
                                }
                                while (true) {
                                    val bytesRead = iStream.read(bytesToRead)
                                    if (bytesRead == -1) {
                                        break
                                    }
                                    zipOut.write(bytesToRead, 0, bytesRead)
                                }
                            }
                        }
                    }
                    zipOut.closeEntry()
                    zipOut.close()
                    if(progressListener != null){
                        progressListener.onDone()
                    }
                }
            }

        }
    }

    actual override fun cancelExporting() {
        if(File(destinationZipFile).exists()){
            File(destinationZipFile).delete()
        }
        exporting = false

    }

    companion object {
        val EXCLUDED_GZIP_TYPES: List<String> = listOf(".webm", ".mp4", ".avi", ".mov", ".wmv",".flv", ".mkv", ".m4v")
        private const  val BUFFER_SIZE = 2048
    }

}