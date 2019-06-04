package com.ustadmobile.port.sharedse.impl.http

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*

/**
 * Implemented for purposes of running unit tests. This HTTP server will serve resources from
 * the classpath using getClass.getResource
 */

class ClassResourcesResponder : FileResponder(), RouterNanoHTTPD.UriResponder {


    class ResourceFileSource(resourcePath: URL?, lastModifiedTime: Long) : IFileSource {

        private val resourceFilelength: Int = 0

        override var lastModifiedTime: Long = 0
            private set

        private var contentBuf: ByteArray? = null

        override var name: String? = null
            private set

        override val inputStream: InputStream
            @Throws(IOException::class)
            get() = ByteArrayInputStream(contentBuf!!)

        override val length: Long
            get() {
                return if (contentBuf != null)
                    contentBuf!!.size.toLong()
                else
                    -1
            }

        init {
            var resIn: InputStream? = null

            if (resourcePath != null) {
                name = resourcePath.file
                val bout = ByteArrayOutputStream()
                try {
                    resIn = resourcePath.openStream()
                    val buf = ByteArray(1024)
                    var bytesRead: Int
                    while(resIn!!.read(buf).also { bytesRead = it } != -1 ){
                        bout.write(buf, 0, bytesRead)
                    }
                } catch (e: IOException) {

                } finally {
                    if (resIn != null) {
                        try {
                            resIn.close()
                        } catch (e: IOException) {
                        }

                    }
                }

                this.contentBuf = bout.toByteArray()
                this.lastModifiedTime = lastModifiedTime
            }
        }

        override var exists: Boolean = contentBuf != null
    }

    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val prefix = uriResource.initParameter(0, String::class.java)
        val resPath = '/' + session.uri.substring(prefix.length)

        val cutOffAfter = if (session.parameters.containsKey("cutoffafter"))
            session.parameters["cutoffafter"]!![0].toLong()
        else
            0L
        val speedLimit = if (session.parameters.containsKey("speedLimit"))
            session.parameters["speedLimit"]!![0].toInt()
        else
            0

        val resourceUrl = javaClass.getResource(resPath)
        var lastModTime: Long? = LAST_MODIFIED_TIMES[resPath]
        if (lastModTime == null) {
            lastModTime = System.currentTimeMillis()
            LAST_MODIFIED_TIMES[resPath] = lastModTime
        }

        val fileSource = ResourceFileSource(resourceUrl, lastModTime)

        val response = newResponseFromFile(NanoHTTPD.Method.GET, uriResource, session, fileSource, null)


        if (cutOffAfter > 0 || speedLimit > 0) {
            val din = DodgyInputStream(response.data, speedLimit, cutOffAfter.toInt())
            response.data = din
        }

        if (session.parameters.containsKey("private")) {
            response.addHeader("Cache-Control", "private")
        }


        return response
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

    override fun other(method: String, uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (NanoHTTPD.Method.HEAD.toString() == method) {
            val resPath = getResourcePathFromRequest(uriResource, session)
            var lastModTime: Long? = LAST_MODIFIED_TIMES[resPath]
            if (lastModTime == null) {
                lastModTime = System.currentTimeMillis()
                LAST_MODIFIED_TIMES[resPath] = lastModTime
            }

            val fileSource = ResourceFileSource(javaClass.getResource(resPath),
                    lastModTime)

            return newResponseFromFile(NanoHTTPD.Method.HEAD, uriResource, session, fileSource, null)
        }

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED,
                "text/plain", "Method not supoprted")
    }

    private fun getResourcePathFromRequest(uriResource: RouterNanoHTTPD.UriResource, session: NanoHTTPD.IHTTPSession): String {
        val prefix = uriResource.initParameter(0, String::class.java)
        return '/' + session.uri.substring(prefix.length)
    }

    companion object {


        val LAST_MODIFIED_TIMES = Hashtable<String, Long>()
    }
}
