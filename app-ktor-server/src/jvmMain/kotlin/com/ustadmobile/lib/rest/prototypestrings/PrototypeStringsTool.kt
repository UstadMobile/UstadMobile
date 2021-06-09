package com.ustadmobile.lib.rest.prototypestrings

import com.ustadmobile.core.generated.locale.MessageIdMap
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.impl.locale.getStringsXmlResource
import com.ustadmobile.lib.util.ext.XmlSerializerFilter
import com.ustadmobile.lib.util.ext.serializeTo
import com.ustadmobile.xmlpullparserkmp.XmlPullParser
import com.ustadmobile.xmlpullparserkmp.XmlSerializer
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class PrototypeStringsTool {

    fun TarArchiveInputStream.extractToDir(dir: File) = use {
        lateinit var tarEntry: TarArchiveEntry
        while(this.nextTarEntry?.also { tarEntry = it } != null) {
            if(tarEntry.isDirectory)
                continue

            val destFile = File(dir, tarEntry.name)
            destFile.parentFile.takeIf { !it.exists() }?.mkdirs()

            FileOutputStream(destFile).use { fout ->
                copyTo(fout)
                fout.flush()
            }
        }
    }


    fun Element.isText() : Boolean {
        return when {
            tagName() == "text" && attr("p:name") == "text" -> true
            tagName() == "p:property" && attr("name") == "label" -> true
            else -> false
        }
    }

    fun substituteStrings(inFile: File, lang: String, outFile: File) {
        val tmpDir = File.createTempFile("stringsub", "tmp").also {
            it.delete()
            it.mkdir()
        }

        val xppFactory = XmlPullParserFactory.newInstance()
        xppFactory.isNamespaceAware = true

        val messageIdMapFlipped = MessageIdMap.idMap.entries.associate { (k, v) -> v to k }
        val englishStrings = UstadMobileSystemImpl::class.java.getStringsXmlResource(
            "/values/strings_ui.xml", xppFactory, messageIdMapFlipped)
        val foreignStrings = UstadMobileSystemImpl::class.java.getStringsXmlResource(
            "/values-$lang/strings_ui.xml", xppFactory, messageIdMapFlipped,
            englishStrings)

        TarArchiveInputStream(GZIPInputStream(FileInputStream(inFile))).extractToDir(tmpDir)


        val stringsNotFound = mutableMapOf<String, Set<String>>()
        tmpDir.listFiles().filter { it.name.endsWith(".xml") }.forEach { xmlFile ->
            val xpp = xppFactory.newPullParser()
            xpp.setInput(ByteArrayInputStream(xmlFile.readText().toByteArray()), "UTF-8")
            val xs = xppFactory.newSerializer()
            val fileOut = FileOutputStream(xmlFile)
            xs.setOutput(fileOut, "UTF-8")
            val filter = PrototypeStringsXmlSerializerFilter(englishStrings,
                foreignStrings)
            xpp.serializeTo(xs, filter = filter)
            fileOut.flush()
            val pageName = filter.pageName ?: "Untitled"
            stringsNotFound[pageName] = filter.stringsNotFound
        }

        TarArchiveOutputStream(GZIPOutputStream(FileOutputStream(outFile))).use { tarOut ->
            tmpDir.walkTopDown().filter { !it.isDirectory }.forEach { file ->
                val relName = file.toRelativeString(tmpDir)
                val tarEntry = tarOut.createArchiveEntry(file, relName)
                tarOut.putArchiveEntry(tarEntry)
                FileInputStream(file).use { fin ->
                    fin.copyTo(tarOut)
                    tarOut.flush()
                }
                tarOut.closeArchiveEntry()
            }
            tarOut.flush()
        }


        println("Extracted to: $tmpDir")

        tmpDir.deleteRecursively()
    }



}