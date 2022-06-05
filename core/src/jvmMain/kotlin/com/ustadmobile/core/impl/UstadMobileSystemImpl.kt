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

import java.io.*
import java.util.*
import com.ustadmobile.core.generated.locale.MessageIdMap
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.impl.locale.getStringsXmlResource
import com.ustadmobile.door.DoorUri
import org.xmlpull.v1.XmlPullParserFactory
import java.util.concurrent.ConcurrentHashMap


/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system.
 *
 *
 * @author mike, kileha3
 * @param xppFactory - XmlPullParser factory that
 */
actual open class UstadMobileSystemImpl(val xppFactory: XmlPullParserFactory,
                                        private val dataRoot: File
) : UstadMobileSystemCommon(){

    private val appConfig: Properties by lazy {
        Properties().also { props ->
            this::class.java.getResourceAsStream(APPCONFIG_PROPERTIES_PATH)?.use { propsIn ->
                props.load(propsIn)
            }
        }
    }

    private val messageIdMapFlipped: Map<String, Int> by lazy {
        MessageIdMap.idMap.entries.associate { (k, v) -> v to k }
    }

    private val defaultStringsXml: StringsXml by lazy {
        this::class.java.getStringsXmlResource("/values/strings_ui.xml", xppFactory,
            messageIdMapFlipped)
    }

    private val foreignStringsXml: MutableMap<String, StringsXml> = ConcurrentHashMap()

    private val appPrefs : Properties by lazy {
        Properties().apply {
            val propFile = File(dataRoot, PREFS_FILENAME)
            if(propFile.exists()) {
                FileReader(propFile).use { fileReader ->
                    load(fileReader)
                }
            }

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
    actual override fun go(viewName: String, args: Map<String, String?>, context: Any, flags: Int,
        ustadGoOptions: UstadGoOptions){
        lastDestination = LastGoToDest(viewName, args)
    }

    actual fun popBack(popUpToViewName: String, popUpInclusive: Boolean, context: Any) {

    }

    /**
     * Get a string for use in the UI
     */
    actual override fun getString(messageCode: Int, context: Any): String{
        //This is really only used in tests, so we just want to be sure that it is returning
        //something that is distinct
        return getString(getDisplayedLocale(context), messageCode, context)
    }

    fun getString(localeCode: String, messageId: Int, context: Any): String {
        val localeCodeLower = localeCode.toLowerCase(Locale.ROOT)

        val stringsXml = if(localeCodeLower.startsWith("en")) {
            defaultStringsXml
        }else {
            foreignStringsXml.computeIfAbsent(localeCodeLower.substring(0, 2)) {
                this::class.java.getStringsXmlResource("/values-$it/strings_ui.xml", xppFactory,
                    messageIdMapFlipped, defaultStringsXml)
            }
        }

        return stringsXml[messageId]
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
    actual override fun getSystemLocale(context: Any): String{
        return Locale.getDefault().toString()
    }





    /**
     * Get a preference for the app
     *
     * @param key preference key as a string
     * @return value of that preference
     */
    actual override fun getAppPref(key: String, context: Any): String?{
        return appPrefs.getProperty(key)
    }


    /**
     * Set a preference for the app
     * @param key preference that is being set
     * @param value value to be set
     */
    actual override fun setAppPref(key: String, value: String?, context: Any){
        if(value != null) {
            appPrefs[key] = value
        }else {
            appPrefs.remove(key)
        }

        FileWriter(File(dataRoot, PREFS_FILENAME)).use {
            appPrefs.store(it, "UTF-8")
        }
    }

    fun clearPrefs() {
        appPrefs.clear()
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
    actual override fun getAppConfigString(key: String, defaultVal: String?, context: Any): String?{
        return appConfig.getProperty(key, defaultVal)
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