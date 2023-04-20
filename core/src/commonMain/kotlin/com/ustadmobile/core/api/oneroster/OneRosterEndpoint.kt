package com.ustadmobile.core.api.oneroster

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.api.DoorJsonRequest
import com.ustadmobile.core.api.DoorJsonResponse
import com.ustadmobile.core.api.oneroster.model.*
import com.ustadmobile.core.api.util.forwardheader.parseForwardHeader
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
import com.ustadmobile.core.api.oneroster.model.Result as OneRosterResult

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

            /**
             * Use forward headers to get the original host and protocol
             */
            val originalForwardHeader = request.headers.entries.firstOrNull{
                it.key.equals("forwarded", ignoreCase = true)
            }?.let { parseForwardHeader(it.value) }?.firstOrNull()

            originalForwardHeader?.protoVal?.also { proto ->
                protocol = URLProtocol.createOrDefault(proto)
            }

            originalForwardHeader?.hostVal?.also { hostVal ->
                host = hostVal.substringBefore(":")
                if(hostVal.contains(":")) {
                    port = hostVal.substringAfter(":").toInt()
                }else {
                    port = DEFAULT_PORT
                }
            }
        }.build()

        val endpoint = Endpoint(endpointUrl.toString())

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
                    DoorJsonResponse(401, "text/plain",
                        responseBody = "Invalid auth token")
                }

                //getClassesForUser
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

                //getResultsForStudentForClass
                apiPathComponents[0] == "classes" &&
                    apiPathComponents.getOrNull(2) == "students" &&
                    apiPathComponents.getOrNull(4) == "results"
                -> {
                    val clazzUid = apiPathComponents[1].toLong()
                    val studentPersonUid = apiPathComponents[3].toLong()

                    val results = db.studentResultDao.findByClazzAndStudent(
                        clazzUid, studentPersonUid, accountPersonUid
                    ).map {
                        it.toOneRosterResult(endpoint)
                    }

                    DoorJsonResponse(
                        statusCode = 200,
                        contentType = "application/json",
                        responseBody = json.encodeToString(ListSerializer(OneRosterResult.serializer()),
                            results)
                    )
                }

                //getLineItem
                apiPathComponents[0] == "lineItems" &&
                    apiPathComponents.size == 2 &&
                    request.method == DoorJsonRequest.Method.GET
                -> {
                    val lineItemSourcedId = apiPathComponents[1]

                    db.courseBlockDao.findBySourcedId(
                        lineItemSourcedId, accountPersonUid
                    )?.toOneRosterLineItem(endpoint)?.let {
                        DoorJsonResponse(
                            statusCode = 200,
                            contentType = "application/json",
                            responseBody = json.encodeToString(LineItem.serializer(), it)
                        )
                    } ?:DoorJsonResponse(
                            statusCode = 404,
                            contentType = "text/plain",
                            responseBody = "Not found"
                    )
                }

                //putLineItem
                apiPathComponents[0] == "lineItems" &&
                    apiPathComponents.size == 2 &&
                    request.method == DoorJsonRequest.Method.PUT
                -> {
                    val lineItemSourcedId = apiPathComponents[1]
                    val existingCourseBlock = db.courseBlockDao.findBySourcedId(
                        lineItemSourcedId, accountPersonUid
                    )

                    val requestBody = request.requestBody
                        ?: return@withDoorTransactionAsync DoorJsonResponse(400, "text/plain", responseBody = "No body")

                    val lineItem = json.decodeFromString(LineItem.serializer(), requestBody)

                    if(existingCourseBlock == null) {
                        db.courseBlockDao.insertAsync(lineItem.toCourseBlock())
                        DoorJsonResponse(201, "text/plain")
                    }else {
                        db.courseBlockDao.updateFromLineItem(
                            cbUid = existingCourseBlock.cbUid,
                            active = lineItem.status == Status.ACTIVE,
                            dateLastModified = parse8601Timestamp(lineItem.dateLastModified),
                            title = lineItem.description,
                            description = lineItem.description,
                            assignDate = parse8601Timestamp(lineItem.assignDate),
                            dueDate = parse8601Timestamp(lineItem.dueDate),
                            resultValueMin = lineItem.resultValueMin,
                            resultValueMax = lineItem.resultValueMax
                        )
                        DoorJsonResponse(200, "text/plain")
                    }
                }

                //putResult
                apiPathComponents[0] == "results" &&
                    apiPathComponents.size == 2 &&
                    request.method == DoorJsonRequest.Method.PUT
                -> {
                    val sourcedId = apiPathComponents[1]
                    val bodyStr = request.requestBody
                        ?: return@withDoorTransactionAsync DoorJsonResponse(400,  "text/plain", responseBody = "No Body")
                    val result = json.decodeFromString(OneRosterResult.serializer(), bodyStr)
                    val studentResult = result.toStudentResult()

                    if(db.studentResultDao.sourcedUidExists(sourcedId)) {
                        DoorJsonResponse(
                            statusCode = 500,
                            contentType = "text/plain",
                        )
                    }else {
                        val blockUidAndClazzUid = db.courseBlockDao.findCourseBlockUidAndClazzUidBySourcedId(
                            result.lineItem.sourcedId, accountPersonUid
                        )

                        db.studentResultDao.insertListAsync(listOf(
                            studentResult.copy(
                                srCourseBlockUid = blockUidAndClazzUid?.cbUid ?: 0,
                                srClazzUid = blockUidAndClazzUid?.cbClazzUid ?: 0,
                            )
                        ))
                        DoorJsonResponse(
                            statusCode = 201,
                            contentType = "text/plain",
                        )
                    }
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