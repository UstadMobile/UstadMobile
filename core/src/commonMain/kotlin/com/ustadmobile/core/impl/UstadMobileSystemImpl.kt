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
import com.ustadmobile.door.DoorUri
import kotlin.jvm.JvmStatic


/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system.
 *
 *
 * @author mike, kileha3
 */
expect open class UstadMobileSystemImpl: UstadMobileSystemCommon {


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
    override fun go(viewName: String, args: Map<String, String?>, context: Any, flags: Int,
                    ustadGoOptions: UstadGoOptions)

    fun popBack(popUpToViewName: String, popUpInclusive: Boolean, context: Any)

    /**
     * Get a string for use in the UI
     */
    override fun getString(messageCode: Int, context: Any): String

    /**
     * Must provide the system's default locale (e.g. en_US.UTF-8)
     *
     * @return System locale
     */
    override fun getSystemLocale(context: Any): String

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
     */
    override suspend fun getAppSetupFile(context: Any, zip: Boolean): String


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
     * Open the given link in a browser and/or tab depending on the platform
     */
    override fun openLinkInBrowser(url: String, context: Any)


    companion object {

    }
}