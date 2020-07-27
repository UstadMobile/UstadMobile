package com.ustadmobile.port.sharedse.impl.http

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.contentformats.xapi.Actor
import com.ustadmobile.core.contentformats.xapi.State
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStateEndpointImpl
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.util.*

class XapiStateResponder : RouterNanoHTTPD.UriResponder {
    internal var contentMapToken = object : TypeToken<HashMap<String, Any>>() {

    }.type


    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val repo = uriResource.initParameter(PARAM_APPREPO_INDEX, UmAppDatabase::class.java)
        try {

            val map = HashMap<String, String>()
            session.parseBody(map)
            val queryParams = session.parameters
            val activityId = queryParams["activityId"]!![0]
            val agentJson = queryParams["agent"]!![0]
            val stateId = if (queryParams.containsKey("stateId"))
                queryParams["stateId"]!![0]
            else
                ""
            val registration = if (queryParams.containsKey("registration"))
                queryParams["registration"]!![0]
            else
                ""
            val since = if (queryParams.containsKey("since"))
                queryParams["since"]!![0]
            else
                ""

            val gson = GsonBuilder().disableHtmlEscaping().create()
            val endpoint = XapiStateEndpointImpl(repo, gson, null)
            val json = endpoint.getContent(stateId, agentJson, activityId, registration, since)
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                    "application/octet", json)


        } catch (e: IOException) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "application/octet", e.message)
        } catch (e: NanoHTTPD.ResponseException) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/octet", e.message)
        }

    }

    override fun put(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val repo = uriResource.initParameter(PARAM_APPREPO_INDEX, UmAppDatabase::class.java)

        val content: ByteArray
        var fin: FileInputStream? = null
        var bout: ByteArrayOutputStream? = null
        val tmpFileName: String
        try {

            val map = HashMap<String, String>()
            session.parseBody(map)
            if (map.containsKey("content")) {
                tmpFileName = map["content"]!!
                fin = FileInputStream(tmpFileName)
                bout = ByteArrayOutputStream()
                UMIOUtils.readFully(fin, bout)
                bout.flush()
                content = bout.toByteArray()
            } else {
                content = session.queryParameterString.toByteArray()
            }
            val queryParams = session.parameters
            val activityId = queryParams["activityId"]?.get(0)
            val agentJson = queryParams["agent"]?.get(0)
            val stateId = queryParams["stateId"]?.get(0)
            val registration = if (queryParams.containsKey("registration"))
                queryParams["registration"]!![0]
            else
                ""

            val contentType = session.headers["content-type"]

            val gson = Gson()
            val agent = gson.fromJson(agentJson, Actor::class.java)
            val contentJson = String(content)
            val contentMap: HashMap<String, Any>
            contentMap = gson.fromJson(contentJson, contentMapToken)

            val state = State(stateId, agent, activityId, contentMap, registration)
            val endpoint = XapiStateEndpointImpl(repo, gson, contentType)
            endpoint.overrideState(state)

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NO_CONTENT,
                    "application/octet", null)

        } catch (e: IOException) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "application/octet", e.message)
        } catch (e: NanoHTTPD.ResponseException) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/octet", e.message)
        } catch (e: NullPointerException) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/octet", e.message)
        } finally {
            UMIOUtils.closeInputStream(fin)
            UMIOUtils.closeOutputStream(bout)

        }

    }

    override fun post(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val repo = uriResource.initParameter(PARAM_APPREPO_INDEX, UmAppDatabase::class.java)

        try {

            val map = HashMap<String, String>()
            session.parseBody(map)
            val queryParams = session.parameters
            val activityId = queryParams["activityId"]?.get(0)
            val agentJson = queryParams["agent"]?.get(0)
            val stateId = queryParams["stateId"]?.get(0)
            val registration = if (queryParams.containsKey("registration"))
                queryParams["registration"]!![0]
            else
                ""

            val gson = Gson()
            val agent = gson.fromJson(agentJson, Actor::class.java)
            val contentMap: HashMap<String, Any>
            contentMap = gson.fromJson(map["postData"], contentMapToken)

            val contentType = session.headers["content-type"]

            val state = State(stateId, agent, activityId, contentMap, registration)
            val endpoint = XapiStateEndpointImpl(repo, gson, contentType)
            endpoint.storeState(state)

            return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.NO_CONTENT,
                    "application/octet", null)

        } catch (e: IOException) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "application/octet", e.message)
        } catch (e: NanoHTTPD.ResponseException) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/octet", e.message)
        } catch (e: NullPointerException) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/octet", e.message)
        }

    }

    override fun delete(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val repo = uriResource.initParameter(PARAM_APPREPO_INDEX, UmAppDatabase::class.java)
        try {

            val map = HashMap<String, String>()
            session.parseBody(map)
            val queryParams = session.parameters
            val activityId = queryParams["activityId"]!![0]
            val agentJson = queryParams["agent"]!![0]
            val stateId = if (queryParams.containsKey("stateId"))
                queryParams["stateId"]!![0]
            else
                ""
            val registration = if (queryParams.containsKey("registration"))
                queryParams["registration"]!![0]
            else
                ""

            val gson = Gson()
            val endpoint = XapiStateEndpointImpl(repo, gson, null)
            if (stateId == null || stateId.isEmpty()) {
                endpoint.deleteListOfStates(agentJson, activityId, registration)
            } else {
                endpoint.deleteStateContent(stateId, agentJson, activityId, registration)

            }
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NO_CONTENT,
                    "application/octet", null)


        } catch (e: IOException) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "application/octet", e.message)
        } catch (e: NanoHTTPD.ResponseException) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/octet", e.message)
        }

    }

    override fun other(method: String, uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
        return null
    }

    companion object {

        const val PARAM_APPREPO_INDEX = 0
    }
}
