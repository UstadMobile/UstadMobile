
import com.ustadmobile.core.account.*
import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.RepSubscriptionInitListener
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabaseJsImplementations
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManagerJs
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.defaultJsonSerializer
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DatabaseBuilderOptions
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.mui.components.themeContext
import com.ustadmobile.mui.components.umCssBaseline
import com.ustadmobile.mui.components.umThemeProvider
import com.ustadmobile.navigation.NavControllerJs
import com.ustadmobile.redux.ReduxAppStateManager.createStore
import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxAppStateManager.getCurrentState
import com.ustadmobile.redux.ReduxDiState
import com.ustadmobile.redux.ReduxThemeState
import com.ustadmobile.util.*
import com.ustadmobile.util.ThemeManager.createAppTheme
import com.ustadmobile.view.SplashView
import com.ustadmobile.view.renderExtraActiveTabWarningComponent
import com.ustadmobile.view.renderMainComponent
import com.ustadmobile.view.renderSplashComponent
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.XmlSerializer
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.*
import org.kodein.di.*
import org.w3c.dom.Element
import react.RBuilder
import react.RComponent
import react.dom.render
import react.redux.provider
import react.setState

fun main() {
    defaultJsonSerializer()
    Napier.base(DebugAntilog())

    window.onload = {
        render(document.getElementById("root")){
            val theme = createAppTheme()
            provider(createStore(ReduxThemeState(theme))){
                umThemeProvider(theme){
                    //Update DI state ready to be used by StyleManager
                    val di = DI { import(diModule) }
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
    }
}

interface IndexProps: UmProps {
    var di: DI
    var activeTabRunning: Boolean
}

/**
 * Setup Database and locales, needed for app to start
 */
fun setUpDbAndLocales(di: DI, rootElement: Element?, showMainComponent: (Boolean) -> Unit){
    val impl : UstadMobileSystemImpl by di.instance()
    val directionAttributeValue = if(impl.isRtlActive()) "rtl" else "ltr"
    rootElement?.setAttribute("dir",directionAttributeValue)
    showMainComponent(false) //start by showing splash screen
    val navController: UstadNavController by di.instance()
    impl.navController = navController

    val url = window.location.href
    val apiUrl = urlSearchParamsToMap()[AppConfig.KEY_API_URL]
        ?: impl.getAppPref(AppConfig.KEY_API_URL, window)
        ?: url.substringBefore(if(url.indexOf("umapp/") != -1) "umapp/" else "#/")

    val dbName = sanitizeDbNameFromUrl(window.location.origin)

    val builderOptions = DatabaseBuilderOptions(
        UmAppDatabase::class,
        UmAppDatabaseJsImplementations, dbName,"./worker.sql-wasm.js")

    val dbBuilder =  DatabaseBuilder.databaseBuilder(builderOptions)

    val nodeIdAndAuth:NodeIdAndAuth by di.on(Endpoint(apiUrl)).instance()
    dbBuilder.addCallback(ContentJobItemTriggersCallback())
        .addSyncCallback(nodeIdAndAuth)
        .addMigrations(*UmAppDatabase.migrationList(nodeIdAndAuth.nodeId).toTypedArray())

    GlobalScope.launch(Dispatchers.Main) {
        val umAppDatabase =  dbBuilder.build()

        val diState = DI {
            import(diModule)
            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(EndpointScope.Default).singleton {
                umAppDatabase
            }
        }

        val localeCode = impl.getDisplayedLocale(this)
        val defaultLocale = impl.getAppPref(AppConfig.KEY_DEFAULT_LANGUAGE, this)

        val appConfigs = Util.loadFileContentAsMap<HashMap<String, String>>("appconfig.json")
        appConfigs.forEach {
            val value = when(it.key){
                AppConfig.KEY_API_URL -> apiUrl
                else -> it.value
            }
            impl.setAppPref(it.key, value, this)
        }

        val defaultAssetPath = "locales/$defaultLocale.xml"
        val defaultStrings = Util.loadAssetsAsText(defaultAssetPath)
        impl.defaultTranslations = Pair(defaultAssetPath , defaultStrings)
        impl.currentTranslations = Pair(defaultAssetPath , defaultStrings)

        if(localeCode != defaultLocale){
            val currentAssetPath = "locales/$localeCode.xml"
            val currentStrings = Util.loadAssetsAsText(currentAssetPath)
            impl.currentTranslations = Pair(currentAssetPath, currentStrings)
        }
        document.title = impl.getString(MessageID.app_name,this)
        impl.setAppPref(SplashView.TAG_LOADED,"true", this)
        dispatch(ReduxDiState(diState))
        showMainComponent(true) //Show main component
    }
}

/**
 * UI changes can't be done on different thread, we need state to be able to update
 * UI that's where this component comes in. It will handle all UI changes during DB and
 * Locale setups process triggered by state change
 */
class IndexComponent (props: IndexProps): RComponent<IndexProps, UmState>(props){

    private var showMainComponent: Boolean = false

    override fun componentDidMount() {
        setUpDbAndLocales(props.di, document.getElementById("root")){
            setState {
                showMainComponent = it
            }
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

//Prepare dependency injection
private val diModule = DI.Module("UstadApp-React"){

    bind<UstadMobileSystemImpl>() with singleton { UstadMobileSystemImpl.instance }

    bind<UstadAccountManager>() with singleton {
        UstadAccountManager(instance(), this, di)
    }

    bind<NodeIdAndAuth>() with scoped(EndpointScope.Default).singleton {
        val systemImpl: UstadMobileSystemImpl = instance()
        val contextIdentifier: String = sanitizeDbNameFromUrl(context.url)
        systemImpl.getOrGenerateNodeIdAndAuth(contextPrefix = contextIdentifier, this)
    }


    bind<CoroutineScope>(DiTag.TAG_PRESENTER_COROUTINE_SCOPE) with provider {
        GlobalScope
    }

    bind<UmAppDatabase>(tag = UmAppDatabase.TAG_REPO) with scoped(EndpointScope.Default).singleton {
        val nodeIdAndAuth: NodeIdAndAuth = instance()
        val db = instance<UmAppDatabase>(tag = UmAppDatabase.TAG_DB)
        val repositoryConfig =  RepositoryConfig.repositoryConfig(
            this,context.url+"UmAppDatabase/",  nodeIdAndAuth.auth,
            nodeIdAndAuth.nodeId, instance(),
            kotlinx.serialization.json.Json { encodeDefaults = true }
        ){
            replicationSubscriptionInitListener = RepSubscriptionInitListener()
        }
        db.asRepository(repositoryConfig)
    }

    constant(UstadMobileSystemCommon.TAG_DOWNLOAD_ENABLED) with false

    bind<ReduxThemeState>() with singleton{
        ReduxThemeState(getCurrentState().appTheme?.theme)
    }

    bind<ContainerMounter>() with singleton {
        ContainerMounterJs()
    }

    bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
        XmlPullParserFactory.newInstance().also {
            it.setNamespaceAware(true)
        }
    }

    bind<XmlPullParserFactory>(tag = DiTag.XPP_FACTORY_NSUNAWARE) with singleton {
        XmlPullParserFactory.newInstance()
    }

    bind<XmlSerializer>() with provider {
        instance<XmlPullParserFactory>().newSerializer()
    }

    bind<CoroutineDispatcher>(tag = UstadMobileSystemCommon.TAG_MAIN_COROUTINE_CONTEXT) with singleton {
        Dispatchers.Main
    }

    bind<ContentEntryOpener>() with scoped(EndpointScope.Default).singleton {
        ContentEntryOpener(di, context)
    }

    bind<HttpClient>() with singleton {
        HttpClient(Js) {
            install(JsonFeature)
            install(HttpTimeout)
        }
    }

    bind<UstadNavController>() with provider {
        NavControllerJs()
    }

    bind<ContainerStorageManager> () with scoped(EndpointScope.Default).singleton{
        ContainerStorageManager(context, di)
    }

    registerContextTranslator {
            account: UmAccount -> Endpoint(account.endpointUrl)
    }

    bind<AuthManager>() with scoped(EndpointScope.Default).singleton {
        AuthManager(context, di)
    }

    bind<Pbkdf2Params>() with singleton {
        val systemImpl: UstadMobileSystemImpl = instance()
        val numIterations = systemImpl.getAppConfigInt(AppConfig.KEY_PBKDF2_ITERATIONS,
            UstadMobileConstants.PBKDF2_ITERATIONS, this)
        val keyLength = systemImpl.getAppConfigInt(AppConfig.KEY_PBKDF2_KEYLENGTH,
            UstadMobileConstants.PBKDF2_KEYLENGTH, this)

        Pbkdf2Params(numIterations, keyLength)
    }

    bind<ClazzLogCreatorManager>() with singleton { ClazzLogCreatorManagerJs() }
}
