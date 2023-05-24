package com.ustadmobile.util.test

import java.io.*
import java.util.*

@Deprecated("Not needed anymore - bug it was designed to address is obsolete")
fun extractTestResourceToFile(testResPath: String, destFile: File,
                              resDirSearchPaths: List<String> = listOf("src/commonTest/resources", "src/jvmTest/resources")) {
    var inStream = null as InputStream?
    var outStream = null as OutputStream?
    try {
        try {
            inStream = CoreJvmTestUtil::class.java.getResourceAsStream(testResPath)
        }catch(ioe: IOException) {
            //that din't work...
        }

        if(inStream == null) {
            for(searchPath in resDirSearchPaths) {
                val resDir = File(System.getProperty("user.dir"), searchPath)
                val resFile = File(resDir, testResPath)
                if(resFile.exists()) {
                    inStream = FileInputStream(resFile)
                    break
                }
            }
        }

        if(inStream == null) {
            throw FileNotFoundException("Could not find resource $testResPath in ${resDirSearchPaths.joinToString()}")
        }

        outStream = FileOutputStream(destFile)
        outStream.write(inStream.readBytes())
    }catch(e: Exception) {
        throw IOException("Could not extract test resource: $testResPath", e)
    }finally {
        inStream?.close()
        outStream?.close()
    }
}

class CoreJvmTestUtil
