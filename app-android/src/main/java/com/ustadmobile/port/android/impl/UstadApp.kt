package com.ustadmobile.port.android.impl

import android.content.Context
import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_DOWNLOAD_ENABLED
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.initPicasso
import com.ustadmobile.port.android.generated.MessageIDMap
import org.kodein.di.*

/**
 * Note: BaseUstadApp extends MultidexApplication on the multidex variant, but extends the
 * normal android.app.Application on non-multidex variants.
 */
open class UstadApp : BaseUstadApp(), DIAware {

    val diModule = DI.Module("UstadApp-Android") {
        bind<UstadMobileSystemImpl>() with singleton { UstadMobileSystemImpl.instance }
//        bind<UstadAccountManager>() with singleton { UstadAccountManager.getInstance(instance(),
//                applicationContext) }

//        bind<UmAppDatabase>(tag = TAG_DB) with provider { instance<UstadAccountManager>().activeDatabase }
//        bind<UmAppDatabase>(tag = TAG_REPO) with provider { instance<UstadAccountManager>().activeRepository }
        constant(TAG_DOWNLOAD_ENABLED) with true
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