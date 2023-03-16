package com.ustadmobile.lib.rest.stringstidy

import com.ustadmobile.lib.rest.resmodel.Resources
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

fun main(){

    // ---- Scarping strings_ui.xml file to get all names of strings
    val file = File("core/locale/main/values/strings_ui.xml")

    val xml = XML {
        autoPolymorphic = true
    }

    val resources = xml.decodeFromString<Resources>(file.readText())

    val stringsToBeFound: MutableList<String> = mutableListOf()

    resources.strings.forEach {
        stringsToBeFound.add(it.name)
    }

    // ---- Reading files to find existing strings
    fun findStrings(folder: String){
        File("$folder/src").walk().forEach {file ->

            if ((file.name.endsWith(".kt") || file.name.endsWith(".xml")) && !file.isDirectory){
                val fileContent = file.readText()

                val stringsFound = mutableListOf<String>()

                stringsToBeFound.forEach { resString ->
                    if (fileContent.contains("MessageID.$resString") || fileContent.contains("R.string.$resString") || fileContent.contains("@string/$resString")){
                        stringsFound.add(resString)
                    }
                }
                stringsToBeFound.removeAll(stringsFound)
            }
        }

        println("$folder scan end")
    }

    println("strings to be found length: ${stringsToBeFound.count()}")
    findStrings("app-react")
    findStrings("app-android")
    findStrings("app-android-launcher")
    findStrings("app-ktor-server")
    findStrings("core")
    findStrings("sharedse")
    println("strings to be found length: ${stringsToBeFound.count()}")
    println(stringsToBeFound)
}
