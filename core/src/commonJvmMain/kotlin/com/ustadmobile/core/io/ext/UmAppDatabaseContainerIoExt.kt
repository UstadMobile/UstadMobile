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
import java.io.FileInputStream
import java.io.IOException
import java.net.URI
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

actual suspend fun UmAppDatabase.addDirToContainer(containerUid: Long, dirUri: DoorUri,
                                                   recursive: Boolean,
                                                   addOptions: ContainerAddOptions) {

    val repo = this as? DoorDatabaseRepository
            ?: throw IllegalStateException("Must use repo for addFileToContainer")
    val db = repo.db as UmAppDatabase

    dirUri.toFile().listFiles()?.forEach { childFile ->
        db.addFileToContainerInternal(containerUid, childFile, recursive, addOptions,
                "")
    }

    containerDao.takeIf { addOptions.updateContainer }?.updateContainerSizeAndNumEntriesAsync(containerUid)
}

actual suspend fun UmAppDatabase.addFileToContainer(containerUid: Long, fileUri: DoorUri,
                                                    pathInContainer: String, addOptions: ContainerAddOptions) {
    val repo = this as? DoorDatabaseRepository
            ?: throw IllegalStateException("Must use repo for addFileToContainer")
    val db = repo.db as UmAppDatabase
    db.addFileToContainerInternal(containerUid, fileUri.toFile(), false,
        addOptions, "", pathInContainer)
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
                                                             fixedPath: String? = null) {

    val storageDirFile = addOptions.storageDirUri.toFile()

    if(file.isFile) {
        //add the file
        val tmpFile = File(storageDirFile,
                "${systemTimeInMillis()}.tmp")
        val relPath = fixedPath ?: relativePathPrefix + file.name

        val entryPath = addOptions.fileNamer.nameContainerFile(relPath, file.toKmpUriString())
        val compress = addOptions.compressionFilter.shouldCompress(entryPath,
                file.toDoorUri().guessMimeType())

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
            val finalDestFile = File(storageDirFile, md5Hex)
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
            addFileToContainerInternal(containerUid, childFile, true, addOptions,
                    relativePathPrefix = "$relativePathPrefix${file.name}/")
        }
    }
}


actual suspend fun UmAppDatabase.addEntriesToContainerFromZip(containerUid: Long,
                                                              zipUri: DoorUri,
                                                              addOptions: ContainerAddOptions) {

    val repo = this as? DoorDatabaseRepository
            ?: throw IllegalStateException("Must use repo for addFileToContainer")
    val db = repo.db as UmAppDatabase

    withContext(Dispatchers.IO) {
        val storageDirFile = addOptions.storageDirUri.toFile()
        val zipInputStream = ZipInputStream(FileInputStream(zipUri.toFile()))
        zipInputStream.use { zipIn ->
            var zipEntry: ZipEntry? = null
            while(zipIn.nextEntry?.also { zipEntry = it } != null) {
                val zipEntryVal = zipEntry ?: throw IllegalStateException("ZipEntry is not null in loop")
                val nameInZip = zipEntryVal.name
                val tmpFileOut = File(storageDirFile, "${systemTimeInMillis()}.tmp")

                //TODO: specify mime type here
                val gzip = addOptions.compressionFilter.shouldCompress(nameInZip, null)
                val md5Sum = zipIn.writeToFileAndGetMd5(tmpFileOut, gzip)

                var containerFile = db.containerEntryFileDao.findEntryByMd5Sum(md5Sum.encodeBase64())
                if(containerFile == null) {
                    val finalDestFile = File(storageDirFile, md5Sum.toHexString())
                    if(!tmpFileOut.renameTo(finalDestFile))
                        throw IOException("Could not rename $tmpFileOut to $finalDestFile")

                    containerFile = finalDestFile.toContainerEntryFile(zipEntryVal.size, md5Sum, gzip).apply {
                        this.cefUid = db.containerEntryFileDao.insert(this)
                    }
                }

                val entryPath = addOptions.fileNamer.nameContainerFile(nameInZip, nameInZip)
                db.containerEntryDao.insertAsync(ContainerEntry().apply {
                    this.cePath = entryPath
                    this.ceContainerUid = containerUid
                    this.ceCefUid = containerFile.cefUid
                })

                tmpFileOut.delete()

            }
        }
    }

    containerDao.takeIf { addOptions.updateContainer }?.updateContainerSizeAndNumEntriesAsync(containerUid)

}