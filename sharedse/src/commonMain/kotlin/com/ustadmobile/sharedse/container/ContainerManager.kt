package com.ustadmobile.sharedse.container

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.util.Base64Coder
import com.ustadmobile.sharedse.io.FileInputStreamSe
import com.ustadmobile.sharedse.io.FileOutputStreamSe
import com.ustadmobile.sharedse.io.FileSe
import com.ustadmobile.sharedse.security.getMessageDigestInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.io.IOException
import kotlinx.io.InputStream
import kotlinx.io.OutputStream

class ContainerManager(private val container: Container,
                              private val db : UmAppDatabase,
                              private val dbRepo: UmAppDatabase,
                              newFilePath: String? = null,
                              private val pathToEntryMap: MutableMap<String, ContainerEntryWithContainerEntryFile> = mutableMapOf()) {

    val newFileDir = if(newFilePath != null) { FileSe(newFilePath) } else { null }

    data class AddEntryOptions(val moveExistingFiles: Boolean = false,
                               val dontUpdateTotals: Boolean = false)


    interface EntrySource {

        /**
         * Length of the entry that is going to be added. This is used only for progress purposes
         */
        val length : Long

        /**
         * The path this entry will have inside the container e.g. META-INF/container.xml
         */
        val pathInContainer: String


        /**
         * An inputstream that provides the contents of the entry
         */
        val inputStream: InputStream

        /**
         * The path to this file. If there is no actual file, e.g. this is being added from a download,
         * then this might be null
         */
        val filePath : String?

        /**
         * The MD5 sum of the entry being added (if known)
         */
        val md5Sum: ByteArray

    }


    class FileEntrySource(private val file: FileSe, override val pathInContainer: String) : EntrySource {
        override val length: Long
            get() = file.length()

        override val inputStream: InputStream
            get() = FileInputStreamSe(file)

        override val filePath: String?
            get() = file.getAbsolutePath()

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



    @UseExperimental(ExperimentalUnsignedTypes::class)
    suspend fun addEntries(vararg entries: EntrySource, addOptions: AddEntryOptions? = null) {
        val addOpts = addOptions ?: AddEntryOptions()
        if(newFileDir == null)
            throw RuntimeException("Cannot add files to container ${container.containerUid} with null newFileDir")


        val existingMd5Sums = entries.map { Base64Coder.encodeToString(it.md5Sum) }
        val existingFiles = db.containerEntryFileDao.findEntriesByMd5Sums(existingMd5Sums)
                .map { it.cefMd5!! to it }.toMap()

        val newContainerEntries = mutableListOf<ContainerEntryWithContainerEntryFile>()

        val entriesParted = entries.partition { Base64Coder.encodeToString(it.md5Sum) in existingFiles.keys }

        //for all entries that we already have
        newContainerEntries.addAll(entriesParted.first.map { ContainerEntryWithContainerEntryFile(it.pathInContainer, container,
                existingFiles[Base64Coder.encodeToString(it.md5Sum)]!!) })

        entriesParted.second.forEach {
            val md5HexStr = it.md5Sum.joinToString(separator = "") { it.toUByte().toString(16)}
            val destFile = FileSe(newFileDir, md5HexStr)
            val currentFilePath = it.filePath

            //TODO: check for any paths that are being overwritten

            if(addOpts.moveExistingFiles && currentFilePath != null) {
                if(!FileSe(currentFilePath).renameTo(destFile)) {
                    throw IOException("Could not rename input file : $currentFilePath")
                }
            }else {
                //copy it
                GlobalScope.async {
                    var destOutStream = null as OutputStream?
                    try {
                        destOutStream = FileOutputStreamSe(destFile)
                        UMIOUtils.readFully(it.inputStream, destOutStream)
                    }catch(e: IOException) {
                        throw e
                    }finally {
                        destOutStream?.close()
                    }
                }.await()
            }

            val containerEntryFile = ContainerEntryFile(Base64Coder.encodeToString(it.md5Sum),
                    destFile.length(), destFile.length(), 0)
            containerEntryFile.cefPath = destFile.getAbsolutePath()
            containerEntryFile.cefUid = db.containerEntryFileDao.insert(containerEntryFile)
            newContainerEntries.add(ContainerEntryWithContainerEntryFile(it.pathInContainer, container,
                    containerEntryFile))
        }

        db.containerEntryDao.insertListAsync(newContainerEntries)

        pathToEntryMap.putAll(newContainerEntries.map { it.cePath!! to it }.toMap())

        if(!addOpts.dontUpdateTotals) {
            container.fileSize = pathToEntryMap.values.fold(0L, {count, next -> count + next.containerEntryFile!!.ceCompressedSize })
            container.cntNumEntries = pathToEntryMap.size
            dbRepo.containerDao.updateContainerSizeAndNumEntries(container.containerUid)
        }
    }

    fun getInputStream(containerEntry: ContainerEntryWithContainerEntryFile): InputStream {
        return FileInputStreamSe(FileSe(containerEntry.containerEntryFile!!.cefPath!!))
    }

    fun getEntry(pathInContainer: String): ContainerEntryWithContainerEntryFile? {
        return pathToEntryMap[pathInContainer]
    }


}