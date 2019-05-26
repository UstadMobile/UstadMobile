package com.ustadmobile.port.sharedse.container


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.Base64Coder
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import kotlinx.io.IOException
import java.io.*
import java.util.*
import java.util.zip.ZipFile


class ContainerManager {

    private var db: UmAppDatabase? = null

    private var dbRepo: UmAppDatabase? = null

    private var container: Container? = null

    private var pathToEntryMap: MutableMap<String, ContainerEntryWithContainerEntryFile> =
        Hashtable<String, ContainerEntryWithContainerEntryFile>()

    private var newFileDir: File? = null

    val containerUid: Long
        get() = container!!.containerUid

    val allEntries: List<ContainerEntryWithContainerEntryFile>
        get() = ArrayList(pathToEntryMap.values)

    constructor(container: Container, db: UmAppDatabase, dbRepo: UmAppDatabase,
                newFileStorageDir: String) {
        this.container = container
        this.db = db
        this.dbRepo = dbRepo
        this.newFileDir = File(newFileStorageDir)

        loadFromDb()
    }

    constructor(container: Container, db: UmAppDatabase, dbRepo: UmAppDatabase) {
        this.container = container
        this.db = db
        this.dbRepo = dbRepo
        loadFromDb()
    }

    constructor(container: Container, db: UmAppDatabase, dbRepo: UmAppDatabase,
                newFileStorageDir: String,
                pathToEntryMap: MutableMap<String, ContainerEntryWithContainerEntryFile>) {
        this.container = container
        this.db = db
        this.dbRepo = dbRepo
        this.pathToEntryMap = pathToEntryMap
    }

    private fun loadFromDb() {
        val entryList = db!!.containerEntryDao
                .findByContainer(container!!.containerUid)
        for (entry in entryList) {
            pathToEntryMap[entry.cePath!!] = entry
        }
    }


    @Throws(IOException::class)
    fun addEntries(fileToPathInContainerMap: Map<File, String>, options: Int) {
        if (newFileDir == null)
            throw IllegalStateException("ContainerManager in read-only mode: no directory for new files set")

        val fileToMd5Map = HashMap<File, String>()

        val buf = ByteArray(8 * 1024)

        for (inFile in fileToPathInContainerMap.keys) {
            fileToMd5Map[inFile] = Base64Coder.encodeToString(UmFileUtilSe.getMd5Sum(inFile, buf))
        }

        //now see if we already have these files
        val existingFiles = db!!.containerEntryFileDao
                .findEntriesByMd5Sums(ArrayList(fileToMd5Map.values))
        val md5ToExistingFileMap = HashMap<String, ContainerEntryFile>()
        for (entryFile in existingFiles) {
            md5ToExistingFileMap[entryFile.cefMd5!!] = entryFile
        }

        val containerEntriesToDelete = LinkedList<ContainerEntry>()

        val newContainerEntries = ArrayList<ContainerEntryWithContainerEntryFile>()
        for ((srcFile, fileMd5) in fileToMd5Map) {
            val pathInContainer = fileToPathInContainerMap[srcFile]

            var containerEntryFile: ContainerEntryFile? = md5ToExistingFileMap[fileMd5]
            if (containerEntryFile == null) {
                //this is not a duplicate - we need to add it
                containerEntryFile = ContainerEntryFile(fileMd5, srcFile.length(),
                        srcFile.length(), ContainerEntryFile.COMPRESSION_NONE)
                if (options and OPTION_COPY != OPTION_COPY) {
                    containerEntryFile.cefPath = srcFile.path
                }

                containerEntryFile.cefUid = db!!.containerEntryFileDao.insert(containerEntryFile)

                if (options and OPTION_COPY == OPTION_COPY) {
                    val dstFile = File(newFileDir, containerEntryFile.cefUid.toString())
                    UmFileUtilSe.copyFile(srcFile, dstFile)
                    containerEntryFile.cefPath = dstFile.absolutePath
                    db!!.containerEntryFileDao.updateFilePath(containerEntryFile.cefUid,
                            containerEntryFile.cefPath!!)

                }
            }

            val containerEntry = ContainerEntryWithContainerEntryFile(pathInContainer!!,
                    container!!, containerEntryFile)
            newContainerEntries.add(containerEntry)

            if (pathToEntryMap.containsKey(pathInContainer)) {
                //this file is already here. the existing entity needs deleted
                containerEntriesToDelete.add(pathToEntryMap[pathInContainer]!!)
            }
        }

        if (!containerEntriesToDelete.isEmpty())
            db!!.containerEntryDao.deleteList(containerEntriesToDelete)

        db!!.containerEntryDao.insertAndSetIds(ArrayList(newContainerEntries))
        for (file in newContainerEntries) {
            pathToEntryMap[file.cePath!!] = file
        }

        if (options and OPTION_UPDATE_TOTALS == OPTION_UPDATE_TOTALS) {
            container!!.cntNumEntries = pathToEntryMap.size
            var sizeCount: Long = 0
            for (entry in pathToEntryMap.values) {
                if (entry.containerEntryFile != null)
                    sizeCount += entry.containerEntryFile!!.ceCompressedSize
            }
            container!!.fileSize = sizeCount

            dbRepo!!.containerDao.updateContainerSizeAndNumEntries(container!!.containerUid)
        }
    }

    @Throws(IOException::class)
    fun addEntries(fileToPathInContainerMap: Map<File, String>, copy: Boolean) {
        addEntries(fileToPathInContainerMap, (if (copy) OPTION_COPY else 0) or OPTION_UPDATE_TOTALS)
    }

    @Throws(IOException::class)
    fun addEntry(file: File, pathInContainer: String, copy: Boolean) {
        val fileToPathMap = HashMap<File, String>()
        fileToPathMap[file] = pathInContainer
        addEntries(fileToPathMap, copy)
    }

    @Throws(IOException::class)
    fun addEntry(file: File, pathInContainer: String, options: Int) {
        val fileToPathMap = HashMap<File, String>()
        fileToPathMap[file] = pathInContainer
        addEntries(fileToPathMap, options)
    }


    /**
     * Add all the entries from the given zip
     * @param zipFile
     * @throws IOException
     */
    @Throws(IOException::class)
    fun addEntriesFromZip(zipFile: ZipFile, flags: Int) {
        var tmpDir: File? = null
        try {
            tmpDir = File.createTempFile("container" + container!!.containerUid, "uziptmp")
            if (!(tmpDir!!.delete() && tmpDir.mkdirs())) {
                throw IOException("Could not make temporary directory")
            }

            val filesToAddMap = HashMap<File, String>()
            val entries = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.isDirectory)
                    continue

                val unzipTmpFile = File(tmpDir, entry.name)
                val parentDirFile = unzipTmpFile.parentFile
                if (!parentDirFile.isDirectory && !parentDirFile.mkdirs())
                    throw IOException("Could not make directory for: " + unzipTmpFile.absolutePath)

                try {
                    zipFile.getInputStream(entry).use { zipIn -> FileOutputStream(unzipTmpFile).use { fileOut -> UMIOUtils.readFully(zipIn, fileOut) } }
                } catch (e: IOException) {
                    throw e
                }

                filesToAddMap[unzipTmpFile] = entry.name
            }

            addEntries(filesToAddMap, flags)
        } catch (e: IOException) {
            throw e
        } finally {
            if (tmpDir != null)
                UmFileUtilSe.deleteRecursively(tmpDir)
        }


    }


    /**
     * Go through the list of items to be added to the container. Any item that we already have
     * (searching by md5 sum) will be linked. A list of those items that are not yet in this
     * container will be returned
     *
     * @return
     */
    fun linkExistingItems(newEntriesList: List<ContainerEntryWithMd5>): Collection<ContainerEntryWithMd5> {
        val newEntryPathToEntryMap = HashMap<String, ContainerEntryWithMd5>()
        for (item in newEntriesList) {
            newEntryPathToEntryMap[item.cePath!!] = item
        }

        //remove those already in our list
        for (existingEntry in pathToEntryMap.values) {
            newEntryPathToEntryMap.remove(existingEntry.cePath)
        }

        val remainingMd5ToPathMap = HashMap<String, String>()
        for (item in newEntryPathToEntryMap.values) {
            remainingMd5ToPathMap[item.cefMd5!!] = item.cePath!!
        }

        //look for those items which are missing for which we have the md5
        val existingFiles = db!!.containerEntryFileDao
                .findEntriesByMd5Sums(ArrayList(remainingMd5ToPathMap.keys))
        val newEntries = ArrayList<ContainerEntryWithContainerEntryFile>()
        for (existingFile in existingFiles) {
            val entryPath = remainingMd5ToPathMap[existingFile.cefMd5]
            newEntries.add(ContainerEntryWithContainerEntryFile(
                    entryPath!!, container!!, existingFile))
            newEntryPathToEntryMap.remove(entryPath)
        }
        db!!.containerEntryDao.insertList(ArrayList(newEntries))

        return newEntryPathToEntryMap.values
    }


    fun getEntry(pathInContainer: String): ContainerEntryWithContainerEntryFile? {
        return pathToEntryMap[pathInContainer]
    }

    @Throws(IOException::class)
    fun getInputStream(containerEntry: ContainerEntry): InputStream {
        val entryWithFile = pathToEntryMap[containerEntry.cePath]
                ?: throw FileNotFoundException("Container UID #" + container!!.containerUid +
                        " has no entry with path " + containerEntry.cePath)

        return FileInputStream(entryWithFile.containerEntryFile!!.cefPath!!)
    }

    /**
     * Make a copy of this container as a new container - e.g. when making a new version of this
     * file, adding files, etc.
     *
     * @return ContainerManager wiht the same contents, linked to the same underlying files, with the
     * last modified timestamp updated.
     */
    fun copyToNewContainer(): ContainerManager {
        val newContainer = Container()
        newContainer.fileSize = container!!.fileSize
        newContainer.lastModified = System.currentTimeMillis()
        newContainer.cntNumEntries = pathToEntryMap.size
        newContainer.containerContentEntryUid = container!!.containerContentEntryUid
        newContainer.mimeType = container!!.mimeType
        newContainer.mobileOptimized = container!!.mobileOptimized
        newContainer.remarks = container!!.remarks
        newContainer.containerUid = dbRepo!!.containerDao.insert(newContainer)

        val newEntryMap = Hashtable<String, ContainerEntryWithContainerEntryFile>()
        val newContainerEntryList = LinkedList<ContainerEntry>()
        for (entryFile in pathToEntryMap.values) {
            val newEntry = ContainerEntryWithContainerEntryFile(
                    entryFile.cePath!!, newContainer, entryFile.containerEntryFile!!)
            newEntryMap[entryFile.cePath] = newEntry
            newContainerEntryList.add(newEntry)
        }

        db!!.containerEntryDao.insertList(newContainerEntryList)

        return ContainerManager(newContainer, db!!, dbRepo!!, newFileDir!!.absolutePath,
                newEntryMap)
    }

    companion object {


        val OPTION_COPY = 1

        val OPTION_MOVE = 2

        val OPTION_UPDATE_TOTALS = 4
    }

}
