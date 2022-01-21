
import com.ustadmobile.core.account.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManagerJs
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.defaultJsonSerializer
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.jsExt.container.ContainerMounterJs
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.mui.components.umThemeProvider
import com.ustadmobile.navigation.NavControllerJs
import com.ustadmobile.redux.ReduxAppStateManager.createStore
import com.ustadmobile.redux.ReduxAppStateManager.getCurrentState
import com.ustadmobile.redux.ReduxDiState
import com.ustadmobile.redux.ReduxThemeState
import com.ustadmobile.util.BrowserTabTracker
import com.ustadmobile.util.ThemeManager.createAppTheme
import com.ustadmobile.view.renderSplashComponent
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.XmlSerializer
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import org.kodein.di.*
import react.dom.render
import react.redux.provider

fun main() {
    defaultJsonSerializer()
    BrowserTabTracker.init()
    window.onload = {
        render(document.getElementById("root")) {
            val diState = ReduxDiState(
                DI.lazy { import(diModule) }
            )
            val theme = createAppTheme()
            provider(createStore(diState, ReduxThemeState(theme))){
                umThemeProvider(theme) {
                    renderSplashComponent()
                }
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

    bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(EndpointScope.Default).singleton {
        getCurrentState().dbInstances.db ?:
        throw IllegalStateException("Database was not built, make sure it is built before proceeding")
    }

    bind<CoroutineScope>(DiTag.TAG_PRESENTER_COROUTINE_SCOPE) with provider {
        GlobalScope
    }

    bind<UmAppDatabase>(tag = UmAppDatabase.TAG_REPO) with scoped(EndpointScope.Default).singleton {
        getCurrentState().dbInstances.repo ?:
        throw IllegalStateException("Repository was not built, make sure it is built before proceeding")
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
