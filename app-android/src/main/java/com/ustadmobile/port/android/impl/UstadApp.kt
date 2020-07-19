package com.ustadmobile.port.android.impl

import android.content.Context
import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.db.UmAppDatabase.Companion.getInstance
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_DOWNLOAD_ENABLED
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_MAIN_COROUTINE_CONTEXT
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadRunner
import com.ustadmobile.core.networkmanager.initPicasso
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManagerAndroidImpl
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.sharedse.network.*
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcher
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcherJvm
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import org.kodein.di.*

/**
 * Note: BaseUstadApp extends MultidexApplication on the multidex variant, but extends the
 * normal android.app.Application on non-multidex variants.
 */
open class UstadApp : BaseUstadApp(), DIAware {

    val diModule = DI.Module("UstadApp-Android") {
        bind<UstadMobileSystemImpl>() with singleton { UstadMobileSystemImpl.instance }

        bind<UstadAccountManager>() with singleton {
            UstadAccountManager(instance(), applicationContext, di)
        }

        bind<UmAppDatabase>(tag = TAG_DB) with scoped(EndpointScope.Default).singleton {
            val dbName = sanitizeDbNameFromUrl(context.url)
            getInstance(applicationContext, dbName)
        }

        bind<UmAppDatabase>(tag = TAG_REPO) with scoped(EndpointScope.Default).singleton {
            instance<UmAppDatabase>(tag = TAG_DB).asRepository<UmAppDatabase>(applicationContext,
                    context.url, "", defaultHttpClient()).also {
                (it as DoorDatabaseRepository).connectivityStatus = DoorDatabaseRepository.STATUS_CONNECTED
            }
        }

        bind<EmbeddedHTTPD>() with singleton {
            EmbeddedHTTPD(0, di).also { it.start() }
        }

        bind<NetworkManagerBle>() with singleton {
            NetworkManagerBle(applicationContext, di, newSingleThreadContext("NetworkManager"))
        }

        bind<ContainerMounter>() with singleton { instance<EmbeddedHTTPD>() }

        bind<ClazzLogCreatorManager>() with singleton { ClazzLogCreatorManagerAndroidImpl(applicationContext) }

        constant(TAG_DOWNLOAD_ENABLED) with true

        bind<ContainerDownloadManager>() with scoped(EndpointScope.Default).singleton {
            ContainerDownloadManagerImpl(endpoint = context, di = di)
        }

        bind<DownloadPreparationRequester>() with scoped(EndpointScope.Default).singleton {
            DownloadPreparationRequesterAndroidImpl(applicationContext, context)
        }

        bind<ContainerDownloadRunner>() with factory {
            arg: DownloadJobItemRunnerDIArgs -> DownloadJobItemRunner(arg.downloadJobItem,
                arg.endpoint.url, di = di)
        }

        bind<CoroutineDispatcher>(tag = TAG_MAIN_COROUTINE_CONTEXT) with singleton { Dispatchers.Main }

        bind<LocalAvailabilityManager>() with scoped(EndpointScope.Default).singleton {
            LocalAvailabilityManagerImpl(di, context)
        }

        bind<ContainerFetcher>() with singleton { ContainerFetcherJvm(di) }

        bind<ContentEntryOpener>() with scoped(EndpointScope.Default).singleton {
            ContentEntryOpener(di, context, applicationContext)
        }

        registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }
    }

    override val di: DI by DI.lazy {
        import(diModule)
    }

    override fun onCreate() {
        super.onCreate()
        UstadMobileSystemImpl.instance.messageIdMap = MessageIDMap.ID_MAP
        initPicasso(applicationContext)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        Napier.base(DebugAntilog())
    }

}