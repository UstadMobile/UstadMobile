package com.ustadmobile.core.impl

import com.ustadmobile.core.util.UmContextWrapper
import com.ustadmobile.core.util.localStorage
import kotlinx.io.InputStream

/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system.
 *
 *
 * @author mike, kileha3
 */

actual class UstadMobileSystemImpl : UstadMobileSystemCommon() {

    private lateinit var appStrings : HashMap<Int, String>

    /**
     * Load all strings to be used in the app
     */
    fun preLoadAppStrings(){

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
        val mContext: UmContextWrapper = context as UmContextWrapper
        val navigateTo = "/home/$viewName"
        val navArgs: Any = mapOf(
            "relativeTo" to mContext.getActiveRoute(),
            "queryParams" to args,
            "queryParamsHandling" to "merge"
        )
        mContext.getRouter().navigate(navigateTo, navArgs)
    }

    /**
     * Get a string for use in the UI
     */
    actual fun getString(messageCode: Int, context: Any): String {
        return appStrings.getValue(messageCode)
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
        return "en_US.UTF-8"
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
        val storage = localStorage()
        return storage.getItem(key)
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
    actual fun setAppPref(key: String, value: String?, context: Any) {
        val storage = localStorage()
        if(value == null){
            storage.removeItem(key)
        }else{
            storage.setItem(key, value)
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        actual val instance: UstadMobileSystemImpl =  UstadMobileSystemImpl()
    }

    actual suspend fun getAssetAsync(context: Any, path: String): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }




}