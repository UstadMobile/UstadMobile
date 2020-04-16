package com.ustadmobile.port.sharedse.impl.http

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.port.sharedse.contentformats.xapi.Statement
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementSerializer
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.StatementEndpoint
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.StatementRequestException
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import java.io.*
import java.util.*

class XapiStatementResponder : RouterNanoHTTPD.UriResponder {


    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val repo = uriResource.initParameter(PARAM_APPREPO_INDEX, UmAppDatabase::class.java)


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
        val repo = uriResource.initParameter(PARAM_APPREPO_INDEX, UmAppDatabase::class.java)

        val contentEntryUid = urlParams[URLPARAM_CONTENTENTRYUID]?.toLongOrNull() ?: 0L
        val gson = createGson()
        var content: ByteArray? = null
        var fin: FileInputStream? = null
        var bout: ByteArrayOutputStream? = null
        val tmpFileName: String
        try {

            var statement: String? = null
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
                statement = session.queryParameterString
            }
            val queryParams = session.parameters
            var statementId = ""
            if (queryParams != null && queryParams.containsKey("statementId")) {
                statementId = queryParams["statementId"]!![0]
            }

            if (content != null || statement != null) {
                statement = content?.let { String(it) } ?: statement

                val statements = getStatementsFromJson(statement!!.trim { it <= ' ' }, gson)
                val endpoint = StatementEndpoint(repo, gson)
                endpoint.storeStatements(statements, statementId, contentEntryUid = contentEntryUid)
            } else {
                throw StatementRequestException("no content found", 204)
            }
        } catch (e: StatementRequestException) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.lookup(e.errorCode),
                    "application/octet", e.message)
        } catch (e: IOException) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "application/octet", e.message)
        } catch (e: NanoHTTPD.ResponseException) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/octet", e.message)
        } finally {
            UMIOUtils.closeInputStream(fin)
            UMIOUtils.closeOutputStream(bout)

        }
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NO_CONTENT,
                "application/octet", null)
    }

    private fun createGson(): Gson {
        val builder = GsonBuilder()
        builder.registerTypeAdapter(Statement::class.java, StatementSerializer())
        builder.registerTypeAdapter(Statement::class.java, StatementDeserializer())
        return builder.create()
    }

    override fun post(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val repo = uriResource.initParameter(PARAM_APPREPO_INDEX, UmAppDatabase::class.java)
        val contentEntryUid = urlParams[URLPARAM_CONTENTENTRYUID]?.toLongOrNull() ?: 0L
        val gson = createGson()
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
            val map = HashMap<String, String>()
            session.parseBody(map)
            val statement = session.queryParameterString.trim { it <= ' ' }

            val statements = getStatementsFromJson(statement, gson)

            val endpoint = StatementEndpoint(repo, gson)
            uuids = endpoint.storeStatements(statements, "",
                    contentEntryUid = contentEntryUid)
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
            UMIOUtils.closeInputStream(`is`)
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
    }
}
