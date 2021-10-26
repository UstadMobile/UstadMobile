package com.ustadmobile.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_JdbcKt
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.view.UstadView.Companion.ARG_API_URL
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DatabaseBuilderOptions
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxAppStateManager.getCurrentState
import com.ustadmobile.redux.ReduxAppStateManager.subscribe
import com.ustadmobile.redux.ReduxDbState
import com.ustadmobile.redux.ReduxStore
import com.ustadmobile.util.Util.loadAssetsAsText
import com.ustadmobile.util.Util.loadFileContentAsMap
import com.ustadmobile.util.urlSearchParamsToMap
import com.ustadmobile.view.SplashView
import com.ustadmobile.view.SplashView.Companion.TAG_LOADED
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.*

class SplashPresenter(private val view: SplashView): DIAware {

    private var timerId = -1

    private val impl : UstadMobileSystemImpl by instance()

    private val accountManager: UstadAccountManager by instance()

    private val navController: UstadNavController by instance()

    private var dbBuildListener : (ReduxStore) -> Unit = { store ->
        if(store.appState.db.instance != null){
            window.clearTimeout(timerId)
            impl.setAppPref(TAG_LOADED,"true", this)
            view.loading = false
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

        var url = window.location.href
        url = url.substringBefore(if(url.indexOf("umapp/") != -1) "umapp/" else "#/")
        val apiUrl = urlSearchParamsToMap()[ARG_API_URL] ?: UMFileUtil.joinPaths(url, "umapp/")

        val dbName = sanitizeDbNameFromUrl(window.location.origin)

        val builderOptions = DatabaseBuilderOptions(
            UmAppDatabase::class,
            UmAppDatabase_JdbcKt::class, dbName,"./worker.sql-asm-debug.js")

        val nodeIdAndAuth = on(accountManager.activeAccount).direct.instance<NodeIdAndAuth>()

        val umAppDatabase =  DatabaseBuilder.databaseBuilder<UmAppDatabase>(builderOptions)
            .addMigrations(*UmAppDatabase.migrationList(nodeIdAndAuth.nodeId).toTypedArray())
            .build()

        //Development user setup
        val person = Person().apply {
            personUid = 0
            firstNames = "Admin"
            lastName = "User"
            admin = true
            username = "demouser"
        };

        umAppDatabase.insertPersonAndGroup(person, groupFlag = PersonGroup.PERSONGROUP_FLAG_PERSONGROUP)
        umAppDatabase.grantScopedPermission(person, Role.ALL_PERMISSIONS, ScopedGrant.ALL_TABLES, person.personUid)

        impl.navController = navController

        val localeCode = impl.getDisplayedLocale(this)
        val defaultLocale = impl.getAppPref(AppConfig.KEY_DEFAULT_LANGUAGE, this)


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