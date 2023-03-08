package com.ustadmobile.core.api.oneroster

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.api.DoorJsonRequest
import com.ustadmobile.core.api.oneroster.OneRosterEndpoint.Companion.HEADER_AUTH
import com.ustadmobile.core.api.oneroster.model.Clazz
import com.ustadmobile.core.controller.TerminologyKeys
import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Clazz as ClazzEntity
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.kodein.di.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import kotlinx.serialization.builtins.ListSerializer
import kotlin.test.assertEquals
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ClazzEnrolment.Companion.ROLE_STUDENT
import io.ktor.http.*
import kotlin.test.assertTrue
import com.ustadmobile.core.api.oneroster.model.Result as OneRosterResult

class OneRosterEndpointTest {


    class DbTestBuilder(
        private val dbName: String = "dbtest"
    ) {
        val endpointScope = EndpointScope()

        /**
         * Temporary directory that can be used by a test. It will be deleted when the test is finished
         */
        val tempDir: File by lazy {
            Files.createTempDirectory("viewmodeltest").toFile()
        }

        val nodeIdAndAuth = NodeIdAndAuth(42L, "secret")

        val db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
                "jdbc:sqlite:build/tmp/$dbName.sqlite", tempDir.absolutePath)
            .addSyncCallback(nodeIdAndAuth)
            .addCallback(ContentJobItemTriggersCallback())
            .addMigrations(*migrationList().toTypedArray())
            .build()
            .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)


        val di = DI {
            bind<Json>() with singleton {
                Json {
                    encodeDefaults = true
                    ignoreUnknownKeys = true
                }
            }

            bind<UstadAccountManager>() with singleton {
                spy(UstadAccountManager(instance(), Any(), di))
            }


        }

        val accountManager: UstadAccountManager by di.instance()
    }


    class OneRosterTestBuilder(
        val dbTestBuilder: DbTestBuilder
    ) {
        val db: UmAppDatabase = dbTestBuilder.db

        //The user who is logged in (e.g. activesession)
        val user = runBlocking {
            db .insertPersonAndGroup (Person().apply {
                firstNames = "Bob"
                lastName = "Jones"
            })
        }

        val di = DI {
            extend(dbTestBuilder.di)

            bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(dbTestBuilder.endpointScope).singleton {
                db
            }
            bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(dbTestBuilder.endpointScope).singleton {
                db
            }
        }

        val json: Json by di.instance()

        val systemImpl: UstadMobileSystemImpl = mock {
            on { getString(any<Int>(), any())}.thenAnswer {
                it.arguments.first().toString()
            }
        }

        suspend fun grantExternalAppPermission(token: String) {
            db.externalAppPermissionDao.insertAsync(ExternalAppPermission(
                eapPersonUid = user.personUid,
                eapAuthToken = "token",
                eapExpireTime = Long.MAX_VALUE
            ))
        }

        suspend fun createClazzAndEnrolUser(
            userRole: Int
        ) : ClazzEntity {
            val clazz = ClazzEntity().apply {
                clazzUid = 1001L //when creating a class in the app, this is generated using primary key manager
                clazzName = "Test Course"
            }

            db.createNewClazzAndGroups(clazz, systemImpl, TERM_MAP, Any())
            db.enrolPersonIntoClazzAtLocalTimezone(user, clazz.clazzUid, userRole)

            return clazz
        }

    }


    fun withOneRosterTest(
        block: suspend OneRosterTestBuilder.() -> Unit
    ) {
        runBlocking {
            block(OneRosterTestBuilder(DbTestBuilder()))
        }
    }


    /**
     *
     */
    @Test
    fun givenValidAuth_whenRequestClassesForUser_thenShouldReturnClassList() = withOneRosterTest {
        createClazzAndEnrolUser(ROLE_STUDENT)
        grantExternalAppPermission("token")

        di.direct.on(Endpoint("http://localhost/")).instance<UmAppDatabase>(tag = DoorTag.TAG_DB)

        val oneRosterEndpoint = OneRosterEndpoint(
            { dbTestBuilder.endpointScope.activeEndpointSet }, di
        )

        val response = oneRosterEndpoint.serve(
            DoorJsonRequest(
                method = DoorJsonRequest.Method.GET,
                url = Url("http://localhost/api/oneroster/users/${user.personUid}/classes"),
                headers = mapOf(
                    HEADER_AUTH to "token"
                )
            )
        )

        assertEquals(200, response.statusCode)

        val clazzes = response.responseBody?.let {
            json.decodeFromString(ListSerializer(Clazz.serializer()), it)
        }

        assertEquals(1, clazzes?.size ?: 0, "Got clazz")
    }

    @Test
    fun givenValidAuth_whenRequestResultsForStudentClass_thenShouldReturnResults() = withOneRosterTest {
        val clazz = createClazzAndEnrolUser(ROLE_STUDENT)
        val courseBlock = CourseBlock().apply {
            cbClazzUid = clazz.clazzUid
            cbType = CourseBlock.BLOCK_EXTERNAL_APP

            cbUid = db.courseBlockDao.insertAsync(this)
        }

        val studentResults = (0 .. 2).map { score ->
            StudentResult(
                srStudentPersonUid = user.personUid,
                srClazzUid = clazz.clazzUid,
                srCourseBlockUid =  courseBlock.cbUid,
                srScore = score.toFloat()
            )
        }
        db.studentResultDao.insertListAsync(studentResults)
        grantExternalAppPermission("token")

        di.direct.on(Endpoint("http://localhost/")).instance<UmAppDatabase>(tag = DoorTag.TAG_DB)

        val oneRosterEndpoint = OneRosterEndpoint(
            { dbTestBuilder.endpointScope.activeEndpointSet }, di
        )

        val response = oneRosterEndpoint.serve(
            DoorJsonRequest(
                method = DoorJsonRequest.Method.GET,
                url = Url("http://localhost/api/oneroster/classes/${clazz.clazzUid}/students/${user.personUid}/results"),
                headers = mapOf(
                    HEADER_AUTH to "token"
                )
            )
        )

        assertEquals(200, response.statusCode)

        val responseResults = response.responseBody?.let {
            json.decodeFromString(ListSerializer(OneRosterResult.serializer()), it)
        }


        assertEquals(studentResults.size, responseResults?.size)
        studentResults.forEach {studentResult ->
            assertTrue(responseResults?.any { it.score == studentResult.srScore } == true)
        }
    }


    companion object {

        val TERM_MAP = mapOf(
            TerminologyKeys.TEACHER_KEY to "Teacher",
            TerminologyKeys.STUDENTS_KEY to "Student"
        )

    }
}