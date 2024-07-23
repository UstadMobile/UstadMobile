package com.ustadmobile.core.domain.xapi.http

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xapi.toXapiSession
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.request.IHttpRequest.Companion.Method
import com.ustadmobile.ihttp.request.IHttpRequestWithTextBody
import com.ustadmobile.ihttp.response.IHttpResponse
import com.ustadmobile.ihttp.response.StringResponse
import io.ktor.util.decodeBase64Bytes
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

        val authHeader = request.headers["Authorization"]?.trim()
            ?: throw HttpApiException(401, "Missing auth")

        val (authScheme, authData) = authHeaderSplitRegex.split(authHeader, limit = 2)

        //Thorw exception if authScheme != basic


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

                return StringResponse(
                    request = request,
                    mimeType = "text/plain",
                    body = "",
                    responseCode = 204,
                )
            }

            else -> {
                return StringResponse(
                    request = request,
                    mimeType = "text/plain",
                    body = "Not found: ${request.method} ${request.url}"
                )
            }
        }
    }

}