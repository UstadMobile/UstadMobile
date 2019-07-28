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

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMIOUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.InputStream
import java.io.*
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.ArrayList


/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system.
 *
 *
 * @author mike, kileha3
 */
actual open class UstadMobileSystemImpl : UstadMobileSystemCommon(){

    private var appConfig: Properties? = null

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
    actual override fun go(viewName: String, args: Map<String, String?>, context: Any, flags: Int){
        lastDestination = LastGoToDest(viewName, args)
    }

    /**
     * Get a string for use in the UI
     */
    actual fun getString(messageCode: Int, context: Any): String{
        return ""
    }


    /**
     * Get storage directories
     *
     * @param context Platform specific context
     * @param callback Storage dir list callback
     */

    actual override fun getStorageDirs(context: Any, callback: UmResultCallback<List<UMStorageDir>>){
        val dirList = ArrayList<UMStorageDir>()
        val systemBaseDir = getSystemBaseDir(context)
        val contentDirName = getContentDirName(context)

        dirList.add(UMStorageDir(systemBaseDir, getString(MessageID.device, context),
                removableMedia = false, isAvailable = true, isUserSpecific = false))

        //Find external directories
        val externalDirs = findRemovableStorage()
        for (extDir in externalDirs) {
            dirList.add(UMStorageDir(UMFileUtil.joinPaths(extDir!!, contentDirName!!),
                    getString(MessageID.memory_card, context),
                    true, true, false, false))
        }

        callback.onDone(dirList)
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
     * Get an asset (from files that are in core/src/flavorName/assets)
     *
     */
    actual fun getAsset(context: Any, path: String, callback: UmCallback<InputStream>){
        GlobalScope.launch {
            var inStream = null as InputStream?
            try {
                try {
                    inStream = this::class.java.getResourceAsStream(path)
                }catch(e: Exception) {
                    //ignore and try again
                }

                if(inStream == null) {
                    for(searchPath in listOf("src/commonMain/resources", "src/jvmMain/resources")) {
                        val resDir = File(System.getProperty("user.dir"), searchPath)
                        val resFile = File(resDir, path)
                        if(resFile.exists()) {
                            inStream = FileInputStream(resFile)
                            break
                        }
                    }
                }
            }finally {

            }

            callback.onSuccess(inStream)
        }
    }


    actual fun getAssetSync(context: Any, path: String): InputStream {
        val latch = CountDownLatch(1)
        val ref = AtomicReference<InputStream?>()
        getAsset(context, path, object: UmCallback<InputStream> {
            override fun onSuccess(result: InputStream?) {
                ref.set(result)
                latch.countDown()
            }

            override fun onFailure(exception: Throwable?) {
                latch.countDown()
            }
        })

        latch.await()
        val result = ref.get()
        if(result != null) {
            return result
        }else {
            throw IOException("Could not lookup $path")
        }
    }


    /**
     * Get a preference for the app
     *
     * @param key preference key as a string
     * @return value of that preference
     */
    actual override fun getAppPref(key: String, context: Any): String?{
        TODO("not implemented")
    }


    /**
     * Set a preference for the app
     * @param key preference that is being set
     * @param value value to be set
     */
    actual override fun setAppPref(key: String, value: String?, context: Any){

    }


    /**
     * Gives a string with the version number
     *
     * @return String with version number
     */
    actual fun getVersion(context: Any): String{
        TODO("not implemented")
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
     * @param callback callback to call when complete or if any error occurs.
     */
    actual override fun getAppSetupFile(context: Any, zip: Boolean, callback: UmCallback<*>){
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
        if (appConfig == null) {
            val appPrefResource = getManifestPreference("com.ustadmobile.core.appconfig",
                    "/com/ustadmobile/core/appconfig.properties", context)
            appConfig = Properties()
            var prefIn: InputStream? = null

            try {
                prefIn = getAssetSync(context, appPrefResource)
                appConfig!!.load(prefIn)
            } catch (e: IOException) {
                UMLog.l(UMLog.ERROR, 685, appPrefResource, e)
            } finally {
                UMIOUtils.closeInputStream(prefIn)
            }
        }

        return appConfig!!.getProperty(key, defaultVal)
    }


    actual fun openFileInDefaultViewer(context: Any, path: String, mimeType: String?,
                                         callback: UmCallback<Any>){
        TODO("not implemented")
    }

    actual fun getSystemBaseDir(context: Any): String{
        return System.getProperty("user.dir")
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
        return null
    }

    /**
     * Check if the directory is writable
     * @param dir Directory to be checked
     * @return True if is writable otherwise is read only
     */
    actual fun canWriteFileInDir(dirPath: String): Boolean {
        var canWriteFiles = false
        val testFile = File(dirPath, System.currentTimeMillis().toString() + ".txt")
        try {
            val writer = FileWriter(testFile)
            writer.append("sampletest")
            writer.flush()
            writer.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            canWriteFiles = false
        } catch (e: IOException) {
            e.printStackTrace()
            canWriteFiles = false
        }

        if (testFile.exists()) {
            canWriteFiles = testFile.delete()
        }
        return canWriteFiles
    }

    actual companion object {
        /**
         * Get an instance of the system implementation - relies on the platform
         * specific factory method
         *
         * @return A singleton instance
         */
        @JvmStatic
        actual var instance: UstadMobileSystemImpl = UstadMobileSystemImpl()
    }

    actual suspend fun getAssetAsync(context: Any, path: String): ByteArray {
        var inStream = null as InputStream?
        try {
            inStream = this::class.java.getResourceAsStream(path)
            if(inStream != null) {
                return inStream.readBytes()
            }

            // we might be running in tests
            val resDir = File(System.getProperty("user.dir"), "src/main/assets")
            inStream = FileInputStream(File(resDir, path))
            return inStream.readBytes()
        }catch(e: IOException) {
            e.printStackTrace()
            throw IOException("Could not find resource: $path")
        }finally {
            inStream?.close()
        }
    }

    actual override suspend fun getStorageDirsAsync(context: Any): List<UMStorageDir?> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Get asset as an input stream asynchronously
     */
    actual suspend fun getAssetInputStreamAsync(context: Any, path: String): InputStream {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}