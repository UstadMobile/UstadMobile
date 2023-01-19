package com.ustadmobile

import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabaseJsImplementations
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.getOrPut
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DatabaseBuilderOptions
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.mui.common.Area
import com.ustadmobile.mui.common.Sizes
import com.ustadmobile.core.components.DIModule
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.mui.components.Header
import com.ustadmobile.mui.components.Sidebar
import com.ustadmobile.mui.components.ThemeModule
import com.ustadmobile.util.Util
import com.ustadmobile.view.Content
import com.ustadmobile.view.UstadScreensModule
import csstype.Auto.auto
import csstype.Display
import csstype.GridTemplateAreas
import csstype.array
import io.github.aakira.napier.Napier
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mui.system.*
import org.kodein.di.DI
import org.w3c.dom.url.URLSearchParams
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.router.dom.HashRouter
import react.useState
import ustadJsDi
import kotlin.random.Random

fun main() {

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

    GlobalScope.launch {
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

        val ustadDi = ustadJsDi(dbBuilt, dbNodeIdAndAuth, appConfigs, apiUrl, defaultStringsXmlStr,
            foreignStringXmlStr)

        createRoot(document.createElement("div").also { document.body!!.appendChild(it) })
            .render(App.create() { di = ustadDi })
    }

}

external interface AppProps: Props {
    var di: DI
}

private val App = FC<AppProps> { props ->
    val mobileMode = false//useMediaQuery("(max-width:960px)")
    var appUiState: AppUiState by useState { AppUiState() }

    HashRouter {
        DIModule {
            di = props.di
            UstadScreensModule {
                ThemeModule {
                    Box {
                        sx {
                            display = Display.grid
                            gridTemplateRows = array(
                                Sizes.Header.Height,
                                auto,
                            )
                            gridTemplateColumns = array(
                                Sizes.Sidebar.Width, auto,
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
                        }

                        //if (mobileMode) Menu() else Sidebar()
                        //Note: If we remove the component, instead of hiding using Display property,
                        // then this seems to make react destroy the content component and create a
                        // completely new one, which we definitely do not want
                        Sidebar {
                            visible = appUiState.navigationVisible
                        }

                        Content {
                            onAppUiStateChanged = {
                                appUiState = it
                            }
                        }
                    }
                }
            }
        }
    }
}
