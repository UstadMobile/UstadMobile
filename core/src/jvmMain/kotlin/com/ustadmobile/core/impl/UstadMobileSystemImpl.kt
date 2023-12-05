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

import com.russhwolf.settings.Settings
import java.util.*
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.concurrentSafeMapOf
import dev.icerock.moko.resources.StringResource


/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system.
 *
 *
 * @author mike, kileha3
 */
actual open class UstadMobileSystemImpl(
    settings: Settings
) : UstadMobileSystemCommon(settings){


    private val localeCache = concurrentSafeMapOf<String, Locale>()

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
    actual override fun go(viewName: String, args: Map<String, String?>, context: Any, flags: Int,
        ustadGoOptions: UstadGoOptions){
        lastDestination = LastGoToDest(viewName, args)
    }

    override fun getString(stringResource: StringResource): String {
        val displayLang = getDisplayedLocale()
        return stringResource.localized(locale = localeCache.getOrPut(displayLang) {
            Locale(displayLang)
        })
    }

    override fun formatString(stringResource: StringResource, vararg args: Any): String {
        val displayLang = getDisplayedLocale()
        return stringResource.localized(
            locale = localeCache.getOrPut(displayLang) {
                Locale(displayLang)
            },
            args = args,
        )
    }

    fun getString(stringResource: StringResource, localeCode: String ) : String{
        return stringResource.localized(Locale(localeCode))
    }

    /**
     * Provides a list of paths to removable storage (e.g. sd card) directories
     *
     * @return
     */
    private fun findRemovableStorage(): Array<String?> {
        return arrayOfNulls(0)
    }

    /**
     * Must provide the system's default locale (e.g. en_US.UTF-8)
     *
     * @return System locale
     */
    actual override fun getSystemLocale(): String{
        return Locale.getDefault().toString()
    }


    /**
     * Gives a string with the version number
     *
     * @return String with version number
     */
    actual fun getVersion(context: Any): String{
        return "JVM"
    }

    /**
     * Get the build timestamp
     *
     * @param context System context object
     *
     * @return Build timestamp in ms since epoch
     */
    actual fun getBuildTimestamp(context: Any): Long{
        TODO("not implemented")
    }

    /**
     * Return absolute path of the application setup file. Asynchronous.
     *
     * @param context System context
     * @param zip if true, the app setup file should be delivered within a zip.
     */
    actual override suspend fun getAppSetupFile(context: Any, zip: Boolean): String{
        TODO("not implemented")
    }


    override fun openFileInDefaultViewer(
        context: Any,
        doorUri: DoorUri,
        mimeType: String?,
        fileName: String?
    ) {

    }


    /**
     * Open the given link in a browser and/or tab depending on the platform
     */
    actual override fun openLinkInBrowser(url: String, context: Any) {
        //On JVM - do nothing at the moment. This is only used for unit testing with verify calls.
    }


    actual companion object {

        const val APPCONFIG_PROPERTIES_PATH = "/com/ustadmobile/core/appconfig.properties"

        const val PREFS_FILENAME = "prefs.properties"

    }

}