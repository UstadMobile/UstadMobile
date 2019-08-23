package com.ustadmobile.port.sharedse.impl.http

import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_GZIP
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import java.util.regex.Pattern
import com.ustadmobile.core.db.UmAppDatabase

class MountedContainerResponder : FileResponder(), RouterNanoHTTPD.UriResponder {

    interface MountedContainerFilter {

        fun filterResponse(responseIn: NanoHTTPD.Response,
                           uriResource: RouterNanoHTTPD.UriResource,
                           urlParams: Map<String, String>,
                           session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response

    }

    @Deprecated("")
    class MountedZipFilter(var pattern: Pattern, var replacement: String)

    class FilteredHtmlSource(private val src: IFileSource, private val scriptPath: String) : IFileSource {

        private var sourceLength: Long = -1

        override val lastModifiedTime: Long
            get() = src.lastModifiedTime

        override val name: String?
            get() = src.name

        override val inputStream: InputStream by lazy {
            //init and filter
            val srcIn = src.inputStream
            try {
                val filterSerializer = EpubHtmlFilterSerializer()
                filterSerializer.scriptSrcToAdd = scriptPath
                filterSerializer.setIntput(srcIn)
                val filteredBytes = filterSerializer.output
                sourceLength = filteredBytes.size.toLong()
                ByteArrayInputStream(filteredBytes)
            } catch (x: XmlPullParserException) {
                throw IOException(x)
            }
        }

        override val length: Long
            get() {
                try {
                    inputStream
                } catch (e: IOException) {
                }

                return sourceLength
            }


        override val exists: Boolean
            get() = src.exists

    }


    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        try {

            var requestUri = RouterNanoHTTPD.normalizeUri(session.uri)
            requestUri = URI(requestUri).normalize().toString()
            val pathInContainer = requestUri.substring(
                    uriResource.uri.length - URI_ROUTE_POSTFIX.length)
            val containerUid = uriResource.uri.split("/")[CONTAINER_UID_INDEX].toLong()
            val context = uriResource.initParameter(0, Any::class.java)
            val entryFile = UmAppDatabase.getInstance(context).containerEntryDao
                    .findByPathInContainer(containerUid, pathInContainer)
                    ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,
                            "text/plain", "Entry not found in container")

            val filterList = uriResource.initParameter(1, List::class.java)
                    as List<MountedContainerFilter>
            var response = newResponseFromFile(uriResource, session,
                    FileSource(File(entryFile.containerEntryFile!!.cefPath!!)))

            if (entryFile.containerEntryFile!!.compression == COMPRESSION_GZIP)
                response.addHeader("Content-Encoding", "gzip")

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
        const val URI_ROUTE_POSTFIX = "(.)+"

        const val CONTAINER_UID_INDEX = 1

        private val HTML_EXTENSIONS = ArrayList<String>()

        init {
            HTML_EXTENSIONS.add("xhtml")
            HTML_EXTENSIONS.add("html")
            HTML_EXTENSIONS.add("htm")
        }
    }
}
