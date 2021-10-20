package com.ustadmobile.controller

import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_JdbcKt
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.UstadView.Companion.ARG_API_URL
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DatabaseBuilderOptions
import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxAppStateManager.getCurrentState
import com.ustadmobile.redux.ReduxAppStateManager.subscribe
import com.ustadmobile.redux.ReduxDbState
import com.ustadmobile.redux.ReduxDiState
import com.ustadmobile.redux.ReduxStore
import com.ustadmobile.util.Util.loadAssetsAsText
import com.ustadmobile.util.Util.loadFileContentAsMap
import com.ustadmobile.util.urlSearchParamsToMap
import com.ustadmobile.view.SplashView
import com.ustadmobile.view.SplashView.Companion.TAG_LOADED
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.*
import react.setState

class SplashPresenter(private val view: SplashView): DIAware {

    private var timerId = -1

    private val impl : UstadMobileSystemImpl by instance()

    private val navController: UstadNavController by instance()

    private var dbBuildListener : (ReduxStore) -> Unit = { store ->
        if(store.appState.db.instance != null){
            window.clearTimeout(timerId)
            impl.setAppPref(TAG_LOADED,"true", this)
            view.loading = false
        }else {
            throw IllegalArgumentException("Database failed to build")
        }
    }


    fun onCreate(){
        val directionAttributeValue = if(impl.isRtlActive()) "rtl" else "ltr"
        val rootElement = document.getElementById("root")
        rootElement?.setAttribute("dir",directionAttributeValue)
        subscribe(dbBuildListener)
        setUpResources()

    }

    /**
     * Initialize all resources needed for the app to run
     */
    private fun setUpResources() = GlobalScope.launch{
        val builderOptions = DatabaseBuilderOptions(
            UmAppDatabase::class,
            UmAppDatabase_JdbcKt::class, "UstadMobileDb","./worker.sql-asm-debug.js")

        val umAppDatabase =  DatabaseBuilder.databaseBuilder<UmAppDatabase>(builderOptions).build()


        impl.navController = navController

        val localeCode = impl.getDisplayedLocale(this)
        val defaultLocale = impl.getAppPref(AppConfig.KEY_DEFAULT_LANGUAGE, this)

        var url = window.location.href
        url = url.substringBefore(if(url.indexOf("umapp/") != -1) "umapp/" else "#/")
        val apiUrl = urlSearchParamsToMap()[ARG_API_URL] ?: UMFileUtil.joinPaths(url, "umapp/")

        val appConfigs = loadFileContentAsMap<HashMap<String, String>>("appconfig.json")
        appConfigs.forEach {
            val value = when(it.key){
                ARG_API_URL -> apiUrl
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
        view.appName = impl.getString(MessageID.app_name,this)

        //setup is done - notify subscribing element
        dispatch(ReduxDbState(umAppDatabase))
    }


    fun onDestroy() {
        window.clearTimeout(timerId)
    }

    override val di: DI
        get() = getCurrentState().di.instance
}