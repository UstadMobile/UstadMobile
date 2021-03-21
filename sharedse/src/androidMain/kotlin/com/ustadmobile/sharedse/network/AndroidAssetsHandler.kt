package com.ustadmobile.sharedse.network


import android.content.Context
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Created by mike on 8/30/16.
 */
class AndroidAssetsHandler : RouterNanoHTTPD.UriResponder {

    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
        val context = uriResource.initParameter(Context::class.java)
        val assetPath = RouterNanoHTTPD.normalizeUri(session.uri).substring(uriResource.uri.length - (URI_ROUTE_POSTFIX.length - 1))
        var assetIn: InputStream? = null
        var response: NanoHTTPD.Response? = null
        try {
            val bout = ByteArrayOutputStream()
            assetIn = context.assets.open(UMFileUtil.joinPaths("http", assetPath))

            assetIn.copyTo(bout)
            val assetBytes = bout.toByteArray()
            val extension = UMFileUtil.getExtension(assetPath)

            response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                    UstadMobileSystemImpl.instance.getMimeTypeFromExtension(extension!!),
                    ByteArrayInputStream(assetBytes), assetBytes.size.toLong())
            response!!.addHeader("Cache-Control", "cache, max-age=86400")
            response.addHeader("Content-Length", assetBytes.size.toString())
        } catch (e: IOException) {
            UMLog.l(UMLog.ERROR, 88, session.uri, e)
        } finally {
            try {
                assetIn?.close()

            } catch (e: IOException) {
                UMLog.l(UMLog.ERROR, 89, session.uri, e)
            }

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

    override fun other(method: String, uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
        return null
    }

    companion object {

        val URI_ROUTE_POSTFIX = "/(.)+"
    }
}
