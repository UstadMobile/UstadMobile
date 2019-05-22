package com.ustadmobile.port.sharedse.impl.http

import com.google.gson.Gson
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD

/**
 * NanoHTTPD router that will provide a list of all ContainEntry objects (and their MD5 sum) so that
 * the DownloadJobItemRunner can decide which of those it needs to download, and which of those it
 * already has.
 */
class ContainerEntryListResponder : RouterNanoHTTPD.UriResponder {

    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val appDatabase = uriResource.initParameter(PARAM_APPDB_INDEX, UmAppDatabase::class.java)

        try {
            val containerUid = if (session.parameters.containsKey(PARAM_CONTAINER_UID) && session.parameters[PARAM_CONTAINER_UID]!!.isNotEmpty())
                session.parameters[PARAM_CONTAINER_UID]!![0].toLong()
            else
                null
            if (containerUid != null) {
                val entryList = appDatabase.containerEntryDao
                        .findByContainerWithMd5(containerUid)
                val status = if (entryList.isEmpty())
                    NanoHTTPD.Response.Status.NOT_FOUND
                else
                    NanoHTTPD.Response.Status.OK
                return NanoHTTPD.newFixedLengthResponse(status, "application/json",
                        Gson().toJson(entryList))
            }
        } catch (e: NumberFormatException) {
            UMLog.l(UMLog.WARN, 700,
                    "ContainerEntryListResponder received bad uid")
        }


        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                "application/json", null)
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

        val PARAM_APPDB_INDEX = 0

        val PARAM_CONTAINER_UID = "containerUid"
    }
}
