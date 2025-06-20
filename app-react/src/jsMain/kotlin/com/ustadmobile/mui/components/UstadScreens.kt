package com.ustadmobile.mui.components

import com.ustadmobile.MuiAppState
import com.ustadmobile.core.components.DIModule
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabaseJsImplementations
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.util.UstadAntilog
import com.ustadmobile.core.util.ext.getOrPut
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DatabaseBuilderOptions
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.mui.common.Area
import com.ustadmobile.mui.common.Sizes
import com.ustadmobile.view.Content
import web.cssom.Display
import mui.system.Box
import org.kodein.di.DI
import remix.run.router.LoaderFunction
import tanstack.query.core.QueryClient
import tanstack.react.query.QueryClientProvider
import mui.system.sx
import web.cssom.array
import web.cssom.Auto
import web.cssom.GridTemplateAreas
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import mui.material.Snackbar
import react.*
import react.router.useLoaderData
import ustadJsDi
import kotlin.random.Random
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.MIGRATE_USERNAME_CLIENT
import web.cssom.None
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.ext.MIGRATION_144_145_CLIENT
import com.ustadmobile.core.db.ext.MIGRATION_148_149_NO_OFFLINE_ITEMS
import com.ustadmobile.core.db.ext.MIGRATION_155_156_CLIENT
import com.ustadmobile.core.db.ext.MIGRATION_161_162_CLIENT
import com.ustadmobile.core.db.ext.MIGRATION_169_170_CLIENT
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.util.ext.hasOnlyOneBitSet
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.util.ext.deleteDatabaseAsync
import mui.system.useMediaQuery
import org.kodein.di.direct
import org.kodein.di.instance
import emotion.react.css
import org.kodein.di.on
import react.dom.html.ReactHTML.div
import react.router.useLocation
import remix.run.router.LoaderFunctionArgs
import web.dom.document
import web.idb.indexedDB

//Roughly as per components/Showcases on MUI-showcase #d71c6d1

/**
 * Data class that is returned by the LoaderFunction
 */
data class UstadScreensLoaderData(val di: DI)

//TanStack Query Client as per
// https://tanstack.com/query/latest/docs/react/quick-start
private val tanstackQueryClient = QueryClient()

class UstadScreenContextData(
    val onAppUiStateChanged: (AppUiState) -> Unit,

    val muiAppState: StateInstance<MuiAppState>,

    val showSnackFunction: SnackBarDispatcher
)

val UstadScreensContext = createContext<UstadScreenContextData>()

val UstadScreens = FC<Props> {
    val mobileMode = useMediaQuery("(max-width:960px)")
    val location = useLocation()
    val loaderData = useLoaderData() as UstadScreensLoaderData
    val accountManager: UstadAccountManager = loaderData.di.direct.instance()
    val currentSession by accountManager.currentUserSessionFlow.collectAsState(null)
    val appUiStateInstance = useState { AppUiState() }

    var appUiState: AppUiState by appUiStateInstance
    var snack: Snack? by useState { null }
    val langConfig = useMemo(dependencies = emptyArray()) {
        loaderData.di.direct.instance<SupportedLanguagesConfig>()
    }
    val currentDb: UmAppDatabase =  loaderData.di.direct.on(accountManager.activeLearningSpace)
        .instance(tag = DoorTag.TAG_DB)

    val currentSite by currentDb.siteDao().getSiteAsFlow().collectAsState(null)


    val muiState = useState { MuiAppState() }

    var muiStateVar by muiState
    var mobileMenuOpen by useState(false)

    var currentRootItemIndex by useState { 0 }
    useEffect(location.pathname) {
        val pathIndex = ROOT_SCREENS.indexOfFirst {
            location.pathname == "/${it.key}"
        }

        if(pathIndex >= 0)
            currentRootItemIndex = pathIndex
    }


    UstadScreensContext(
        UstadScreenContextData(
            onAppUiStateChanged = {
                appUiState = it
            },
            muiAppState = muiState,
            showSnackFunction = {
                snack = it
            },
        )
    ) {
        DIModule {
            di = loaderData.di
            UstadLanguageConfigProvider {
                languagesConfig = langConfig




                QueryClientProvider {
                    client = tanstackQueryClient

                    Box {
                        try {
                            /* Cypress testing needs to know if the new or existing screen (where a
                             * user selects new account or existing account) will be displayed,
                             * which is shown if new user registration is allowed.
                             *
                             * This DOM element allows Cypress tests to check accordingly.
                             *
                             * This is set by a script generated by /api/sysconfig/script
                             */
                            val isRegistrationAllowed = js("_ustadRegistrationAllowed") as Boolean

                            div {
                                id = "registration_enabled"
                                css {
                                    display = None.none
                                }
                                + "$isRegistrationAllowed"
                            }
                        }catch (e :Exception){
                            Napier.e(
                                "_ustadRegistrationAllowed not set/found : required for " +
                                        "cypress testing: ensure /api/sysconfig/script is included ",
                            e)
                            throw e
                        }

                        sx {
                            display = Display.grid
                            gridTemplateRows = array(
                                Sizes.Header.Height,
                                Auto.auto,
                            )
                            gridTemplateColumns = array(
                                Sizes.Sidebar.Width, Auto.auto,
                            )

                            //As per https://developer.mozilla.org/en-US/docs/Web/CSS/grid-template-areas
                            gridTemplateAreas = GridTemplateAreas(
                                arrayOf(Area.Header, Area.Header),
                                if (mobileMode || !appUiState.navigationVisible)
                                    arrayOf(Area.Content, Area.Content)
                                else
                                    arrayOf(Area.Sidebar, Area.Content),
                            )
                        }
                        val isOnlyOneVisible = currentSite?.bottomNavVisibilityFlag?.hasOnlyOneBitSet() == true

                        Header {
                            this.appUiState = appUiState
                            setAppBarHeight = {
                                if (muiStateVar.appBarHeight != it) {
                                    muiStateVar = muiStateVar.copy(appBarHeight = it)
                                }
                            }
                            showMenuIcon = mobileMode
                            onClickMenuIcon = {
                                mobileMenuOpen = !mobileMenuOpen
                            }
                            sidebarVisible = !mobileMode &&
                                        appUiState.navigationVisible
                                        && currentSession?.person?.isPersonalAccount != true &&
                                        !isOnlyOneVisible
                        }

                        //if (mobileMode) Menu() else Sidebar()
                        //Note: If we remove the component, instead of hiding using Display property,
                        // then this seems to make react destroy the content component and create a
                        // completely new one, which we definitely do not want
                        Sidebar {
                            visible = !mobileMode &&
                                        appUiState.navigationVisible
                                        && currentSession?.person?.isPersonalAccount != true &&
                                        !isOnlyOneVisible
                            selectedRootItemIndex = currentRootItemIndex
                            site = currentSite
                        }

                        UstadMobileMenu {
                            isOpen = (mobileMenuOpen && appUiState.navigationVisible)
                            visible = (mobileMode && appUiState.navigationVisible)
                            onSetOpen = {
                                mobileMenuOpen = it
                            }
                            selectedRootItemIndex = currentRootItemIndex
                        }

                        Content()

                        Snackbar {
                            open = snack != null
                            onClose = { _, _ ->
                                snack = null
                            }
                            autoHideDuration = 8000

                            message = ReactNode(snack?.message ?: "")
                        }
                    }
                }
            }
        }
    }


}

/**
 * Use the router loader function to initialize things that require asynchronous initialization e.g.
 * the database, loading strings / appconfig etc.
 */
val ustadScreensLoader: LoaderFunction<Any?> = { args: LoaderFunctionArgs<Any?> ->
    Napier.base(UstadAntilog())
    Napier.d("Index: Window.onLoad")
    val dbName = sanitizeDbNameFromUrl(window.location.origin)
    val dbUrl = "sqlite:$dbName"
    val nodeId = localStorage.getOrPut("${dbName}_nodeId") {
        Random.nextLong(0, Long.MAX_VALUE).toString()
    }.toLong()
    val nodeAuth = localStorage.getOrPut("${dbName}_nodeAuth") {
        randomUuid().toString()
    }

    val dbNodeIdAndAuth = NodeIdAndAuth(nodeId, nodeAuth)

    val builderOptions = DatabaseBuilderOptions(
        UmAppDatabase::class,
        UmAppDatabaseJsImplementations, dbUrl = dbUrl,
        nodeId  = dbNodeIdAndAuth.nodeId,
        webWorkerPath = "./worker.sql-wasm.js"
    )


    val dbBuilder = DatabaseBuilder.databaseBuilder(builderOptions)
        .addSyncCallback(dbNodeIdAndAuth)
        .addMigrations(*migrationList().toTypedArray())
        .addMigrations(MIGRATION_144_145_CLIENT)
        .addMigrations(MIGRATION_148_149_NO_OFFLINE_ITEMS)
        .addMigrations(MIGRATION_155_156_CLIENT)
        .addMigrations(MIGRATION_161_162_CLIENT)
        .addMigrations(MIGRATION_169_170_CLIENT)
        .addMigrations(MIGRATE_USERNAME_CLIENT)
        .addMigrations()

    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.promise {
        lateinit var dbBuilt: UmAppDatabase
        try {
            dbBuilt = dbBuilder.build()
            Napier.d("Built UmAppDatabase")
        } catch (e: Exception) {
            Napier.w("Exception building database - trying to clear", e)

            //Probably something with no migration path, clear and retry
            indexedDB.deleteDatabaseAsync(dbName)
            localStorage.clear()

            //Try again
            dbBuilt = dbBuilder.build()
        }

        val json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        val httpClient = HttpClient(Js) {
            install(ContentNegotiation) {
                json(json = json)
            }
            install(HttpTimeout)
        }


        val jsStringsProvider = MR.stringsLoader.getOrLoad()

        val di = ustadJsDi(
            dbBuilt = dbBuilt,
            dbNodeIdAndAuth = dbNodeIdAndAuth,
            json = json,
            httpClient = httpClient,
            stringsProvider = jsStringsProvider,
        )

        document.getElementById("loading")?.remove()

        UstadScreensLoaderData(di)
    }
}.unsafeCast<LoaderFunction<Any?>>()
