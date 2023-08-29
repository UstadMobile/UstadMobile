package com.ustadmobile.lib.rest.prototypestrings

import com.ustadmobile.core.generated.locale.MessageIdMap
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.locale.getStringsXmlResource
import com.ustadmobile.lib.util.ext.serializeTo
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

fun main(args: Array<String>) {
    if(args.size != 5) {
        println("Usage: inEpgzFile inCsvFile lang outEpgzFile outCsvFile")
        System.exit(1)
        return
    }

    PrototypeLocalizer().substituteStrings(
        inEpgzFile = File(args[0]),
        inCsvFile = File(args[1]),
        lang = args[2],
        outEpgzFile = File(args[3]),
        outCsvFile = File(args[4]))
}

/**
 * The PrototypeLocalizer will take the XML in a Pencil prototype file and replace English text
 * with a foreign translation using values from the Strings XML.
 *
 * The Pencil EPGZ file is a .tar.gz file containing multiple xml files.
 */
class PrototypeLocalizer {

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

    /**
     * Run the main string substitution on the XML.
     *
     * @param inEpgzFile the Pencil EPGZ file containing the prototype in English
     * @param inCsvFile (reserved for future use) a csv file containing non string xml substitutions
     *  (e.g. substitutions for names etc)
     * @param lang the language code of the foreign language to localize the prototype into
     * @param outEpgzFile File where the new localized Pencil EPGZ file will be written
     * @param outCsvFile File where a CSV of untranslated terms will be written
     */
    fun substituteStrings(inEpgzFile: File, inCsvFile: File, lang: String, outEpgzFile: File, outCsvFile: File) {
        val tmpDir = File.createTempFile("stringsub", "tmp").also {
            it.delete()
            it.mkdir()
        }

        val xppFactory = XmlPullParserFactory.newInstance()
        xppFactory.isNamespaceAware = true

        val messageIdMapFlipped = MessageIdMap.idMap.entries.associate { (k, v) -> v to k }
        val englishStrings = UstadMobileSystemImpl::class.java.getStringsXmlResource(
            "/values/strings.xml", xppFactory, messageIdMapFlipped)
        val foreignStrings = UstadMobileSystemImpl::class.java.getStringsXmlResource(
            "/values-$lang/strings.xml", xppFactory, messageIdMapFlipped,
            englishStrings)

        TarArchiveInputStream(GZIPInputStream(FileInputStream(inEpgzFile))).extractToDir(tmpDir)


        val stringsNotFoundMap = mutableMapOf<String, Set<String>>()
        tmpDir.listFiles().filter { it.name.endsWith(".xml") }.forEach { xmlFile ->
            val xpp = xppFactory.newPullParser()
            xpp.setInput(ByteArrayInputStream(xmlFile.readText().toByteArray()), "UTF-8")
            val xs = xppFactory.newSerializer()
            val fileOut = FileOutputStream(xmlFile)
            xs.setOutput(fileOut, "UTF-8")
            val filter = PrototypeLocalizerXmlFilter(englishStrings,
                foreignStrings)
            xpp.serializeTo(xs, filter = filter)
            fileOut.flush()
            val pageName = filter.pageName ?: "Untitled"
            stringsNotFoundMap[pageName] = filter.stringsNotFound
        }

        TarArchiveOutputStream(GZIPOutputStream(FileOutputStream(outEpgzFile))).use { tarOut ->
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


        //Create a map of the strings that are not found to a list of the page names in which they
        // are used
        val missingMap: Map<String, List<String>> = stringsNotFoundMap.flatMap { it.value }.toSet().map { missingString ->
            missingString to stringsNotFoundMap.entries.filter { missingString in it.value }.map { it.key }
        }.toMap()

        var csvText = """"String","Substitute","Pages","Xml" ${'\n'}"""
        val notWordRegex = Regex("\\W")
        missingMap.forEach { string, pageList ->
            val spaceRegex = Regex("\\s+")

            //Csv requires us to escape a single quote to being double quote
            val stringCsvEscaped = string.replace("\"", "\"\"")
            val stringName = string.lowercase().replace(" ", "_")
                .replace(notWordRegex, "")

            val xml = """<string name=""$stringName"">$stringCsvEscaped</string>"""

            csvText += """"$stringCsvEscaped","","${pageList.joinToString()}","$xml"${'\n'}"""
        }

        outCsvFile.writeText(csvText)

        tmpDir.deleteRecursively()
    }



}