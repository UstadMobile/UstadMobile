package com.ustadmobile.port.sharedse.util

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.Container
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object UmFileUtilSe {

    /**
     * Check if the directory is writable
     * @param dir Directory to be checked
     * @return True if is writable otherwise is read only
     */
    fun canWriteFileInDir(dir: File): Boolean {
        var canWriteFiles = false
        val testFile = File(dir.absoluteFile, System.currentTimeMillis().toString() + ".txt")
        try {
            val writer = FileWriter(testFile)
            writer.append("sampletest")
            writer.flush()
            writer.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            canWriteFiles = false
        } catch (e: IOException) {
            e.printStackTrace()
            canWriteFiles = false
        }

        if (testFile.exists()) {
            canWriteFiles = testFile.delete()
        }
        return canWriteFiles
    }

    fun deleteRecursively(file: File): Boolean {
        var allDeleted = true
        for (childFile in file.listFiles()!!) {
            if (childFile.isDirectory) {
                allDeleted = allDeleted and deleteRecursively(childFile)
            } else if (!childFile.delete()) {
                UMLog.l(UMLog.WARN, 53, "WARN: delete recursively " +
                        "could not delete child file " + childFile.absolutePath)
                childFile.deleteOnExit()
                allDeleted = false
            }
        }

        val thisFileDeleted = file.delete()
        allDeleted = allDeleted and thisFileDeleted
        if (!thisFileDeleted) {
            file.deleteOnExit()
            UMLog.l(UMLog.WARN, 53, "WARN: delete recursively " +
                    "could not delete " + file.absolutePath)
        }

        return allDeleted
    }


    @Throws(IOException::class)
    @JvmOverloads
    fun copyFile(src: File, dst: File, bufferSize: Int = UMIOUtils.DEFAULT_BUFFER_SIZE) {
        FileInputStream(src).use { fin -> FileOutputStream(dst).use { fout -> UMIOUtils.readFully(fin, fout, bufferSize) } }
    }


    @Throws(IOException::class)
    @JvmOverloads
    fun getMd5Sum(`in`: InputStream, buf: ByteArray = ByteArray(UMIOUtils.DEFAULT_BUFFER_SIZE)): ByteArray {
        try {
            val digest = MessageDigest.getInstance("MD5")

            var bytesRead: Int

            while (`in`.read(buf).also { bytesRead = it } != -1) {
                digest.update(buf, 0, bytesRead)
            }

            return digest.digest()
        } catch (ne: NoSuchAlgorithmException) {
            throw IOException(ne)
        } finally {
            `in`.close()
        }
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun getMd5Sum(file: File, buf: ByteArray = ByteArray(UMIOUtils.DEFAULT_BUFFER_SIZE)): ByteArray {
        try {
            FileInputStream(file).use { fin ->
                val digest = MessageDigest.getInstance("MD5")

                var bytesRead: Int
                while (fin.read(buf).also { bytesRead = it } != -1) {
                    digest.update(buf, 0, bytesRead)
                }

                return digest.digest()
            }
        } catch (e: IOException) {
            throw e
        } catch (ne: NoSuchAlgorithmException) {
            throw IOException(ne)
        }

    }

    @Throws(IOException::class)
    fun extractResourceToFile(resourcePath: String, destFile: File) {
        FileOutputStream(destFile).use { fout -> UmFileUtilSe::class.java.getResourceAsStream(resourcePath)!!.use { resIn -> UMIOUtils.readFully(resIn, fout) } }
    }

    /**
     * Copy input stream to a file
     */
    @JvmStatic
    fun File.copyInputStreamToFile(inputStream: InputStream) {
        inputStream.use { input ->
            this.outputStream().use { fileOut ->
                input.copyTo(fileOut)
            }
        }
    }

    @Throws(IOException::class)
    fun makeTempDir(prefix: String, postfix: String): File {
        val tmpDir = File.createTempFile(prefix, postfix)
        return if (tmpDir.delete() && tmpDir.mkdirs())
            tmpDir
        else
            throw IOException("Could not delete / create tmp dir")
    }

    /**
     * Represents a temporary container created from a zip
     */
    class TempZipContainer(val container: Container, val containerManager: ContainerManager, val containerFileDir: File)

    /**
     * Small helper method, mostly for use with tests. Given a path of a resource, the resource
     * will be extracted, and a container will be made. Once the contents of the zip have been
     * added to the container, the zip itself will be deleted from the disk. After usage, only
     * containerFileDir will need to be deleted. This can be helpful if you want to quickly create
     * a container for an zip based content (e.g. EPUB, Xapi, zip, etc).
     *
     * @param db UmAppDatabase
     * @param repo repo for UmAppDatabase
     * @param resourcePath The path to the resource as it stored e.g. /com/ustadmobile/path/to/file.zip
     * @param containerFileDir The temporary directory where container file entries will actually be stored
     * @return TempZipContainer representing a zip container, and info about it's entries.
     * @throws IOException if there is an IOException in the underlying operation
     */
    @Throws(IOException::class)
    @JvmOverloads
    fun makeTempContainerFromClassResource(db: UmAppDatabase,
                                           repo: UmAppDatabase,
                                           resourcePath: String,
                                           containerFileDir: File = makeTempDir("makeTempContainerDir", "." + System.currentTimeMillis())): TempZipContainer {
        var tmpZipFile: File? = null
        try {
            tmpZipFile = File.createTempFile("makeTempContainerFromClass", "." + System.currentTimeMillis())
            extractResourceToFile(resourcePath, tmpZipFile)
            val container = Container()
            container.containerUid = repo.containerDao.insert(container)

            val containerManager = ContainerManager(container, db, repo,
                    containerFileDir.absolutePath)

            addEntriesFromZipToContainer(tmpZipFile!!.absolutePath, containerManager)
            return TempZipContainer(container, containerManager, containerFileDir)
        } catch (e: IOException) {
            throw e
        } finally {

            if (tmpZipFile != null && !tmpZipFile.delete())
                tmpZipFile.deleteOnExit()
        }

    }

}
/**
 * Synanomous to makeTempContainerFromClassResource(db, repo, resourcePath, makeTempDir)
 *
 * @param db UmAppDatabase
 * @param repo repo for UmAppDatabase
 * @param resourcePath The path to the resource as it stored e.g. /com/ustadmobile/path/to/file.zip
 *
 * @return TempZipContainer given the above parameters
 * @throws IOException If there is an IOException in the underlying operation
 */
