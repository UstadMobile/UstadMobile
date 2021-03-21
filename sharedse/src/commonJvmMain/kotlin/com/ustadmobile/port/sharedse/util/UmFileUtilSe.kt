package com.ustadmobile.port.sharedse.util


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

}
