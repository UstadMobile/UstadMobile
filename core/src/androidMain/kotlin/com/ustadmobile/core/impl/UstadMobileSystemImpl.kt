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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.navigation.*
import com.ustadmobile.core.impl.nav.toNavOptions
import com.ustadmobile.core.io.ext.isGzipped
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.toBundleWithNullableValues
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import java.io.*
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system.
 *
 *
 * @author mike, kileha3
 */
actual open class UstadMobileSystemImpl : UstadMobileSystemCommon() {

    private var appConfig: Properties? = null

    private var appPreferences: SharedPreferences? = null

    var messageIdMap: Map<Int, Int> = mapOf()

    /**
     * This should be used only for testing. This will use the given navcontroller instead of
     * finding the navcontroller from the mainactivity. This is used for Espresso testing on Fragments.
     */
    @VisibleForTesting
    var navController: NavController? = null

    private val viewNameToAndroidImplMap = mapOf<String, String>(
            "DownloadDialog" to "${PACKAGE_NAME}DownloadDialogFragment",
            SplashScreenView.VIEW_NAME to "${PACKAGE_NAME}SplashScreenActivity",
            OnBoardingView.VIEW_NAME to "${PACKAGE_NAME}OnBoardingActivity",
            EpubContentView.VIEW_NAME to "${PACKAGE_NAME}EpubContentActivity",
            AboutView.VIEW_NAME to "${PACKAGE_NAME}AboutActivity",
            ContentEntryImportLinkView.VIEW_NAME to "${PACKAGE_NAME}ContentEntryImportLinkActivity",
            HarView.VIEW_NAME to "${PACKAGE_NAME}HarActivity",
            ContentEntryImportLinkView.VIEW_NAME to "${PACKAGE_NAME}ContentEntryImportLinkActivity",
            SchoolEditView.VIEW_NAME to "${PACKAGE_NAME}SchoolEditActivity",
            PersonGroupEditView.VIEW_NAME to "${PACKAGE_NAME}PersonGroupEditActivity"
    )

    /**
     * Simple async task to handle getting the setup file
     * Param 0 = boolean - true to zip, false otherwise
     */
    private class GetSetupFileAsyncTask(private val zipIt: Boolean,private val context: Context){

       suspend fun getFile(): String {
            val apkFile = File(context.applicationInfo.sourceDir)
            //TODO: replace this with something from appconfig.properties
            val di: DI by closestDI(context)
            val impl : UstadMobileSystemImpl = di.direct.instance()

            val baseName = impl.getAppConfigString(AppConfig.KEY_APP_BASE_NAME, "", context) + "-" +
                    impl.getVersion(context)


            var apkFileIn: FileInputStream? = null
            val outDir = File(context.filesDir, "shared")
            if (!outDir.isDirectory)
                outDir.mkdirs()

            if (zipIt) {
                var zipOut: ZipOutputStream? = null
                val outZipFile = File(outDir, "$baseName.zip")
                try {
                    zipOut = ZipOutputStream(FileOutputStream(outZipFile))
                    zipOut.putNextEntry(ZipEntry("$baseName.apk"))
                    apkFileIn = FileInputStream(apkFile)
                    apkFileIn.copyTo(zipOut)
                    zipOut.closeEntry()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    zipOut?.close()
                    apkFileIn?.close()
                }

                return outZipFile.absolutePath
            } else {
                var fout: FileOutputStream? = null
                val outApkFile = File(outDir, "$baseName.apk")
                try {
                    apkFileIn = FileInputStream(apkFile)
                    fout = FileOutputStream(outApkFile)
                    apkFileIn.copyTo(fout)
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    apkFileIn?.close()
                    fout?.close()
                }

                return outApkFile.absolutePath
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
    actual override fun go(viewName: String, args: Map<String, String?>, context: Any,
                           flags: Int, ustadGoOptions: UstadGoOptions) {

        val destinationQueryPos = viewName.indexOf('?')
        val viewNameOnly = if (destinationQueryPos == -1) {
            viewName
        }else {
            viewName.substring(0, destinationQueryPos)
        }
        val allArgs = args + UMFileUtil.parseURLQueryString(viewName)


        val di: DI by closestDI(context as Context)
        val destinationProvider: DestinationProvider = di.direct.instance()

        val ustadDestination = destinationProvider.lookupDestinationName(viewNameOnly)
        if(ustadDestination != null) {
            val navController = navController ?: (context as Activity).findNavController(destinationProvider.navControllerViewId)

            //Note: default could be set using style as per https://stackoverflow.com/questions/50482095/how-do-i-define-default-animations-for-navigation-actions
            val options = ustadGoOptions.toNavOptions(navController, destinationProvider)

            navController.navigate(ustadDestination.destinationId,
                    allArgs.toBundleWithNullableValues(), options)

            return
        }


        val androidImplClassName = viewNameToAndroidImplMap[viewName] ?: return
        val androidImplClass: Class<*>
        val ctx = context as Context
        try {
            androidImplClass = Class.forName(androidImplClassName)
        }catch(e: Exception) {
            Log.wtf(TAG, "No activity for $viewName found")
            Toast.makeText(ctx, "ERROR: No Activity found for view: $viewName",
                    Toast.LENGTH_LONG).show()
            return
        }

        val argsBundle = UMAndroidUtil.mapToBundle(args)

        if (DialogFragment::class.java.isAssignableFrom(androidImplClass as Class<*>)) {
            var toastMsg: String? = null
            try {
                val dialog = androidImplClass.newInstance() as DialogFragment
                if (args != null)
                    dialog.arguments = argsBundle
                val activity = context as AppCompatActivity
                dialog.show(activity.supportFragmentManager, TAG_DIALOG_FRAGMENT)
            } catch (e: InstantiationException) {
                Log.wtf(TAG, "Could not instantiate dialog", e)
                toastMsg = "Dialog error: $e"
            } catch (e2: IllegalAccessException) {
                Log.wtf(TAG, "Could not instantiate dialog", e2)
                toastMsg = "Dialog error: $e2"
            }

            if (toastMsg != null) {
                Toast.makeText(ctx, toastMsg, Toast.LENGTH_LONG).show()
            }
        } else {
            val startIntent = Intent(ctx, androidImplClass)
            if (ctx is Activity) {
                var referrer = ""
                if (ctx.intent.extras != null) {
                    referrer = ctx.intent.extras!!.getString(ARG_REFERRER, "")
                }

                if (flags and GO_FLAG_CLEAR_TOP > 0) {
                    referrer = UMFileUtil.clearTopFromReferrerPath(viewName, args,
                            referrer)
                } else {
                    referrer += "/" + viewName + "?" + UMFileUtil.mapToQueryString(args)
                }

                startIntent.putExtra(ARG_REFERRER, referrer)
            }
            startIntent.flags = flags
            if (argsBundle != null)
                startIntent.putExtras(argsBundle)

            ctx.startActivity(startIntent)
        }
    }

    actual fun popBack(popUpToViewName: String, popUpInclusive: Boolean, context: Any) {
        val di : DI by closestDI { context as Context }
        val destinationProvider: DestinationProvider = di.direct.instance()

        val navController = navController ?: (context as Activity)
                .findNavController(destinationProvider.navControllerViewId)

        val popBackDestId = if(popUpToViewName == UstadView.CURRENT_DEST) {
            navController.currentDestination?.id ?: 0
        }else {
            destinationProvider.lookupDestinationName(popUpToViewName)
                    ?.destinationId ?: 0
        }

        navController.popBackStack(popBackDestId, popUpInclusive)
    }


    /**
     * Get a string for use in the UI
     */
    actual override fun getString(messageCode: Int, context: Any): String {
        val androidId = messageIdMap[messageCode]
        return if (androidId != null) {
            (context as Context).resources.getString(androidId)
        } else {
            return ""
        }
    }

    /**
     * Must provide the system's default locale (e.g. en_US.UTF-8)
     *
     * @return System locale
     */
    actual override fun getSystemLocale(context: Any): String {
        return Locale.getDefault().toString()
    }

    /**
     * Get a preference for the app
     *
     * @param key preference key as a string
     * @return value of that preference
     */
    actual override fun getAppPref(key: String, context: Any): String? {
        return getAppSharedPreferences(context as Context).getString(key, null)
    }


    /**
     * Set a preference for the app
     * @param key preference that is being set
     * @param value value to be set
     */
    override actual fun setAppPref(key: String, value: String?, context: Any) {
        val prefs = getAppSharedPreferences(context as Context)
        val editor = prefs.edit()
        if (value != null) {
            editor.putString(key, value)
        } else {
            editor.remove(key)
        }
        editor.apply()
    }


    /**
     * Gives a string with the version number
     *
     * @return String with version number
     */
    actual fun getVersion(context: Any): String {
        val ctx = context as Context
        var versionInfo: String? = null
        try {
            val pInfo = ctx.packageManager.getPackageInfo(context.packageName, 0)
            versionInfo = 'v'.toString() + pInfo.versionName + " (#" + pInfo.versionCode + ')'.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            UMLog.l(UMLog.ERROR, 90, null, e)
        }

        return versionInfo!!
    }

    /**
     * Get the build timestamp
     *
     * @param context System context object
     *
     * @return Build timestamp in ms since epoch
     */
    actual fun getBuildTimestamp(context: Any): Long {
        val ctx = context as Context
        try {
            val pInfo = ctx.packageManager.getPackageInfo(context.packageName, 0)
            return pInfo.lastUpdateTime
        } catch (e: PackageManager.NameNotFoundException) {
            UMLog.l(UMLog.ERROR, 90, null, e)
        }
        return 0
    }

    /**
     * Return absolute path of the application setup file. Asynchronous.
     *
     * @param context System context
     * @param zip if true, the app setup file should be delivered within a zip.
     */
    actual override suspend fun getAppSetupFile(context: Any, zip: Boolean): String {
        val setupFileAsyncTask = GetSetupFileAsyncTask(zip,
            context as Context)
       return setupFileAsyncTask.getFile()
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
    fun getManifestPreference(key: String, context: Any): String? {
        try {
            val ctx = context as Context
            val ai2 = ctx.packageManager.getApplicationInfo(ctx.packageName,
                    PackageManager.GET_META_DATA)
            val metaData = ai2.metaData
            if (metaData != null) {
                return metaData.getString(key)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            UMLog.l(UMLog.ERROR, UMLog.ERROR, key, e)
        }
        return null
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
        if (appConfig == null) {
            val appPrefResource = getManifestPreference("com.ustadmobile.core.appconfig",
                    context) ?: "com/ustadmobile/core/appconfig.properties"
            appConfig = Properties()
            var prefIn: InputStream? = null

            try {
                prefIn =  (context as Context).assets.open(appPrefResource)
                appConfig!!.load(prefIn)
            } catch (e: IOException) {
                UMLog.l(UMLog.ERROR, 685, appPrefResource, e)
            } finally {
                prefIn?.close()
            }
        }

        return appConfig!!.getProperty(key, defaultVal)
    }


    override fun openFileInDefaultViewer(
        context: Any,
        doorUri: DoorUri,
        mimeType: String?,
        fileName: String?,
    ) {
        var mMimeType = mimeType
        val ctx = context as Context
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val uri: Uri
        if(doorUri.uri.toString().startsWith("content://")){
            uri = doorUri.uri
        }else{
            var file = doorUri.toFile()

            if (file.isGzipped()) {

                var gzipIn: GZIPInputStream? = null
                var destOut: FileOutputStream? = null
                try {
                    gzipIn = GZIPInputStream(FileInputStream(file))
                    val destFile = File(file.parentFile, file.name + "unzip")
                    destOut = FileOutputStream(destFile)
                    gzipIn.copyTo(destOut)
                    file = destFile
                } finally {
                    gzipIn?.close()
                    destOut?.flush()
                    destOut?.close()
                }
            }
            uri = FileProvider.getUriForFile(ctx, "${context.packageName}.provider", file)
        }
        if (mMimeType.isNullOrEmpty()) {
            mMimeType = "*/*"
        }
        intent.setDataAndType(uri, mMimeType)
        val pm = ctx.packageManager
        if (intent.resolveActivity(pm) != null) {
            ctx.startActivity(intent)
        } else {
            throw NoAppFoundException("No activity found for mimetype: $mMimeType", mMimeType)
        }
    }

    private fun getAppSharedPreferences(context: Context): SharedPreferences {
        if (appPreferences == null) {
            appPreferences = context.getSharedPreferences(APP_PREFERENCES_NAME,
                    Context.MODE_PRIVATE)
        }
        return appPreferences!!
    }


    /**
     * Open the given link in a browser and/or tab depending on the platform
     */
    actual override fun openLinkInBrowser(url: String, context: Any) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        (context as Context).startActivity(intent)
    }



    actual companion object {

        const val TAG = "UstadMobileImplAndroid"

        private const val PACKAGE_NAME = "com.ustadmobile.port.android.view."

        const val APP_PREFERENCES_NAME = "UMAPP-PREFERENCES"

        const val TAG_DIALOG_FRAGMENT = "UMDialogFrag"


    }


}