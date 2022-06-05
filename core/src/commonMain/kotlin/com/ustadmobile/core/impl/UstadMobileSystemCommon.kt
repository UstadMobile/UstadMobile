package com.ustadmobile.core.impl

import com.soywiz.klock.DateTime
import com.soywiz.klock.years
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileConstants.LANGUAGE_NAMES
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_INTENT_MESSAGE
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.doorMainDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlin.js.JsName

/**
 * Class has all the shared function across all supported platforms
 */
abstract class UstadMobileSystemCommon {

    //for testing purpose only
    var networkManager: Any? = null

    /**
     * The currently active locale
     */
    private var locale: String = ""

    internal data class LastGoToDest(val viewName: String, val args: Map<String, String?>)

    /**
     * Options that are used to control navigation
     */
    data class UstadGoOptions(
            /**
             * If not null, this functions the same as popUpTo on Android's NavController. E.g.
             * it will pop any view between the top of the stack and the given view name. If a
             * blank string is provided ( UstadView.CURRENT_DEST ), this means popup off the
             * current destination
             */
            val popUpToViewName: String? = null,

            /**
             * If true, then popup include popUpToViewName.
             */
            val popUpToInclusive: Boolean = false,

            /**
             * Serialization strategy, i.e On JS there is no way to serialize without a strategy
             */
            val serializer: KSerializer<*>? = null) {

        companion object {
            val Default = UstadGoOptions(null, false)
        }

    }

    /**
     * The last destination that was called via the go method. This is used for testing purposes.
     */
    internal var lastDestination: LastGoToDest? = null

    /**
     * Return absolute path of the application setup file. Asynchronous.
     *
     * @param context System context
     * @param zip if true, the app setup file should be delivered within a zip.
     */

    @JsName("getAppSetupFile")
    abstract suspend fun getAppSetupFile(context: Any, zip: Boolean): String


    /**
     * Lookup a value from the app runtime configuration. These come from a properties file loaded
     * from the assets folder.
     *
     * @param key The config key to lookup
     * @param defaultVal The default value to return if the key is not found
     * @param context Systme context object
     *
     * @return The value of the key if found, if not, the default value provided
     */
    @JsName("getAppConfigString")
    abstract fun getAppConfigString(key: String, defaultVal: String?, context: Any): String?

    /**
     * Get the default first destination that the user should be taken to after logging in or
     * selecting to continue as a guest.
     */
    fun getAppConfigDefaultFirstDest(context: Any): String {
        return getAppConfigString(AppConfig.KEY_FIRST_DEST, null, context)
            ?: ContentEntryList2View.VIEW_NAME_HOME
    }

    fun goToDeepLink(deepLink: String, accountManager: UstadAccountManager, context: Any) {
        if(deepLink.contains(LINK_ENDPOINT_VIEWNAME_DIVIDER)) {
            val endpointUrl = deepLink.substringBefore(LINK_ENDPOINT_VIEWNAME_DIVIDER)
                .requirePostfix("/")
            val viewUri = deepLink.substringAfter(LINK_ENDPOINT_VIEWNAME_DIVIDER)


            val intentMessage = getString(MessageID.opening_link, context)
                .replace("%1\$s", deepLink)

            val maxDateOfBirth = if(viewUri.startsWith(ParentalConsentManagementView.VIEW_NAME)) {
                (DateTime.now() - UstadMobileConstants.ADULT_AGE_THRESHOLD.years).unixMillisLong
            }else {
                0L
            }

            //if there are any accounts that match endpoint url the user wants to work with,
            // then go to the accountmanager list in picker mode, otherwise go directly to the login
            // screen for that particular server.
            GlobalScope.launch(doorMainDispatcher()) {
                if(accountManager.activeSessionCount(maxDateOfBirth) { it == endpointUrl } > 0) {
                    val args = mapOf(ARG_NEXT to viewUri,
                        AccountListView.ARG_FILTER_BY_ENDPOINT to endpointUrl,
                        AccountListView.ARG_ACTIVE_ACCOUNT_MODE to AccountListView.ACTIVE_ACCOUNT_MODE_INLIST,
                        UstadView.ARG_TITLE to getString(MessageID.select_account, context),
                        UstadView.ARG_INTENT_MESSAGE to intentMessage,
                        UstadView.ARG_LISTMODE to ListViewMode.PICKER.toString(),
                        UstadView.ARG_MAX_DATE_OF_BIRTH to maxDateOfBirth.toString())
                    go(AccountListView.VIEW_NAME, args, context)
                }else {
                    val args = mapOf(ARG_NEXT to viewUri,
                        ARG_INTENT_MESSAGE to intentMessage,
                        ARG_SERVER_URL to endpointUrl)
                    go(Login2View.VIEW_NAME, args, context)
                }

            }
        }
    }

    /**
     * Go to a new view using a ViewLink in the form of ViewName?arg1=val1&arg2=val2 . This function
     * will parse the arguments from the query string into a map
     *
     * @param destination Destination name in the form of ViewName?arg1=val1&arg2=val2 etc.
     * @param context System context object
     * @param ustadGoOptions Go Options to specify popUpTo etc.
     */
    open fun goToViewLink(destination: String, context: Any, ustadGoOptions: UstadGoOptions = UstadGoOptions()) {
        val destinationQueryPos = destination.indexOf('?')
        if(destinationQueryPos == -1) {
            go(destination, mapOf(), context, ustadGoOptions)
        }else {
            val destArgs = UMFileUtil.parseURLQueryString(destination)
            go(destination.substring(0, destinationQueryPos), destArgs, context, ustadGoOptions)
        }
    }

    open fun go(viewName: String, args: Map<String, String?>, context: Any) {
        go(viewName, args, context, 0, UstadGoOptions(null, false))
    }

    open fun go(viewName: String, args: Map<String, String?>, context: Any, ustadGoOptions: UstadGoOptions) {
        go(viewName, args, context, 0, ustadGoOptions)
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
    @JsName("go")
    abstract fun go(viewName: String, args: Map<String, String?>, context: Any, flags: Int,
                    ustadGoOptions: UstadGoOptions)

    /**
     * Provides the currently active locale
     *
     * @return The currently active locale code, or a blank "" string meaning the locale is the system default.
     */
    @JsName("getLocale")
    open fun getLocale(context: Any) = getAppPref(PREFKEY_LOCALE, LOCALE_USE_SYSTEM, context)

    @JsName("setLocale")
    fun setLocale(locale: String, context: Any) = setAppPref(PREFKEY_LOCALE, locale, context)


    /**
     * Get a preference for the app
     *
     * @param key preference key as a string
     * @return value of that preference
     */
    @JsName("getAppPref")
    abstract fun getAppPref(key: String, context: Any): String?

    /**
     * Set a preference for the app
     *
     * @param key preference that is being set
     * @param value value to be set
     */
    abstract fun setAppPref(key: String, value: String?, context: Any)

    /**
     * Get a preference for the app.  If not set, return the provided defaultVal
     *
     * @param key preference key as string
     * @param defaultVal default value to return if not set
     * @return value of the preference if set, defaultVal otherwise
     */
    open fun getAppPref(key: String, defaultVal: String, context: Any): String {
        val valFound = getAppPref(key, context)
        return valFound ?: defaultVal
    }

    open fun getOrPutAppPref(key: String, context: Any, block: () -> String): String {
        return getAppPref(key, context) ?: block().also { newValue ->
            setAppPref(key, newValue, context)
        }
    }

    /**
     * Must provide the system's default locale (e.g. en_US.UTF-8)
     *
     * @return System locale
     */
    @JsName("getSystemLocale")
    abstract fun getSystemLocale(context: Any): String

    /**
     * Provides the language code of the currently active locale. This is different to getLocale. If
     * the locale is currently set to LOCALE_USE_SYSTEM then that language will be resolved and the
     * code returned.
     *
     * @param context
     *
     * @return The locale as the user sees it.
     */
    open fun getDisplayedLocale(context: Any): String {
        var locale = getLocale(context)
        if (locale == LOCALE_USE_SYSTEM)
            locale = getSystemLocale(context)

        return locale.substring(0, 2)
    }

    /**
     * Get a string for use in the UI using a constant int from MessageID
     */
    @JsName("getString")
    abstract fun getString(messageCode: Int, context: Any): String

    /**
     * Get list of all UI supported languages
     */
    @JsName("getAllUiLanguage")
    @Deprecated("Use getAllUiLanguagesList instead")
    open fun getAllUiLanguage(context: Any): Map<String, String> {
        val languagesConfigVal = getAppConfigString(AppConfig.KEY_SUPPORTED_LANGUAGES,
                "", context) ?: throw IllegalStateException("No SUPPORTED LANGUAGES IN APPCONFIG!")
        val languageList = languagesConfigVal.split(",")
        return languageList.map { it to (LANGUAGE_NAMES[it] ?: it) }.toMap()
    }

    /**
     * Get a list of all languages available for the UI. This is a list of pairs in the form of
     * langcode, language display name. The first entry will always be empty constant which
     * tells the app to use the system default language.
     *
     * @param context
     */
    @JsName("getAllUiLanguagesList")
    open fun getAllUiLanguagesList(context: Any): List<Pair<String, String>> {
        val languagesConfigVal = getAppConfigString(AppConfig.KEY_SUPPORTED_LANGUAGES,
                "", context) ?: throw IllegalStateException("No SUPPORTED LANGUAGES IN APPCONFIG!")
        val availableLangs = languagesConfigVal.split(",").sorted()


        return listOf(LOCALE_USE_SYSTEM to getString(MessageID.use_device_language, context)) +
                availableLangs.map { it to (LANGUAGE_NAMES[it] ?: it) }
    }

    /**
     * Return the mime type for the given extension
     *
     * @param extension the extension without the leading .
     *
     * @return The mime type if none; or null if it's not known
     */
    open fun getMimeTypeFromExtension(extension: String): String? {
        return if (MIME_TYPES_REVERSE.containsKey(extension)) MIME_TYPES_REVERSE[extension] else null

    }

    /**
     * Return the extension of the given mime type
     *
     * @param mimeType The mime type
     *
     * @return File extension for the mime type without the leading .
     */
    open fun getExtensionFromMimeType(mimeType: String): String? {
        return if (MIME_TYPES.containsKey(mimeType)) {
            MIME_TYPES[mimeType]
        } else null

    }


    /**
     * Get a boolean from the app configuration. App config is stored as a string, so this is
     * converted to a boolean using Boolean.parseBoolean
     *
     * @param key The preference key to lookup
     * @param defaultVal The default value to return if the key is not found
     * @param context System context object
     * @return The boolean value of the given preference key if found, otherwise the default value
     */
    private fun getAppConfigBoolean(key: String, defaultVal: Boolean, context: Any): Boolean {
        val strVal = getAppConfigString(key, null, context)
        return strVal?.toBoolean() ?: defaultVal
    }

    /**
     * Get a boolean from the app configuration. App config is stored as a string, so this is
     * converted to a boolean using Boolean.parseBoolean
     *
     * @param key The preference key to lookup
     * @param context System viewContext object
     * @return The boolean value of the given preference key if found, otherwise false
     */
    fun getAppConfigBoolean(key: String, context: Any): Boolean {
        return getAppConfigBoolean(key, false, context)
    }


    /**
     * Get an integer from the app configuration.
     *
     * @param key The preference key to lookup
     * @param defaultVal The default value if the preference key is not found
     * @param context System context object
     * @return The integer value of the value if found, otherwise the default value
     */
    open fun getAppConfigInt(key: String, defaultVal: Int, context: Any): Int {
        return getAppConfigString(key, "" + defaultVal, context)!!.toInt()
    }

    /**
     * Determine if the two given locales are the same as far as what the user will see.
     *
     * @param oldLocale
     *
     * @return
     */
    open fun hasDisplayedLocaleChanged(oldLocale: String?, context: Any): Boolean {
        val currentlyDisplayedLocale = getDisplayedLocale(context)
        return !(currentlyDisplayedLocale != null && oldLocale != null
                && oldLocale.substring(0, 2) == currentlyDisplayedLocale.substring(0, 2))
    }

    protected fun getContentDirName(context: Any): String? {
        return getAppConfigString(AppConfig.KEY_CONTENT_DIR_NAME, DEFAULT_CONTENT_DIR_NAME, context)
    }


    abstract fun openLinkInBrowser(url: String, context: Any)

    /**
     * Handle clicking link that decides to open on the web or to open in the browser
     */
    fun handleClickLink(url: String, accountManager: UstadAccountManager, context: Any){
        if(url.contains(LINK_ENDPOINT_VIEWNAME_DIVIDER)) {
            val components = UstadUrlComponents.parse(url)
            if(components.endpoint == accountManager.activeEndpoint.url){
                goToViewLink(components.viewUri, context)
            }else{
                goToDeepLink(url, accountManager, context)
            }
        }else{
            //Send link to system
            openLinkInBrowser(url, context)
        }
    }

    /**
     * Open the given DoorUri in the default viewer. On Android this means using a VIEW intent.
     * On the web, this will result in a file download in the browser so the user can open the
     * file
     *
     * @param context
     * @param doorUri DoorUri of item to open
     * @param mimeType MimeType to open (used to control which apps will open it on Android)
     * @param fileName Controls the name given to the file when opened on the browser
     */
    abstract fun openFileInDefaultViewer(
        context: Any,
        doorUri: DoorUri,
        mimeType: String?,
        fileName: String? = null,
    )

    companion object {
        private val MIME_TYPES = mapOf("image/jpg" to "jpg", "image/jpg" to "jpg",
                "image/jpeg" to "jpg", "image/png" to "png", "image/gif" to "gif",
                "image/svg" to "svg", "application/epub+zip" to "epub")

        private val MIME_TYPES_REVERSE = MIME_TYPES.entries.associateBy({ it.value }) { it.key }

        /**
         * Suggested name to create for content on Devices
         */
        private const val DEFAULT_CONTENT_DIR_NAME = "ustadmobileContent"

        /**
         * The return value from getLocale when the user has said to use the system's locale
         */
        const val LOCALE_USE_SYSTEM = ""

        /**
         * The preference key where we save a string for the user's locale preference
         */
        @JsName("PREFKEY_LOCALE")
        const val PREFKEY_LOCALE = "locale"


        /**
         * Ported from old CatalogPresenter
         *
         * Save/retrieve resource from user specific directory
         */
        const val USER_RESOURCE = 2


        /**
         * Ported from old CatalogPresenter
         *
         * Save/retrieve resource from shared directory
         */
        const val SHARED_RESOURCE = 4

        const val ARG_REFERRER = "ref"

        /**
         * As per Android Intent.FLAG_CLEAR_TOP
         */
        const val GO_FLAG_CLEAR_TOP = 67108864

        const val TAG_DOWNLOAD_ENABLED = "dlenabled"

        const val TAG_MAIN_COROUTINE_CONTEXT = 16

        const val TAG_DLMGR_SINGLETHREAD_CONTEXT = 32

        const val TAG_LOCAL_HTTP_PORT = 64

        const val LINK_INTENT_FILTER = "umclient"

        /**
         * The web version of the application will always live under a folder called /umapp/. The
         * viewname will start with a # (as it uses the REACT hash router). Therefor this string is
         * used as a divider between the endpoint URL and the view name and its view arguments
         */
        const val LINK_ENDPOINT_VIEWNAME_DIVIDER = "/umapp/#/"

        const val SUBDIR_SITEDATA_NAME = "sitedata"

        const val SUBDIR_CONTAINER_NAME = "container"

        const val SUBDIR_ATTACHMENTS_NAME = "attachments"

        /**
         * The RedirectFragment will remove itself from the view stack.
         */
        const val PREF_ROOT_VIEWNAME = "rootViewName"

        const val TAG_CLIENT_ID = "client_id"

    }
}