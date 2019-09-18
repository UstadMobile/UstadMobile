package com.ustadmobile.port.sharedse.impl.http

import com.ustadmobile.core.util.UMFileUtil
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.FileHeader
import java.io.*
import com.ustadmobile.lib.util.RangeResponse
import com.ustadmobile.lib.util.parseRangeRequestHeader

/**
 * This is a RouterNanoHTTPD Responder that can be used to serve files from the file system or
 * files from within a zip. It can handle etags, validating if-not-modified requests, and partial
 * requests. Will return a 404 response if the file does not exist. This has been tested to
 * successfully serve streaming video and audio over http to a WebView.
 *
 * To serve from a file:
 *
 * return FileResponder.newResponseFromFile(uriResource, session, new FileResponder.FileSource(file))
 *
 * To serve an entry from a zip file:
 *
 * return FileResponder.newResponseFromFile(uriResource, session, new FileResponder.ZipEntrySource(zipEntry, zipFile))
 *
 * Created by mike on 2/22/17.
 */
abstract class FileResponder {


    /**
     * Interface used to describe a file or file like source to serve an HTTP request: in our case
     * this can be a File or a ZipEntry
     */
    interface IFileSource {

        /**
         * The total length of the response: use for content-length header
         * @return the total length of the response in bytes for the content-length header
         */
        val length: Long

        /**
         * The last modified time in ms since the epoch
         *
         * @return The last modified time in ms since the epoch
         */
        val lastModifiedTime: Long

        /**
         * Get the input stream for the data
         *
         * @return InputStream for the data
         * @throws IOException
         */
        val inputStream: InputStream

        /**
         * Provides the base name of the file: only currently used for etag generation purposes
         *
         * @return The base name of the file
         */
        val name: String?

        /**
         * Determine if the file or zip entry exists
         *
         * @return True if file exists, false otherwise
         */
        val exists: Boolean

    }

    class FileSource(private val src: File) : IFileSource {

        override val length: Long
            get() = src.length()

        override val lastModifiedTime: Long
            get() = src.lastModified()

        override val inputStream: InputStream
            @Throws(IOException::class)
            get() = BufferedInputStream(FileInputStream(src))

        override val name: String
            get() = src.name

        override val exists: Boolean = src.exists()
    }

    class ZipEntrySource : IFileSource {

        private var entry: FileHeader? = null

        private var zipFile: ZipFile? = null


        override val length: Long
            get() = entry!!.uncompressedSize

        override val lastModifiedTime: Long
            get() = entry!!.lastModFileTime.toLong()

        override val inputStream: InputStream
            @Throws(IOException::class)
            get() {
                try {
                    return zipFile!!.getInputStream(entry!!)
                } catch (ze: ZipException) {
                    throw IOException(ze)
                }

            }

        override val name: String
            get() = entry!!.fileName

        override val exists: Boolean
            get() =  entry != null//must exist if there is an entry here

        /**
         *
         * @param entry
         * @param zipFile
         */
        constructor(entry: FileHeader, zipFile: ZipFile) {
            this.entry = entry
            this.zipFile = zipFile
        }

        constructor(zipFile: ZipFile, pathInZip: String) {
            this.zipFile = zipFile
            try {
                this.entry = zipFile.getFileHeader(pathInZip)
            } catch (e: ZipException) {

            }

        }

    }

    companion object {


        /**
         * Create a NanoHTTPD response from a file or file like object (e.g. zip entry). This will handle
         * validating etags (and return 302 not-modified if a if-not-modified header was sent). It will
         * also take care of responding to partial range requests. It will respond 404 if the file does
         * not exist.
         *
         * @param method The HTTP method being used : We support GET (default) and HEAD (for headers only with no response body)
         * @param uriResource uriResource from the request
         * @param session session from the request
         * @param file Interface representing the file or file like source
         * @param cacheControlHeader The cache control header to put on the response. Optional: can be null for no cache-control header
         * @return An appropriate NanoHTTPD.Response as above for the request
         */
        @JvmOverloads
        fun newResponseFromFile(method: NanoHTTPD.Method, uriResource: RouterNanoHTTPD.UriResource, session: NanoHTTPD.IHTTPSession, file: IFileSource, cacheControlHeader: String? = "cache, max-age=86400"): NanoHTTPD.Response {
            val isHeadRequest = method == NanoHTTPD.Method.HEAD
            try {
                val range: RangeResponse?
                val ifNoneMatchHeader: String?
                var retInputStream: InputStream?

                if (!file.exists) {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain",
                            if (isHeadRequest) null else "File not found")
                }

                val totalLength = file.length
                val lastModifiedTime = file.lastModifiedTime
                val fileName = file.name
                val etagNameInput = fileName
                val mimeType = EmbeddedHTTPD.getMimeType(session.uri)


                //Check to see if the etag provided by the client matches: in which case we can send 302 not modified
                val etag = Integer.toHexString((file.name + lastModifiedTime + "" +
                        totalLength).hashCode())
                val extension = UMFileUtil.getExtension(fileName!!)
                ifNoneMatchHeader = session.headers["if-none-match"]
                if (ifNoneMatchHeader != null && ifNoneMatchHeader == etag) {
                    val r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_MODIFIED,
                            EmbeddedHTTPD.getMimeType(fileName), null)
                    r.addHeader("ETag", etag)
                    return r
                }

                val rangeHeader = session.headers["range"] as String?
                range = if(rangeHeader != null) {
                    parseRangeRequestHeader(rangeHeader, totalLength)
                }else {
                    null
                }

                retInputStream = if (isHeadRequest) null else file.inputStream
                if (range != null && range.statusCode == 206) {
                    retInputStream = if (isHeadRequest) null else RangeInputStream(retInputStream!!,
                            range.fromByte, range.toByte)
                    //val contentLength = range[1] + 1 - range[0]
                    val r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.PARTIAL_CONTENT,
                            mimeType, retInputStream, range.actualContentLength)

                    r.addHeader("ETag", etag)
                    range.responseHeaders.forEach { r.addHeader(it.key, it.value) }
                    r.addHeader("Connection", "close")
                    return r
                } else if(range?.statusCode == 416) {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.RANGE_NOT_SATISFIABLE, "text/plain",
                            if (isHeadRequest) null else "Range request not satisfiable")
                } else {
                    //Workaround : NanoHTTPD is using the InputStream.available method incorrectly
                    // see RangeInputStream.available
                    retInputStream = if (isHeadRequest) null else retInputStream
                    val r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                            mimeType, retInputStream, totalLength)

                    r.addHeader("ETag", etag)
                    r.addHeader("Content-Length", totalLength.toString())
                    r.addHeader("Connection", "close")
                    if (cacheControlHeader != null)
                        r.addHeader("Cache-Control", cacheControlHeader)
                    return r
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR,
                        "text/plain", if (isHeadRequest) null else "Internal exception: $e")
            }

        }

        fun newResponseFromFile(uriResource: RouterNanoHTTPD.UriResource, session: NanoHTTPD.IHTTPSession, file: IFileSource): NanoHTTPD.Response {
            return newResponseFromFile(NanoHTTPD.Method.GET, uriResource, session, file)
        }

    }


}
