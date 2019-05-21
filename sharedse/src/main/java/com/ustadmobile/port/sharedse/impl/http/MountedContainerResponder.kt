package com.ustadmobile.port.sharedse.impl.http

import com.google.gson.reflect.TypeToken
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.port.sharedse.container.ContainerManager

import org.xmlpull.v1.XmlPullParserException

import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.util.ArrayList
import java.util.regex.Pattern

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD

class MountedContainerResponder : FileResponder(), RouterNanoHTTPD.UriResponder {

    interface MountedContainerFilter {

        fun filterResponse(responseIn: NanoHTTPD.Response,
                           uriResource: RouterNanoHTTPD.UriResource,
                           urlParams: Map<String, String>,
                           session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response

    }

    @Deprecated("")
    class MountedZipFilter(var pattern: Pattern, var replacement: String)

    class FilteredHtmlSource(private val src: FileResponder.IFileSource, private val scriptPath: String) : FileResponder.IFileSource {

        private var inputStream: ByteArrayInputStream? = null

        private var length: Long = -1

        override val lastModifiedTime: Long
            get() = src.lastModifiedTime

        override val name: String
            get() = src.name

        override fun getLength(): Long {
            try {
                getInputStream()
            } catch (e: IOException) {
            }

            return length
        }

        @Throws(IOException::class)
        override fun getInputStream(): InputStream {
            if (inputStream == null) {
                //init and filter
                val srcIn = src.inputStream
                try {
                    val filterSerializer = EpubHtmlFilterSerializer()
                    filterSerializer.scriptSrcToAdd = scriptPath
                    filterSerializer.setIntput(srcIn)
                    val filteredBytes = filterSerializer.output
                    length = filteredBytes.size.toLong()
                    inputStream = ByteArrayInputStream(filteredBytes)
                } catch (x: XmlPullParserException) {
                    throw IOException(x)
                }

            }
            return inputStream
        }

        override fun exists(): Boolean {
            return src.exists()
        }
    }


    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        try {
            var requestUri = RouterNanoHTTPD.normalizeUri(session.uri)
            requestUri = URI(requestUri).normalize().toString()
            val pathInContainer = requestUri.substring(
                    uriResource.uri.length - URI_ROUTE_POSTFIX.length)
            val container = uriResource.initParameter(0, ContainerManager::class.java)
            val entry = container.getEntry(pathInContainer)
                    ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,
                            "text/plain", "Entry not found in container")

            val filterList = uriResource.initParameter(1, List<*>::class.java)
            var response = FileResponder.newResponseFromFile(uriResource, session,
                    FileResponder.FileSource(File(entry.containerEntryFile!!.cefPath!!)))
            for (filter in filterList) {
                response = filter.filterResponse(response, uriResource, urlParams, session)
            }

            return response
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "text/plain", "URISyntax error: $e")
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
