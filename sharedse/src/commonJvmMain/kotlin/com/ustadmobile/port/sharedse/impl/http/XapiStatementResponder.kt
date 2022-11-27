package com.ustadmobile.port.sharedse.impl.http

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.StatementRequestException
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.*
import java.util.*
import io.github.aakira.napier.Napier

class XapiStatementResponder : RouterNanoHTTPD.UriResponder {


    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (urlParams.containsKey(PARAM_STATEMENT_ID) || urlParams.containsKey(PARAM_VOID_STATEMENT_ID)) {

            // single statement
            if (urlParams.containsKey(PARAM_STATEMENT_ID) && urlParams.containsKey(PARAM_VOID_STATEMENT_ID)) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                        "application/octet", null)
            }

            val keyList = urlParams.keys
            val wantedList = Arrays.asList(*WANTED_KEYS)
            for (key in keyList) {
                if (!wantedList.contains(key)) {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                            "application/octet", null)
                }
            }

        } else {

            // list of statements


        }


        return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK,
                "application/octet", null)
    }

    override fun put(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val di = uriResource.initParameter(PARAM_APPREPO_INDEX, DI::class.java)
        val gson: Gson by di.instance()
        val endpointUrl = urlParams[URI_PARAM_ENDPOINT] ?: throw IllegalArgumentException("No endpoint")


        val contentEntryUid = urlParams[URLPARAM_CONTENTENTRYUID]?.toLongOrNull() ?: 0L
        val clazzUid = urlParams[URLPARAM_CLAZZUID]?.toLongOrNull() ?: 0L
        try {
            val statement : String = session.parseRequestBody() ?:
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "application/octet", "no body")
            val queryParams = session.parameters
            var statementId = ""
            if (queryParams != null && queryParams.containsKey("statementId")) {
                statementId = queryParams["statementId"]!![0]
            }

            if (statement != null) {
                val statements = getStatementsFromJson(statement.trim { it <= ' ' }, gson)
                val statementEndpoint: XapiStatementEndpoint =  di.on(Endpoint(endpointUrl)).direct.instance()
                statementEndpoint.storeStatements(statements, statementId,
                        contentEntryUid = contentEntryUid, clazzUid)
            } else {
                throw StatementRequestException("no content found", 204)
            }
        } catch (e: StatementRequestException) {
            Napier.e("StatementException", e)
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.lookup(e.errorCode),
                    "application/octet", e.message)
        } catch (e: IOException) {
            Napier.e("IOException", e)
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "application/octet", e.message)
        } catch (e: NanoHTTPD.ResponseException) {
            Napier.e("ResponseException", e)
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/octet", e.message)
        } catch(e: Exception) {
            Napier.e("Other Exception", e)
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", e.message)
        }
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NO_CONTENT,
                "application/octet", null)
    }

    override fun post(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val di = uriResource.initParameter(PARAM_APPREPO_INDEX, DI::class.java)
        val gson: Gson by di.instance()
        val endpointUrl = urlParams.get(URI_PARAM_ENDPOINT) ?: throw IllegalArgumentException("No endpoint")

        val contentEntryUid = urlParams[URLPARAM_CONTENTENTRYUID]?.toLongOrNull() ?: 0L
        val clazzUid = urlParams[URLPARAM_CLAZZUID]?.toLongOrNull() ?: 0L
        val uuids: List<String>
        var `is`: InputStream? = null
        try {

            val queryParams = session.parameters
            if (queryParams != null && queryParams.containsKey("method")) {

                val method = queryParams["method"]!![0]
                if (method.equals("put", ignoreCase = true)) {
                    return put(uriResource, urlParams, session)
                } else if (method.equals("get", ignoreCase = true)) {
                    return get(uriResource, urlParams, session)
                }

            }

            val requestBody: String = session.parseRequestBody() ?:
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                "application/octet", "no body")
            val statement = requestBody.trim { it <= ' ' }

            val statements = getStatementsFromJson(statement, gson)

            val statementEndpoint: XapiStatementEndpoint =  di.on(Endpoint(endpointUrl)).direct.instance()
            uuids = statementEndpoint.storeStatements(statements, "",
                    contentEntryUid = contentEntryUid, clazzUid)
            `is` = ByteArrayInputStream(gson.toJson(uuids).toByteArray())

            return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK,
                    "application/octet", `is`)

        } catch (e: IOException) {
            return if (e.message != null && e.message == "Has Existing Statements") {
                NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.CONFLICT,
                        "application/octet", null)
            } else NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "application/octet", e.message)
        } catch (e: NanoHTTPD.ResponseException) {
            return if (e.message != null && e.message == "Has Existing Statements") {
                NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.CONFLICT, "application/octet", null)
            } else NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/octet", e.message)
        } finally {
            `is`?.close()
        }

    }

    private fun getStatementsFromJson(statement: String, gson: Gson): ArrayList<Statement> {
        val statements = ArrayList<Statement>()
        if (statement.startsWith("{")) {
            val obj = gson.fromJson(statement, Statement::class.java)
            statements.add(obj)
        } else {
            statements.addAll(gson.fromJson(statement, STATEMENT_LIST_TYPE))
        }
        return statements
    }

    override fun delete(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
        return null
    }

    override fun other(method: String, uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
        return null
    }

    inner class StatementResult(var statements: List<Statement>?, var more: String?)

    companion object {

        private val STATEMENT_LIST_TYPE = object : TypeToken<ArrayList<Statement>>() {

        }.type

        const val PARAM_STATEMENT_ID = "statementId"
        private const val PARAM_VOID_STATEMENT_ID = "voidedStatementId"
        private const val PARAM_ATTACHMENTS = "attachments"
        private const val PARAM_FORMAT = "format"

        private val WANTED_KEYS = arrayOf(PARAM_STATEMENT_ID, PARAM_VOID_STATEMENT_ID, PARAM_ATTACHMENTS, PARAM_FORMAT)

        private const val PARAM_APPREPO_INDEX = 0

        const val URLPARAM_CONTENTENTRYUID = "contentEntryUid"

        const val URLPARAM_CLAZZUID = "clazzUid"

        const val URI_PARAM_ENDPOINT = "endpoint"
    }
}
