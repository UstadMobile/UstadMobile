package com.ustadmobile.core.domain.xapi.http

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xapi.toXapiSession
import com.ustadmobile.core.util.ext.firstNonWhiteSpaceChar
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.request.IHttpRequest.Companion.Method
import com.ustadmobile.ihttp.request.IHttpRequestWithTextBody
import com.ustadmobile.ihttp.response.IHttpResponse
import com.ustadmobile.ihttp.response.StringResponse
import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class XapiHttpServerUseCase(
    private val statementResource: XapiStatementResource,
    private val db: UmAppDatabase,
    private val json: Json,
    private val endpoint: Endpoint,
) {

    //Simple split by whitespace
    private val authHeaderSplitRegex = Regex("\\s+")

    /**
     * @param pathSegments the segments of the path AFTER the xapi endpoint (exclusive)
     */
    suspend operator fun invoke(
        pathSegments: List<String>,
        request: IHttpRequest
    ): IHttpResponse {
        return try {
            val authHeader = request.headers["Authorization"]?.trim()
                ?: throw HttpApiException(401, "Missing auth")

            val (authScheme, authData) = authHeaderSplitRegex.split(authHeader, limit = 2)

            if(!authScheme.equals("Basic", true))
                throw HttpApiException(400, "Only basic auth supported")

            val authUser = authData.decodeBase64Bytes().decodeToString()
            val (xseUid, auth) = authUser.split(":")

            val xapiSessionEntity = db.xapiSessionEntityDao().findByUidAsync(xseUid.toLong())
                ?: throw HttpApiException(401, "Unauthorized: invalid session")

            if (xapiSessionEntity.xseAuth != auth) {
                throw HttpApiException(401, "Unauthorized: invalid auth")
            }

            val xapiSession = xapiSessionEntity.toXapiSession(endpoint)
            val resourceName = pathSegments.first()
            when {
                resourceName == "statements" && request.method == Method.PUT -> {
                    val requestBody = (request as IHttpRequestWithTextBody).bodyAsText()
                        ?: throw HttpApiException(400, "missing request body")
                    val stmtId = request.queryParam("statementId") ?: throw HttpApiException(
                        400, "missing statementId")

                    val statement: XapiStatement = json.decodeFromString(requestBody)

                    statementResource.put(
                        statement = statement,
                        statementIdParam = stmtId,
                        xapiSession = xapiSession
                    )

                    StringResponse(
                        request = request,
                        mimeType = "text/plain",
                        body = "",
                        responseCode = 204,
                    )
                }

                resourceName == "statements" && request.method == Method.POST -> {
                    val requestBody = (request as IHttpRequestWithTextBody).bodyAsText()
                        ?: throw HttpApiException(400, "missing request body")

                    //As per the Xapi spec the request body could be a single statement, or an
                    //array of statements.
                    val firstNonWhiteSpaceChar = requestBody.firstNonWhiteSpaceChar()
                    val statements = if(firstNonWhiteSpaceChar == '[') {
                        json.decodeFromString(ListSerializer(XapiStatement.serializer()), requestBody)
                    }else {
                        listOf(json.decodeFromString(XapiStatement.serializer(), requestBody))
                    }

                    val uuids = statementResource.post(
                        statements = statements, xapiSession = xapiSession
                    )

                    StringResponse(
                        request = request,
                        mimeType = "application/json",
                        body = json.encodeToString(
                            ListSerializer(String.serializer()), uuids.map { it.toString() }
                        )
                    )
                }

                else -> {
                    return StringResponse(
                        request = request,
                        mimeType = "text/plain",
                        body = "Not found: ${request.method} ${request.url}",
                        responseCode = 404,
                    )
                }
            }
        }catch(e: HttpApiException) {
            StringResponse(
                request = request,
                mimeType = "text/plain",
                body = e.message ?: "",
                responseCode = e.statusCode,
            )
        }catch(t: Throwable) {
            StringResponse(
                request = request,
                mimeType = "text/plain",
                body = t.message ?: "",
                responseCode = 500,
            )
        }
    }

}