package com.ustadmobile.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.util.UmUtil
import com.ustadmobile.util.UmUtil.loadLocalFiles
import com.ustadmobile.view.SplashView
import com.ustadmobile.view.SplashView.Companion.LOADED_TAG
import kotlinx.browser.window

class SplashPresenter(private val view: SplashView){

    private var timerId = -1

    private val impl = UstadMobileSystemImpl.instance

    suspend fun handleResourceLoading() {
        val appConfigs = loadLocalFiles<HashMap<String, String>>("appconfig.json")
        val localization = loadLocalFiles<HashMap<String, HashMap<Int, String>>>("localization.json")
        appConfigs.forEach {
            impl.setAppPref(it.key, it.value, this)
        }
        var locale = impl.getLocale(this)
        locale = if(locale.isEmpty()) "en" else locale.split("_").first()
        console.log(locale)
        localization[locale]?.let { impl.setLocaleStrings(it) }
        val loaded = (impl.getAppPref(LOADED_TAG, this)?:"false").toBoolean()
        timerId = window.setTimeout({
            view.appName = impl.getString(MessageID.app_name,this)
            view.showMainComponent()
            impl.setAppPref(LOADED_TAG,"true", this)
        }, if(loaded) 2000 else 5000)
    }

    fun onDestroy() {
        window.clearTimeout(timerId)
    }
}