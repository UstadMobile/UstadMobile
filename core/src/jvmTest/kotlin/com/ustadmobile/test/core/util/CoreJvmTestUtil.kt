package com.ustadmobile.test.core.util

import com.ustadmobile.core.util.UMIOUtils
import java.io.*
import java.net.URL
import java.net.URLClassLoader
import java.util.*

fun checkJndiSetup() {
    if(System.getProperty("java.naming.factory.initial") == null) {
        val jndiProps = Properties()
        jndiProps.load(FileReader("src/commonTest/resources/jndi.properties"))

        jndiProps.setProperty("org.osjava.sj.root",
                File(System.getProperty("user.dir"), "jndi-config").absolutePath)
        jndiProps.stringPropertyNames().forEach {
            System.setProperty(it, jndiProps.getProperty(it))
        }

        //setup the env from the file that we had gradle make
        val envProps = Properties()
        envProps.load(FileReader("build/local.env.properties"))
        envProps.stringPropertyNames().filter { System.getProperty(it) == null }.forEach {
            System.setProperty(it, envProps.getProperty(it))
        }

        val sqliteTmpDir = File(System.getProperty("user.dir"), "build/tmp")
        if(!sqliteTmpDir.isDirectory) {
            try {
                sqliteTmpDir.mkdirs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        val addMethod = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
        addMethod.isAccessible = true
        addMethod.invoke(ClassLoader.getSystemClassLoader(),
                File(System.getProperty("user.dir"), "src/commonTest/resources").toURI().toURL())
    }
}

fun extractTestResourceToFile(testResPath: String, destFile: File) {
    var inStream = null as InputStream?
    var outStream = null as OutputStream?
    try {
        try {
            inStream = CoreJvmTestUtil::class.java.getResourceAsStream(testResPath)
        }catch(ioe: IOException) {
            //that din't work...
        }

        if(inStream == null) {
            val resDir = File(System.getProperty("user.dir"), "src/commonTest/resources")
            inStream = FileInputStream(File(resDir, testResPath))
        }

        outStream = FileOutputStream(destFile)
        UMIOUtils.readFully(inStream, outStream)
    }catch(e: Exception) {
        throw IOException("Could not extract test resource: $testResPath", e)
    }finally {
        inStream?.close()
        outStream?.close()
    }
}

class CoreJvmTestUtil
