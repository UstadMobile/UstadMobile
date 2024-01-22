package com.ustadmobile.util.test.ext

import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream

/**
 * Convenience function to create a temporary file and copy the content from a resource.
 */
fun TemporaryFolder.newFileFromResource(
    clazz: Class<*>,
    resourcePath: String,
    fileName: String? = null,
): File {
    val file = if(fileName != null) newFile(fileName) else newFile()
    clazz.getResourceAsStream(resourcePath)!!.use { resourceIn ->
        FileOutputStream(file).use { fileOut ->
            resourceIn.copyTo(fileOut)
            fileOut.flush()
        }
    }
    return file
}