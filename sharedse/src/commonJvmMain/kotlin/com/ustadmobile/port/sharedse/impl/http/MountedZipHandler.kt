package com.ustadmobile.port.sharedse.impl.http


import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.URLTextUtil
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.FileHeader
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern

/**
 * A RouterNanoHTTPD UriResponder that when mounted serves files from the zip for content viewing
 * purposes. It will replace autoplay with data-autoplay so that the autoplay can be triggered
 * by javascript when the WebView is actually in view (rather than when it is loaded).
 *
 * Initialization parameters:
 * ZipFile object representing the zip to be mounted
 * Boolean epubHtmlFilterEnabled - true to enable epub filter for pagination, autoplay control - false otherwise
 * String epubScriptPath - the script src to add if
 *
 * Created by mike on 8/30/16.
 */
@Deprecated("")
class MountedZipHandler : FileResponder(), RouterNanoHTTPD.UriResponder {

    class MountedZipFilter(var pattern: Pattern, var replacement: String)

    class FilteredHtmlSource(private val src: FileResponder.IFileSource, private val scriptPath: String) : FileResponder.IFileSource {

        private var filteredLen = -1L

        override val length: Long
            get() {
                try {
                    inputStream
                }catch (e: IOException){

                }
                return filteredLen
            }

        override val inputStream: InputStream by lazy {
            //init and filter
            val srcIn = src.inputStream
            try {
                val filterSerializer = EpubHtmlFilterSerializer()
                filterSerializer.scriptSrcToAdd = scriptPath
                filterSerializer.setIntput(srcIn)
                val filteredBytes = filterSerializer.output
                filteredLen = filteredBytes.size.toLong()
                ByteArrayInputStream(filteredBytes)
            } catch (x: XmlPullParserException) {
                throw IOException(x)
            }
        }

        override val exists: Boolean
            get() = src.exists

        //private var length: Long = -1

        override val lastModifiedTime: Long
            get() = src.lastModifiedTime

        override val name: String?
            get() = src.name

        override val eTag: String?
            get() = null
    }

    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val requestUri = RouterNanoHTTPD.normalizeUri(session.uri)
        var pathInZip = requestUri.substring(
                uriResource.uri.length - URI_ROUTE_POSTFIX.length)
        val zipFile = uriResource.initParameter(0, ZipFile::class.java)

        //normalize the path
        pathInZip = pathInZip.replace("//", "/")

        if (session.uri.endsWith("/")) {
            return listDirectory(pathInZip, zipFile)
        }

        var src: FileResponder.IFileSource = FileResponder.ZipEntrySource(zipFile, pathInZip)
        val extension = UMFileUtil.getExtension(pathInZip)

        if (uriResource.initParameter(1, Boolean::class.java) && HTML_EXTENSIONS.contains(extension)) {
            src = FilteredHtmlSource(src, uriResource.initParameter(2, String::class.java))
        }

        return FileResponder.newResponseFromFile(uriResource, session, src)


    }

    fun listDirectory(dirInZip: String, zipfile: ZipFile): NanoHTTPD.Response {
        var dirInZip = dirInZip
        val xhtmlBuffer = StringBuffer()
        xhtmlBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\">")
                .append(" <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" ")
                .append(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> \n")
                .append("  <html xmlns=\"http://www.w3.org/1999/xhtml\"> \n")
                .append("<body>")

        if (!dirInZip.endsWith("/"))
            dirInZip += "/"


        val entries: MutableList<FileHeader>
        try {
            entries = zipfile.fileHeaders as MutableList<FileHeader>
        } catch (ze: ZipException) {
            ze.printStackTrace()
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR,
                    "text/plain", ze.toString())
        }

        val filesInDir = ArrayList<String>()
        val subdirs = ArrayList<String>()

        var pathAfterDir: String
        var lastSepPos: Int
        for (currentEntry in entries) {
            if (currentEntry.fileName.substring(0, dirInZip.length) == dirInZip) {
                pathAfterDir = currentEntry.getFileName().substring(dirInZip.length)

                lastSepPos = pathAfterDir.indexOf('/')
                if (lastSepPos == -1) {
                    //no further paths, this is a file
                    filesInDir.add(pathAfterDir)
                } else {
                    pathAfterDir = pathAfterDir.substring(0, lastSepPos)
                    if (!subdirs.contains(pathAfterDir))
                        subdirs.add(pathAfterDir)
                }

            }
        }

        xhtmlBuffer.append("<h2>Subdirectories</h2>\n<ul>")
        appendEntryLinksToBuffer(subdirs, xhtmlBuffer)
        xhtmlBuffer.append("</ul><h2>Files</h2><ul>")
        appendEntryLinksToBuffer(filesInDir, xhtmlBuffer)
        xhtmlBuffer.append("</ul></body></html>")

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                "application/xhtml+xml", xhtmlBuffer.toString())
    }

    private fun appendEntryLinksToBuffer(entries: List<String>, buffer: StringBuffer) {
        for (entry in entries) {
            buffer.append("<li><a href=\"").append(URLTextUtil.urlEncodeUTF8(entry))
                    .append("</a></li>\n")
        }
    }


    override fun put(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
        return null
    }

    override fun post(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
        return null
    }

    override fun delete(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
        return null
    }

    override fun other(method: String, uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
        return null
    }

    companion object {

        /**
         * The string that is added
         */
        val URI_ROUTE_POSTFIX = "(.)+"

        private val HTML_EXTENSIONS = ArrayList<String>()

        init {
            HTML_EXTENSIONS.add("xhtml")
            HTML_EXTENSIONS.add("html")
            HTML_EXTENSIONS.add("htm")
        }
    }


}
