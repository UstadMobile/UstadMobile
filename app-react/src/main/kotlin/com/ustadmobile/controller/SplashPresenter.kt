package com.ustadmobile.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabaseJsImplementations
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.AppConfig.KEY_API_URL
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DatabaseBuilderOptions
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxAppStateManager.getCurrentState
import com.ustadmobile.redux.ReduxDbState
import com.ustadmobile.util.Util.loadAssetsAsText
import com.ustadmobile.util.Util.loadFileContentAsMap
import com.ustadmobile.util.urlSearchParamsToMap
import com.ustadmobile.view.SplashView
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

class SplashPresenter(private val view: SplashView): DIAware {

    private val impl : UstadMobileSystemImpl by instance()

    fun onCreate(){
        val directionAttributeValue = if(impl.isRtlActive()) "rtl" else "ltr"
        val rootElement = document.getElementById("root")
        rootElement?.setAttribute("dir",directionAttributeValue)
        val navController: UstadNavController by instance()
        impl.navController = navController
        setUpResources()
    }

    /**
     * Initialize all resources needed for the app to run, database included
     */
    private fun setUpResources() = GlobalScope.launch {
        val url = window.location.href
        val apiUrl = urlSearchParamsToMap()[KEY_API_URL]
            ?: impl.getAppPref(KEY_API_URL,this)
            ?: url.substringBefore(if(url.indexOf("umapp/") != -1) "umapp/" else "#/")

        val dbName = sanitizeDbNameFromUrl(window.location.origin)

        val builderOptions = DatabaseBuilderOptions(
            UmAppDatabase::class,
            UmAppDatabaseJsImplementations, dbName,"./worker.sql-wasm.js")



        val dbBuilder =  DatabaseBuilder.databaseBuilder(builderOptions)
        val umAppDatabase =  dbBuilder.build()

        dispatch(ReduxDbState(umAppDatabase))

        val accountManager: UstadAccountManager by instance()
        val nodeIdAndAuth:NodeIdAndAuth by di.on(accountManager.activeAccount).instance()
        dbBuilder.addCallback(ContentJobItemTriggersCallback())
            .addMigrations(*UmAppDatabase.migrationList(nodeIdAndAuth.nodeId).toTypedArray())

        val localeCode = impl.getDisplayedLocale(this)
        val defaultLocale = impl.getAppPref(AppConfig.KEY_DEFAULT_LANGUAGE, this)

        val appConfigs = loadFileContentAsMap<HashMap<String, String>>("appconfig.json")
        appConfigs.forEach {
            val value = when(it.key){
                KEY_API_URL -> apiUrl
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
        view.loading = false
        impl.setAppPref(SplashView.TAG_LOADED,"false", this)
    }

    override val di: DI
        get() = getCurrentState().di.instance
}