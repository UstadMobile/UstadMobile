package com.ustadmobile.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.util.UmReactUtil
import com.ustadmobile.util.UmReactUtil.loadMapFromLocalFile
import com.ustadmobile.view.SplashView
import com.ustadmobile.view.SplashView.Companion.LOADED_TAG
import kotlinx.browser.window

class SplashPresenter(private val view: SplashView){

    private var timerId = -1

    private val impl = UstadMobileSystemImpl.instance

    suspend fun handleResourceLoading() {
        val appConfigs = loadMapFromLocalFile<HashMap<String, String>>("appconfig.json")
        val localization = loadMapFromLocalFile<HashMap<String, HashMap<Int, String>>>("localization.json")
        val entries = UmReactUtil.loadListFromFiles<List<Any>>("entries.json")
        val languages = UmReactUtil.loadListFromFiles<List<Language>>("languages.json")
        js("window.entries = entries")
        js("window.languages = languages")
        appConfigs.forEach {
            impl.setAppPref(it.key, it.value, this)
        }
        var locale = impl.getLocale(this)
        locale = if(locale.isEmpty()) "en" else locale.split("_").first()
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