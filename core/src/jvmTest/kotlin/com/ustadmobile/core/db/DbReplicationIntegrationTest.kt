package com.ustadmobile.core.db

import com.ustadmobile.core.account.*
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.core.util.test.waitUntilAsync
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.*
import com.ustadmobile.door.util.NodeIdAuthCache
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.rest.ext.initAdminUser
import com.ustadmobile.lib.rest.ext.ktorInitRepo
import com.ustadmobile.lib.util.randomString
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.Rule
import org.junit.Assert
import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import org.junit.rules.TemporaryFolder
import org.kodein.di.ktor.DIFeature
import java.io.File
import kotlin.random.Random
import javax.naming.InitialContext


class DbReplicationIntegrationTest {

    private lateinit var remoteServer: ApplicationEngine

    private lateinit var remoteDi: DI

    private val remoteDb: UmAppDatabase
        get() = remoteDi.on(Endpoint("localhost")).direct.instance(tag = DoorTag.TAG_DB)

    private val localDb1: UmAppDatabase
        get() = localDi1.on(Endpoint(TEST_SERVER_HOST)).direct.instance(tag = DoorTag.TAG_DB)

    private val localDbRepo1: UmAppDatabase
        get() = localDi1.on(Endpoint(TEST_SERVER_HOST)).direct.instance(tag = DoorTag.TAG_REPO)

    private lateinit var remoteVirtualHostScope: EndpointScope

    private lateinit var httpClient: HttpClient

    private lateinit var okHttpClient: OkHttpClient

    private lateinit var jsonSerializer: Json

    private lateinit var localDi1: DI

    private lateinit var localDi2: DI

    private val localDb2: UmAppDatabase
        get() = localDi2.on(Endpoint(TEST_SERVER_HOST)).direct.instance(tag = DoorTag.TAG_DB)

    private val localDbRepo2: UmAppDatabase
        get() = localDi2.on(Endpoint(TEST_SERVER_HOST)).direct.instance(tag = DoorTag.TAG_REPO)

    @Rule
    @JvmField
    var tempFolder = TemporaryFolder()

    //We can't use the normal UstadTestRule because we need multiple client databases for the same
    // endpoint.
    private fun DI.MainBuilder.bindDbAndRelated(
        dbName: String,
        //"Client" mode means use the replication subscription
        clientMode: Boolean
    ) {
        bind<UstadMobileSystemImpl>() with singleton {
            UstadMobileSystemImpl(instance(tag  = DiTag.XPP_FACTORY_NSAWARE),
                tempFolder.newFolder())
        }

        bind<UstadAccountManager>() with singleton {
            UstadAccountManager(instance(), Any(), di)
        }

        bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
            XmlPullParserFactory.newInstance().also {
                it.isNamespaceAware = true
            }
        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(remoteVirtualHostScope).singleton {
            val nodeIdAndAuth: NodeIdAndAuth = instance()
            InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
            DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, dbName)
                .addSyncCallback(nodeIdAndAuth)
                .addCallback(ContentJobItemTriggersCallback())
                .build().also { db ->
                    db.clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
                    db.addIncomingReplicationListener(RepIncomingListener(db))
                }
        }

        bind<HttpClient>() with singleton {
            httpClient
        }

        bind<OkHttpClient>() with singleton {
            okHttpClient
        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(remoteVirtualHostScope).singleton {
            val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
            val doorNode = instance<NodeIdAndAuth>()
            val endpoint = if(clientMode) TEST_SERVER_ENDPOINT else "http://localhost/"
            val repo: UmAppDatabase = db.asRepository(RepositoryConfig.repositoryConfig(
                Any(), endpoint,
                doorNode.nodeId, doorNode.auth, instance(), instance()
            ) {
                useReplicationSubscription = clientMode
                if(clientMode)
                    replicationSubscriptionInitListener = RepSubscriptionInitListener()

                attachmentsDir = tempFolder.newFolder().absolutePath
            }).also {
                if(clientMode) {
                    it.siteDao.insert(Site().apply {
                        siteName = "Test"
                        authSalt = randomString(16)
                    })
                }
            }

            repo
        }

        bind<NodeIdAndAuth>() with scoped(remoteVirtualHostScope).singleton {
            NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), "secret")
        }

        bind<NodeIdAuthCache>() with scoped(remoteVirtualHostScope).singleton {
            instance<UmAppDatabase>(tag = DoorTag.TAG_DB).nodeIdAuthCache
        }

        bind<File>(tag = DiTag.TAG_CONTEXT_DATA_ROOT) with scoped(remoteVirtualHostScope).singleton {
            tempFolder.newFolder("contextroot")
        }

        bind<Pbkdf2Params>() with singleton {
            val systemImpl: UstadMobileSystemImpl = instance()
            val numIterations = systemImpl.getAppConfigInt(
                AppConfig.KEY_PBKDF2_ITERATIONS,
                UstadMobileConstants.PBKDF2_ITERATIONS, context)
            val keyLength = systemImpl.getAppConfigInt(
                AppConfig.KEY_PBKDF2_KEYLENGTH,
                UstadMobileConstants.PBKDF2_KEYLENGTH, context)

            Pbkdf2Params(numIterations, keyLength)
        }

        bind<AuthManager>() with scoped(EndpointScope.Default).singleton {
            AuthManager(context, di)
        }
    }

    @Before
    fun setup() {
        Napier.takeLogarithm()
        Napier.base(DebugAntilog())

        okHttpClient = OkHttpClient.Builder().build()

        httpClient = HttpClient(OkHttp) {
            install(JsonFeature)
            engine {
                preconfigured = okHttpClient
            }
        }

        remoteVirtualHostScope = EndpointScope()

        remoteDi = DI {
            bindDbAndRelated("UmAppDatabase", false)

            registerContextTranslator { _: ApplicationCall -> Endpoint("localhost") }
        }

        val remoteRepo: UmAppDatabase = remoteDi.direct.on(Endpoint("localhost"))
            .instance(tag = DoorTag.TAG_REPO)
        remoteRepo.preload()
        remoteRepo.ktorInitRepo()
        runBlocking {
            remoteRepo.initAdminUser(Endpoint("localhost"), remoteDi)
        }


        localDi1 = DI {
            bindDbAndRelated("local1", true)

            registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }
        }

        localDi2 = DI {
            bindDbAndRelated("local2", true)

            registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }
        }

        jsonSerializer = Json {
            encodeDefaults = true
        }



        remoteServer = embeddedServer(Netty, 8089, configure = {
            requestReadTimeoutSeconds = 600
            responseWriteTimeoutSeconds = 600
        }) {
            install(DIFeature){
                extend(remoteDi)
            }

            routing {
                route("UmAppDatabase") {
                    UmAppDatabase_KtorRoute()
                }
            }
        }
        remoteServer.start()
    }

    @After
    fun stop() {
        remoteServer.stop(1000, 1000)
    }

    @Test
    fun givenUserSessionCreated_whenContentEntryAdded_thenShouldReplicate() {
        //create a local session
        val contentEntry = ContentEntry().apply {
            title = "Hello World"
            contentEntryUid = remoteDb.contentEntryDao.insert(this)
        }

        val accountManager: UstadAccountManager by localDi1.instance()
        val adminPerson = remoteDb.personDao.findByUsername("admin") !!

        //put the person who just "logged in" in the local database
        localDb1.personDao.insert(adminPerson)

        runBlocking {
            accountManager.addSession(adminPerson, TEST_SERVER_HOST, "secret")
        }

        //wait for contententry to land...
        runBlocking {
            localDb1.waitUntil(10001, listOf("ContentEntry")) {
                localDb1.contentEntryDao.findByUid(contentEntry.contentEntryUid) != null
            }
        }

        //now create a second one
        val contentEntry2 = ContentEntry().apply {
            title = "Hello World 2"
            contentEntryUid = remoteDb.contentEntryDao.insert(this)
        }

        runBlocking {
            localDb1.waitUntil(10002, listOf("ContentEntry")) {
                localDb1.contentEntryDao.findByUid(contentEntry2.contentEntryUid) != null
            }
        }


        Assert.assertNotNull(localDb1.contentEntryDao.findByUid(contentEntry.contentEntryUid))
        Assert.assertNotNull(localDb1.contentEntryDao.findByUid(contentEntry2.contentEntryUid))
    }

    @Test
    fun givenUserSessionCreated_whenConnectedAndClazzCreated_thenScopedGrantShouldReplicateAndClazzShouldReplicate() {
        val accountManager: UstadAccountManager by localDi1.instance()
        val adminPerson = remoteDb.personDao.findByUsername("admin") !!

        //put the person who just "logged in" in the local database
        localDb1.personDao.insert(adminPerson)

        runBlocking {
            accountManager.addSession(adminPerson, TEST_SERVER_HOST, "secret")
        }

        //wait for contententry to land...
        runBlocking {
            localDb1.waitUntilAsync(10003, listOf("ScopedGrant")) {
                localDb1.scopedGrantDao.findByTableIdAndEntityUid(ScopedGrant.ALL_TABLES,
                    ScopedGrant.ALL_ENTITIES).firstOrNull { it.scopedGrant?.sgGroupUid == adminPerson.personGroupUid } != null
            }
        }

        val replicatedScopedGrant = runBlocking {
            localDb1.scopedGrantDao.findByTableIdAndEntityUid(ScopedGrant.ALL_TABLES,
                ScopedGrant.ALL_ENTITIES).firstOrNull { it.scopedGrant?.sgGroupUid == adminPerson.personGroupUid }
        }

        Assert.assertNotNull(replicatedScopedGrant)

        val clazz = Clazz().apply {
            this.clazzName = "Test class"
            clazzUid = remoteDb.clazzDao.insert(this)
        }

        runBlocking {
            localDb1.waitUntil(10004, listOf("Clazz")) {
                localDb1.clazzDao.findByUid(clazz.clazzUid) != null
            }
        }

        Assert.assertNotNull(localDb1.clazzDao.findByUid(clazz.clazzUid))

    }

    @Test
    fun givenClazzCreatedOnServer_whenUserWithScopedPermissionConnected_thenShouldReplicateClazzWithPermissionAndNotOthers() {
        val localAccountManager: UstadAccountManager by localDi1.instance()

        //create the new user
        val teacherPerson = runBlocking {
            remoteDb.insertPersonAndGroup(Person().apply {
                firstNames = "Teacher"
                lastName = "Teacher"
                username = "teacher"
            })
        }

        val newClazz = Clazz().apply {
            clazzName = "Test clazz"
            clazzUid = remoteDb.clazzDao.insert(this)
        }

        val anotherClazz = Clazz().apply {
            clazzName = "Other clazz"
            clazzUid = remoteDb.clazzDao.insert(this)
        }

        runBlocking {
            remoteDb.grantScopedPermission(teacherPerson,
                Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT, Clazz.TABLE_ID, newClazz.clazzUid)
        }

        localDb1.personDao.insert(teacherPerson)
        runBlocking {
            localAccountManager.addSession(teacherPerson, TEST_SERVER_HOST, "secret")
        }

        runBlocking {
            localDb1.waitUntil(10006, listOf("Clazz")) {
                localDb1.clazzDao.findByUid(newClazz.clazzUid) != null
            }
        }

        Assert.assertNotNull(localDb1.clazzDao.findByUid(newClazz.clazzUid))

        //make sure that the other class does not come
        Thread.sleep(1000)
        Assert.assertNull(localDb1.clazzDao.findByUid(anotherClazz.clazzUid))
    }

    @Test
    fun givenUserLoggedInWithPermissionOnClazz_whenNewPersonEnroledInClazz_thenShouldReplicateRelatedEntities() {
        val localAccountManager: UstadAccountManager by localDi1.instance()

        //create the new user
        val teacherPerson = runBlocking {
            remoteDb.insertPersonAndGroup(Person().apply {
                firstNames = "Teacher"
                lastName = "Teacher"
                username = "teacher"
            })
        }

        val studentPerson = runBlocking {
            remoteDb.insertPersonAndGroup(Person().apply {
                firstNames = "Student"
                lastName = "student"
                username = "student"
            })
        }

        val newClazz = Clazz().apply {
            clazzName = "Test clazz"
            clazzUid = remoteDb.clazzDao.insert(this)
        }

        runBlocking {
            remoteDb.grantScopedPermission(teacherPerson,
                Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT, Clazz.TABLE_ID, newClazz.clazzUid)
        }

        localDb1.personDao.insert(teacherPerson)
        runBlocking {
            localAccountManager.addSession(teacherPerson, TEST_SERVER_HOST, "secret")
        }

        runBlocking {
            localDb1.waitUntil(10007, listOf("Clazz")) {
                localDb1.clazzDao.findByUid(newClazz.clazzUid) != null
            }
        }

        runBlocking {
            remoteDb.enrolPersonIntoClazzAtLocalTimezone(studentPerson, newClazz.clazzUid,
                ClazzEnrolment.ROLE_STUDENT)
            val localDbNodeId = (localDbRepo1 as DoorDatabaseRepository).config.nodeId
            remoteDb.replicationNotificationDispatcher.onNewDoorNode(localDbNodeId, "")

            localDb1.waitUntil(10008, listOf("Person")) {
                localDb1.personDao.findByUsername(studentPerson.username) != null
            }
        }
    }

    @Test
    fun givenEmptyDatabase_whenNewClazzCreated_whenReplicatedThenAllClazzGroupsShouldReplicate() {
        val accountManager: UstadAccountManager by localDi1.instance()
        val adminPerson = remoteDb.personDao.findByUsername("admin") !!

        //put the person who just "logged in" in the local database
        localDb1.personDao.insert(adminPerson)

        runBlocking {
            accountManager.addSession(adminPerson, TEST_SERVER_HOST, "secret")
        }

        //wait for admin scopedgrant to land...
        runBlocking {
            localDb1.waitUntilAsync(10003, listOf("ScopedGrant")) {
                localDb1.scopedGrantDao.findByTableIdAndEntityUid(ScopedGrant.ALL_TABLES,
                    ScopedGrant.ALL_ENTITIES).firstOrNull { it.scopedGrant?.sgGroupUid == adminPerson.personGroupUid } != null
            }
        }

        val newClazz = Clazz().apply {
            clazzName = "Test Class"
            clazzStartTime = systemTimeInMillis()
        }

        runBlocking {
            localDb1.createNewClazzAndGroups(newClazz, localDi1.direct.instance(), Any())
            localDb1.scopedGrantDao.insertAsync(ScopedGrant().apply {
                sgFlags = ScopedGrant.FLAG_TEACHER_GROUP.or(ScopedGrant.FLAG_NO_DELETE)
                sgPermissions = Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT
                sgEntityUid = newClazz.clazzUid
                sgTableId = Clazz.TABLE_ID
                sgGroupUid = newClazz.clazzTeachersPersonGroupUid
            })
        }

        runBlocking {
            remoteDb.waitUntil(10018, listOf("Clazz")) {
                remoteDb.clazzDao.findByUid(newClazz.clazzUid) != null
            }
        }

        val scopedGrantsOnLocal = runBlocking {
            localDb1.scopedGrantDao.findByTableIdAndEntityUid(Clazz.TABLE_ID,
                newClazz.clazzUid)
        }

        runBlocking {
            remoteDb.waitUntilAsync(10021, listOf("ScopedGrant")) {
                val found = remoteDb.scopedGrantDao.findByTableIdAndEntityIdSync(Clazz.TABLE_ID,
                    newClazz.clazzUid)
                println("Found # ${found.size}")
                found.size == scopedGrantsOnLocal.size
            }
        }

        runBlocking {
            remoteDb.waitUntil(10019, listOf("PersonGroup")) {
                remoteDb.personGroupDao.findByUid(newClazz.clazzTeachersPersonGroupUid) != null
            }
        }

        Assert.assertNotNull(remoteDb.personGroupDao
            .findByUid(newClazz.clazzTeachersPersonGroupUid))
    }

    @Test
    fun givenClazzCreatedOnRemote1ByAdmin_whenTeacherStartsSessionOnLocal2_thenShouldReplicateAllRelatedEntities() {
        val accountManager1: UstadAccountManager by localDi1.instance()
        val accountManager2: UstadAccountManager by localDi2.instance()

        val adminPerson = remoteDb.personDao.findByUsername("admin") !!

        //put the person who just "logged in" in the local database
        localDb1.personDao.insert(adminPerson)

        runBlocking {
            accountManager1.addSession(adminPerson, TEST_SERVER_HOST, "secret")
        }

        val teacherPerson = runBlocking {
            localDb1.insertPersonAndGroup(Person().apply {
               firstNames = "Edna"
                lastName = "K"
                username = "edna"
            })
        }

        val newClazz = Clazz().apply {
            clazzName = "Test Clazz"
        }


        runBlocking {
            localDb1.createNewClazzAndGroups(newClazz, localDi1.direct.instance(), Any())

            //Create scopedgrants for groups (that would otherwisee be handled by clazzeditpresenter)
            localDb1.grantScopedPermission(newClazz.clazzTeachersPersonGroupUid,
                Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT, Clazz.TABLE_ID, newClazz.clazzUid)
            localDb1.grantScopedPermission(newClazz.clazzStudentsPersonGroupUid,
                Role.ROLE_CLAZZ_STUDENT_PERMISSIONS_DEFAULT, Clazz.TABLE_ID, newClazz.clazzUid)
        }

        runBlocking {
            localDb1.enrolPersonIntoClazzAtLocalTimezone(teacherPerson, newClazz.clazzUid,
                ClazzEnrolment.ROLE_TEACHER)
        }

        //wait for the new entities to hit the server
        runBlocking {
            remoteDb.waitUntilAsync(10042, listOf("Clazz")) {
                remoteDb.clazzDao.findByUid(newClazz.clazzUid) != null
            }
        }

        runBlocking {
            Assert.assertNull("Teacher not on local2 until login",
                localDb2.personDao.findByUid(teacherPerson.personUid))
        }


        //Now login as teacher on local2 as would happen when they login
        localDb2.personDao.insert(teacherPerson)

        runBlocking {
            accountManager2.addSession(teacherPerson, TEST_SERVER_HOST, "secret2")
        }

        val teacherInDb2AfterAddSession = runBlocking { localDb2.personDao.findByUid(teacherPerson.personUid) }
        Assert.assertNotNull(teacherInDb2AfterAddSession)


        runBlocking {
            localDb2.waitUntil(10043, listOf("Clazz")) {
                localDb2.clazzDao.findByUid(newClazz.clazzUid) != null
            }
        }

        Assert.assertEquals("Got clazz into localdb2",
            localDb1.clazzDao.findByUid(newClazz.clazzUid),
            localDb2.clazzDao.findByUid(newClazz.clazzUid))
    }

    companion object {

        const val TEST_SERVER_HOST = "http://localhost:8089/"

        const val TEST_SERVER_ENDPOINT = "${TEST_SERVER_HOST}UmAppDatabase/"

    }

}