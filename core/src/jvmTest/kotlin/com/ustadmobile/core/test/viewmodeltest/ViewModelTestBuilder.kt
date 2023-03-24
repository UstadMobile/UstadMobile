package com.ustadmobile.core.test.viewmodeltest

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.di.CommonJvmDiModule
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.isLazyInitialized
import com.ustadmobile.core.viewmodel.ViewModel
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.util.test.nav.TestUstadSavedStateHandle
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockWebServer
import org.kodein.di.*
import org.mockito.kotlin.spy
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.nio.file.Files
import kotlin.random.Random
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.util.test.nav.TestUstadNavController
import org.mockito.kotlin.mock


typealias TestViewModelFactory<T> = ViewModelTestBuilder<T>.() -> T

class ViewModelTestBuilder<T: ViewModel> internal constructor(
    private val repoConfig: TestRepoConfig,
) {

    val savedStateHandle = TestUstadSavedStateHandle()

    private lateinit var viewModelFactoryVar: TestViewModelFactory<T>

    private val endpointScope = EndpointScope()

    private val testStartTime = systemTimeInMillis()

    private val xppFactory: XmlPullParserFactory by lazy {
        XmlPullParserFactory.newInstance().also {
            it.isNamespaceAware = true
        }
    }

    /**
     * Temporary directory that can be used by a test. It will be deleted when the test is finished
     */
    val tempDir: File by lazy {
        Files.createTempDirectory("viewmodeltest").toFile()
    }

    /**
     * MockWebServer that can be used by test. If accessed, it will be shutdown
     */
    val mockWebServer: MockWebServer by lazy {
        MockWebServer()
    }

    /**
     * Shorthand to get the active endpoint
     */
    val activeEndpoint: Endpoint
        get() = di.direct.instance<UstadAccountManager>().activeEndpoint

    val accountManager: UstadAccountManager
        get() = di.direct.instance()

    val activeDb: UmAppDatabase
        get() = di.direct.on(activeEndpoint).instance(tag= DoorTag.TAG_DB)

    val activeRepo: UmAppDatabase
        get() = di.direct.on(activeEndpoint).instance(tag = DoorTag.TAG_REPO)

    private var diVar = DI {
        import(CommonJvmDiModule)

        bind<Json>() with singleton {
            Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }
        }

        bind<UstadAccountManager>() with singleton {
            spy(UstadAccountManager(instance(), Any(), di))
        }

        bind<UstadMobileSystemImpl>() with singleton {
            spy(UstadMobileSystemImpl(xppFactory, tempDir))
        }

        bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
            NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())
        }

        bind<Pbkdf2Params>() with singleton {
            Pbkdf2Params(iterations = 10000, keyLength = 512)
        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
            val sanitizedName = sanitizeDbNameFromUrl(context.url)
            val dbUrl = "jdbc:sqlite:build/tmp/$sanitizedName.sqlite"
            val attachmentsDir = File(tempDir, "attachments-$testStartTime")
            val nodeIdAndAuth: NodeIdAndAuth = instance()
            spy(
                DatabaseBuilder.databaseBuilder(UmAppDatabase::class, dbUrl,
                    attachmentsDir.absolutePath)
                .addSyncCallback(nodeIdAndAuth)
                .addCallback(ContentJobItemTriggersCallback())
                .addMigrations(*migrationList().toTypedArray())
                .build()
                .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId).also {
                    it.siteDao.insert(Site().apply {
                        siteUid = 1L
                        siteName = "My Site"
                        guestLogin = false
                        registrationAllowed = false
                        authSalt = randomString(20)
                    })
                }
            )
        }

        if(repoConfig.useDbAsRepo) {
            bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope).singleton {
                instance(tag = DoorTag.TAG_DB)
            }
        }

        bind<SnackBarDispatcher>() with singleton {
            mock { }
        }

        bind<UstadNavController>() with singleton {
            spy(TestUstadNavController())
        }
    }

    init {

    }

    @ViewModelDslMarker
    fun extendDi(
        block: DI.MainBuilder.() -> Unit,
    ) {
        val extendedDi = DI {
            extend(diVar)
            block()
        }
        diVar = extendedDi
    }

    val di: DI
        get() = diVar

    val viewModel: T by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        viewModelFactoryVar()
    }

    @ViewModelDslMarker
    fun viewModelFactory(
        block: TestViewModelFactory<T>
    ) {
        viewModelFactoryVar = block
    }

    suspend fun <T> stateInViewModelScope(flow: Flow<T>): StateFlow<T> {
        return flow.stateIn(viewModel.viewModelScope)
    }

    /**
     * Sets the active session
     */
    suspend fun setActiveUser(
        endpoint: Endpoint,
        person: Person = Person().apply {
            firstNames = "Test"
            lastName = "User"
            username = "testuser"
        }
    ): Person {
        val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)

        val accountManager = di.direct.instance<UstadAccountManager>()

        db.withDoorTransactionAsync {
            val personInDb = db.insertPersonAndGroup(person)
            val session = accountManager.addSession(personInDb, endpoint.url, "dummypassword")
            accountManager.activeSession = session
        }

        return person
    }


    internal fun cleanup() {
        try {
            viewModel.viewModelScope.cancel()
        }catch(e: Exception) {
            e.printStackTrace()
        }

        if(this::tempDir.isLazyInitialized) {
            tempDir.deleteRecursively()
            tempDir.deleteOnExit()
        }

        if(this::mockWebServer.isLazyInitialized) {
            mockWebServer.shutdown()
        }
        endpointScope.activeEndpointUrls.forEach {

        }

    }

}