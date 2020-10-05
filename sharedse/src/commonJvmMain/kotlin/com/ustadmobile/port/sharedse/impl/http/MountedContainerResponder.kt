package com.ustadmobile.port.sharedse.impl.http

import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_GZIP
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import java.util.regex.Pattern
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMURLEncoder
import java.io.ByteArrayInputStream
import com.ustadmobile.lib.util.parseAcceptedEncoding


class MountedContainerResponder : FileResponder(), RouterNanoHTTPD.UriResponder {

    interface MountedContainerFilter {

        fun filterResponse(responseIn: NanoHTTPD.Response,
                           uriResource: RouterNanoHTTPD.UriResource,
                           urlParams: Map<String, String>,
                           session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response

    }

    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        try {
            var requestUri = session.uri
            val containerUid = uriResource.initParameter(PARAM_CONTAINERUID_INDEX, String::class.java).toLong()
            val pathInContainer: String = requestUri.substring(
                    uriResource.uri.length - URI_ROUTE_POSTFIX.length).removePrefix("/")
            val appDb = uriResource.initParameter(PARAM_DB_INDEX, UmAppDatabase::class.java)
            val entryFile = appDb.containerEntryDao
                    .findByPathInContainer(containerUid, pathInContainer)
                    ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,
                            "text/plain", "Entry not found in container: $pathInContainer uriResource.uri=${uriResource.uri}\n" +
                            "requestUri=${requestUri}")

            val filterList = uriResource.initParameter(PARAM_FILTERS_INDEX, List::class.java)
                    as List<MountedContainerFilter>

            var responseFile = entryFile.containerEntryFile?.cefPath?.let { File(it) }
                    ?: return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,
                    "text/plain", "Entry found but does not have containerEntryFile/cefPath: $pathInContainer uriResource.uri=${uriResource.uri}\n" +
                    "requestUri=${requestUri}")

            //Look at the accept-encoding header to see if we can use gzip.
            val acceptsGzip = parseAcceptedEncoding(session.getHeaders().get("accept-encoding"))
                    .isEncodingAcceptable("gzip")

            val fileIsGzipped = entryFile.containerEntryFile?.compression == COMPRESSION_GZIP

            var fileSource = if(!fileIsGzipped || acceptsGzip) {
                FileSource(responseFile)
            }else {
                InflateFileSource(responseFile, entryFile.containerEntryFile?.ceTotalSize ?: throw IllegalStateException("no total size"))
            }

            var response = newResponseFromFile(uriResource, session, fileSource)

            if (acceptsGzip && fileIsGzipped)
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


        const val PARAM_CONTAINERUID_INDEX = 0

        const val PARAM_DB_INDEX = 1

        const val PARAM_FILTERS_INDEX = 2


        init {
            HTML_EXTENSIONS.add("xhtml")
            HTML_EXTENSIONS.add("html")
            HTML_EXTENSIONS.add("htm")
        }
    }
}
