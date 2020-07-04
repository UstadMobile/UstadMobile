package com.ustadmobile.port.android

import android.content.Context
import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier

import com.toughra.ustadmobile.launcher.BuildConfig
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.initPicasso
import com.ustadmobile.port.android.generated.MessageIDMap

import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraHttpSender
import org.acra.data.StringFormat
import org.acra.sender.HttpSender
import org.kodein.di.*

/**
 * Created by varuna on 8/23/2017.
 *
 * Note: UmBaseApplication extends MultidexApplication on the multidex variant, but extends the
 * normal android.app.Application on non-multidex variants.
 *
 */
@AcraCore(reportFormat = StringFormat.JSON)
@AcraHttpSender(uri = BuildConfig.ACRA_HTTP_URI,
        basicAuthLogin = BuildConfig.ACRA_BASIC_LOGIN,
        basicAuthPassword = BuildConfig.ACRA_BASIC_PASS,
        httpMethod = HttpSender.Method.POST)
class App : UmBaseApplication(), DIAware {

    override val di: DI by DI.lazy {
        bind<UstadMobileSystemImpl>() with singleton { UstadMobileSystemImpl.instance }
        bind<UstadAccountManager>() with singleton { UstadAccountManager.getInstance(instance(),
                applicationContext) }
    }


    override fun onCreate() {
        super.onCreate()
        UstadMobileSystemImpl.instance.messageIdMap = MessageIDMap.ID_MAP
        initPicasso(applicationContext)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        ACRA.init(this)
        Napier.base(DebugAntilog())
    }

    companion object {

        val ATTACHMENTS_DIR = "attachments"
    }
}

