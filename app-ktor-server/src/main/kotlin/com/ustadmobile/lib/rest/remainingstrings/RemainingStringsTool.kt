package com.ustadmobile.lib.rest.remainingstrings

import com.ustadmobile.core.generated.locale.MessageIdMap
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.locale.getStringsXmlResource
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*

fun main(args: Array<String>) {
    if(args.size != 2) {
        println("Remaining strings tool")
        println("Usage: <langcode> <output directory>")
        System.exit(1)
        return
    }

    val remainingStringsTool = RemainingStringsTool()
    val remainingStrings = remainingStringsTool.findRemainingStrings(args[0])
    remainingStringsTool.serializeRemainingStrings(remainingStrings, File(args[1]))
    println("Saved ${remainingStrings.size} remaining strings for translation into ${args[0]} to ${args[1]}")
}


/**
 * This will save Android XML strings resource files that contain what remains to be translated
 * in any given language. It will copy in the English strings and comments as required for
 * submission to Google Play localization. This functionality is not provided by Weblate.
 */
class RemainingStringsTool {

    data class RemainingString(val name: String, val english: String, val comment: String?)

    fun findRemainingStrings(lang: String) : List<RemainingString> {
        val xppFactory = XmlPullParserFactory.newInstance()
        xppFactory.isNamespaceAware = true

        val messageIdMapFlipped = MessageIdMap.idMap.entries.associate { (k, v) -> v to k }

        val englishStrings = UstadMobileSystemImpl::class.java.getStringsXmlResource(
            "/values/strings_ui.xml", xppFactory, messageIdMapFlipped, null,
            true, true)
        val foreignStrings = UstadMobileSystemImpl::class.java.getStringsXmlResource(
            "/values-$lang/strings_ui.xml", xppFactory, messageIdMapFlipped)

        val remainingStringList = mutableListOf<RemainingString>()
        englishStrings.messageIdOrder.forEach { messageId ->
            try {
                foreignStrings.get(messageId)
            }catch(e: IllegalArgumentException) {
                val stringName = MessageIdMap[messageId]
                    ?: throw IllegalStateException("MessageID $messageId not found in English")
                val comment = englishStrings.getComment(messageId)
                remainingStringList += RemainingString(stringName, englishStrings[messageId], comment)
            }
        }

        return remainingStringList
    }

    fun serializeRemainingStrings(remainingStrings: List<RemainingString>, destDir: File) {
        val outputFile = File(destDir, "strings_ui.xml")
        if(!destDir.exists())
            destDir.mkdirs()

        BufferedWriter(FileWriter(outputFile)).use { bufOut ->
            bufOut.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
            bufOut.write("<resources>\n")
            remainingStrings.forEach { remainingString ->
                remainingString.comment?.also { comment ->
                    bufOut.write("<!-- $comment -->\n")
                }
                bufOut.write("<string name=\"${remainingString.name}\">${remainingString.english}</string>\n")
            }
            bufOut.write("</resources>\n")
        }
    }

}