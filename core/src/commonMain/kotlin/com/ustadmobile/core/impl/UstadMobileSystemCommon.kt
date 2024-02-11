package com.ustadmobile.core.impl

import com.russhwolf.settings.Settings
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.door.DoorUri
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.KSerializer
import kotlin.js.JsName
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import dev.icerock.moko.resources.PluralsResource

/**
 * Class has all the shared function across all supported platforms
 */
abstract class UstadMobileSystemCommon(
    private val settings: Settings,
    protected val langConfig: SupportedLanguagesConfig,
) {

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

            val clearStack: Boolean = false,

            /**
             * Serialization strategy, i.e On JS there is no way to serialize without a strategy
             */
            val serializer: KSerializer<*>? = null,

            ) {

        companion object {
            val Default = UstadGoOptions(null, false)
        }

    }


    /**
     * Represents a user interface language
     */
    data class UiLanguage(val langCode: String, val langDisplay: String)

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
     * Get the default first destination that the user should be taken to after logging in or
     * selecting to continue as a guest.
     */
    fun getDefaultFirstDest(): String {
        return ClazzListViewModel.DEST_NAME_HOME
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

    abstract fun getString(stringResource: StringResource): String

    abstract fun formatString(
        stringResource: StringResource,
        vararg args: Any
    ): String

    abstract fun formatPlural(pluralsResource: PluralsResource, number: Int): String

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