package com.ustadmobile.port.android

import android.content.Context
import com.toughra.ustadmobile.launcher.BuildConfig
import com.ustadmobile.port.android.impl.UstadApp
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.HttpSender

/**
 * Created by varuna on 8/23/2017.
 *
 */
class App : UstadApp() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if(BuildConfig.ACRA_HTTP_URI.isNotBlank()) {
            initAcra {
                reportFormat = StringFormat.JSON
                httpSender {
                    uri = BuildConfig.ACRA_HTTP_URI
                    basicAuthLogin = BuildConfig.ACRA_BASIC_LOGIN
                    basicAuthPassword = BuildConfig.ACRA_BASIC_PASS
                    httpMethod = HttpSender.Method.POST
                }
            }
        }
    }

    companion object {

        val ATTACHMENTS_DIR = "attachments"
    }
}

