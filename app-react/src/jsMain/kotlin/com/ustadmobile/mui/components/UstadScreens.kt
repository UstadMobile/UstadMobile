package com.ustadmobile.mui.components

import com.ustadmobile.MuiAppState
import com.ustadmobile.core.components.DIModule
import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabaseJsImplementations
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.impl.UstadMobileSystemImpl
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
import com.ustadmobile.util.Util
import com.ustadmobile.util.resolveEndpoint
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
import web.url.URLSearchParams
import kotlin.random.Random
import com.ustadmobile.core.MR

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
) {

}

val UstadScreensContext = createContext<UstadScreenContextData>()

val UstadScreens = FC<Props> {
    val mobileMode = false//useMediaQuery("(max-width:960px)")
    val appUiStateInstance = useState { AppUiState() }
    var appUiState: AppUiState by appUiStateInstance
    val loaderData = useLoaderData() as UstadScreensLoaderData
    var snack: Snack? by useState { null }

    val muiState = useState { MuiAppState() }

    var muiStateVar by muiState


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
                    }

                    //if (mobileMode) Menu() else Sidebar()
                    //Note: If we remove the component, instead of hiding using Display property,
                    // then this seems to make react destroy the content component and create a
                    // completely new one, which we definitely do not want
                    Sidebar {
                        visible = appUiState.navigationVisible
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
        UmAppDatabaseJsImplementations, dbUrl = dbUrl, webWorkerPath = "./worker.sql-wasm.js")

    val dbBuilder =  DatabaseBuilder.databaseBuilder(builderOptions)
        .addCallback(ContentJobItemTriggersCallback())
        .addSyncCallback(dbNodeIdAndAuth)
        .addMigrations(*migrationList().toTypedArray())

    val defaultAssetPath = "locales/en.xml"

    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.promise {
        val dbBuilt = dbBuilder.build()
        val defaultStringsXmlStr = Util.loadAssetsAsText(defaultAssetPath)
        val displayedLocale = UstadMobileSystemImpl.displayedLocale
        val foreignStringXmlStr = if(displayedLocale != "en") {
            Util.loadAssetsAsText("locales/$displayedLocale.xml")
        }else {
            null
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

        UstadScreensLoaderData(di)
    }
}
