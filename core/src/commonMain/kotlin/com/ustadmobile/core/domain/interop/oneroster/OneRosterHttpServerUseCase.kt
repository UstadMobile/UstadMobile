package com.ustadmobile.core.domain.interop.oneroster

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.oneroster.model.Clazz
import com.ustadmobile.core.domain.interop.oneroster.model.LineItem
import com.ustadmobile.core.domain.interop.oneroster.model.Result as OneRosterResult
import com.ustadmobile.core.util.isimplerequest.ISimpleTextRequest
import com.ustadmobile.core.util.isimpleresponse.StringSimpleTextResponse
import com.ustadmobile.core.util.stringvalues.IStringValues
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Processes OneRoster HTTP requests (serializing/deserializing, checking authentication, etc)
 * and delegates to the OneRosterEndpoint to run the required logic (validation, converting between
 * the OneRoster model and database entities, etc).
 */
class OneRosterHttpServerUseCase(
    private val db: UmAppDatabase,
    private val oneRosterEndpoint: OneRosterEndpoint,
    private val json: Json,
) {


    private fun newPlainTextResponse(statusCode: Int, bodyText: String) = StringSimpleTextResponse(
        responseCode = statusCode,
        responseBody = bodyText,
        headers = IStringValues.contentType("text/plain"),
    )

    suspend operator fun invoke(
        request: ISimpleTextRequest
    ): StringSimpleTextResponse {
        val authToken = request.headers["Authorization"]?.substringAfter("Bearer ")?.trim()
            ?: return newPlainTextResponse(401, "No auth token")

        val accountPersonUid = db.externalAppPermissionDao().getPersonUidByAuthToken(
            authToken, systemTimeInMillis()
        )

        if(accountPersonUid == 0L)
            return newPlainTextResponse(401, "Invalid auth token")

        val pathSegments = request.path.split("/")

        val apiPathSegments = pathSegments.subList(
            pathSegments.lastIndexOf("oneroster") + 1,
            pathSegments.size
        )

        return when {
            //getClassesForUser
            apiPathSegments[0] == "users" && apiPathSegments.getOrNull(2) == "classes" -> {
                val classes = oneRosterEndpoint.getClassesForUser(accountPersonUid, apiPathSegments[1])
                StringSimpleTextResponse(
                    responseCode = 200,
                    responseBody = json.encodeToString(ListSerializer(Clazz.serializer()), classes),
                    headers = IStringValues.contentType("application/json"),
                )
            }

            //GerResultsForStudentForClass
            apiPathSegments[0] == "classes" &&
                    apiPathSegments.getOrNull(2) == "students" &&
                    apiPathSegments.getOrNull(4) == "results"
            -> {
                val results = oneRosterEndpoint.getResultsForStudentForClass(
                    accountPersonUid = accountPersonUid,
                    clazzSourcedId = apiPathSegments[1],
                    studentSourcedId = apiPathSegments[3],
                )

                StringSimpleTextResponse(
                    responseCode = 200,
                    responseBody = json.encodeToString(
                        ListSerializer(OneRosterResult.serializer()), results
                    ),
                    headers = IStringValues.contentType("application/json"),
                )
            }

            //getLineItem
            apiPathSegments[0] == "lineItems" && apiPathSegments.size == 2  &&
                    request.method.equals("GET", true)-> {

                oneRosterEndpoint.getLineItem(
                    accountPersonUid = accountPersonUid,
                    lineItemSourcedId = apiPathSegments[1],
                )?.let {
                    StringSimpleTextResponse(
                        responseCode = 200,
                        responseBody = json.encodeToString(LineItem.serializer(), it),
                        headers = IStringValues.contentType("application/json"),
                    )
                } ?: newPlainTextResponse(
                    404,
                    "Not Found LineItem sourcedId= ${apiPathSegments[1]}"
                )
            }

            //putLineItem
            apiPathSegments[0] == "lineItems" && apiPathSegments.size == 2  &&
                    request.method.equals("PUT", true)-> {
                val lineItemSourcedId = apiPathSegments[1]
                val requestBody = request.body ?: return newPlainTextResponse(
                    400, "Put line item request had no body")

                val lineItem = json.decodeFromString(LineItem.serializer(), requestBody)
                val putResult = oneRosterEndpoint.putLineItem(
                    accountPersonUid = accountPersonUid,
                    lineItemSourcedId = lineItemSourcedId,
                    lineItem = lineItem
                )

                StringSimpleTextResponse(
                    headers = IStringValues.contentType("text/plain"),
                    responseCode = putResult.statusCode,
                    responseBody = putResult.body,
                )
            }

            //putResult
            apiPathSegments[0] == "results" && apiPathSegments.size == 2  &&
                    request.method.equals("PUT", true)-> {
                val sourcedId = apiPathSegments[1]
                val bodyStr = request.body ?: return newPlainTextResponse(400,
                    "Put result request had no body")
                val result = json.decodeFromString(OneRosterResult.serializer(), bodyStr)

                val response = oneRosterEndpoint.putResult(
                    accountPersonUid = accountPersonUid,
                    resultSourcedId = sourcedId,
                    result = result,
                )

                StringSimpleTextResponse(
                    headers = IStringValues.contentType("text/plain"),
                    responseCode = response.statusCode,
                    responseBody = response.body,
                )
            }

            else ->  {
                newPlainTextResponse(404, "Not found")
            }
        }
    }



}