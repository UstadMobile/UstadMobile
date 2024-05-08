package com.ustadmobile.core.domain.interop.oneroster

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.oneroster.model.Clazz
import com.ustadmobile.core.util.isimplerequest.ISimpleTextRequest
import com.ustadmobile.door.http.DoorJsonResponse
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Processes OneRoster HTTP requests (serializing/deserializing, checking authentication, etc)
 * and delegates to the OneRoster endpoint to run the required logic.
 */
class OneRosterHttpServerUseCase(
    private val db: UmAppDatabase,
    private val oneRosterEndpoint: OneRosterEndpoint,
    private val json: Json,
) {


    private fun newPlainTextResponse(statusCode: Int, bodyText: String) = DoorJsonResponse(
        responseCode = statusCode,
        bodyText = bodyText,
        contentType = "text/plain"
    )

    suspend operator fun invoke(
        request: ISimpleTextRequest
    ): DoorJsonResponse {
        val authToken = request.headers["Authorization"]?.substringAfter("Bearer ")?.trim()
            ?: return newPlainTextResponse(401, "No auth token")

        val accountPersonUid = db.externalAppPermissionDao.getPersonUidByAuthToken(
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
                DoorJsonResponse(
                    responseCode = 200,
                    bodyText = json.encodeToString(ListSerializer(Clazz.serializer()), classes),
                    contentType = "application/json"
                )
            }

            else ->  {
                newPlainTextResponse(404, "Not found")
            }
        }
    }



}