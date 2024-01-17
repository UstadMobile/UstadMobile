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
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import mui.material.Snackbar
import mui.material.Typography
import react.*
import react.router.useLoaderData
import ustadJsDi
import web.location.location
import web.url.URL
import kotlin.random.Random
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.util.ext.deleteDatabaseAsync
import mui.material.useMediaQuery
import org.kodein.di.direct
import org.kodein.di.instance
import react.router.useLocation
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
    val appUiStateInstance = useState { AppUiState() }
    val location = useLocation()

    var appUiState: AppUiState by appUiStateInstance
    val loaderData = useLoaderData() as UstadScreensLoaderData
    var snack: Snack? by useState { null }
    val langConfig = useMemo(dependencies = emptyArray()) {
        loaderData.di.direct.instance<SupportedLanguagesConfig>()
    }


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

                        Header {
                            this.appUiState = appUiState
                            setAppBarHeight = {
                                if(muiStateVar.appBarHeight != it) {
                                    muiStateVar = muiStateVar.copy(appBarHeight = it)
                                }
                            }
                            showMenuIcon = mobileMode
                            onClickMenuIcon = {
                                mobileMenuOpen = !mobileMenuOpen
                            }
                        }

                        //if (mobileMode) Menu() else Sidebar()
                        //Note: If we remove the component, instead of hiding using Display property,
                        // then this seems to make react destroy the content component and create a
                        // completely new one, which we definitely do not want
                        Sidebar {
                            visible = !mobileMode && appUiState.navigationVisible
                            selectedRootItemIndex = currentRootItemIndex
                        }

                        if(mobileMode) {
                            UstadMobileMenu {
                                isOpen = (mobileMenuOpen && appUiState.navigationVisible)
                                onSetOpen = {
                                    mobileMenuOpen = it
                                }
                                selectedRootItemIndex = currentRootItemIndex
                            }
                        }

                        Content()

                        Snackbar {
                            open = snack != null
                            onClose = { _, _ ->
                                snack = null
                            }

                            Typography {
                                + (snack?.message ?: "")
                            }
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
val ustadScreensLoader: LoaderFunction<*> = {
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
        nodeId = dbNodeIdAndAuth.nodeId,
        webWorkerPath = "./worker.sql-wasm.js")

    val dbBuilder =  DatabaseBuilder.databaseBuilder(builderOptions)
        .addSyncCallback(dbNodeIdAndAuth)
        .addMigrations(*migrationList().toTypedArray())

    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.promise {
        lateinit var dbBuilt: UmAppDatabase
        @Suppress("LiftReturnOrAssignment") // We don't want the database to be closed after the block
        try {
            dbBuilt = dbBuilder.build()
        }catch(e: Exception) {
            Napier.w("Exception building database - trying to clear")
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


        val ustadConfigHref = URL("ustad-config.json", location.href).href
        val configJson: Map<String, String> = httpClient.get(ustadConfigHref).body()
        val jsStringsProvider = MR.stringsLoader.getOrLoad()

        val di = ustadJsDi(
            dbBuilt = dbBuilt,
            dbNodeIdAndAuth = dbNodeIdAndAuth,
            json = json,
            httpClient = httpClient,
            configMap = configJson,
            stringsProvider = jsStringsProvider,
        )

        document.getElementById("loading")?.remove()

        UstadScreensLoaderData(di)
    }
}
