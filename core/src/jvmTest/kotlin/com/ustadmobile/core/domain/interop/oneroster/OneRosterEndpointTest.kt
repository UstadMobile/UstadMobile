package com.ustadmobile.core.domain.interop.oneroster

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.domain.interop.oneroster.model.Clazz
import com.ustadmobile.core.domain.interop.oneroster.model.LineItem
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.util.isimplerequest.StringSimpleTextRequest
import com.ustadmobile.core.util.stringvalues.asIStringValues
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.door.DatabaseBuilder
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

    private val endpoint = Endpoint("http://localhost:8087/")

    @BeforeTest
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, "jdbc:sqlite::memory:", nodeId = 1L
        ).build()
        oneRosterEndpoint = OneRosterEndpoint(db, null, endpoint)
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
            cbClazzUid = clazzUid,
            cbSourcedId = sourcedId,
            cbType = CourseBlock.BLOCK_EXTERNAL_APP,
        )

        return courseBlock.copy(
            cbUid = db.courseBlockDao.insertAsync(courseBlock)
        )
    }

    private suspend fun grantExternalAppPermission() {
        db.externalAppPermissionDao.insertAsync(
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
                ListSerializer(Clazz.serializer()), response.bodyText)
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

            val studentResults = (0 .. 2).map { score ->
                StudentResult(
                    srStudentPersonUid = accountPerson.personUid,
                    srClazzUid = clazz.clazzUid,
                    srCourseBlockUid =  courseBlock.cbUid,
                    srScore = score.toFloat()
                )
            }
            db.studentResultDao.insertListAsync(studentResults)

            val response = httpEndpoint.invoke(
                StringSimpleTextRequest(
                    path = "/api/oneroster/classes/${clazz.clazzUid}/students/${accountPerson.personUid}/results",
                    headers = mapOf("Authorization" to listOf("Bearer test-token")).asIStringValues(),
                )
            )

            assertEquals(200, response.responseCode)
            val responseResults = response.bodyText.let {
                json.decodeFromString(ListSerializer(OneRosterResult.serializer()), it)
            }


            assertEquals(studentResults.size, responseResults.size)
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
            val lineItem = json.decodeFromString(LineItem.serializer(), response.bodyText)
            assertEquals(courseBlock.cbSourcedId, lineItem.sourcedId)
        }
    }

}