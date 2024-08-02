package com.ustadmobile.core.test.viewmodeltest

import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.xapi.XapiJson
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.domain.xxhash.XXStringHasherCommonJvm
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.config.GenderConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.di.CommonJvmDiModule
import com.ustadmobile.core.impl.nav.NavResultReturner
import com.ustadmobile.core.impl.nav.NavResultReturnerImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.isLazyInitialized
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.util.test.nav.TestUstadSavedStateHandle
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockWebServer
import org.kodein.di.*
import org.mockito.kotlin.spy
import java.io.File
import java.nio.file.Files
import kotlin.random.Random
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.util.test.nav.TestUstadNavController
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import org.mockito.kotlin.mock
import java.util.Properties



typealias TestViewModelFactory<T> = ViewModelTestBuilder<T>.() -> T

@OptIn(ExperimentalXmlUtilApi::class)
class ViewModelTestBuilder<T: ViewModel> internal constructor(
    private val repoConfig: TestRepoConfig,
) {

    val savedStateHandle = TestUstadSavedStateHandle()

    private lateinit var viewModelFactoryVar: TestViewModelFactory<T>

    val endpointScope = EndpointScope()

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

    val systemImpl: UstadMobileSystemImpl
        get() = di.direct.instance()

    val navResultReturner: NavResultReturner
        get() = di.direct.instance()

    val json: Json
        get() = di.direct.instance()

    private val dbsToClose = mutableListOf<UmAppDatabase>()

    @ExperimentalXmlUtilApi
    private var diVar = DI {
        import(CommonJvmDiModule)

        bind<Json>() with singleton {
            Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }
        }

        bind<XapiJson>() with singleton { XapiJson() }

        bind<ApiUrlConfig>() with singleton {
            ApiUrlConfig(presetApiUrl = null)
        }

        bind<UstadAccountManager>() with singleton {
            spy(UstadAccountManager(instance(), di))
        }

        bind<Settings>() with singleton {
            PropertiesSettings(
                delegate = Properties(),
                onModify = {
                    //Do nothing
                }
            )
        }

        bind<SupportedLanguagesConfig>() with singleton {
            SupportedLanguagesConfig(
                systemLocales = listOf("en-US"),
                settings = instance(),
            )
        }

        bind<UstadMobileSystemImpl>() with singleton {
            spy(
                UstadMobileSystemImpl(
                    settings = instance(),
                    langConfig = instance(),
                )
            )
        }

        bind<XML>() with singleton {
            XML {
                defaultPolicy {
                    unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
                }
            }
        }

        bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
            NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())
        }

        bind<Pbkdf2Params>() with singleton {
            Pbkdf2Params(iterations = 10000, keyLength = 512)
        }

        bind<AuthManager>() with scoped(endpointScope).singleton {
            AuthManager(context, di)
        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
            val dbUrl = "jdbc:sqlite::memory:"
            val nodeIdAndAuth: NodeIdAndAuth = instance()
            spy(
                DatabaseBuilder.databaseBuilder(UmAppDatabase::class, dbUrl,
                    nodeId = nodeIdAndAuth.nodeId)
                .addSyncCallback(nodeIdAndAuth)
                .addMigrations(*migrationList().toTypedArray())
                .build()
                .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId).also {
                    it.siteDao().insert(Site().apply {
                        siteUid = 1L
                        siteName = "My Site"
                        guestLogin = false
                        registrationAllowed = false
                        authSalt = randomString(20)
                    })
                }
            ).also {
                dbsToClose.add(it)
            }
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

        bind<NavResultReturner>() with singleton {
            spy(NavResultReturnerImpl())
        }

        bind<GenderConfig>() with singleton {
            GenderConfig()
        }

        bind<XXStringHasher>() with singleton {
            XXStringHasherCommonJvm()
        }

        bind<AddNewPersonUseCase>() with scoped(endpointScope).singleton {
            AddNewPersonUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance(tag = DoorTag.TAG_REPO),
            )
        }
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
            accountManager.currentUserSession = session
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

        dbsToClose.forEach {
            try {
                it.close()
            }catch(e: Exception) {
                //do nothing - can happen if there is any pending database stuff going on
            }
        }
        dbsToClose.clear()
    }

}