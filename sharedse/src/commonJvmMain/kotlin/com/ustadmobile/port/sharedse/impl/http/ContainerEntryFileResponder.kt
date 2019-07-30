package com.ustadmobile.port.sharedse.impl.http

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_GZIP
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import java.io.File

class ContainerEntryFileResponder : FileResponder(), RouterNanoHTTPD.UriResponder {

    override fun get(uriResource: RouterNanoHTTPD.UriResource,
                     urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val db = uriResource.initParameter(INIT_PARAM_DB_INDEX, UmAppDatabase::class.java)

        val url = RouterNanoHTTPD.normalizeUri(session.uri)
        val lastSlashPos = url.lastIndexOf('/')
        if (lastSlashPos == -1) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "application/octet", null)
        }

        val entryFileUid = url.substring(lastSlashPos + 1)
        try {
            val entryFile = db.containerEntryFileDao.findByUid(
                    java.lang.Long.parseLong(entryFileUid))
            if (entryFile?.cefPath == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,
                        "application/octet", null)
            }

            val file = File(entryFile.cefPath!!)


            return newResponseFromFile(uriResource, session, FileSource(file))
        } catch (ne: NumberFormatException) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "application/octet", null)
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

        val INIT_PARAM_DB_INDEX = 0
    }
}
