package com.ustadmobile.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.util.DummyDataPreload
import com.ustadmobile.util.DummyDataPreload.Companion.TAG_ENTRIES
import com.ustadmobile.util.StateManager
import com.ustadmobile.util.UmReactUtil.loadAssetAsText
import com.ustadmobile.util.UmReactUtil.loadMapFromLocalFile
import com.ustadmobile.view.SplashView
import com.ustadmobile.view.SplashView.Companion.LOADED_TAG
import kotlinx.browser.window
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class SplashPresenter(private val view: SplashView): DIAware{

    private var timerId = -1

    private val impl : UstadMobileSystemImpl by instance()

    private val accountManager: UstadAccountManager by instance()

    suspend fun handleResourceLoading() {
        val localeCode = impl.getDisplayedLocale(this)
        val defaultLocale = impl.getAppPref(AppConfig.KEY_DEFAULT_LANGUAGE, this)

        val appConfigs = loadMapFromLocalFile<HashMap<String, String>>("appconfig.json")
        appConfigs.forEach {
            impl.setAppPref(it.key, it.value, this)
        }


        val defaultAssetPath = "locales/$defaultLocale.xml"
        val defaultStrings = loadAssetAsText(defaultAssetPath)
        impl.defaultTranslations = Pair(defaultAssetPath , defaultStrings)
        impl.currentTranslations = Pair(defaultAssetPath , defaultStrings)

        if(localeCode != defaultLocale){
            val currentAssetPath = "locales/$localeCode.xml"
            val currentStrings = loadAssetAsText(currentAssetPath)
            impl.currentTranslations = Pair(currentAssetPath, currentStrings)
        }

        val loaded = (impl.getAppPref(LOADED_TAG, this)?:"false").toBoolean()

        view.appName = impl.getString(MessageID.app_name,this)
        view.rtlSupported = impl.isRTLSupported(this)

        val preload = DummyDataPreload(accountManager.activeAccount.endpointUrl, di)
        if(!loaded || impl.getAppPref(TAG_ENTRIES, this) == null){
            preload.verifyAndImportEntries(::goToNext)
        }
        if(loaded){
            timerId = window.setTimeout({ goToNext()}, 200)
        }
    }

    private fun goToNext(){
        view.showMainComponent()
        impl.setAppPref(LOADED_TAG,"true", this)
    }

    fun onDestroy() {
        window.clearTimeout(timerId)
    }

    override val di: DI
        get() = StateManager.getCurrentState().di
}