
import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabaseJsImplementations
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.util.UstadAntilog
import com.ustadmobile.core.util.defaultJsonSerializer
import com.ustadmobile.core.util.ext.getOrPut
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DatabaseBuilderOptions
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.mui.components.themeContext
import com.ustadmobile.mui.components.umCssBaseline
import com.ustadmobile.mui.components.umThemeProvider
import com.ustadmobile.redux.ReduxAppStateManager.createStore
import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxDiState
import com.ustadmobile.redux.ReduxThemeState
import com.ustadmobile.util.*
import com.ustadmobile.util.ThemeManager.createAppTheme
import com.ustadmobile.view.renderExtraActiveTabWarningComponent
import com.ustadmobile.view.renderMainComponent
import com.ustadmobile.view.renderSplashComponent
import io.github.aakira.napier.Napier
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import react.RBuilder
import react.RComponent
import react.dom.render
import react.redux.provider
import react.setState
import kotlin.random.Random

fun main() {
    defaultJsonSerializer()
    Napier.base(UstadAntilog())

    window.onload = {
        Napier.d("Index: Window.onLoad")
        val url = window.location.href
        val apiUrl = urlSearchParamsToMap()[AppConfig.KEY_API_URL]
            ?: url.substringBefore(if(url.indexOf("umapp/") != -1) "umapp/" else "#/")

        val dbName = sanitizeDbNameFromUrl(window.location.origin)
        val nodeId = localStorage.getOrPut("${dbName}_nodeId") {
            Random.nextLong(0, Long.MAX_VALUE).toString()
        }.toLong()
        val nodeAuth = localStorage.getOrPut("${dbName}_nodeAuth") {
            randomUuid().toString()
        }

        val dbNodeIdAndAuth = NodeIdAndAuth(nodeId, nodeAuth)

        val builderOptions = DatabaseBuilderOptions(
            UmAppDatabase::class,
            UmAppDatabaseJsImplementations, dbName,"./worker.sql-wasm.js")

        val dbBuilder =  DatabaseBuilder.databaseBuilder(builderOptions)
            .addCallback(ContentJobItemTriggersCallback())
            .addSyncCallback(dbNodeIdAndAuth)
            .addMigrations(*UmAppDatabase.migrationList(dbNodeIdAndAuth.nodeId).toTypedArray())

        val defaultAssetPath = "locales/en.xml"


        GlobalScope.launch {
            val dbBuilt = dbBuilder.build()
            Napier.d("Index: built db")

            val appConfigs = Util.loadFileContentAsMap<HashMap<String, String>>("appconfig.json")
            Napier.d("Index: loaded appConfig")

            val defaultStrings = Util.loadAssetsAsText(defaultAssetPath)

            val localeCode = "en"
            val defaultLocale = "en"
            if(localeCode != defaultLocale){
                val currentAssetPath = "locales/$localeCode.xml"
                val currentStrings = Util.loadAssetsAsText(currentAssetPath)
            }

            val di = ustadJsDi(dbBuilt, dbNodeIdAndAuth, appConfigs, apiUrl, defaultAssetPath,
                defaultStrings)

            render(document.getElementById("root")){
                val theme = createAppTheme()
                provider(createStore(ReduxThemeState(theme))){
                    umThemeProvider(theme){
                        dispatch(ReduxDiState(di))
                        BrowserTabTracker.init { activeTabRunning ->
                            child(IndexComponent::class){
                                attrs.di = di
                                attrs.activeTabRunning = activeTabRunning
                            }
                        }

                    }
                }
            }
            Napier.d("Index: Made DI")
        }
    }
}

interface IndexProps: UmProps {
    var di: DI
    var activeTabRunning: Boolean
}


/**
 * UI changes can't be done on different thread, we need state to be able to update
 * UI that's where this component comes in. It will handle all UI changes during DB and
 * Locale setups process triggered by state change
 */
class IndexComponent (props: IndexProps): RComponent<IndexProps, UmState>(props){

    private var showMainComponent: Boolean = false

    override fun componentDidMount() {
        setState {
            showMainComponent = true
        }
    }

    override fun RBuilder.render() {
        umCssBaseline()
        themeContext.Consumer { _ ->
            if(showMainComponent && !props.activeTabRunning){
                renderMainComponent()
            } else if(showMainComponent && props.activeTabRunning){
                renderExtraActiveTabWarningComponent(props.di)
            }else {
                renderSplashComponent()
            }
        }
    }
}
