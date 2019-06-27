package com.ustadmobile.port.sharedse.impl.http

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import java.io.File
import java.net.URI

/**
 * This is a simple static directory file responder.
 */
class StaticFileDirResponder: FileResponder(), RouterNanoHTTPD.UriResponder {

    fun serveFileRequest(method: NanoHTTPD.Method, uriResource: RouterNanoHTTPD.UriResource, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        var requestUri = RouterNanoHTTPD.normalizeUri(session.uri)
        requestUri = URI(requestUri).normalize().toString()
        val relativePath = requestUri.substring(
                uriResource.uri.length - MountedContainerResponder.URI_ROUTE_POSTFIX.length)
        val baseDir = uriResource.initParameter(0, File::class.java)
        return newResponseFromFile(method, uriResource, session, FileSource(File(baseDir, relativePath)))
    }

    override fun put(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?)
        = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain",
            "Method not supported")

    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response
        = serveFileRequest(NanoHTTPD.Method.GET, uriResource, urlParams, session)

    override fun other(method: String, uriResource: RouterNanoHTTPD.UriResource, urlParams: MutableMap<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
            if(method.equals("HEAD", ignoreCase = true)) {
                serveFileRequest(NanoHTTPD.Method.HEAD, uriResource, urlParams, session)
            }else{
                NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain",
                        "Method not supported")
            }

    override fun post(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?)
            = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain",
                "Method not supported")

    override fun delete(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?)
            = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain",
                "Method not supported")
}