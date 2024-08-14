
import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.set
import com.ustadmobile.BuildConfigJs
import com.ustadmobile.core.account.*
import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.getversion.GetVersionUseCase
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsFromLocalUriUseCase
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsFromLocalUriUseCaseJs
import com.ustadmobile.core.domain.showpoweredby.GetShowPoweredByUseCase
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.config.AppConfig
import com.ustadmobile.core.impl.config.AppConfigMap
import com.ustadmobile.core.impl.config.GenderConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig.Companion.PREFKEY_ACTIONED_PRESET
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig.Companion.PREFKEY_LOCALE
import com.ustadmobile.core.impl.di.DomainDiModuleJs
import com.ustadmobile.core.impl.di.commonDomainDiModule
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManagerJs
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.XmlSerializer
import io.ktor.client.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.kodein.di.*
import com.ustadmobile.core.impl.locale.StringProviderJs
import com.ustadmobile.core.util.ext.toNullIfBlank
import com.ustadmobile.core.viewmodel.OnBoardingViewModel
import com.ustadmobile.domain.getversion.GetVersionUseCaseJs
import com.ustadmobile.util.resolveEndpoint
import dev.icerock.moko.resources.provider.JsStringProvider
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import web.location.location
import web.navigator.navigator
import web.url.URLSearchParams



/**
 * KodeIn DI builder for JS/Browser.
 */
@OptIn(ExperimentalXmlUtilApi::class)
internal fun ustadJsDi(
    dbBuilt: UmAppDatabase,
    dbNodeIdAndAuth: NodeIdAndAuth,
    json: Json,
    httpClient: HttpClient,
    configMap: Map<String, String>,
    stringsProvider: JsStringProvider,
) = DI {
    import(commonDomainDiModule(EndpointScope.Default))
    import(DomainDiModuleJs(EndpointScope.Default))

    val apiUrl = resolveEndpoint(location.href, URLSearchParams(location.search))
    console.log("Api URL = $apiUrl (location.href = ${location.href}")

    bind<AppConfig>() with singleton {
        AppConfigMap(configMap)
    }

    bind<GenderConfig>() with singleton {
        GenderConfig(
            genderConfigStr = BuildConfigJs.APP_UI_GOPTS.toNullIfBlank() ?: GenderConfig.DEFAULT_GENDER_OPTIONS
        )
    }

    bind<Settings>() with singleton {
        StorageSettings().also {
            //We don't use onboarding on the web, so mark this as completed
            it[OnBoardingViewModel.PREF_TAG] = "true"

            /*
             * Check if there is a preset default language, and apply if not already actioned
             */
            val presetLang = BuildConfigJs.APP_PRESET_LOCALE
            if(!presetLang.isEmpty() && it.getStringOrNull(PREFKEY_ACTIONED_PRESET) != "true") {
                it[PREFKEY_LOCALE] = presetLang
                it[PREFKEY_ACTIONED_PRESET] = "true"
            }
        }
    }

    bind<JsStringProvider>() with singleton {
        stringsProvider
    }

    bind<StringProviderJs>() with singleton {
        val jsStringProvider: JsStringProvider = instance()
        val localeConfig: SupportedLanguagesConfig = instance()
        StringProviderJs(localeConfig.displayedLocale, jsStringProvider)
    }

    bind<SupportedLanguagesConfig>() with singleton {
        SupportedLanguagesConfig(
            availableLanguagesConfig = BuildConfigJs.APP_UI_LANGUAGES.toNullIfBlank() ?:
                SupportedLanguagesConfig.DEFAULT_SUPPORTED_LANGUAGES,
            systemLocales = navigator.languages.toList(),
            settings = instance(),
        )
    }

    bind<ApiUrlConfig>() with singleton {
        ApiUrlConfig(apiUrl)
    }

    bind<UstadMobileSystemImpl>() with singleton {
        val jsStringProvider: JsStringProvider = instance()
        UstadMobileSystemImpl(
            settings = instance(),
            langConfig = instance(),
            jsStringProvider = jsStringProvider,
        )
    }

    bind<UstadAccountManager>() with singleton {
        UstadAccountManager(settings = instance(), di = di)
    }

    bind<NodeIdAndAuth>() with scoped(EndpointScope.Default).singleton {
        dbNodeIdAndAuth
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(EndpointScope.Default).singleton {
        dbBuilt
    }

    bind<UmAppDataLayer>() with scoped(EndpointScope.Default).singleton {
        val nodeIdAndAuth: NodeIdAndAuth = instance()
        val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
        val repositoryConfig =  RepositoryConfig.repositoryConfig(
            this,context.url+"UmAppDatabase/",  nodeIdAndAuth.auth,
            nodeIdAndAuth.nodeId,
            httpClient = instance(),
            json = instance()
        ){

        }
        UmAppDataLayer(
            localDb = db,
            repository = db.asRepository(repositoryConfig)
        )
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

    bind<HttpClient>() with singleton {
        httpClient
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

    bind<XML>() with singleton {
        XML {
            defaultPolicy {
                unknownChildHandler  = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
            }
        }
    }

    bind<GetVersionUseCase>() with singleton {
        GetVersionUseCaseJs()
    }

    bind<GetShowPoweredByUseCase>() with singleton {
        GetShowPoweredByUseCase(BuildConfigJs.APP_UI_SHOW_POWERED_BY.toBoolean())
    }

    bind<BulkAddPersonsFromLocalUriUseCase>() with scoped(EndpointScope.Default).provider {
        BulkAddPersonsFromLocalUriUseCaseJs(
            httpClient = instance(),
            endpoint = context,
            json = instance(),
            repo = instance<UmAppDataLayer>().repositoryOrLocalDb,
        )
    }

}