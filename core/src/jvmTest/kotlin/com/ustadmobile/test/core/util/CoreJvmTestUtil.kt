package com.ustadmobile.test.core.util

import java.io.File
import java.io.FileReader
import java.util.*

fun checkJndiSetup() {
    if(System.getProperty("java.naming.factory.initial") == null) {
        val jndiProps = Properties()
        jndiProps.load(FileReader("src/commonTest/resources/jndi.properties"))

        jndiProps.setProperty("org.osjava.sj.root",
                File(System.getProperty("user.dir"), "jndi-config").absolutePath)
        System.setProperties(jndiProps)

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

    }
}