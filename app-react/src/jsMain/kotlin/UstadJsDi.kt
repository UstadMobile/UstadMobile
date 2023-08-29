
import com.ustadmobile.core.account.*
import com.ustadmobile.core.db.RepSubscriptionInitListener
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageIdMap
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.di.commonDomainDiModule
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManagerJs
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.XmlSerializer
import com.ustadmobile.xmlpullparserkmp.setInputString
import io.ktor.client.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.serialization.json.Json
import org.kodein.di.*
import com.ustadmobile.core.impl.locale.JsStringXml
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.impl.locale.StringProviderJs
import com.ustadmobile.util.resolveEndpoint
import dev.icerock.moko.resources.provider.JsStringProvider
import web.location.location
import web.url.URLSearchParams



/**
 * KodeIn DI builder for JS/Browser.
 */
internal fun ustadJsDi(
    dbBuilt: UmAppDatabase,
    dbNodeIdAndAuth: NodeIdAndAuth,
    defaultStringsXmlStr: String,
    displayLocaleStringsXmlStr: String?,
    json: Json,
    httpClient: HttpClient,
    configMap: Map<String, String>,
    stringsProvider: JsStringProvider,
) = DI {

    import(commonDomainDiModule(EndpointScope.Default))

    val messageIdMapFlipped: Map<String, Int> by lazy {
        MessageIdMap.idMap.entries.associate { (k, v) -> v to k }
    }

    val xppFactory = XmlPullParserFactory.newInstance()

    val apiUrl = resolveEndpoint(location.href, URLSearchParams(location.search))
    console.log("Api URL = $apiUrl (location.href = ${location.href}")

    bind<JsStringProvider>() with singleton {
        stringsProvider
    }

    bind<StringProvider>() with singleton {
        val systemImpl: UstadMobileSystemImpl = instance()
        val jsStringProvider: JsStringProvider = instance()
        StringProviderJs(systemImpl.getDisplayedLocale(), jsStringProvider)
    }

    bind<StringsXml>(tag = JsStringXml.DEFAULT) with singleton {
        val defaultXpp = xppFactory.newPullParser()
        defaultXpp.setInputString(defaultStringsXmlStr)
        StringsXml(defaultXpp, xppFactory, messageIdMapFlipped, "en")
    }

    if(displayLocaleStringsXmlStr != null) {
        bind<StringsXml>(tag = JsStringXml.DISPLAY) with singleton{
            val foreignXpp = xppFactory.newPullParser()
            foreignXpp.setInputString(displayLocaleStringsXmlStr)
            val defaultStringsXml = instance<StringsXml>(tag = JsStringXml.DEFAULT)
            StringsXml(foreignXpp, xppFactory, messageIdMapFlipped,
                UstadMobileSystemImpl.displayedLocale, defaultStringsXml)
        }
    }

    bind<SupportedLanguagesConfig>() with singleton {
        configMap["com.ustadmobile.uilanguages"]?.let {languageList ->
            SupportedLanguagesConfig(languageList)
        } ?: SupportedLanguagesConfig()
    }

    bind<ApiUrlConfig>() with singleton {
        ApiUrlConfig(apiUrl)
    }

    bind<UstadMobileSystemImpl>() with singleton {
        val jsStringProvider: JsStringProvider = instance()
        UstadMobileSystemImpl(
            instance(tag = JsStringXml.DEFAULT),
            instanceOrNull(tag = JsStringXml.DISPLAY),
            jsStringProvider,
        )
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

    bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(EndpointScope.Default).singleton {
        dbBuilt
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(EndpointScope.Default).singleton {
        val nodeIdAndAuth: NodeIdAndAuth = instance()
        val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
        val repositoryConfig =  RepositoryConfig.repositoryConfig(
            this,context.url+"UmAppDatabase/",  nodeIdAndAuth.auth,
            nodeIdAndAuth.nodeId, instance(), instance()
        ){
            replicationSubscriptionInitListener = RepSubscriptionInitListener()
        }
        db.asRepository(repositoryConfig)
    }

    constant(UstadMobileSystemCommon.TAG_DOWNLOAD_ENABLED) with false

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
        httpClient
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
        val numIterations = UstadMobileConstants.PBKDF2_ITERATIONS
        val keyLength = UstadMobileConstants.PBKDF2_KEYLENGTH

        Pbkdf2Params(numIterations, keyLength)
    }

    bind<ClazzLogCreatorManager>() with singleton { ClazzLogCreatorManagerJs() }

    bind<Json>() with singleton {
        json
    }
}