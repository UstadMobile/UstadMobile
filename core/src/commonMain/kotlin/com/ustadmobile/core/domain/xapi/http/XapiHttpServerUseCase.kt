package com.ustadmobile.core.domain.xapi.http

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.xapi.XapiJson
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xapi.state.ListXapiStateIdsUseCase
import com.ustadmobile.core.domain.xapi.state.RetrieveXapiStateUseCase
import com.ustadmobile.core.domain.xapi.state.StoreXapiStateUseCase
import com.ustadmobile.core.domain.xapi.state.XapiStateParams
import com.ustadmobile.core.domain.xapi.toXapiSession
import com.ustadmobile.core.util.ext.firstNonWhiteSpaceChar
import com.ustadmobile.ihttp.headers.iHeadersBuilder
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.request.IHttpRequest.Companion.Method
import com.ustadmobile.ihttp.request.IHttpRequestWithTextBody
import com.ustadmobile.ihttp.response.ByteArrayResponse
import com.ustadmobile.ihttp.response.IHttpResponse
import com.ustadmobile.ihttp.response.StringResponse
import io.ktor.http.fromHttpToGmtDate
import io.ktor.http.toHttpDate
import io.ktor.util.date.GMTDate
import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

class XapiHttpServerUseCase(
    private val statementResource: XapiStatementResource,
    private val storeXapiStateUseCase: StoreXapiStateUseCase,
    private val retrieveXapiStateUseCase: RetrieveXapiStateUseCase,
    private val listXapiStateIdsUseCase: ListXapiStateIdsUseCase,
    private val db: UmAppDatabase,
    xapiJson: XapiJson,
    private val endpoint: Endpoint,
) {

    private val json = xapiJson.json

    //Simple split by whitespace
    private val authHeaderSplitRegex = Regex("\\s+")

    private fun IHttpRequest.queryParamOrThrow(paramName: String): String {
        return this.queryParam(paramName) ?: throw HttpApiException(400, "Missing $paramName")
    }

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

                //Xapi State Resource
                resourceName == "activities" && pathSegments.getOrNull(1) == "state" -> {
                    fun IHttpRequest.xapiStateParams() = XapiStateParams(
                        activityId = queryParamOrThrow("activityId"),
                        agent = queryParamOrThrow("agent"),
                        registration = queryParam("registration"),
                        stateId = queryParamOrThrow("stateId")
                    )

                    when (request.method) {
                        Method.POST, Method.PUT -> {
                            storeXapiStateUseCase(
                                xapiSession = xapiSession,
                                method = request.method,
                                contentType = request.headers["content-type"] ?: "application/octet-stream",
                                xapiStateParams = request.xapiStateParams(),
                                request = request,
                            )

                            StringResponse(
                                request = request,
                                mimeType = "text/plain",
                                responseCode = 204,
                                body = "",
                            )
                        }

                        Method.GET -> {
                            //if the stateId is not included in params, then list state ids as per the spec
                            if(request.queryParam("stateId") == null) {
                                val listResponse = listXapiStateIdsUseCase.invoke(
                                    request = ListXapiStateIdsUseCase.ListXapiStateIdsRequest(
                                        activityId = request.queryParamOrThrow("activityId"),
                                        agent = xapiSession.agent,
                                        registration = request.queryParam("registration"),
                                        since = request.queryParam("since")?.fromHttpToGmtDate()?.timestamp ?: 0
                                    ),
                                    xapiSession = xapiSession
                                )

                                return StringResponse(
                                    request = request,
                                    mimeType = "application/json",
                                    extraHeaders = iHeadersBuilder {
                                        if(listResponse.lastModified > 0)
                                            header("last-modified", GMTDate(listResponse.lastModified).toHttpDate())
                                    },
                                    body = json.encodeToString(
                                        ListSerializer(String.serializer()), listResponse.stateIds
                                    )
                                )
                            }


                            val result = retrieveXapiStateUseCase(
                                xapiSession = xapiSession,
                                xapiStateParams = request.xapiStateParams(),
                            )

                            when(result) {
                                is RetrieveXapiStateUseCase.ByteRetrieveXapiStateResult -> {
                                    ByteArrayResponse(request, result.contentType, bodyBytes = result.content)
                                }

                                is RetrieveXapiStateUseCase.TextRetrieveXapiStateResult -> {
                                    StringResponse(request, result.contentType, body = result.content)
                                }

                                else -> {
                                    throw HttpApiException(404, "not found")
                                }
                            }
                        }
                        else -> {
                            throw HttpApiException(400, "Bad request: ${request.method} ${request.url}")
                        }
                    }
                }

                else -> {
                    throw HttpApiException(404, "not found: ${request.url}")
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