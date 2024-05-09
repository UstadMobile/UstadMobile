package com.ustadmobile.core.domain.interop.oneroster

import com.ustadmobile.core.db.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.domain.interop.oneroster.model.Clazz
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.util.isimplerequest.StringSimpleTextRequest
import com.ustadmobile.core.util.stringvalues.asIStringValues
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.Clazz as ClazzEntity
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ExternalAppPermission
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OneRosterEndpointTest {

    private lateinit var db: UmAppDatabase

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private lateinit var oneRosterEndpoint : OneRosterEndpoint

    private lateinit var accountPerson: Person

    @BeforeTest
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, "jdbc:sqlite::memory:", nodeId = 1L
        ).build()
        oneRosterEndpoint = OneRosterEndpoint(db, null)
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

    private suspend fun createCourseBlock(clazzUid: Long) {
        db.courseBlockDao.insertAsync(
            CourseBlock(

            )
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

    fun givenValidAuth_whenRequestResultsForStudentClass_thenShouldReturnResults() {
        val httpEndpoint = OneRosterHttpServerUseCase(db, oneRosterEndpoint, json)
        runBlocking {
            val clazzAndEnrolment= createCourseAndEnrolPerson()
        }
    }

}