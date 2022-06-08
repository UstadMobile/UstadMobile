package com.ustadmobile.core.impl

import com.ustadmobile.core.generated.locale.MessageIdMap
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.setInputString
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlin.js.Date
import com.ustadmobile.door.DoorUri
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLAnchorElement

/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system.
 *
 *
 * @author mike, kileha3
 * @param defaultStringsXmlStr The string of the strings_ui.xml file for English (must be loaded
 *        (asynchronously in advance)
 * @param displayLocaleStringsXmlStr The String of the strings_ui.xml file for the display locale
 *        if the display locale is not English.
 */
actual open class UstadMobileSystemImpl(
    private val xppFactory: XmlPullParserFactory,
    private val navController: UstadNavController,
    defaultStringsXmlStr: String,
    displayLocaleStringsXmlStr: String?
): UstadMobileSystemCommon() {

    private val messageIdMapFlipped: Map<String, Int> by lazy {
        MessageIdMap.idMap.entries.associate { (k, v) -> v to k }
    }

    private val defaultStringsXml: StringsXml

    private val displayLocaleStringsXml: StringsXml?

    init {
        val defaultXpp = xppFactory.newPullParser()
        defaultXpp.setInputString(defaultStringsXmlStr)
        defaultStringsXml = StringsXml(defaultXpp, xppFactory, messageIdMapFlipped, "en")

        displayLocaleStringsXml = if(displayLocaleStringsXmlStr != null) {
            val foreignXpp = xppFactory.newPullParser()
            foreignXpp.setInputString(displayLocaleStringsXmlStr)
            StringsXml(foreignXpp, xppFactory, messageIdMapFlipped,
                displayedLocale, defaultStringsXml)
        }else {
            null
        }
    }

    /**
     * Get a string for use in the UI
     */
    actual override fun getString(messageCode: Int, context: Any): String {
        return (displayLocaleStringsXml ?: defaultStringsXml)[messageCode]
    }


    /**
     * Must provide the system's default locale (e.g. en_US.UTF-8)
     *
     * @return System locale
     */
    actual override fun getSystemLocale(context: Any): String {
        return systemLocale
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


    override fun openFileInDefaultViewer(
        context: Any,
        doorUri: DoorUri,
        mimeType: String?,
        fileName: String?,
    ) {
        val aElement = document.createElement("a") as HTMLAnchorElement
        GlobalScope.launch {
            aElement.asDynamic().style.display = "none"
            aElement.href = doorUri.toString()
            fileName?.also { aElement.download = it }
            aElement.click()
        }
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
    actual override fun openLinkInBrowser(url: String, context: Any) {
        window.open(url, "_blank")
    }



    /**
     * Provide language UI directionality
     * @return TRUE if the UI direction is RTL otherwise it's FALSE
     */
    fun isRtlActive(): Boolean {
        return displayedLocale in UstadMobileConstants.RTL_LANGUAGES
    }

    actual companion object {

        /**
         * Locale functions are provided here because Javascript needs to load resource XML files
         * asynchronously before SystemImpl is instantiated.
         */
        private val systemLocale: String
            get() = "${window.navigator.language}.UTF-8"

        val displayedLocale: String
            get() {
                val localePref = localStorage.getItem(PREFKEY_LOCALE) ?: LOCALE_USE_SYSTEM
                return if(localePref == LOCALE_USE_SYSTEM) {
                    systemLocale.substring(0, 2)
                }else {
                    localePref
                }
            }
    }
}