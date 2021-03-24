package com.ustadmobile.port.sharedse.impl.http

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.contentformats.xapi.Actor
import com.ustadmobile.core.contentformats.xapi.State
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStateEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.StatementRequestException
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStateEndpointImpl
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.util.*

class XapiStateResponder : RouterNanoHTTPD.UriResponder {
    internal var contentMapToken = object : TypeToken<HashMap<String, Any>>() {

    }.type


    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val di = uriResource.initParameter(PARAM_APPREPO_INDEX, DI::class.java)
        val endpointUrl = urlParams[URI_PARAM_ENDPOINT] ?: throw IllegalArgumentException("No endpoint")
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

            val stateEndpoint: XapiStateEndpoint =  di.on(Endpoint(endpointUrl)).direct.instance()
            val json = stateEndpoint.getContent(stateId, agentJson, activityId, registration, since)
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
        val di = uriResource.initParameter(PARAM_APPREPO_INDEX, DI::class.java)
        val endpointUrl = urlParams[URI_PARAM_ENDPOINT] ?: throw IllegalArgumentException("No endpoint")
        val gson: Gson by di.instance()
        val content: String?
        try {

            val map = HashMap<String, String>()
            session.parseBody(map)
            content = if (map.containsKey("content")) {
                map["content"]
            } else {
                session.queryParameterString
            }
            val queryParams = session.parameters
            val activityId = queryParams["activityId"]?.get(0)
            val agentJson = queryParams["agent"]?.get(0)
            val stateId = queryParams["stateId"]?.get(0)
            val registration = if (queryParams.containsKey("registration"))
                queryParams["registration"]!![0]
            else
                ""

            isContentTypeJson(session.headers["content-type"])

            val agent = gson.fromJson(agentJson, Actor::class.java)
            val contentMap: HashMap<String, Any> = gson.fromJson(content, contentMapToken)

            val state = State(stateId, agent, activityId, contentMap, registration)
            val stateEndpoint: XapiStateEndpoint =  di.on(Endpoint(endpointUrl)).direct.instance()
            stateEndpoint.overrideState(state)

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NO_CONTENT,
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

    override fun post(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val di = uriResource.initParameter(PARAM_APPREPO_INDEX, DI::class.java)
        val endpointUrl = urlParams[URI_PARAM_ENDPOINT] ?: throw IllegalArgumentException("No endpoint")
        val gson: Gson by di.instance()
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

            val agent = gson.fromJson(agentJson, Actor::class.java)
            val contentMap: HashMap<String, Any>
            contentMap = gson.fromJson(map["postData"], contentMapToken)

            isContentTypeJson(session.headers["content-type"])

            val state = State(stateId, agent, activityId, contentMap, registration)
            val stateEndpoint: XapiStateEndpoint =  di.on(Endpoint(endpointUrl)).direct.instance()
            stateEndpoint.storeState(state)

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

    private fun isContentTypeJson(contentType: String?) {
        if (contentType?.isEmpty() == true || contentType != "application/json") {
            throw StatementRequestException("Content Type missing or not set to application/json")
        }
    }

    override fun delete(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val di = uriResource.initParameter(PARAM_APPREPO_INDEX, DI::class.java)
        val endpointUrl = urlParams[URI_PARAM_ENDPOINT] ?: throw IllegalArgumentException("No endpoint")
        val gson: Gson by di.instance()

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


            val stateEndpoint: XapiStateEndpoint =  di.on(Endpoint(endpointUrl)).direct.instance()
            if (stateId == null || stateId.isEmpty()) {
                stateEndpoint.deleteListOfStates(agentJson, activityId, registration)
            } else {
                stateEndpoint.deleteStateContent(stateId, agentJson, activityId, registration)

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

        const val URI_PARAM_ENDPOINT = "endpoint"
    }
}
