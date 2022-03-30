package com.ustadmobile.core.impl

import com.ustadmobile.core.generated.locale.MessageIdMap
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.setInputString
import io.ktor.client.request.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlin.js.Date

/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system.
 *
 *
 * @author mike, kileha3
 */
actual open class UstadMobileSystemImpl(private val xppFactory: XmlPullParserFactory): UstadMobileSystemCommon() {

    private val messageIdMapFlipped: Map<String, Int> by lazy {
        MessageIdMap.idMap.entries.associate { (k, v) -> v to k }
    }

    private lateinit var defaultStringsXml: StringsXml

    private var foreignStringXml: StringsXml? = null

    var defaultTranslations: Pair<String,String> = Pair("","")
        set(value) {
            val xpp = xppFactory.newPullParser()
            xpp.setInputString(value.second)
            defaultStringsXml = StringsXml(xpp,xppFactory,messageIdMapFlipped, value.first)
            field = value
        }

    var currentTranslations: Pair<String,String> = Pair("","")
        set(value) {
            val xpp = xppFactory.newPullParser()
            xpp.setInputString(value.second)
            foreignStringXml = StringsXml(xpp,xppFactory,messageIdMapFlipped, value.first, defaultStringsXml)
            field = value
        }

    lateinit var navController: UstadNavController

    /**
     * Get a string for use in the UI
     */
    actual override fun getString(messageCode: Int, context: Any): String {
        return (foreignStringXml ?: defaultStringsXml)[messageCode]
    }


    /**
     * Must provide the system's default locale (e.g. en_US.UTF-8)
     *
     * @return System locale
     */
    actual override fun getSystemLocale(context: Any): String {
        return "${window.navigator.language}.UTF-8"
    }

    /**
     * Get a preference for the app
     *
     * @param key preference key as a string
     * @return value of that preference
     */

    actual override fun getAppPref(key: String, context: Any): String? {
        return localStorage.getItem(key)
    }

    /**
     * Return absolute path of the application setup file. Asynchronous.
     *
     * @param context System context
     * @param zip if true, the app setup file should be delivered within a zip.
     */
    actual override suspend fun getAppSetupFile(context: Any, zip: Boolean): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Set a preference for the app
     * @param key preference that is being set
     * @param value value to be set
     */
    actual override fun setAppPref(key: String, value: String?, context: Any) {
        if(value == null){
            localStorage.removeItem(key)
        }else{
            localStorage.setItem(key, value)
        }
    }

    /**
     * Gives a string with the version number
     *
     * @return String with version number
     */
    actual fun getVersion(context: Any): String {
        return ""
    }

    /**
     * Get the build timestamp
     *
     * @param context System context object
     *
     * @return Build timestamp in ms since epoch
     */
    actual fun getBuildTimestamp(context: Any): Long = Date().getTime().toLong()

    /**
     * Lookup a value from the app runtime configuration. These come from a properties file loaded
     * from the assets folder, the path of which is set by the manifest preference
     * com.sutadmobile.core.appconfig .
     *
     * @param key The config key to lookup
     * @param defaultVal The default value to return if the key is not found
     * @param context Systme context object
     *
     * @return The value of the key if found, if not, the default value provided
     */
    actual override fun getAppConfigString(key: String, defaultVal: String?, context: Any): String? {
        val value =  localStorage.getItem(key)
        return  value ?: defaultVal
    }


    actual fun openFileInDefaultViewer(context: Any, path: String, mimeType: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual companion object {

        /**
         * Get an instance of the system implementation - relies on the platform
         * specific factory method
         *
         * @return A singleton instance
         */
        actual var instance: UstadMobileSystemImpl =  UstadMobileSystemImpl(XmlPullParserFactory.newInstance())
    }

    /**
     * The main method used to go to a new view. This is implemented at the platform level. On
     * Android this involves starting a new activity with the arguments being turned into an
     * Android bundle. On J2ME it creates a new Form and shows it, on iOS it looks up the related
     * UIViewController.
     *
     * @param viewName The name of the view to go to: This should match the view's interface .VIEW_NAME constant
     * @param args (Optional) Hahstable of arguments for the new view (e.g. catalog/container url etc)
     * @param context System context object
     */
    actual override fun go(viewName: String, args: Map<String, String?>, context: Any,
                           flags: Int,
                           ustadGoOptions: UstadGoOptions) {
        navController.navigate(viewName,args as Map<String, String>,ustadGoOptions)
    }

    actual fun popBack(popUpToViewName: String, popUpInclusive: Boolean, context: Any) {
        navController.popBackStack(popUpToViewName,popUpInclusive)
    }

    /**
     * Open the given link in a browser and/or tab depending on the platform
     */
    actual fun openLinkInBrowser(url: String, context: Any) {
        window.open(url, "_blank")
    }
}