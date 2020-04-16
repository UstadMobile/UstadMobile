package com.ustadmobile.staging.core.xlsx

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Zip util
 */
actual class ZipUtil {

    actual constructor()

    fun zipThisFoldersContents(sourcePath: String, toLocation: String): Boolean {
        val folder = File(sourcePath)
        try {
            val origin: BufferedInputStream? = null
            val dest = FileOutputStream(toLocation)
            val out = ZipOutputStream(BufferedOutputStream(
                    dest))

            zipSubFolder(out, folder, sourcePath.length)

            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true

    }

    /**
     * Zips soure path folder to location
     * @param sourcePath    Source of folder to zip
     * @param toLocation    Zip file path to save to.
     * @return
     */
    fun zipFileAtPath(sourcePath: String, toLocation: String): Boolean {

        val sourceFile = File(sourcePath)
        try {
            val origin: BufferedInputStream
            val dest = FileOutputStream(toLocation)
            val out = ZipOutputStream(BufferedOutputStream(
                    dest))
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length)
            } else {
                val data = ByteArray(BUFFER)
                val fi = FileInputStream(sourcePath)
                origin = BufferedInputStream(fi, BUFFER)
                val entry = ZipEntry(getLastPathComponent(sourcePath))
                entry.setTime(sourceFile.lastModified()) // to keep modification time after unzipping
                out.putNextEntry(entry)
                var count: Int

                while (true) {
                    val count = origin.read(data, 0, BUFFER)
                    if(count == -1){
                        break
                    }
                    out.write(data, 0, count)
                }
//                while ((count = origin.read(data, 0, BUFFER)) != -1) {
//                    out.write(data, 0, count)
//                }
            }
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }

    /**
     * Zips a subfolder
     *
     * @param out               Zip outputstream
     * @param folder            The subfolder
     * @param basePathLength    The basePath length
     * @throws IOException      exception
     */
    @Throws(IOException::class)
    fun zipSubFolder(out: ZipOutputStream, folder: File,
                     basePathLength: Int) {

        val fileList = folder.listFiles()
        var origin: BufferedInputStream
        for (currentFile in fileList) {
            if (currentFile.isDirectory()) {
                zipSubFolder(out, currentFile, basePathLength)
            } else {
                val data = ByteArray(BUFFER)
                val currentFilePath = currentFile.getPath()
                val relativePath = currentFilePath
                        .substring(basePathLength)
                val fi = FileInputStream(currentFilePath)
                origin = BufferedInputStream(fi, BUFFER)
                val entry = ZipEntry(relativePath)
                entry.setTime(currentFile.lastModified()) // to keep modification time after unzipping
                out.putNextEntry(entry)
                var count: Int

                while (true) {
                    val count = origin.read(data, 0, BUFFER)
                    if(count == -1){
                        break
                    }
                    out.write(data, 0, count)
                }

//                while ((count = origin.read(data, 0, BUFFER)) != -1) {
//                    out.write(data, 0, count)
//                }
                origin.close()
            }
        }
    }

    /**
     * Gets the last path component
     * @param filePath  the file path
     * @return  The last path
     */
    private fun getLastPathComponent(filePath: String): String {
        val segments = filePath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (segments.size == 0) "" else segments[segments.size - 1]
    }


    actual fun createEmptyZipFile(zipPath:String){
        createEmptyZipFile1(zipPath)
    }

    companion object {
        private val BUFFER = 2048




        /**
         * Creates an empty Zip file.
         * @param zipPath       The path of the zip.
         * @return              The zip file as File object. Null if failed.
         * @throws IOException  File exception
         */
        @Throws(IOException::class)
        fun createEmptyZipFile1(zipPath: String){

            //Create the file.
            val output = File(zipPath)
            if (output.createNewFile()) {
                createZip(zipPath)
                //return File(zipPath)
            } else {
                //return null
            }
        }


        fun createZip(outfile: String) {

            try {
                //input file
                val input = FileInputStream(outfile)
                //output file
                val zip = ZipOutputStream(FileOutputStream(outfile))
                //name the file inside the zip file
                zip.putNextEntry(ZipEntry(outfile))

                val buffer = ByteArray(1024)
                var len: Int
                //copy the file to the zip
                while (true) {
                    val len = input.read(buffer)
                    if(len < 0){
                        break
                    }
                    println(len)
                    zip.write(buffer, 0, len)
                }

//                while ((len = input.read(buffer)) > 0) {
//                    println(len)
//                    zip.write(buffer, 0, len)
//                }
                zip.closeEntry()
                zip.flush()
                input.close()
                zip.close()

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        /**
         * Adds a list of files to a zip
         * @param files     A string array of all files paths that need to be added to the zip
         * @param zipFile   The file path of the zip file to add the files to.
         */
        fun zip(files: Array<String>, zipFile: String) {
            try {
                var origin: BufferedInputStream
                val dest = FileOutputStream(zipFile)

                val out = ZipOutputStream(BufferedOutputStream(dest))

                val data = ByteArray(BUFFER)

                for (i in files.indices) {
                    print("Adding: " + files[i])
                    val fi = FileInputStream(files[i])
                    origin = BufferedInputStream(fi, BUFFER)
                    val entry = ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1))
                    out.putNextEntry(entry)
                    var count: Int

                    while (true) {
                        val count = origin.read(data, 0, BUFFER)
                        if(count == -1){
                            break
                        }
                        out.write(data, 0, count)
                    }

//                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
//                        out.write(data, 0, count)
//                    }
                    origin.close()
                }

                out.finish()
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}