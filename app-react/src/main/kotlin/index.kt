import com.ccfraser.muirwik.components.mThemeProvider
import com.ustadmobile.core.account.ClientId
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.defaultJsonSerializer
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.mocks.container.ContainerMounterJs
import com.ustadmobile.mocks.db.ReactDatabase
import com.ustadmobile.redux.ReduxAppStateManager
import com.ustadmobile.redux.ReduxAppStateManager.getCurrentState
import com.ustadmobile.redux.ReduxDiState
import com.ustadmobile.redux.ReduxThemeState
import com.ustadmobile.util.ThemeManager
import com.ustadmobile.view.splashScreen
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.XmlSerializer
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.kodein.di.*
import react.dom.render
import react.redux.provider

fun main() {
    defaultJsonSerializer()
    window.onload = {
        render(document.getElementById("root")) {

            val diState = ReduxDiState(
                DI.lazy {
                    import(diModule)
                }
            )

            provider(ReduxAppStateManager.createStore(diState)){
                mThemeProvider(ThemeManager.createAppTheme()) {
                    splashScreen()
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

    bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(EndpointScope.Default).singleton {
        val dbName = sanitizeDbNameFromUrl(context.url)
        ReactDatabase.getInstance(this, dbName)
    }

    bind<UmAppDatabase>(tag = UmAppDatabase.TAG_REPO) with scoped(EndpointScope.Default).singleton {
        instance(tag = UmAppDatabase.TAG_DB)
    }

    constant(UstadMobileSystemCommon.TAG_DOWNLOAD_ENABLED) with false

    bind<ClientId>(tag = UstadMobileSystemCommon.TAG_CLIENT_ID) with scoped(EndpointScope.Default).singleton {
        ClientId(9090)
    }

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
    bind<CoroutineDispatcher>(tag = UstadMobileSystemCommon.TAG_MAIN_COROUTINE_CONTEXT) with singleton { Dispatchers.Main }

    bind<ContentEntryOpener>() with scoped(EndpointScope.Default).singleton {
        ContentEntryOpener(di, context)
    }

    bind<HttpClient>() with singleton {
        HttpClient(Js) {
            install(JsonFeature)
            install(HttpTimeout)
        }
    }

    registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }
}
