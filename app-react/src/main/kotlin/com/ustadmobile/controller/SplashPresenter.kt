package com.ustadmobile.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadView.Companion.ARG_API_URL
import com.ustadmobile.redux.ReduxAppStateManager.getCurrentState
import com.ustadmobile.mocks.DummyDataPreload
import com.ustadmobile.mocks.DummyDataPreload.Companion.TAG_ENTRIES
import com.ustadmobile.util.Util.loadAssetsAsText
import com.ustadmobile.util.Util.loadFileContentAsMap
import com.ustadmobile.util.urlSearchParamsToMap
import com.ustadmobile.view.SplashView
import com.ustadmobile.view.SplashView.Companion.TAG_LOADED
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    fun handleResourceLoading() = GlobalScope.launch{
        val localeCode = impl.getDisplayedLocale(this)
        val defaultLocale = impl.getAppPref(AppConfig.KEY_DEFAULT_LANGUAGE, this)

        val hasUmApp = window.location.href.indexOf("umapp/") != -1
        val apiUrl = urlSearchParamsToMap()[ARG_API_URL]
            ?: window.location.href.substringBefore(if(hasUmApp) "umapp/" else "#/")

        val appConfigs = loadFileContentAsMap<HashMap<String, String>>("appconfig.json")
        appConfigs.forEach {
            val value = when(it.key){
                ARG_API_URL -> "${apiUrl}api/"
                else -> it.value
            }
            impl.setAppPref(it.key, value, this)
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

        val launchedBefore = (impl.getAppPref(TAG_LOADED, this)?:"false").toBoolean()

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
        impl.setAppPref(TAG_LOADED,"true", this)
        view.showMainComponent()
    }

    fun onDestroy() {
        window.clearTimeout(timerId)
    }

    override val di: DI
        get() = getCurrentState().appDi.di
}