package com.ustadmobile.port.sharedse.impl.http

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import org.kodein.di.DI
import com.ustadmobile.core.account.Endpoint
import org.kodein.di.instance
import org.kodein.di.on
import com.google.gson.Gson
import com.ustadmobile.door.ext.DoorTag

/**
 * NanoHTTPD router that will provide a list of all ContainEntry objects (and their MD5 sum) so that
 * the DownloadJobItemRunner can decide which of those it needs to download, and which of those it
 * already has.
 */
class ContainerEntryListResponder : RouterNanoHTTPD.UriResponder {

    private fun newBadRequestResponse(errorMessage: String): NanoHTTPD.Response =
            NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", errorMessage)

    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val di = uriResource.initParameter(PARAM_DI_INDEX, DI::class.java)
        val endpoint = urlParams[PATH_VAR_ENDPOINT] ?: return newBadRequestResponse("No endpoint in urlParams")
        val appDatabase: UmAppDatabase by di.on(Endpoint(endpoint)).instance(tag = DoorTag.TAG_DB)
        val containerUid = session.parameters.get(PARAM_CONTAINER_UID)?.firstOrNull()?.toLong()
                ?: return newBadRequestResponse("No containerUid param")
        val entryList = appDatabase.containerEntryDao.findByContainerWithMd5(containerUid)
        val gson: Gson by di.instance()

        val status = if (entryList.isEmpty())
            NanoHTTPD.Response.Status.NOT_FOUND
        else
            NanoHTTPD.Response.Status.OK

        return NanoHTTPD.newFixedLengthResponse(status, "application/json", gson.toJson(entryList))
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

        const val PARAM_DI_INDEX = 0

        const val PATH_VAR_ENDPOINT = "endpoint"

        const val PARAM_CONTAINER_UID = "containerUid"
    }
}
