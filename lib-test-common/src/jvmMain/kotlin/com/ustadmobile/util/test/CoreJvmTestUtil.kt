package com.ustadmobile.util.test

import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import java.io.*
import java.util.*
import javax.naming.InitialContext

actual fun checkJndiSetup() {
    if(System.getProperty("java.naming.factory.initial") == null) {
        val jndiProps = Properties()
        jndiProps.load(FileReader("src/commonTest/resources/jndi.properties"))

        jndiProps.setProperty("org.osjava.sj.root",
                File(System.getProperty("user.dir"), "jndi-config").absolutePath)

        //setup the env from the file that we had gradle make
        val envProps = Properties()
        envProps.load(FileReader("build/local.env.properties"))
        envProps.stringPropertyNames().filter { System.getProperty(it) == null }.forEach {
            System.setProperty(it, envProps.getProperty(it))
        }

        jndiProps.stringPropertyNames().forEach {
            System.setProperty(it, jndiProps.getProperty(it))
        }

        val sqliteTmpDir = File(System.getProperty("user.dir"), "build/tmp")
        if(!sqliteTmpDir.isDirectory) {
            try {
                sqliteTmpDir.mkdirs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

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
