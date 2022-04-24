import com.ustadmobile.core.account.*
import com.ustadmobile.core.db.RepSubscriptionInitListener
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManagerJs
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.core.navigation.NavControllerJs
import com.ustadmobile.redux.ReduxAppStateManager
import com.ustadmobile.redux.ReduxThemeState
import com.ustadmobile.util.ContainerMounterJs
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.XmlSerializer
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.serialization.json.Json
import org.kodein.di.*

/**
 * KodeIn DI builder for JS/Browser.
 */
internal fun ustadJsDi(
    dbBuilt: UmAppDatabase,
    dbNodeIdAndAuth: NodeIdAndAuth,
    appConfigs: HashMap<String, String>,
    apiUrl: String,
    defaultStringsXmlStr: String,
    displayLocaleStringsXmlStr: String?,
) = DI {
    bind<UstadMobileSystemImpl>() with singleton {
        UstadMobileSystemImpl(XmlPullParserFactory.newInstance(), instance(),
            defaultStringsXmlStr, displayLocaleStringsXmlStr
        ).also { impl ->
            appConfigs.forEach {
                val value = when(it.key){
                    AppConfig.KEY_API_URL -> apiUrl
                    else -> it.value
                }
                impl.setAppPref(it.key, value, this)
            }
        }
    }

    bind<UstadAccountManager>() with singleton {
        UstadAccountManager(instance(), this, di)
    }

    bind<NodeIdAndAuth>() with scoped(EndpointScope.Default).singleton {
        dbNodeIdAndAuth
    }


    bind<CoroutineScope>(DiTag.TAG_PRESENTER_COROUTINE_SCOPE) with provider {
        GlobalScope
    }

    bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(EndpointScope.Default).singleton {
        dbBuilt
    }

    bind<UmAppDatabase>(tag = UmAppDatabase.TAG_REPO) with scoped(EndpointScope.Default).singleton {
        val nodeIdAndAuth: NodeIdAndAuth = instance()
        val db = instance<UmAppDatabase>(tag = UmAppDatabase.TAG_DB)
        val repositoryConfig =  RepositoryConfig.repositoryConfig(
            this,context.url+"UmAppDatabase/",  nodeIdAndAuth.auth,
            nodeIdAndAuth.nodeId, instance(), instance()
        ){
            replicationSubscriptionInitListener = RepSubscriptionInitListener()
        }
        db.asRepository(repositoryConfig)
    }

    constant(UstadMobileSystemCommon.TAG_DOWNLOAD_ENABLED) with false

    bind<ReduxThemeState>() with singleton{
        ReduxThemeState(ReduxAppStateManager.getCurrentState().appTheme?.theme)
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

    bind<UstadNavController>() with singleton {
        NavControllerJs(json = instance())
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
        val numIterations = systemImpl.getAppConfigInt(
            AppConfig.KEY_PBKDF2_ITERATIONS,
            UstadMobileConstants.PBKDF2_ITERATIONS, this)
        val keyLength = systemImpl.getAppConfigInt(
            AppConfig.KEY_PBKDF2_KEYLENGTH,
            UstadMobileConstants.PBKDF2_KEYLENGTH, this)

        Pbkdf2Params(numIterations, keyLength)
    }

    bind<ClazzLogCreatorManager>() with singleton { ClazzLogCreatorManagerJs() }

    bind<Json>() with singleton {
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }
}