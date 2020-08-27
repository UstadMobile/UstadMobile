package com.ustadmobile.port.android

import android.content.Context
import com.toughra.ustadmobile.launcher.BuildConfig
import com.ustadmobile.port.android.impl.UstadApp
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraHttpSender
import org.acra.data.StringFormat
import org.acra.sender.HttpSender

/**
 * Created by varuna on 8/23/2017.
 *
 */
@AcraCore(reportFormat = StringFormat.JSON)
@AcraHttpSender(uri = BuildConfig.ACRA_HTTP_URI,
        basicAuthLogin = BuildConfig.ACRA_BASIC_LOGIN,
        basicAuthPassword = BuildConfig.ACRA_BASIC_PASS,
        httpMethod = HttpSender.Method.POST)
class App : UstadApp() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        ACRA.init(this)
    }

    companion object {

        val ATTACHMENTS_DIR = "attachments"
    }
}

