package com.ustadmobile.core.api.oneroster

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.api.DoorJsonRequest
import com.ustadmobile.core.api.DoorJsonResponse
import com.ustadmobile.core.api.oneroster.model.Clazz
import com.ustadmobile.core.api.oneroster.model.toOneRosterClass
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import io.ktor.http.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

/**
 * @param dbGetter function that will provide the database
 */
class OneRosterEndpoint(
    @Suppress("unused") //reserved for future use
    private val activeEndpointsGetter: () -> Set<Endpoint>,
    private val di: DI,
) {

    private val json: Json by di.instance()

    /**
     * request.path should strip out prefix e.g. use
     * https://ktor.io/docs/routing-in-ktor.html#path_parameter_tailcard
     */
    suspend fun serve(request: DoorJsonRequest): DoorJsonResponse {
        val endpointUrl = URLBuilder().takeFrom(request.url).apply {
            encodedPath = encodedPath.substringBefore("/api/oneroster/").requirePostfix("/")
        }.build()

        val matchingEndpoints = endpointUrl to URLBuilder(endpointUrl).apply {
            protocol = if(endpointUrl.protocol == URLProtocol.HTTP) {
                URLProtocol.HTTPS
            }else {
                URLProtocol.HTTP
            }
        }

        val activeEndpoints = activeEndpointsGetter()
        val matchingEndpointUrls = Pair(matchingEndpoints.first.toString(),
            matchingEndpoints.second.toString())

        val endpoint = activeEndpoints.firstOrNull {
            it.url == matchingEndpointUrls.first || it.url == matchingEndpointUrls.second
        } ?: return DoorJsonResponse(400, "text/plain", responseBody = "No such endpoint")

        val pathSegments = request.url.pathSegments
        val index = pathSegments.indexOfLast {
            it == "oneroster" && pathSegments.getOrNull(pathSegments.indexOf(it) - 1) == "api"
        }

        //Split out the prefix to get the path within the OneRoster API
        val apiPathComponents = pathSegments.subList(index + 1, pathSegments.size)


        val authToken = request.headers.keys.firstOrNull { it.equals(HEADER_AUTH, true) }?.let { header ->
            request.headers[header]
        } ?: return DoorJsonResponse(403, "text/plain", responseBody = "No auth token")

        val db : UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)

        return db.withDoorTransactionAsync {
            val accountPersonUid = db.externalAppPermissionDao.getPersonUidByAuthToken(authToken,
                systemTimeInMillis())

            when {
                accountPersonUid == 0L -> {
                    DoorJsonResponse(403, "text/plain",
                        responseBody = "Invalid auth token")
                }

                apiPathComponents[0] == "users" && apiPathComponents.last() == "classes" -> {
                    //list all classes for user
                    val classes = db.clazzDao.findOneRosterUserClazzes(
                        accountPersonUid = accountPersonUid,
                        filterByEnrolledMemberPersonUid = apiPathComponents[1].toLongOrNull() ?: -1L
                    ).map {
                        it.toOneRosterClass()
                    }

                    DoorJsonResponse(
                        statusCode = 200,
                        contentType = "application/json",
                        responseBody = json.encodeToString(ListSerializer(Clazz.serializer()), classes)
                    )
                }

                else -> DoorJsonResponse(404, "text/plain",
                    responseBody = "Not found")
            }
        }
    }

    companion object {

        const val HEADER_AUTH = "auth-token"

    }

}