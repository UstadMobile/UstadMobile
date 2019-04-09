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

import com.ustadmobile.core.impl.http.UmHttpCall
import com.ustadmobile.core.impl.http.UmHttpRequest
import com.ustadmobile.core.impl.http.UmHttpResponse
import com.ustadmobile.core.impl.http.UmHttpResponseCallback
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.lib.util.UMUtil

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlSerializer

import java.io.IOException
import java.io.InputStream
import java.util.Hashtable


/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system.
 *
 *
 * @author mike
 */
abstract class UstadMobileSystemImpl {

    /**
     * Returns whether or not the init method has already been run
     *
     * @return true if init has been called with a first context used to load certain resources,
     * false otherwise
     */
    protected var isInitialized: Boolean = false
        private set

    /**
     * The currently active locale
     */
    private var locale: String? = null

    /**
     * Get access to the logger to use on this implementation
     *
     * @return Platform logger
     */
    abstract val logger: UMLog

    /**
     * Do any required startup operations: init will be called on creation
     *
     * This must make the shared content directory if it does not already exist
     */
    open fun init(context: Any) {
        UstadMobileSystemImpl.l(UMLog.DEBUG, 519, null)
        //We don't need to do init again
        if (isInitialized) {
            return
        }

        isInitialized = true
    }

    /**
     * Go to a new view : This is simply a convenience wrapper for go(viewName, args, context):
     * it will parse the a destination into the viewname and arguments, and then build a hashtable
     * to pass on.
     *
     * @param destination Destination name in the form of ViewName?arg1=val1&arg2=val2 etc.
     * @param context System context object
     */
    fun go(destination: String?, context: Any) {
        val destinationQueryPos = destination!!.indexOf('?')
        if (destinationQueryPos == -1) {
            go(destination, null, context)
        } else {
            go(destination.substring(0, destinationQueryPos), UMFileUtil.parseURLQueryString(
                    destination) as Map<String, String>?, context)
        }
    }

    fun go(viewName: String, args: Map<String, String>?, context: Any) {
        go(viewName, args, context, 0)
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
    abstract fun go(viewName: String, args: Map<String, String>?, context: Any, flags: Int)

    /**
     * Provides the currently active locale
     *
     * @return The currently active locale code, or a blank "" string meaning the locale is the system default.
     */
    open fun getLocale(context: Any): String? {
        return locale
    }

    /**
     * Provides the language code of the currently active locale. This is different to getLocale. If
     * the locale is currently set to LOCALE_USE_SYSTEM then that language will be resolved and the
     * code returned.
     *
     * @param context
     *
     * @return The locale as the user sees it.
     */
    fun getDisplayedLocale(context: Any): String? {
        var locale = getLocale(context)
        if (locale == LOCALE_USE_SYSTEM)
            locale = getSystemLocale(context)

        return locale
    }

    open fun setLocale(locale: String, context: Any) {
        this.locale = locale
    }

    /**
     * Starts the user interface for the app
     */
    fun startUI(context: Any) {
        val activeAccount = UmAccountManager.getActiveAccount(context)


        if (getAppConfigBoolean(AppConfig.KEY_FIRST_DEST_LOGIN_REQUIRED, context) && activeAccount == null) {
            go(Login2View.VIEW_NAME, null, context)
        } else {
            go(getAppConfigString(AppConfig.KEY_FIRST_DEST, null, context), context)
        }
    }

    /**
     * Get a string for use in the UI
     */
    abstract fun getString(messageCode: Int, context: Any): String

    /**
     * Gets the cache directory for the platform for either user specific
     * cache contents / shared cache contents
     *
     * @param mode USER_RESOURCE or SHARED_RESOURCE
     * @return String filepath to the cache dir for that mode
     */
    abstract fun getCacheDir(mode: Int, context: Any): String

    /**
     * Get storage directories
     *
     * @param mode bitmask flag of USER_RESOURCE or SHARED_RESOURCE
     * @return Array of storage
     */
    @Deprecated("")
    abstract fun getStorageDirs(mode: Int, context: Any): Array<UMStorageDir?>

    abstract fun getStorageDirs(context: Any, callback: UmResultCallback<List<UMStorageDir>>)

    /**
     * Provides the path to the shared content directory
     *
     * @return URI of the shared content directory
     */
    @Deprecated("- Use getStorageDirs and getCacheDirinstead\n" +
            "      ")
    abstract fun getSharedContentDir(context: Any): String

    /**
     * Provides the path to content directory for a given user
     *
     * @param username username to get content dir for
     * @return URI of the given users content directory
     */
    @Deprecated("use getStorageDirs and getCacheDir instead\n" +
            "     \n" +
            "      ")
    abstract fun getUserContentDirectory(context: Any, username: String): String


    /**
     * Must provide the system's default locale (e.g. en_US.UTF-8)
     *
     * @return System locale
     */
    abstract fun getSystemLocale(context: Any): String


    /**
     * Get an asset (from files that are in core/src/flavorName/assets)
     *
     */
    abstract fun getAsset(context: Any, path: String, callback: UmCallback<InputStream>)

    /**
     * Get a preference for the app
     *
     * @param key preference key as a string
     * @return value of that preference
     */
    abstract fun getAppPref(key: String, context: Any): String?

    /**
     * Get a preference for the app.  If not set, return the provided defaultVal
     *
     * @param key preference key as string
     * @param defaultVal default value to return if not set
     * @return value of the preference if set, defaultVal otherwise
     */
    fun getAppPref(key: String, defaultVal: String, context: Any): String {
        val valFound = getAppPref(key, context)
        return valFound ?: defaultVal
    }

    /**
     * Set a preference for the app
     * @param key preference that is being set
     * @param value value to be set
     */
    abstract fun setAppPref(key: String, value: String, context: Any)


    /**
     * Make an asynchronous http request. This can (on platforms with a filesystem) rely on the
     * caching directory.
     *
     * @param request request to make
     * @param responseListener response listener to receive response when ready
     */
    abstract fun makeRequestAsync(request: UmHttpRequest,
                                  responseListener: UmHttpResponseCallback): UmHttpCall


    /**
     * Directly send an asynchronous http request. This must *NOT* rely on the httpcachedir, as it
     * will be used by HttpCacheDir as the underlying implementation to retrieve data from the network.
     *
     * @param request request to make
     * @param responseListener response listener
     * @return call
     */
    abstract fun sendRequestAsync(request: UmHttpRequest,
                                  responseListener: UmHttpResponseCallback): UmHttpCall

    /**
     * Directly send a synchronous request. THIS IS NOT FOR NORMAL USAGE. It is intended only to be
     * used by the cache so requests can be pumped through the system http library, if present on
     * that implementation. As http libraries like okhttp
     *
     * It must *NOT* be used directly by presenters etc.
     *
     * @param request request to make
     * @return response
     */
    @Throws(IOException::class)
    abstract fun sendRequestSync(request: UmHttpRequest): UmHttpResponse


    @Throws(IOException::class)
    abstract fun makeRequestSync(request: UmHttpRequest): UmHttpResponse


    /**
     * Make a new instance of an XmlPullParser (e.g. Kxml).  This is added as a
     * method in the implementation instead of using the factory API because
     * it enables the J2ME version to use the minimal jar
     *
     * @return A new default options XmlPullParser
     */
    @Throws(XmlPullParserException::class)
    abstract fun newPullParser(): XmlPullParser

    /**
     * Make a new instance of an XmlSerializer (org.xmlpull.v1.XmlSerializer)
     *
     * @return New instance of an XML Serializer
     */
    abstract fun newXMLSerializer(): XmlSerializer

    /**
     * Make a new XmlPullParser from a given inputstream
     * @param in InputStream to read from
     * @param encoding Encoding to be used e.g. UTF-8
     *
     * @return a new XmlPullParser with set with the given inputstream
     */
    @Throws(XmlPullParserException::class)
    @JvmOverloads
    fun newPullParser(`in`: InputStream, encoding: String = UstadMobileConstants.UTF8): XmlPullParser {
        l(UMLog.DEBUG, 523, encoding)
        val xpp = newPullParser()
        xpp.setInput(`in`, encoding)
        return xpp
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
     * Gives a string with the version number
     *
     * @return String with version number
     */
    abstract fun getVersion(context: Any): String

    /**
     * Get the build timestamp
     *
     * @param context System context object
     *
     * @return Build timestamp in ms since epoch
     */
    abstract fun getBuildTimestamp(context: Any): Long

    /**
     * Return absolute path of the application setup file. Asynchronous.
     *
     * @param context System context
     * @param zip if true, the app setup file should be delivered within a zip.
     * @param callback callback to call when complete or if any error occurs.
     */
    abstract fun getAppSetupFile(context: Any, zip: Boolean, callback: UmCallback<*>)


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
    abstract fun getManifestPreference(key: String, context: Any): String?

    /**
     * Wrapper to retrieve preference keys from the system Manifest.
     *
     * On Android: uses meta-data elements on the application element in AndroidManifest.xml
     * On J2ME: uses the jad file
     *
     * @param key The key to lookup
     * @param defaultVal The default value to return if the key is not found
     * @param context System context object
     *
     * @return The value of the manifest preference key if found, otherwise the default value
     */
    fun getManifestPreference(key: String, defaultVal: String, context: Any): String {
        val `val` = getManifestPreference(key, context)
        return `val` ?: defaultVal
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
    abstract fun getAppConfigString(key: String, defaultVal: String?, context: Any): String?

    /**
     * Get a boolean from the app configuration. App config is stored as a string, so this is
     * converted to a boolean using Boolean.parseBoolean
     *
     * @param key The preference key to lookup
     * @param defaultVal The default value to return if the key is not found
     * @param context System context object
     * @return The boolean value of the given preference key if found, otherwise the default value
     */
    fun getAppConfigBoolean(key: String, defaultVal: Boolean, context: Any): Boolean {
        val strVal = getAppConfigString(key, null, context)
        return if (strVal == null)
            defaultVal
        else
            java.lang.Boolean.parseBoolean(strVal)
    }

    /**
     * Get a boolean from the app configuration. App config is stored as a string, so this is
     * converted to a boolean using Boolean.parseBoolean
     *
     * @param key The preference key to lookup
     * @param context System context object
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
    fun getAppConfigInt(key: String, defaultVal: Int, context: Any): Int {
        return Integer.parseInt(getAppConfigString(key, "" + defaultVal, context)!!)
    }

    /**
     * Determine if the two given locales are the same as far as what the user will see.
     *
     * @param oldLocale
     *
     * @return
     */
    fun hasDisplayedLocaleChanged(oldLocale: String?, context: Any): Boolean {
        val currentlyDisplayedLocale = getDisplayedLocale(context)
        return !(currentlyDisplayedLocale != null && oldLocale != null
                && oldLocale.substring(0, 2) == currentlyDisplayedLocale.substring(0, 2))
    }

    protected fun getContentDirName(context: Any): String? {
        return getAppConfigString(AppConfig.KEY_CONTENT_DIR_NAME, DEFAULT_CONTENT_DIR_NAME, context)
    }

    abstract fun openFileInDefaultViewer(context: Any, path: String, mimeType: String,
                                         callback: UmCallback<Any>)

    companion object {

        internal var mainInstance: Any? = null

        /**
         * Suggested name to create for content on Devices
         */
        val DEFAULT_CONTENT_DIR_NAME = "ustadmobileContent"


        /**
         * Flag to indicate a download requested has failed
         *
         * Same value as android.app.DownloadManager.STATUS_FAILED
         */
        val DLSTATUS_FAILED = 16


        /**
         * Indicates that a download has not actually started yet
         */
        val DLSTATUS_NOT_STARTED = 0


        /**
         * The return value from getLocale when the user has said to use the system's locale
         */
        val LOCALE_USE_SYSTEM = ""

        private val MIME_TYPES = Hashtable<String, String>()

        private var MIME_TYPES_REVERSE: Map<String, String> = Hashtable()

        /**
         * Ported from old CatalogPresenter
         *
         * Save/retrieve resource from user specific directory
         */
        val USER_RESOURCE = 2


        /**
         * Ported from old CatalogPresenter
         *
         * Save/retrieve resource from shared directory
         */
        val SHARED_RESOURCE = 4

        val STATUS_ACQUIRED = 0

        val STATUS_ACQUISITION_IN_PROGRESS = 1

        val ARG_REFERRER = "ref"

        /**
         * As per Android Intent.FLAG_ACTIVITY_SINGLE_TOP
         */
        val GO_FLAG_SINGLE_TOP = 536870912

        /**
         * As per Android Intent.FLAG_CLEAR_TOP
         */
        val GO_FLAG_CLEAR_TOP = 67108864


        init {
            MIME_TYPES["image/jpg"] = "jpg"
            MIME_TYPES["image/jpeg"] = "jpg"
            MIME_TYPES["image/png"] = "png"
            MIME_TYPES["image/gif"] = "gif"
            MIME_TYPES["image/svg"] = "svg"
            MIME_TYPES["application/epub+zip"] = "epub"

            MIME_TYPES_REVERSE = UMUtil.flipMap(MIME_TYPES, MIME_TYPES_REVERSE)
        }

        /**
         * Get an instance of the system implementation - relies on the platform
         * specific factory method
         *
         * @return A singleton instance
         */
        val instance: UstadMobileSystemImpl
            get() {
                if (mainInstance == null) {
                    mainInstance = UstadMobileSystemImplFactory.makeSystemImpl()
                }

                return mainInstance as UstadMobileSystemImpl
            }

        /**
         * Only for testing purposes (e.g. to use a mockito spy)
         */
        fun setMainInstance(instance: UstadMobileSystemImpl?) {
            mainInstance = instance
        }

        /**
         * Convenience shortcut for logging
         * @see UMLog.l
         * @param level log level
         * @param code log code
         * @param message message to log
         */
        fun l(level: Int, code: Int, message: String?) {
            instance.logger.l(level, code, message.toString())
        }

        /**
         * Convenience shortcut for logging
         * @see UMLog.l
         * @param level log level
         * @param code log code
         * @param message log message
         * @param exception exception that occurred to log
         */
        fun l(level: Int, code: Int, message: String?, exception: Exception) {
            instance.logger.l(level, code, message!!, exception)
        }
    }


}
/**
 * Make a new XmlPullParser from a given inputstream assuming UTF-8 encoding
 * @param in InputStream to read from
 * @return a new XmlPullParser with set with the given inputstream
 * @throws XmlPullParserException If there is a pull parser exception in the underlying implementation
 */