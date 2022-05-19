package com.ustadmobile.lib.contentscrapers

import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.contentformats.epub.opf.OpfItem
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.io.ext.readString
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.CODEC2_PATH_KEY
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.FFMPEG_PATH_KEY
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.WEBP_PATH_KEY
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.fileHasContent
import com.ustadmobile.port.sharedse.util.UmZipUtils

import org.apache.commons.io.FileUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Entities
import org.jsoup.parser.Parser

import java.nio.file.InvalidPathException
import java.nio.file.Paths
import java.util.ArrayList
import java.util.HashMap
import java.util.regex.Pattern

import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_CSS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_JPG
import com.ustadmobile.lib.contentscrapers.ScraperConstants.PNG_EXT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*


object ShrinkerUtil {

    val HTML_MIME_TYPES = listOf("application/xhtml+xml", "text/html")
    val IMAGE_MIME_TYPES = listOf(MIMETYPE_JPG, "image/png", "image/jpeg")

    var cwebpPath = System.getProperty(WEBP_PATH_KEY)
    
    var ffmpegPath = System.getProperty(FFMPEG_PATH_KEY)
    
    const val STYLE_OUTSOURCE_TO_LINKED_CSS = 0

    const val STYLE_KEEP = 1

    const val STYLE_DROP = 2

    class EpubShrinkerOptions {

        var styleElementHelper: ((Element) -> Int)? = null

        var linkHelper: (() -> String)? = null

        var editor: ((Document) -> Document)? = null

    }

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            System.err.println("Usage:<file location><optional log{trace, debug, info, warn, error, fatal}>")
            System.exit(1)
        }
        UMLogUtil.setLevel(if (args.size == 2) args[1] else "")

        try {
            val epubFile = File(args[1])
            shrinkEpub(epubFile)

        } catch (e: Exception) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Failed to shrink epub " + args[1])
        }

    }


    @Throws(IOException::class)
    fun shrinkEpub(epub: File): File {
        val tmpFolder: File
        try {
            tmpFolder = createTmpFolderForZipAndUnZip(epub)
            shrinkEpubFiles(tmpFolder, null)
        } catch (e: IOException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            throw e
        }

        return tmpFolder
    }

    @Throws(IOException::class)
    fun shrinkEpub(epub: File, options: EpubShrinkerOptions): File {
        val tmpFolder: File
        try {
            tmpFolder = createTmpFolderForZipAndUnZip(epub)
            shrinkEpubFiles(tmpFolder, options)
        } catch (e: IOException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            throw e
        }

        return tmpFolder
    }


    @Throws(IOException::class)
    private fun createTmpFolderForZipAndUnZip(contentFile: File): File {
        val parentFolder = contentFile.parentFile
        val tmpFolder = File(parentFolder, UMFileUtil.stripExtensionIfPresent(contentFile.name))
        UmZipUtils.unzip(contentFile, tmpFolder)
        return tmpFolder
    }


    @Throws(IOException::class)
    fun cleanXml(xmlFile: File) {

        FileUtils.openInputStream(xmlFile).use { `is` ->
            val doc = Jsoup.parse(`is`, UTF_ENCODING, "", Parser.xmlParser())
            doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml)
            doc.outputSettings().prettyPrint(false)
            FileUtils.writeStringToFile(xmlFile, doc.toString(), UTF_ENCODING)
        }
    }


    @Throws(IOException::class)
    private fun shrinkEpubFiles(directory: File, options: EpubShrinkerOptions?): Boolean {
        var opfFileInputStream: FileInputStream? = null
        var ocfFileInputStream: FileInputStream? = null
        var opfFileOutputStream: FileOutputStream? = null
        val xppFactory = XmlPullParserFactory.newInstance()

        try {
            val ocfDoc = OcfDocument()
            val ocfFile = File(directory, Paths.get("META-INF", "container.xml").toString())
            ocfFileInputStream = FileInputStream(ocfFile)
            val ocfParser = xppFactory.newPullParser()
            ocfDoc.loadFromParser(ocfParser)

            val opfFile = File(directory, ocfDoc.getRootFiles()[0].fullPath!!)
            val opfDir = opfFile.parentFile

            cleanXml(opfFile)
            val document = OpfDocument()
            opfFileInputStream = FileInputStream(opfFile)
            val xmlPullParser = xppFactory.newPullParser()
            document.loadFromOPF(xmlPullParser)

            val manifestList = document.getManifestItems()
            val replacedFiles = HashMap<File, File>()
            val styleMap = HashMap<String, String>()
            val newOpfItems = ArrayList<OpfItem>()

            for (itemValue in manifestList.values) {

                if (IMAGE_MIME_TYPES.contains(itemValue.mediaType)) {
                    val oldHrefValue = itemValue.href
                    val newHref = UMFileUtil.stripExtensionIfPresent(oldHrefValue!!) + ScraperConstants.WEBP_EXT

                    val inputFile = File(opfDir, oldHrefValue)
                    val outputFile = File(opfDir, newHref)

                    try {
                        convertImageToWebp(inputFile, outputFile)
                    } catch (e: Exception) {
                        UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                        continue
                    }

                    replacedFiles[inputFile] = outputFile

                    if (fileHasContent(outputFile)) {
                        itemValue.href = newHref
                        itemValue.setMimeType(ScraperConstants.MIMETYPE_WEBP)
                    }
                }
            }

            var countStyle = 0
            for (opfItem in manifestList.values) {
                if (HTML_MIME_TYPES.contains(opfItem.mediaType)) {

                    val htmlFile = File(opfDir, opfItem.href!!)
                    FileInputStream(htmlFile).use { htmlFileInputStream ->
                        var html = htmlFileInputStream.readString()
                        /*
                         * Pratham uses an entity code to map &nbsp; to &#160; - this confuses jsoup
                         */
                        html = html.replace("&nbsp;".toRegex(), "&#160;")
                        html = html.replace("\\u2029".toRegex(), "")
                        html = html.replace("<!DOCTYPE html[<!ENTITY nbsp \"&#160;\">]>",
                                "<!DOCTYPE html>")

                        var doc = Jsoup.parse(html, "", Parser.xmlParser())
                        doc.outputSettings().prettyPrint(false)
                        doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml)

                        doc = options?.editor?.invoke(doc) ?: doc

                        if (replacedFiles.size != 0) {
                            val elements = doc.select("[src]")
                            for (element in elements) {
                                cleanUpAttributeListWithMultipleSrc(element, replacedFiles, htmlFile)
                            }
                        }
                        val styleList = doc.select("style[type=text/css]")
                        for (style in styleList) {
                            val styleAction = options?.styleElementHelper?.invoke(style)
                                    ?: STYLE_OUTSOURCE_TO_LINKED_CSS

                            if (styleAction == STYLE_DROP) {
                                continue
                            }

                            val cssText = style.text()
                            if (cssText != null && !cssText.isEmpty()) {
                                var pathToStyleFile: String? = styleMap[cssText]
                                if (pathToStyleFile == null) {
                                    val styleFile = File(htmlFile.parentFile, "style_" + ++countStyle + ".css")
                                    FileUtils.writeStringToFile(styleFile, cssText, UTF_ENCODING)
                                    pathToStyleFile = Paths.get(htmlFile.parentFile.toURI())
                                            .relativize(Paths.get(styleFile.toURI()))
                                            .toString().replace(Pattern.quote("\\").toRegex(), "/")

                                    val pathFromOpfToStyleFile = Paths.get(opfDir.toURI())
                                            .relativize(Paths.get(styleFile.toURI()))
                                            .toString().replace(Pattern.quote("\\").toRegex(), "/")

                                    val styleOpf = OpfItem()
                                    styleOpf.href = pathFromOpfToStyleFile
                                    styleOpf.mediaType = MIMETYPE_CSS
                                    styleOpf.id = "style_$countStyle"
                                    newOpfItems.add(styleOpf)

                                    styleMap[cssText] = pathToStyleFile
                                }
                                doc.head().append("<link rel=\"stylesheet\" type=\"text/css\" href=\"$pathToStyleFile\"/>")
                            }
                        }
                        val cssToAdd = options?.linkHelper?.invoke()

                        if (cssToAdd != null) {

                            val cssFile = File(htmlFile.parentFile, "cssHelper.css")
                            FileUtils.writeStringToFile(cssFile, cssToAdd, UTF_ENCODING)
                            val pathToCss = Paths.get(htmlFile.parentFile.toURI())
                                    .relativize(Paths.get(cssFile.toURI()))
                                    .toString().replace(Pattern.quote("\\").toRegex(), "/")

                            val pathFromOpfToCssFile = Paths.get(opfDir.toURI())
                                    .relativize(Paths.get(cssFile.toURI()))
                                    .toString().replace(Pattern.quote("\\").toRegex(), "/")

                            val styleOpf = OpfItem()
                            styleOpf.href = pathFromOpfToCssFile
                            styleOpf.mediaType = MIMETYPE_CSS
                            styleOpf.id = "cssHelper"

                            newOpfItems.add(styleOpf)

                            doc.head().append("<link rel=\"stylesheet\" type=\"text/css\" href=\"$pathToCss\"/>")
                            styleMap[cssToAdd] = pathFromOpfToCssFile
                        }

                        styleList.remove()
                        FileUtils.writeStringToFile(htmlFile, doc.toString(), UTF_ENCODING)
                    }

                }
            }

            for (item in newOpfItems) {
                manifestList[item.id!!] = item
            }

            if (newOpfItems.size == 0 && replacedFiles.size == 0 && styleMap.size == 0) {
                return false
            }

            val xmlSerializer = xppFactory.newSerializer()
            opfFileOutputStream = FileOutputStream(opfFile)
            xmlSerializer.setOutput(opfFileOutputStream, UTF_ENCODING)
            document.serialize(xmlSerializer)
            opfFileOutputStream.flush()

            for (replacedFile in replacedFiles.keys) {
                if (fileHasContent(replacedFile) && !replacedFile.delete()) {
                    throw IllegalStateException("Could not delete: $replacedFile")
                }
            }
            return true
        } catch (e: XmlPullParserException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Failed to xmlpullparse for directory " + directory.path)
        } catch (e: IOException) {
            UMLogUtil.logError("IO Exception for directory " + directory.path)
            throw e
        } finally {
            opfFileInputStream?.close()
            opfFileOutputStream?.close()
            ocfFileInputStream?.close()
        }

        return false
    }

    fun cleanUpAttributeListWithMultipleSrc(element: Element, replacedFiles: Map<File, File>, htmlFile: File) {
        val attrList = element.attributes().asList()
        var foundReplaced = false
        for ((key, srcValue) in attrList) {

            if (key.contains("src")) {

                try {
                    var srcFile = File(htmlFile.parentFile, srcValue)
                    srcFile = Paths.get(srcFile.path).normalize().toFile()

                    val newFile = replacedFiles[srcFile]
                    if (newFile != null) {
                        foundReplaced = true
                        val newHref = Paths.get(htmlFile.parentFile.toURI())
                                .relativize(Paths.get(newFile.toURI()))
                                .toString().replace(Pattern.quote("\\").toRegex(), "/")

                        deleteAllAttributesWithSrc(element)
                        element.attr("src", newHref)
                        break
                    }
                } catch (ignored: InvalidPathException) {

                }

            }
        }
        if (!foundReplaced) {
            UMLogUtil.logInfo("Did not find the replacement file for " + element.selectFirst("[src]")?.attr("src"))
        }
    }

    private fun deleteAllAttributesWithSrc(element: Element) {
        val attrList = element.attributes().asList()
        val attrToDelete = ArrayList<String>()
        for ((key) in attrList) {
            if (key.contains("src")) {
                attrToDelete.add(key)
            }
        }
        for (attr in attrToDelete) {
            element.removeAttr(attr)
        }

    }
    
   

    /**
     * Given a source file and a destination file, convert the image(src) to webp(dest)
     *
     * @param src  file image path
     * @param dest webp file path
     */
    @Throws(IOException::class)
    fun convertImageToWebp(src: File, dest: File) {
        if (!fileHasContent(src)) {
            throw FileNotFoundException("convertImageToWebp: Source file: " + src.absolutePath + " does not exist")
        }

        val webpExecutableFile = File(cwebpPath)
        if (!webpExecutableFile.exists()) {
            throw IOException("Webp executable does not exist: $cwebpPath")
        }
        var pngFile: File? = null
        var process: Process? = null
        val builder = ProcessBuilder(cwebpPath, src.path, "-o", dest.path)
        try {
            process = builder.start()
            process!!.waitFor()
            val exitValue = process.exitValue()
            if (exitValue != 0) {
                UMLogUtil.logError("Error Stream for src " + src.path + process.errorStream.bufferedReader().use { it.readText() })
                pngFile = File(UMFileUtil.stripExtensionIfPresent(src.path) + PNG_EXT)
                convertJpgToPng(src, pngFile)
                convertImageToWebp(pngFile, dest)
                pngFile.delete()
            }
        } catch (e: IOException) {
            throw e
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            pngFile?.delete()
            process?.destroy()
        }

        if (!fileHasContent(dest)) {
            throw IOException("convertImaegToWebP: source existed, but output does not " + dest.path)
        }

    }


    /**
     * Given a source file and a destination file, convert the jpg(src) to png(dest)
     *
     * @param src  file image path
     * @param dest webp file path
     */
    @Throws(IOException::class)
    private fun convertJpgToPng(src: File, dest: File) {
        if (!fileHasContent(src)) {
            throw FileNotFoundException("convertImageToWebp: Source file: " + src.absolutePath + " does not exist")
        }

        val webpExecutableFile = File(cwebpPath)
        if (!webpExecutableFile.exists()) {
            throw IOException("Webp executable does not exist: $cwebpPath")
        }

        var process: Process? = null
        val builder = ProcessBuilder("/usr/bin/mogrify", "-format", "png", src.path, dest.path)
        try {
            process = builder.start()
            process!!.waitFor()
            val exitValue = process.exitValue()
            if (exitValue != 0) {
                UMLogUtil.logError("Error Stream for src " + src.path + process.errorStream.readString())
                throw IOException()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            process?.destroy()
        }

        if (!fileHasContent(dest)) {
            throw IOException("convertJpegToPng: source existed, but output does not " + dest.path)
        }

    }


    @Throws(IOException::class)
    fun convertVideoToWebM(src: File, dest: File) {
        if (!fileHasContent(src)) {
            throw FileNotFoundException("convertVideoToWebm: Source file: " + src.absolutePath + " does not exist")
        }

        val webpExecutableFile = File(ffmpegPath)
        if (!webpExecutableFile.exists()) {
            throw IOException("ffmpeg executable does not exist: $ffmpegPath")
        }

        val builder = ProcessBuilder(ffmpegPath, "-i",
                src.path, "-vf", "scale=480x270", "-r", "20", "-c:v", "vp9", "-crf", "40", "-b:v", "0", "-c:a", "libopus", "-b:a", "12000", "-vbr", "on", dest.path)
        builder.redirectErrorStream(true)
        var process: Process? = null
        try {
            process = builder.start()
            process?.inputStream?.readBytes()
            process.waitFor()
            val exitValue = process.exitValue()
            if (exitValue != 0) {
                UMLogUtil.logError("Error Stream for src " + src.path + process.errorStream.readString())
                throw IOException()
            }
            process.destroy()
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            process?.destroy()
        }
        if (!fileHasContent(dest)) {
            throw IOException("convertVideoToWebm: source existed, but output does not " + dest.path)
        }

    }

    @Throws(IOException::class)
    fun convertAudioToOpos(src: File, dest: File) {
        if (!fileHasContent(src)) {
            throw FileNotFoundException("convertAudioToOpos: Source file: " + src.absolutePath + " does not exist")
        }

        val webpExecutableFile = File(ffmpegPath)
        if (!webpExecutableFile.exists()) {
            throw IOException("ffmpeg executable does not exist: $ffmpegPath")
        }

        val builder = ProcessBuilder(ffmpegPath, "-i", src.path, "-c:a", "libopus", "-b:a", "12000", "-vbr", "on", dest.path)
        builder.redirectErrorStream(true)
        var process: Process? = null
        try {
            process = builder.start()
            process?.inputStream?.readBytes()
            process?.waitFor()
            val exitValue = process.exitValue()
            if (exitValue != 0) {
                UMLogUtil.logError("Error Stream for src " + src.path + process.errorStream.readString())
            }
            process.destroy()
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            process?.destroy()
        }
        if (!fileHasContent(dest)) {
            throw IOException("convertAudioToOpos: source existed, but output does not " + dest.path)
        }

    }


    @Throws(IOException::class)
    fun convertKhanVideoToWebMAndCodec2(src: File, dest: File) {

        if (!fileHasContent(src)) {
            throw FileNotFoundException("convertKhanToWebmAndCodec2: Source file: " + src.absolutePath + " does not exist")
        }

        val webpExecutableFile = File(ffmpegPath)
        if (!webpExecutableFile.exists()) {
            throw IOException("ffmpeg executable does not exist: $ffmpegPath")
        }

        val videoBuilder = ProcessBuilder(ffmpegPath, "-i", src.path, "-vf", "scale=480x270", "-r", "5", "-c:v", "vp9", "-b:v", "0", "-crf", "40", "-an", "-y", dest.path)
        videoBuilder.redirectErrorStream(true)

        val rawFile = File(dest.parentFile, "audio.raw")
        val rawBuilder = ProcessBuilder(ffmpegPath, "-i", src.path, "-vn", "-c:a", "pcm_s16le", "-ar", "8000", "-ac", "1", "-f", "s16le", "-y", rawFile.path)
        rawBuilder.redirectErrorStream(true)

        val audioFile = File(dest.parentFile, "audio.c2")
        val audioBuilder = ProcessBuilder(System.getProperty(CODEC2_PATH_KEY), "3200", rawFile.path, audioFile.path)
        audioBuilder.redirectErrorStream(true)

        var process: Process? = null
        try {
            process = videoBuilder.start()
            startProcess(process!!)

            UMLogUtil.logTrace("got the webm file")

            process = rawBuilder.start()
            startProcess(process!!)

            UMLogUtil.logTrace("got the raw file")

            process = audioBuilder.start()
            startProcess(process!!)

            UMLogUtil.logTrace("got the c2 file")

        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            process?.destroy()
            ContentScraperUtil.deleteFile(rawFile)
        }

        if (!fileHasContent(dest)) {
            throw IOException("convertVideoToWebMAndCodec: source existed, but webm output does not " + dest.path)
        }

        if (!fileHasContent(audioFile)) {
            throw IOException("convertVideoToWebMAndCodec: source existed, but audio output does not " + dest.path)
        }


    }

    @Throws(IOException::class, InterruptedException::class)
    private fun startProcess(process: Process) {
        try {
            process.inputStream.readBytes()
            process.waitFor()
            val exitValue = process.exitValue()
            if (exitValue != 0) {
                UMLogUtil.logError("Error Stream for src " + process.errorStream.readString())
            }
        }finally {
            process.destroy()
        }
    }


}
