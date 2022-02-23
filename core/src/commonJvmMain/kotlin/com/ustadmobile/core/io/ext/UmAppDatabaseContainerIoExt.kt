package com.ustadmobile.core.io.ext

import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.encodeBase64
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
import java.util.Base64
import kotlinx.coroutines.NonCancellable
import kotlin.coroutines.cancellation.CancellationException
import com.ustadmobile.lib.util.getSystemTimeInMillis

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
private suspend fun UmAppDatabase.addFileToContainerInternal(containerUid: Long,
                                                             file: File,
                                                             recursive: Boolean,
                                                             addOptions: ContainerAddOptions,
                                                             relativePathPrefix: String,
                                                             fixedPath: String? = null,
                                                             context: Any,
                                                             di: DI) {

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
 * @param originalLength the length of the data
 * @param pathInContainer the path that this entry will receive (used to pass to the
 * addoptions.compressionFilter)
 * @param addOptions ContainerAddOptions
 *
 * @return ContainerEntryFile with the data as per the MD5Sum of the given InputStream
 */
private suspend fun UmAppDatabase.insertOrLookupContainerEntryFile(containerUid: Long, src: InputStream,
                                                                   originalLength: Long,
                                                                   pathInContainer: String,
                                                                   addOptions: ContainerAddOptions) : ContainerEntryFile {

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

    withContext(Dispatchers.IO) {
        val containerEntriesToAdd = mutableListOf<ContainerEntry>()
        zipInputStream.use { zipIn ->
            var zipEntry: ZipEntry? = null
            while(zipIn.nextEntry?.also { zipEntry = it } != null) {
                val zipEntryVal = zipEntry ?: throw IllegalStateException("ZipEntry is not null in loop")
                val nameInZip = zipEntryVal.name

                val containerFile = withContext(NonCancellable) {
                    db.insertOrLookupContainerEntryFile(containerUid, zipIn, zipEntryVal.size,
                                nameInZip, addOptions)
                }

                val entryPath = addOptions.fileNamer.nameContainerFile(nameInZip, nameInZip)

                containerEntriesToAdd.add(ContainerEntry().apply {
                    this.cePath = entryPath
                    this.ceContainerUid = containerUid
                    this.ceCefUid = containerFile.cefUid
                })

            }
        }

        db.containerEntryDao.insertListAsync(containerEntriesToAdd)

        db.containerDao.takeIf { addOptions.updateContainer }
            ?.updateContainerSizeAndNumEntriesAsync(containerUid, getSystemTimeInMillis())
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
