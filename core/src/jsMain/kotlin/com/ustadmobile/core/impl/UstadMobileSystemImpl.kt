package com.ustadmobile.core.impl

import com.ustadmobile.core.controller.ContentEntryDetailPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.view.HomeView
import kotlinx.io.InputStream
import kotlin.browser.localStorage
import kotlin.browser.window
import kotlin.js.json

/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system.
 *
 *
 * @author mike, kileha3
 */
actual open class UstadMobileSystemImpl : UstadMobileSystemCommon() {

    @JsName("stringMap")
    private var stringMap : Any = Any()

    private var isBaseHomePath = false

    /**
     * Load all strings to be used in the app
     */
    @JsName("setLocaleStrings")
    fun setLocaleStrings(values : Any){
        this.stringMap = values
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
    actual override fun go(viewName: String, args: Map<String, String?>, context: Any, flags: Int) {
        val umContext: dynamic = context
        val basePath = if(args.containsKey(ARG_CONTENT_ENTRY_UID)
                || isHomeBasePath(args)) "/${HomeView.VIEW_NAME}/" else "/"
        umContext.router.navigate(arrayOf(basePath + viewName), mapToRouterParams(args))
    }

    private fun isHomeBasePath(args: Map<String, String?>): Boolean{
        var isHomePath = false
        for ((key, _) in args) {
            if(key == "path"){
                isHomePath = true
            }
        }
        return isHomePath
    }


    private fun mapToRouterParams(args: Map<String, String?>): Any{
        val params = json()
        for ((key, value) in args) {
            if(key != "path"){
                params[key] = value
            }else{
                isBaseHomePath = true
            }
        }
        return json("queryParams" to params, "queryParamsHandling" to "merge")
    }

    /**
     * Get a string for use in the UI
     */
    @JsName("getString")
    actual fun getString(messageCode: Int, context: Any): String {
        val map : dynamic = this.stringMap
        return map[messageCode].toString()
    }

    /**
     * Get storage directories
     *
     * @param context Platform specific context
     * @param callback Storage dir list callback
     */
    actual override fun getStorageDirs(context: Any, callback: UmResultCallback<List<UMStorageDir>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
     * Get an asset (from files that are in core/src/flavorName/assets)
     *
     */
    actual fun getAsset(context: Any, path: String, callback: UmCallback<InputStream>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
     * @param callback callback to call when complete or if any error occurs.
     */
    actual override fun getAppSetupFile(context: Any, zip: Boolean, callback: UmCallback<*>) {
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Get the build timestamp
     *
     * @param context System context object
     *
     * @return Build timestamp in ms since epoch
     */
    actual fun getBuildTimestamp(context: Any): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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

    /**
     * Wrapper to retrieve preference keys from the system Manifest.
     *
     * On Android: uses meta-data elements on the application element in AndroidManifest.xml
     * On J2ME: uses the jad file
     *
     * @param key The key to lookup
     * @param context System context object
     *
     * @return The value of the manifest preference key if found, null otherwise
     */
    actual override fun getManifestPreference(key: String, context: Any): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun openFileInDefaultViewer(context: Any, path: String, mimeType: String?, callback: UmCallback<Any>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Get an asset Synchronously
     * @param context
     * @param path
     * @return
     */
    actual fun getAssetSync(context: Any, path: String): InputStream {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Returns the system base directory to work from
     *
     * @return
     */
    actual fun getSystemBaseDir(context: Any): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Check if the directory is writable
     * @param dir Directory to be checked
     * @return True if is writable otherwise is read only
     */
    actual fun canWriteFileInDir(dirPath: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual companion object {
        /**
         * Get an instance of the system implementation - relies on the platform
         * specific factory method
         *
         * @return A singleton instance
         */
        actual var instance: UstadMobileSystemImpl =  UstadMobileSystemImpl()
    }

    actual suspend fun getAssetAsync(context: Any, path: String): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }



    /**
     * Get asset as an input stream asynchronously
     */
    actual suspend fun getAssetInputStreamAsync(context: Any, path: String): InputStream {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override suspend fun getStorageDirsAsync(context: Any): List<UMStorageDir?> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}