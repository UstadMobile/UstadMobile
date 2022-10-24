package com.ustadmobile.core.io.ext

import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.*
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContainerEntry
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.ByteArrayInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import com.ustadmobile.door.ext.openInputStream
import org.kodein.di.DI
import com.ustadmobile.core.contentformats.har.HarEntry
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.util.ext.maxQueryParamListSize
import kotlinx.coroutines.NonCancellable
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.core.db.dao.findExistingMd5SumsByMd5SumsSafe
import com.ustadmobile.core.io.*
import com.ustadmobile.lib.db.entities.Container
import java.io.FileInputStream
import java.util.*

actual suspend fun UmAppDatabase.addDirToContainer(containerUid: Long, dirUri: DoorUri,
                                                   recursive: Boolean, context:Any, di: DI,
                                                   addOptions: ContainerAddOptions) {

    val repo = this as? DoorDatabaseRepository
            ?: throw IllegalStateException("Must use repo for addFileToContainer")
    val db = repo.db as UmAppDatabase

    dirUri.toFile().listFiles()?.forEach { childFile ->
        withContext(NonCancellable) {
            db.addFileToContainerInternal(containerUid, childFile, recursive, addOptions,
                        "", context = context, di = di)
        }
    }

    containerDao.takeIf { addOptions.updateContainer }?.updateContainerSizeAndNumEntriesAsync(
        containerUid, getSystemTimeInMillis())
}

actual suspend fun UmAppDatabase.addFileToContainer(
    containerUid: Long,
    fileUri: DoorUri,
    pathInContainer: String,
    context: Any,
    di: org.kodein.di.DI,
    addOptions: ContainerAddOptions
) {
    val repo = this as? DoorDatabaseRepository
            ?: throw IllegalStateException("Must use repo for addFileToContainer")
    val db = repo.db as UmAppDatabase
    withContext(NonCancellable) {
        db.addFileToContainerInternal(containerUid, fileUri.toFile(), false,
                addOptions, "", pathInContainer, context, di)
        containerDao.takeIf { addOptions.updateContainer }?.updateContainerSizeAndNumEntriesAsync(
            containerUid, getSystemTimeInMillis())
    }
}


fun File.toContainerEntryFile(totalSize: Long, md5Sum: ByteArray, gzipped: Boolean) = com.ustadmobile.lib.db.entities.ContainerEntryFile().also {
    it.ceCompressedSize  = this.length()
    it.ceTotalSize = totalSize
    it.cefMd5 = md5Sum.encodeBase64()
    it.cefPath = this.absolutePath
    it.compression = if(gzipped) {
        com.ustadmobile.lib.db.entities.ContainerEntryFile.COMPRESSION_GZIP
    }else {
        com.ustadmobile.lib.db.entities.ContainerEntryFile.COMPRESSION_NONE
    }
}

/**
 * @param containerUid container uid we a
 */
private suspend fun UmAppDatabase.addFileToContainerInternal(
    containerUid: Long,
    file: File,
    recursive: Boolean,
    addOptions: ContainerAddOptions,
    relativePathPrefix: String,
    fixedPath: String? = null,
    context: Any,
    di: DI
) {

    val storageDirFile = addOptions.storageDirUri.toFile()
    val containerUidFolder = File(storageDirFile, "$containerUid")
    containerUidFolder.mkdirs()
    if(file.isFile) {
        //add the file
        val tmpFile = File(containerUidFolder,
                "${systemTimeInMillis()}.tmp")
        val relPath = fixedPath ?: relativePathPrefix + file.name

        val entryPath = addOptions.fileNamer.nameContainerFile(relPath, file.toKmpUriString())
        val compress = addOptions.compressionFilter.shouldCompress(entryPath,
                file.toDoorUri().guessMimeType(context, di))

        val md5Sum = withContext(Dispatchers.IO) {
            if(compress) {
                file.gzipAndGetMd5(tmpFile)
            }else if(addOptions.moveFiles) {
                file.md5Sum
            }else {
                file.copyAndGetMd5(tmpFile)
            }
        }

        val md5Hex = md5Sum.toHexString()

        //check if we already have this file in the database
        var containerFile = containerEntryFileDao.findEntryByMd5Sum(md5Sum.encodeBase64())

        if(containerFile == null) {
            val finalDestFile = File(containerUidFolder, md5Hex)
            if(!compress && addOptions.moveFiles) {
                if(!file.renameTo(finalDestFile))
                    throw IOException("Could not rename $file to $finalDestFile")
            }else {
                if(!tmpFile.renameTo(finalDestFile))
                    throw IOException("Could not rename $tmpFile to $finalDestFile")
            }

            containerFile = finalDestFile.toContainerEntryFile(totalSize = file.length(),
                    md5Sum =  md5Sum, gzipped = compress).apply {
                this.cefUid = containerEntryFileDao.insertAsync(this)
            }
        }

        //link the existing entry
        containerEntryDao.insertAsync(ContainerEntry().apply {
            this.cePath = entryPath
            this.ceContainerUid = containerUid
            this.ceCefUid = containerFile.cefUid
        })

        tmpFile.takeIf { it.exists() }?.delete()

    }else if(recursive && file.isDirectory) {
        file.listFiles()?.forEach { childFile ->
            withContext(NonCancellable) {
                addFileToContainerInternal(containerUid, childFile, true, addOptions,
                        relativePathPrefix = "$relativePathPrefix${file.name}/",
                        context = context, di = di)
            }
        }
    }
}

/**
 * Lookup a ContainerEntryFile that matches the MD5 of the data from the given InputStream. If there
 * is no ContainerEntryFile for that MD5 Sum, add a new one. Returns the result
 *
 * @param src InputStream from which to read data
 * @param originalLength the length of the data (uncompressed)
 * @param pathInContainer the path that this entry will receive (used to pass to the
 * addoptions.compressionFilter)
 * @param addOptions ContainerAddOptions
 *
 * @return ContainerEntryFile with the data as per the MD5Sum of the given InputStream
 */
private suspend fun UmAppDatabase.insertOrLookupContainerEntryFile(
    containerUid: Long,
    src: InputStream,
    originalLength: Long,
    pathInContainer: String,
    addOptions: ContainerAddOptions
) : ContainerEntryFile {

    val storageDirFile = addOptions.storageDirUri.toFile()
    val containerFolder = File(storageDirFile, "$containerUid")
    containerFolder.mkdirs()
    val tmpFile = File(containerFolder, "${systemTimeInMillis()}.tmp")
    val gzip = addOptions.compressionFilter.shouldCompress(pathInContainer, null)
    val md5Sum = src.writeToFileAndGetMd5(tmpFile, gzip)

    var containerFile = containerEntryFileDao.findEntryByMd5Sum(md5Sum.encodeBase64())
    if(containerFile == null) {
        val finalDestFile = File(containerFolder, md5Sum.toHexString())
        if(!tmpFile.renameTo(finalDestFile))
            throw IOException("Could not rename $tmpFile to $finalDestFile")

        containerFile = finalDestFile.toContainerEntryFile(originalLength, md5Sum, gzip).apply {
            this.cefUid = containerEntryFileDao.insert(this)
        }
    }else {
        tmpFile.delete()
    }

    return containerFile
}

suspend fun UmAppDatabase.addContainerFromUri(containerUid: Long, uri: com.ustadmobile.door.DoorUri,
                                              context: Any, di: DI, nameInContainer: String,
                                              addOptions: ContainerAddOptions){
    val inputStream = uri.openInputStream(context) ?: throw IOException("resource not found: ${uri.getFileName(context)}")
    val size = uri.getSize(context, di)
    val repo = this as? DoorDatabaseRepository
            ?: throw IllegalStateException("Must use repo for addFileToContainer")
    val db = repo.db as UmAppDatabase
    val containerFile = withContext(NonCancellable) {
        db.insertOrLookupContainerEntryFile(containerUid, inputStream, size, nameInContainer, addOptions)
    }

    ContainerEntry().apply {
        this.cePath = nameInContainer
        this.ceContainerUid = containerUid
        this.ceCefUid = containerFile.cefUid
        this.ceUid = db.containerEntryDao.insert(this)
    }

    containerDao.takeIf { addOptions.updateContainer }?.updateContainerSizeAndNumEntriesAsync(
        containerUid, getSystemTimeInMillis())
}

suspend fun UmAppDatabase.addEntriesToContainerFromZip(
    containerUid: Long,
    zipInputStream: ZipInputStream,
    addOptions: ContainerAddOptions
) {

    val db = if(this is DoorDatabaseRepository) {
        this.db as UmAppDatabase
    }else {
        this
    }

    class FileToAdd(
        val tmpFile: File,
        val md5Sum: ByteArray,
        val pathInContainer: String,
        val isCompressed: Boolean,
        val uncompressedSize: Long
    ) {
        val md5Base64: String by lazy(LazyThreadSafetyMode.NONE) {
            md5Sum.encodeBase64()
        }
    }

    withContext(Dispatchers.IO) {
        val storageDirFile = addOptions.storageDirUri.toFile()
        val containerDir = File(storageDirFile, containerUid.toString())
        containerDir.takeIf { !it.exists() } ?.mkdirs()

        val zipFilesToAdd = mutableListOf<FileToAdd>()
        zipInputStream.use { zipIn ->
            lateinit var zipEntry: ZipEntry

            var fileCounter = 0
            while(zipIn.nextEntry?.also { zipEntry = it } != null) {
                //Do not include zip directories
                if(zipEntry.isDirectory)
                    continue

                val nameInZip = addOptions.fileNamer.nameContainerFile(zipEntry.name,
                    zipEntry.name)

                val entryTmpFile = File(containerDir, "${fileCounter++}.tmp")
                val useGzip = addOptions.compressionFilter.shouldCompress(nameInZip, null)
                val entryMd5 = zipIn.writeToFileAndGetMd5(entryTmpFile, useGzip)
                zipFilesToAdd += FileToAdd(entryTmpFile, entryMd5, nameInZip, useGzip, zipEntry.size)
            }
        }

        val existingMd5s = db.containerEntryFileDao.findExistingMd5SumsByMd5SumsSafe(
            zipFilesToAdd.map { it.md5Base64 }, maxQueryParamListSize).filterNotNull().toSet()
        val filesToStore = mutableMapOf<String, FileToAdd>()
        val filesToDelete = mutableListOf<FileToAdd>()

        //Need to handle the edge case where we have a zip that contains two or more entries with
        // the same md5
        zipFilesToAdd.forEach {
            if(it.md5Base64 !in existingMd5s && !filesToStore.containsKey(it.md5Base64))
                filesToStore[it.md5Base64] = it
            else
                filesToDelete += it
        }

        val containerEntryFilesToInsert = filesToStore.map { fileToAddEntry ->
            val fileToAdd = fileToAddEntry.value
            val destFile = File(containerDir, fileToAdd.md5Sum.toHexString())
            if(!fileToAdd.tmpFile.renameTo(destFile))
                throw IOException("Could not rename ${fileToAdd.tmpFile.absolutePath} to " +
                    "${destFile.absolutePath} container uid $containerUid path = ${fileToAdd.pathInContainer}")

            ContainerEntryFile().apply {
                cefMd5 = fileToAdd.md5Base64
                compression = if(fileToAdd.isCompressed) {
                    ContainerEntryFile.COMPRESSION_GZIP
                }else {
                    ContainerEntryFile.COMPRESSION_NONE
                }
                ceCompressedSize = destFile.length()
                ceTotalSize = fileToAdd.uncompressedSize
                cefPath = destFile.absolutePath
            }
        }

        filesToDelete.forEach {
            it.tmpFile.delete()
        }


        db.withDoorTransactionAsync { txDb: UmAppDatabase ->
            txDb.containerEntryFileDao.insertListAsync(containerEntryFilesToInsert)
            zipFilesToAdd.forEach {
                txDb.containerEntryDao.insertWithMd5SumsAsync(containerUid, it.pathInContainer,
                    it.md5Base64)
            }
            txDb.containerDao.takeIf { addOptions.updateContainer }
                ?.updateContainerSizeAndNumEntriesAsync(containerUid, getSystemTimeInMillis())
        }
    }
}

actual suspend fun UmAppDatabase.addEntriesToContainerFromZip(
    containerUid: Long,
    zipUri: com.ustadmobile.door.DoorUri,
    addOptions: ContainerAddOptions,
    context: Any
) {
    withContext(Dispatchers.IO) {
        val zipInputStream = ZipInputStream(zipUri.openInputStream(context))
        addEntriesToContainerFromZip(containerUid, zipInputStream, addOptions)
    }
}

/**
 * Add entries to the given container from a resource. This will open an inputStream using
 * Class.getResourceAsStream
 *
 * @param containerUid The ContainerUID to add the contents to
 * @param javaClass Java.lang.Class that will be used to invoke getResourceAsStream
 * @param resourcePath the resource path as it will be provided to getResourceAsStream
 * @param addOptions ContainerAddOptions
 */
suspend fun UmAppDatabase.addEntriesToContainerFromZipResource(containerUid: Long, javaClass: Class<*>,
                                                               resourcePath: String, addOptions: ContainerAddOptions) {
    withContext(Dispatchers.IO) {
        val zipInputStream = ZipInputStream(javaClass.getResourceAsStream(resourcePath))
        addEntriesToContainerFromZip(containerUid, zipInputStream, addOptions)
    }
}

suspend fun UmAppDatabase.addEntryToContainerFromResource(containerUid: Long, javaClass: Class<*>,
                                                          resourcePath: String, pathInContainer: String,
                                                          di: DI,
                                                          addOptions: ContainerAddOptions) {
    withContext(Dispatchers.IO) {
        val tmpFile = File(addOptions.storageDirUri.toFile(), "${systemTimeInMillis()}.tmp")
        val resourceIn = javaClass.getResourceAsStream(resourcePath) ?: throw IOException("resource not found: $resourcePath")
        resourceIn.writeToFile(tmpFile)
        addFileToContainer(containerUid, tmpFile.toDoorUri(), pathInContainer, Any(), di, addOptions)
        tmpFile.takeIf { it.exists() }?.delete()
    }
}


suspend fun UmAppDatabase.addHarEntryToContainer(containerUid: Long, harEntry: HarEntry,
                                                 pathInContainer: String,
                                                 addOptions: ContainerAddOptions) {

    val harResponse = harEntry.response ?: throw IllegalArgumentException("HarEntry being added" +
            " as $pathInContainer to $containerUid must have a response!")
    val harContent = harResponse.content ?: throw IllegalArgumentException("HarEntry being added" +
            " as $pathInContainer to $containerUid must have response content!")
    val harContentText = harContent.text ?: throw IllegalArgumentException("HarEntry being added" +
            " as $pathInContainer to $containerUid must have response content text!")

    withContext(Dispatchers.IO) {
        val storageDir = addOptions.storageDirUri.toFile()
        val tmpFile = File(storageDir, "${systemTimeInMillis()}.tmp")
        val harInputStream = if(harContent.encoding == "base64") {
            ByteArrayInputStream(Base64.getDecoder().decode(harContentText))
        }else {
            ByteArrayInputStream(harContentText.toByteArray())
        }


        val compress = addOptions.compressionFilter.shouldCompress(pathInContainer, null)

        val entryMd5 = harInputStream.writeToFileAndGetMd5(tmpFile, compress)

    }

    containerDao.takeIf { addOptions.updateContainer }
            ?.updateContainerSizeAndNumEntriesAsync(containerUid, getSystemTimeInMillis())

}

fun ContainerBuilder.addFile(
    pathInContainer: String,
    file: File,
    compression: ContainerBuilder.Compression = ContainerBuilder.Compression.GZIP
) : ContainerBuilder {
    containerSources += ContainerFileSource(pathInContainer, file, compression)
    return this
}

fun ContainerBuilder.addZip(
    zipInput: () -> ZipInputStream,
    pathInContainerPrefix: String = "",
    compression: PathCompressionFilter = DefaultPathCompressionFilter(),
) : ContainerBuilder {
    containerSources += ContainerZipSource(zipInput, pathInContainerPrefix, compression)
    return this
}

fun ContainerBuilder.addZip(
    zipFile: File,
    pathInContainerPrefix: String = "",
    compression: PathCompressionFilter = DefaultPathCompressionFilter(),
): ContainerBuilder {
    containerSources += ContainerZipSource({ ZipInputStream(FileInputStream(zipFile)) },
        pathInContainerPrefix, compression)
    return this
}

fun ContainerBuilder.addZip(
    zipUri: DoorUri,
    context: Any,
    pathInContainerPrefix: String = "",
    compression: PathCompressionFilter = DefaultPathCompressionFilter(),
) : ContainerBuilder {
    containerSources += ContainerZipSource({ ZipInputStream(zipUri.openInputStream(context)
        ?: throw IllegalArgumentException("Cannot get input stream for uri: $zipUri")) },
        pathInContainerPrefix, compression)
    return this
}

/**
 * Directly add the given DoorUri to the container being built
 */
fun ContainerBuilder.addUri(
    pathInContainer: String,
    uri: DoorUri,
    context: Any,
    compression: ContainerBuilder.Compression = ContainerBuilder.Compression.GZIP,
): ContainerBuilder {
    containerSources += ContainerUriSource(pathInContainer, uri, context, compression)
    return this
}

fun ContainerBuilder.addText(
    pathInContainer: String,
    text: String,
    compression: ContainerBuilder.Compression = ContainerBuilder.Compression.GZIP,
) : ContainerBuilder {
    containerSources += ContainerTextSource(pathInContainer, text, compression)
    return this
}

actual suspend fun ContainerBuilder.build(): Container {
    val container = Container().apply {
        this.containerContentEntryUid = contentEntryUid
        this.mimeType = this@build.mimeType
        this.cntLastModified = getSystemTimeInMillis()
        this.containerUid = db.containerDao.insertAsync(this)
    }

    val containerUid = container.containerUid

    val containerUidDir = File(containerStorageUri.toFile(), containerUid.toString()).also {
        it.mkdirs()
    }

    containerSources.forEach { source: ContainerBuilder.ContainerSource ->
        when(source) {
            is ContainerFileSource -> db.addContainerAddFile(source, containerUid, containerUidDir)
            is ContainerZipSource -> db.addContainerAddZip(source, containerUid, containerUidDir)
            is ContainerUriSource -> db.addContainerAddUri(source, containerUid, containerUidDir)
            is ContainerTextSource -> db.addContainerAddText(source, containerUid, containerUidDir)
            else -> throw IllegalArgumentException("unsupported source: $source")
        }
    }

    db.containerDao.updateContainerSizeAndNumEntriesAsync(containerUid, getSystemTimeInMillis())

    return container
}

private suspend fun UmAppDatabase.addContainerAddFile(
    fileSource: ContainerFileSource,
    containerUid: Long,
    containerUidFolder: File,
) {
    //add the file
    val tmpFile = File(containerUidFolder,
        "${systemTimeInMillis()}.tmp")

    val md5Sum = withContext(Dispatchers.IO) {
        if(fileSource.compression == ContainerBuilder.Compression.GZIP) {
            fileSource.file.gzipAndGetMd5(tmpFile)
        }else if(fileSource.moveOriginalFile) {
            fileSource.file.md5Sum
        }else {
            fileSource.file.copyAndGetMd5(tmpFile)
        }
    }

    val md5Hex = md5Sum.toHexString()

    val db = if(this is DoorDatabaseRepository) {
        this.db as UmAppDatabase
    }else {
        this
    }

    //check if we already have this file in the database
    var containerFile = db.containerEntryFileDao.findEntryByMd5Sum(md5Sum.encodeBase64())

    val totalSize = fileSource.file.length()
    if(containerFile == null) {
        val finalDestFile = File(containerUidFolder, md5Hex)
        if(fileSource.compression != ContainerBuilder.Compression.GZIP && fileSource.moveOriginalFile) {
            if(!fileSource.file.renameTo(finalDestFile))
                throw IOException("Could not rename ${fileSource.file} to $finalDestFile")
        }else {
            if(!tmpFile.renameTo(finalDestFile))
                throw IOException("Could not rename $tmpFile to $finalDestFile")
        }

        containerFile = finalDestFile.toContainerEntryFile(totalSize = totalSize,
            md5Sum =  md5Sum, gzipped = fileSource.compression == ContainerBuilder.Compression.GZIP
        ).apply {
            this.cefUid = db.containerEntryFileDao.insertAsync(this)
        }
    }

    //link the existing entry
    db.containerEntryDao.insertAsync(ContainerEntry().apply {
        this.cePath = fileSource.pathInContainer
        this.ceContainerUid = containerUid
        this.ceCefUid = containerFile.cefUid
    })

    tmpFile.takeIf { it.exists() }?.delete()
}

private suspend fun UmAppDatabase.addContainerAddZip(
    zipSource: ContainerZipSource,
    containerUid: Long,
    containerUidFolder: File,
) {
    val db = if(this is DoorDatabaseRepository) {
        this.db as UmAppDatabase
    }else {
        this
    }

    class FileToAdd(
        val tmpFile: File,
        val md5Sum: ByteArray,
        val pathInContainer: String,
        val isCompressed: Boolean,
        val uncompressedSize: Long
    ) {
        val md5Base64: String by lazy(LazyThreadSafetyMode.NONE) {
            md5Sum.encodeBase64()
        }
    }

    withContext(Dispatchers.IO) {
        val containerDir = containerUidFolder

        val zipFilesToAdd = mutableListOf<FileToAdd>()
        zipSource.zipInput().use { zipIn ->
            lateinit var zipEntry: ZipEntry

            var fileCounter = 0
            while(zipIn.nextEntry?.also { zipEntry = it } != null) {
                //Do not include zip directories
                if(zipEntry.isDirectory)
                    continue

                val nameInZip = "${zipSource.pathInContainerPrefix}${zipEntry.name}"

                val entryTmpFile = File(containerDir, "${fileCounter++}.tmp")
                val useGzip = zipSource.compression.getCompressionForPath(zipEntry.name) == ContainerBuilder.Compression.GZIP
                val entryMd5 = zipIn.writeToFileAndGetMd5(entryTmpFile, useGzip)
                zipFilesToAdd += FileToAdd(entryTmpFile, entryMd5, nameInZip, useGzip, zipEntry.size)
            }
        }

        val existingMd5s = db.containerEntryFileDao.findExistingMd5SumsByMd5SumsSafe(
            zipFilesToAdd.map { it.md5Base64 }, maxQueryParamListSize).filterNotNull().toSet()
        val filesToStore = mutableMapOf<String, FileToAdd>()
        val filesToDelete = mutableListOf<FileToAdd>()

        //Need to handle the edge case where we have a zip that contains two or more entries with
        // the same md5
        zipFilesToAdd.forEach {
            if(it.md5Base64 !in existingMd5s && !filesToStore.containsKey(it.md5Base64))
                filesToStore[it.md5Base64] = it
            else
                filesToDelete += it
        }

        val containerEntryFilesToInsert = filesToStore.map { fileToAddEntry ->
            val fileToAdd = fileToAddEntry.value
            val destFile = File(containerDir, fileToAdd.md5Sum.toHexString())
            if(!fileToAdd.tmpFile.renameTo(destFile))
                throw IOException("Could not rename ${fileToAdd.tmpFile.absolutePath} to " +
                    "${destFile.absolutePath} container uid $containerUid path = ${fileToAdd.pathInContainer}")

            ContainerEntryFile().apply {
                cefMd5 = fileToAdd.md5Base64
                compression = if(fileToAdd.isCompressed) {
                    ContainerEntryFile.COMPRESSION_GZIP
                }else {
                    ContainerEntryFile.COMPRESSION_NONE
                }
                ceCompressedSize = destFile.length()
                ceTotalSize = fileToAdd.uncompressedSize
                cefPath = destFile.absolutePath
            }
        }

        filesToDelete.forEach {
            it.tmpFile.delete()
        }


        db.withDoorTransactionAsync { txDb: UmAppDatabase ->
            txDb.containerEntryFileDao.insertListAsync(containerEntryFilesToInsert)
            zipFilesToAdd.forEach {
                txDb.containerEntryDao.insertWithMd5SumsAsync(containerUid, it.pathInContainer,
                    it.md5Base64)
            }
        }
    }
}

private suspend fun UmAppDatabase.addContainerAddUri(
    uriSource: ContainerUriSource,
    containerUid: Long,
    containerUidFolder: File
) {
    val uri = uriSource.uri
    val inputStream = uri.openInputStream(uriSource.context)
        ?: throw IOException("resource not found: ${uri.getFileName(uriSource.context)}")
    val countingInputStream = CountInputStream(inputStream)
    val db = if(this is DoorDatabaseRepository) {
        this.db as UmAppDatabase
    }else {
        this
    }

    val tmpFile = File(containerUidFolder, "${systemTimeInMillis()}.tmp")
    val useGzip = uriSource.compression == ContainerBuilder.Compression.GZIP
    val md5Sum = countingInputStream.writeToFileAndGetMd5(tmpFile, useGzip)
    val originalLength = countingInputStream.byteReadCount

    var containerFile = db.containerEntryFileDao.findEntryByMd5Sum(md5Sum.encodeBase64())
    if(containerFile == null) {
        val finalDestFile = File(containerUidFolder, md5Sum.toHexString())
        if(!tmpFile.renameTo(finalDestFile))
            throw IOException("Could not rename $tmpFile to $finalDestFile")

        containerFile = finalDestFile.toContainerEntryFile(originalLength, md5Sum, useGzip).apply {
            this.cefUid = db.containerEntryFileDao.insert(this)
        }
    }else {
        tmpFile.delete()
    }

    //link it
    db.containerEntryDao.insertAsync(ContainerEntry().apply {
        this.cePath = uriSource.pathInContainer
        this.ceContainerUid = containerUid
        this.ceCefUid = containerFile.cefUid
    })
}

private suspend fun UmAppDatabase.addContainerAddText(
    textSource: ContainerTextSource,
    containerUid: Long,
    containerUidFolder: File
) {
    val tmpFile = File(containerUidFolder, "${systemTimeInMillis()}.tmp")
    val useGzip = textSource.compression == ContainerBuilder.Compression.GZIP
    val countingInputStream = CountInputStream(ByteArrayInputStream(textSource.text.toByteArray()))
    val md5Sum = countingInputStream.writeToFileAndGetMd5(tmpFile, useGzip)
    val originalLength = countingInputStream.byteReadCount

    val db = if(this is DoorDatabaseRepository) {
        this.db as UmAppDatabase
    }else {
        this
    }

    var containerFile = db.containerEntryFileDao.findEntryByMd5Sum(md5Sum.encodeBase64())
    if(containerFile == null) {
        val finalDestFile = File(containerUidFolder, md5Sum.toHexString())
        if(!tmpFile.renameTo(finalDestFile))
            throw IOException("Could not rename $tmpFile to $finalDestFile")

        containerFile = finalDestFile.toContainerEntryFile(originalLength, md5Sum, useGzip).apply {
            this.cefUid = db.containerEntryFileDao.insert(this)
        }
    }else {
        tmpFile.delete()
    }

    //link it
    db.containerEntryDao.insertAsync(ContainerEntry().apply {
        this.cePath = textSource.pathInContainer
        this.ceContainerUid = containerUid
        this.ceCefUid = containerFile.cefUid
    })
}
