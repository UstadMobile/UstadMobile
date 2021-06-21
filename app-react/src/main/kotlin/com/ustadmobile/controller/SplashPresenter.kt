package com.ustadmobile.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.redux.ReduxAppStateManager
import com.ustadmobile.redux.ReduxAppStateManager.getCurrentState
import com.ustadmobile.util.DummyDataPreload
import com.ustadmobile.util.DummyDataPreload.Companion.TAG_ENTRIES
import com.ustadmobile.util.Util.loadAssetsAsText
import com.ustadmobile.util.Util.loadFileContentAsMap
import com.ustadmobile.view.SplashView
import com.ustadmobile.view.SplashView.Companion.LOADED_TAG
import kotlinx.browser.document
import kotlinx.browser.window
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class SplashPresenter(private val view: SplashView): DIAware{

    private var timerId = -1

    private val impl : UstadMobileSystemImpl by instance()

    private val accountManager: UstadAccountManager by instance()

    fun onCreate(){
        val directionAttributeValue = if(impl.isRtlActive()) "rtl" else "ltr"
        val rootElement = document.getElementById("root")
        rootElement?.setAttribute("dir",directionAttributeValue)
    }

    suspend fun handleResourceLoading() {
        val localeCode = impl.getDisplayedLocale(this)
        val defaultLocale = impl.getAppPref(AppConfig.KEY_DEFAULT_LANGUAGE, this)

        val appConfigs = loadFileContentAsMap<HashMap<String, String>>("appconfig.json")
        appConfigs.forEach {
            impl.setAppPref(it.key, it.value, this)
        }


        val defaultAssetPath = "locales/$defaultLocale.xml"
        val defaultStrings = loadAssetsAsText(defaultAssetPath)
        impl.defaultTranslations = Pair(defaultAssetPath , defaultStrings)
        impl.currentTranslations = Pair(defaultAssetPath , defaultStrings)

        if(localeCode != defaultLocale){
            val currentAssetPath = "locales/$localeCode.xml"
            val currentStrings = loadAssetsAsText(currentAssetPath)
            impl.currentTranslations = Pair(currentAssetPath, currentStrings)
        }

        val launchedBefore = (impl.getAppPref(LOADED_TAG, this)?:"false").toBoolean()

        view.appName = impl.getString(MessageID.app_name,this)

        val dataPreload = DummyDataPreload(accountManager.activeAccount.endpointUrl, di)
        if(!launchedBefore || impl.getAppPref(TAG_ENTRIES, this) == null){
            dataPreload.verifyAndImportEntries(::showMainComponent)
        }
        if(launchedBefore){
            timerId = window.setTimeout({
                showMainComponent()
            }, 200)
        }
    }

    private fun showMainComponent(){
        window.clearTimeout(timerId)
        impl.setAppPref(LOADED_TAG,"true", this)
        view.showMainComponent()
    }

    fun onDestroy() {
        window.clearTimeout(timerId)
    }

    override val di: DI
        get() = getCurrentState().appDi.di
}