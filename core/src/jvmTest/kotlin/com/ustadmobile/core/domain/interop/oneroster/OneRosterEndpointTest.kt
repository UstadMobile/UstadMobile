package com.ustadmobile.core.domain.interop.oneroster

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.domain.interop.oneroster.model.Clazz
import com.ustadmobile.core.domain.interop.oneroster.model.GUIDRef
import com.ustadmobile.core.domain.interop.oneroster.model.GuidRefType
import com.ustadmobile.core.domain.interop.oneroster.model.LineItem
import com.ustadmobile.core.domain.interop.oneroster.model.Status
import com.ustadmobile.core.domain.interop.timestamp.format8601Timestamp
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.xxhash.XXStringHasherCommonJvm
import com.ustadmobile.core.domain.xxhash.toLongOrHash
import com.ustadmobile.core.util.isimplerequest.StringSimpleTextRequest
import com.ustadmobile.core.util.stringvalues.asIStringValues
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Clazz as ClazzEntity
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ExternalAppPermission
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.StudentResult
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.ustadmobile.core.domain.interop.oneroster.model.Result as OneRosterResult

class OneRosterEndpointTest {

    private lateinit var db: UmAppDatabase

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private lateinit var oneRosterEndpoint : OneRosterEndpoint

    private lateinit var accountPerson: Person

    private val learningSpace = LearningSpace("http://localhost:8087/")

    private val xxHasher = XXStringHasherCommonJvm()

    @BeforeTest
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, "jdbc:sqlite::memory:", nodeId = 1L
        ).build()
        oneRosterEndpoint = OneRosterEndpoint(db, null, learningSpace, xxHasher, json)
        runBlocking {
            accountPerson = Person().apply {
                firstNames = "One"
                lastName = "Roster"
                username = "oneroster"

            }
            accountPerson.personUid = AddNewPersonUseCase(db, null).invoke(accountPerson)
        }
    }

    private suspend fun createCourseAndEnrolPerson(
        clazz: ClazzEntity = ClazzEntity(
            clazzName = "Test clazz"
        ),
        role: Int = ClazzEnrolment.ROLE_STUDENT,
        personToEnrol: Person = accountPerson,
    ): Pair<ClazzEntity, ClazzEnrolment> {
        clazz.clazzUid = CreateNewClazzUseCase(db).invoke(clazz)
        val enrolment = ClazzEnrolment(
            clazzUid = clazz.clazzUid,
            personUid = personToEnrol.personUid,
            role = role,
        )
        EnrolIntoCourseUseCase(db, null).invoke(
            enrolment,
            timeZoneId = TimeZone.currentSystemDefault().id,
        )
        return Pair(clazz, enrolment)
    }

    private suspend fun createCourseBlock(
        clazzUid: Long,
        sourcedId: String? = null,
    ) : CourseBlock{
        val courseBlock = CourseBlock(
            cbUid = sourcedId?.let { xxHasher.toLongOrHash(sourcedId) }
                ?: db.doorPrimaryKeyManager.nextId(CourseBlock.TABLE_ID),
            cbClazzUid = clazzUid,
            cbSourcedId = sourcedId,
            cbType = CourseBlock.BLOCK_EXTERNAL_APP,
        )

        return courseBlock.copy(
            cbUid = db.courseBlockDao().insertAsync(courseBlock)
        )
    }

    private suspend fun grantExternalAppPermission() {
        db.externalAppPermissionDao().insertAsync(
            ExternalAppPermission(
                eapAuthToken = "test-token",
                eapPersonUid = accountPerson.personUid,
                eapExpireTime = UNSET_DISTANT_FUTURE
            )
        )
    }

    @Test
    fun givenValidAuth_whenRequestClassesForUser_thenShouldReturnClassList() {
        val httpEndpoint = OneRosterHttpServerUseCase(db, oneRosterEndpoint, json)
        runBlocking {
            val clazzAndEnrolment= createCourseAndEnrolPerson()
            grantExternalAppPermission()

            val response = httpEndpoint.invoke(
                StringSimpleTextRequest(
                    path = "/api/oneroster/users/${accountPerson.personUid}/classes",
                    body = null,
                    headers = mapOf("Authorization" to listOf("Bearer test-token")).asIStringValues()
                )
            )

            assertEquals(200, response.responseCode)
            val responseResults = json.decodeFromString(
                ListSerializer(Clazz.serializer()), response.responseBody!!)
            assertEquals(1, responseResults.size)
            assertEquals(clazzAndEnrolment.first.clazzName, responseResults.first().title)
        }
    }

    @Test
    fun givenValidAuth_whenRequestResultsForStudentClass_thenShouldReturnResults() {
        val httpEndpoint = OneRosterHttpServerUseCase(db, oneRosterEndpoint, json)
        runBlocking {
            val (clazz, _)= createCourseAndEnrolPerson()
            val courseBlock = createCourseBlock(clazz.clazzUid)
            grantExternalAppPermission()

            val studentResults = (0 .. 2).mapIndexed { index, score ->
                StudentResult(
                    srUid = index.toLong(),
                    srStudentPersonUid = accountPerson.personUid,
                    srClazzUid = clazz.clazzUid,
                    srCourseBlockUid =  courseBlock.cbUid,
                    srScore = score.toFloat()
                )
            }
            db.studentResultDao().insertListAsync(studentResults)

            val response = httpEndpoint.invoke(
                StringSimpleTextRequest(
                    path = "/api/oneroster/classes/${clazz.clazzUid}/students/${accountPerson.personUid}/results",
                    headers = mapOf("Authorization" to listOf("Bearer test-token")).asIStringValues(),
                )
            )

            assertEquals(200, response.responseCode)
            val responseResults = response.responseBody?.let {
                json.decodeFromString(ListSerializer(OneRosterResult.serializer()), it)
            }


            assertEquals(studentResults.size, responseResults!!.size)
            studentResults.forEach {studentResult ->
                assertTrue(responseResults.any { it.score == studentResult.srScore })
            }
        }
    }

    @Test
    fun givenLineItemExists_whenCallGetLineItemWithSourcedId_thenShouldReturn200() {
        val httpEndpoint = OneRosterHttpServerUseCase(db, oneRosterEndpoint, json)
        runBlocking {
            val (clazz, _)= createCourseAndEnrolPerson()
            val courseBlock = createCourseBlock(
                clazz.clazzUid, sourcedId = randomUuidAsString()
            )
            grantExternalAppPermission()

            val response = httpEndpoint.invoke(
                StringSimpleTextRequest(
                    path = "/api/oneroster/lineItems/${courseBlock.cbSourcedId}",
                    headers = mapOf("Authorization" to listOf("Bearer test-token")).asIStringValues(),
                )
            )

            assertEquals(200, response.responseCode)
            val lineItem = json.decodeFromString(LineItem.serializer(), response.responseBody!!)
            assertEquals(courseBlock.cbSourcedId, lineItem.sourcedId)
        }
    }

    @Test
    fun givenLineItemDoesNotExist_whenCallGetLineItem_thenShouldReturn404() {
        val httpEndpoint = OneRosterHttpServerUseCase(db, oneRosterEndpoint, json)
        runBlocking {
            createCourseAndEnrolPerson()
            grantExternalAppPermission()

            val response = httpEndpoint.invoke(
                StringSimpleTextRequest(
                    path = "/api/oneroster/lineItems/doesNotExist",
                    headers = mapOf("Authorization" to listOf("Bearer test-token")).asIStringValues(),
                )
            )

            assertEquals(404, response.responseCode)
        }
    }

    @Test
    fun givenValidLineItem_whenCallPutLineItem_thenShouldInsertAndReturn201() {
        val httpEndpoint = OneRosterHttpServerUseCase(db, oneRosterEndpoint, json)
        runBlocking {
            val (clazz, _) = createCourseAndEnrolPerson()
            grantExternalAppPermission()

            val newLineItem = LineItem(
                sourcedId = "${clazz.clazzUid}_lesson001",
                status = Status.ACTIVE,
                dateLastModified = format8601Timestamp(systemTimeInMillis()),
                title = "Lesson 001",
                description = "Lesson 001 result",
                assignDate = format8601Timestamp(0),
                dueDate = format8601Timestamp(systemTimeInMillis()),
                `class` = GUIDRef(
                    href = "http://localhost/",
                    sourcedId = clazz.clazzUid.toString(),
                    type = GuidRefType.clazz
                ),
                resultValueMin = 0.toFloat(),
                resultValueMax = 10.toFloat(),
            )

            val response = httpEndpoint.invoke(
                StringSimpleTextRequest(
                    method = "PUT",
                    headers = mapOf("Authorization" to listOf("Bearer test-token")).asIStringValues(),
                    path = "/api/oneroster/lineItems/${newLineItem.sourcedId}",
                    body = json.encodeToString(LineItem.serializer(), newLineItem)
                )
            )

            assertEquals(201, response.responseCode)
            val courseBlockInDb = db.courseBlockDao().findBySourcedId(
                newLineItem.sourcedId, accountPerson.personUid)
            assertEquals(newLineItem.sourcedId, courseBlockInDb?.cbSourcedId)
            assertEquals(clazz.clazzUid, courseBlockInDb?.cbClazzUid ?: -1)
        }
    }


    @Test
    fun givenValidResult_whenCallPutResult_thenShouldInsertAndReturn201() {
        val httpEndpoint = OneRosterHttpServerUseCase(db, oneRosterEndpoint, json)
        runBlocking {
            val (clazz, _)= createCourseAndEnrolPerson()
            val lineItemSourcedId = "${clazz.clazzUid}-Lesson001"
            createCourseBlock(
                clazz.clazzUid, sourcedId = lineItemSourcedId
            )
            grantExternalAppPermission()

            val oneRosterResult = OneRosterResult(
                sourcedId = randomUuidAsString(),
                status = Status.ACTIVE,
                dateLastModified = format8601Timestamp(systemTimeInMillis()),
                metaData = null,
                lineItem = GUIDRef(
                    sourcedId = lineItemSourcedId,
                    href = "http://localhost",
                    type = GuidRefType.lineItem
                ),
                student = GUIDRef(
                    sourcedId = accountPerson.personUid.toString(),
                    href = "http://localhost/",
                    type = GuidRefType.student
                ),
                score = 5.toFloat(),
                scoreDate = format8601Timestamp(systemTimeInMillis()),
                comment = "OK, not great, not terrible"
            )

            val response = httpEndpoint.invoke(
                StringSimpleTextRequest(
                    method = "PUT",
                    headers = mapOf("Authorization" to listOf("Bearer test-token")).asIStringValues(),
                    path = "/api/oneroster/results/${oneRosterResult.sourcedId}",
                    body = json.encodeToString(OneRosterResult.serializer(), oneRosterResult),
                )
            )

            assertEquals(201, response.responseCode,
                "Response code should be 201. Body=${response.responseBody}")
            val resultInDb = db.studentResultDao().findByClazzAndStudent(
                clazz.clazzUid, accountPerson.personUid, accountPerson.personUid
            )
            assertEquals(oneRosterResult.score, resultInDb.first().studentResult.srScore)
        }
    }

    @Test
    fun givenValidRawOneRosterLineItem_whenCallPutLineItem_thenShouldRespond201() {
        val httpEndpoint = OneRosterHttpServerUseCase(db, oneRosterEndpoint, json)
        runBlocking {
            val (clazz, _)= createCourseAndEnrolPerson()
            val lineItemSourcedId = "${clazz.clazzUid}-Lesson001"
            createCourseBlock(
                clazz.clazzUid, sourcedId = lineItemSourcedId
            )
            grantExternalAppPermission()

            val lineItemId = "puzzle0102-${clazz.clazzUid}"
            val lineItemJson = """
                {
                  "sourcedId": "$lineItemId",
                  "status": "active",
                  "dateLastModified": "2023-03-14T10:09:04.004Z",
                  "metadata": {},
                  "title": "puzzle0102",
                  "description": "puzzle0102",
                  "assignDate": "2023-03-14T10:09:04.004Z",
                  "dueDate": "2024-03-14T10:09:04.013Z",
                  "class": {
                    "href": "${clazz.clazzUid}",
                    "sourcedId": "${clazz.clazzUid}",
                    "type": "class"
                  },
                  "category": {
                    "href": "category",
                    "sourcedId": "category",
                    "type": "category"
                  },
                  "resultValueMin": 0,
                  "resultValueMax": 100
                }
            """

            val response = httpEndpoint(
                StringSimpleTextRequest(
                    method = "PUT",
                    headers = mapOf("Authorization" to listOf("Bearer test-token")).asIStringValues(),
                    path = "/api/oneroster/lineItems/$lineItemId",
                    body = lineItemJson
                )
            )
            assertEquals(201, response.responseCode)
        }
    }

}