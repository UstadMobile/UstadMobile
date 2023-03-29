package com.ustadmobile.mui.components

import com.ustadmobile.MuiAppState
import com.ustadmobile.core.components.DIModule
import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabaseJsImplementations
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.impl.AppConfig
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
import com.ustadmobile.view.Content
import csstype.Display
import mui.system.Box
import org.kodein.di.DI
import remix.run.router.LoaderFunction
import tanstack.query.core.QueryClient
import tanstack.react.query.QueryClientProvider
import mui.system.sx
import csstype.array
import csstype.Auto
import csstype.GridTemplateAreas
import io.github.aakira.napier.Napier
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import mui.material.Snackbar
import mui.material.Typography
import org.w3c.dom.url.URLSearchParams
import react.*
import react.router.useLoaderData
import ustadJsDi
import kotlin.random.Random

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

    val muiAppState: StateFlow<MuiAppState>,

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

    val muiStateFlow = useMemo(dependencies = emptyArray()) {
        MutableStateFlow(MuiAppState())
    }



    UstadScreensContext(
        UstadScreenContextData(
            onAppUiStateChanged = {
                appUiState = it
            },
            muiAppState = muiStateFlow,
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
                            if(muiStateFlow.value.appBarHeight != it) {
                                muiStateFlow.update { prev -> prev.copy(appBarHeight = it) }
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
val ustadScreensLoader: LoaderFunction = {
    Napier.base(UstadAntilog())
    Napier.d("Index: Window.onLoad")
    val url = window.location.href
    val apiUrl = URLSearchParams().get(AppConfig.KEY_API_URL)
        ?: url.substringBefore(if(url.indexOf("umapp/") != -1) "umapp/" else "#/")

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
        val appConfigs = Util.loadFileContentAsMap<HashMap<String, String>>("appconfig.json")
        Napier.d("Index: loaded appConfig")
        val defaultStringsXmlStr = Util.loadAssetsAsText(defaultAssetPath)
        val displayedLocale = UstadMobileSystemImpl.displayedLocale
        val foreignStringXmlStr = if(displayedLocale != "en") {
            Util.loadAssetsAsText("locales/$displayedLocale.xml")
        }else {
            null
        }

        val di = ustadJsDi(dbBuilt, dbNodeIdAndAuth, appConfigs, apiUrl, defaultStringsXmlStr,
            foreignStringXmlStr)
        UstadScreensLoaderData(di)
    }
}
