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
import com.ustadmobile.lib.db.entities.ExternalAppPermission
import com.ustadmobile.lib.db.entities.Person
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
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import kotlinx.serialization.builtins.ListSerializer
import kotlin.test.assertEquals
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import io.ktor.http.*

class OneRosterEndpointTest {


    class DbTestBuilder(
        private val dbName: String = "dbtest"
    ) {
        private val endpointScope = EndpointScope()

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
        private val dbTestBuilder: DbTestBuilder
    ) {
        val db: UmAppDatabase = dbTestBuilder.db

        val user = runBlocking {
            db .insertPersonAndGroup (Person().apply {
                firstNames = "Bob"
                lastName = "Jones"
            })
        }

        val di = DI {
            extend(dbTestBuilder.di)
        }

        val json: Json by di.instance()

        val systemImpl: UstadMobileSystemImpl = mock {
            on { getString(any<Int>(), any())}.thenAnswer {
                it.arguments.first().toString()
            }
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
        val clazz = ClazzEntity().apply {
            clazzUid = 1001L //when creating a class in the app, this is generated using primary key manager
            clazzName = "Test Course"
        }

        db.createNewClazzAndGroups(clazz, systemImpl, TERM_MAP, Any())

        db.enrolPersonIntoClazzAtLocalTimezone(user, clazz.clazzUid, ClazzEnrolment.ROLE_STUDENT)

        db.externalAppPermissionDao.insertAsync(ExternalAppPermission(
            eapPersonUid = user.personUid,
            eapAuthToken = "token",
            eapExpireTime = Long.MAX_VALUE
        ))

        val endpointScope  = EndpointScope()
        val di = DI {
            bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
                db
            }
            bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope).singleton {
                db
            }
            bind<Json>() with singleton {
                json
            }
        }

        di.direct.on(Endpoint("http://localhost/")).instance<UmAppDatabase>(tag = DoorTag.TAG_DB)

        val endpoint = OneRosterEndpoint(
            { endpointScope.activeEndpointSet }, di
        )

        val response = endpoint.serve(
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

    companion object {

        val TERM_MAP = mapOf(
            TerminologyKeys.TEACHER_KEY to "Teacher",
            TerminologyKeys.STUDENTS_KEY to "Student"
        )

    }
}