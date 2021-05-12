package com.ustadmobile.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.generated.locale.MessageIdMap
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.util.DummyDataPreload
import com.ustadmobile.util.DummyDataPreload.Companion.TAG_ENTRIES
import com.ustadmobile.util.StateManager
import com.ustadmobile.util.UmReactUtil.loadAssetAsText
import com.ustadmobile.util.UmReactUtil.loadMapFromLocalFile
import com.ustadmobile.view.SplashView
import com.ustadmobile.view.SplashView.Companion.LOADED_TAG
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.setInputString
import kotlinx.browser.window
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class SplashPresenter(private val view: SplashView): DIAware{

    private var timerId = -1

    private val impl : UstadMobileSystemImpl by instance()

    private val xmlPullParserFactory : XmlPullParserFactory by instance(tag = DiTag.XPP_FACTORY_NSAWARE)

    private val accountManager: UstadAccountManager by instance()

    private val messageIdMapFlipped: Map<String, Int> by lazy {
        MessageIdMap.idMap.entries.associate { (k, v) -> v to k }
    }

    suspend fun handleResourceLoading() {
        val localeCode = impl.getDisplayedLocale(this)
        val defaultAssetPath = "locales/en.xml"
        val otherAssetPath = "locales/$localeCode.xml"
        val defaultStrings = loadAssetAsText(defaultAssetPath)

        var xpp = xmlPullParserFactory.newPullParser()
        xpp.setInputString(defaultStrings)
        var currentXml = StringsXml(xpp,xmlPullParserFactory,messageIdMapFlipped, defaultAssetPath)

        if(localeCode != "en"){
            xpp = xmlPullParserFactory.newPullParser()
            val strings = loadAssetAsText(otherAssetPath)
            xpp.setInputString(strings)
            currentXml = StringsXml(xmlPullParserFactory.newPullParser(),xmlPullParserFactory, messageIdMapFlipped,otherAssetPath, currentXml)
        }
        impl.currentXml = currentXml

        loadMapFromLocalFile<HashMap<String, String>>("appconfig.json").forEach {
            impl.setAppPref(it.key, it.value, this)
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