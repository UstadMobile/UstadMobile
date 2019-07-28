/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */

package com.ustadmobile.core.impl
import kotlinx.io.InputStream
import kotlin.jvm.JvmStatic


/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system.
 *
 *
 * @author mike, kileha3
 */
expect open class UstadMobileSystemImpl : UstadMobileSystemCommon {


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
    override fun go(viewName: String, args: Map<String, String?>, context: Any, flags: Int)

    /**
     * Get a string for use in the UI
     */
    fun getString(messageCode: Int, context: Any): String


    /**
     * Get storage directories
     *
     * @param context Platform specific context
     * @param callback Storage dir list callback
     */

    override fun getStorageDirs(context: Any, callback: UmResultCallback<List<UMStorageDir>>)

    override suspend fun getStorageDirsAsync(context: Any): List<UMStorageDir?>

    /**
     * Must provide the system's default locale (e.g. en_US.UTF-8)
     *
     * @return System locale
     */
    override fun getSystemLocale(context: Any): String

    /**
     * Get an asset (from files that are in core/src/flavorName/assets)
     *
     */
    fun getAsset(context: Any, path: String, callback: UmCallback<InputStream>)


    /**
     * Get asset as an input stream asynchronously
     */
    suspend fun getAssetInputStreamAsync(context: Any, path: String) : InputStream

    /**
     * Get a preference for the app
     *
     * @param key preference key as a string
     * @return value of that preference
     */
    override fun getAppPref(key: String, context: Any): String?

    /**
     * Return absolute path of the application setup file. Asynchronous.
     *
     * @param context System context
     * @param zip if true, the app setup file should be delivered within a zip.
     * @param callback callback to call when complete or if any error occurs.
     */
    override fun getAppSetupFile(context: Any, zip: Boolean, callback: UmCallback<*>)


    /**
     * Set a preference for the app
     * @param key preference that is being set
     * @param value value to be set
     */
    override fun setAppPref(key: String, value: String?, context: Any)


    /**
     * Gives a string with the version number
     *
     * @return String with version number
     */
    fun getVersion(context: Any): String

    /**
     * Get the build timestamp
     *
     * @param context System context object
     *
     * @return Build timestamp in ms since epoch
     */
    fun getBuildTimestamp(context: Any): Long


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
    override fun getAppConfigString(key: String, defaultVal: String?, context: Any): String?


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
    override fun getManifestPreference(key: String, context: Any): String?


    fun openFileInDefaultViewer(context: Any, path: String, mimeType: String?,
                                         callback: UmCallback<Any>)

    /**
     * Get an asset Synchronously
     * @param context
     * @param path
     * @return
     */
    fun getAssetSync(context: Any, path: String): InputStream

    suspend fun getAssetAsync(context: Any, path: String): ByteArray

    /**
     * Returns the system base directory to work from
     *
     * @return
     */
    fun getSystemBaseDir(context: Any): String


    /**
     * Check if the directory is writable
     * @param dir Directory to be checked
     * @return True if is writable otherwise is read only
     */
    fun canWriteFileInDir(dirPath: String): Boolean

    companion object {

        /**
         * Get an instance of the system implementation - relies on the platform
         * specific factory method
         *
         * @return A singleton instance
         */
        @JvmStatic
        var instance: UstadMobileSystemImpl
    }
}