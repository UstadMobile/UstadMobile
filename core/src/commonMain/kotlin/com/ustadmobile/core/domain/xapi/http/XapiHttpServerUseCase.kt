package com.ustadmobile.core.domain.xapi.http

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.xapi.XapiJson
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.ext.agent
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xapi.state.DeleteXapiStateUseCase
import com.ustadmobile.core.domain.xapi.state.ListXapiStateIdsUseCase
import com.ustadmobile.core.domain.xapi.state.RetrieveXapiStateUseCase
import com.ustadmobile.core.domain.xapi.state.StoreXapiStateUseCase
import com.ustadmobile.core.domain.xapi.state.h5puserdata.H5PUserDataEndpointUseCase
import com.ustadmobile.core.domain.xxhash.XXStringHasher
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
    private val deleteXapiStateRequest: DeleteXapiStateUseCase,
    private val h5PUserDataEndpointUseCase: H5PUserDataEndpointUseCase,
    private val db: UmAppDatabase,
    xapiJson: XapiJson,
    private val endpoint: Endpoint,
    private val xxStringHasher: XXStringHasher,
) {

    private val json = xapiJson.json

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
            //Allow use of Authorization as a query parameter - needed for h5puserdata where it is
            //not possible to set a header
            val authHeader = request.headers["Authorization"]?.trim()
                ?: request.queryParam("Authorization")
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

            /**
             * Check the agent parameter (if provided) matches the session. This protects the state
             * APIs against access by unauthorized users
             */
            val agent = try {
                request.queryParam("agent")?.let {
                    json.decodeFromString(XapiAgent.serializer(), it)
                }
            }catch(e: Throwable) {
                throw HttpApiException(400, "Agent is not valid json: ${e.message}", e)
            }

            if(agent != null && agent.identifierHash(xxStringHasher) != xapiSessionEntity.xseActorUid) {
                throw HttpApiException(403, "Unauthorized: Agent does not match session")
            }

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
                        xapiSession = xapiSessionEntity,
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
                        statements = statements, xapiSession = xapiSessionEntity
                    )

                    StringResponse(
                        request = request,
                        mimeType = "application/json; charset=utf-8",
                        body = json.encodeToString(
                            ListSerializer(String.serializer()), uuids.map { it.toString() }
                        )
                    )
                }

                //Xapi State Resource
                resourceName == "activities" && pathSegments.getOrNull(1) == "state" -> {
                    when (request.method) {
                        Method.POST, Method.PUT -> {
                            storeXapiStateUseCase(
                                xapiSession = xapiSessionEntity,
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
                                        agent = xapiSessionEntity.agent(endpoint),
                                        registration = request.queryParam("registration"),
                                        since = request.queryParam("since")?.fromHttpToGmtDate()?.timestamp ?: 0
                                    ),
                                    xapiSession = xapiSessionEntity
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
                                xapiSession = xapiSessionEntity,
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

                        Method.DELETE -> {
                            deleteXapiStateRequest(
                                request = DeleteXapiStateUseCase.DeleteXapiStateRequest(
                                    activityId = request.queryParamOrThrow("activityId"),
                                    agent = json.decodeFromString(XapiAgent.serializer(), request.queryParamOrThrow("agent")),
                                    registration = request.queryParam("registration"),
                                    stateId = request.queryParam("stateId")
                                ),
                                session = xapiSessionEntity
                            )

                            return StringResponse(
                                request = request,
                                mimeType = "text/plain",
                                responseCode = 204,
                                body = ""
                            )
                        }

                        else -> {
                            throw HttpApiException(400, "Bad request: ${request.method} ${request.url}")
                        }
                    }
                }

                resourceName == "activities" && pathSegments.getOrNull(1) == "h5p-userdata" -> {
                    h5PUserDataEndpointUseCase(
                        request = request,
                        xapiSessionEntity = xapiSessionEntity,
                    )
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