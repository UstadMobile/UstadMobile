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
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.ustadmobile.core.BuildConfig
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.view.*
import kotlinx.io.InputStream
import java.io.*
import java.util.*
import java.util.concurrent.Executors
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

    private val deviceStorageIndex = 0

    private val sdCardStorageIndex = 1

    private val bgExecutorService = Executors.newCachedThreadPool()

    private var appPreferences: SharedPreferences? = null

    var messageIdMap: Map<Int, Int> = mapOf()


    private val viewNameToAndroidImplMap = mapOf<String,Any>(
            "DownloadDialog" to Class.forName("${PACKAGE_NAME}DownloadDialogFragment"),
            VideoPlayerView.VIEW_NAME to Class.forName("${PACKAGE_NAME}VideoPlayerActivity"),
            ContentEditorView.VIEW_NAME to Class.forName("${PACKAGE_NAME}ContentEditorActivity"),
            ContentEditorPageListView.VIEW_NAME to Class.forName("${PACKAGE_NAME}ContentEditorPageListFragment"),
            ContentEntryListView.VIEW_NAME to Class.forName("${PACKAGE_NAME}ContentEntryListActivity"),
            ContentEntryEditView.VIEW_NAME to Class.forName("${PACKAGE_NAME}ContentEntryEditFragment"),
            SelectMultipleLocationTreeDialogView.VIEW_NAME to Class.forName("${PACKAGE_NAME}SelectMultipleLocationTreeDialogFragment"),
            SelectMultipleEntriesTreeDialogView.VIEW_NAME to Class.forName("${PACKAGE_NAME}SelectMultipleEntriesTreeDialogFragment"),
            XapiReportDetailView.VIEW_NAME to Class.forName("${PACKAGE_NAME}XapiReportDetailActivity"),
            WebChunkView.VIEW_NAME to Class.forName("${PACKAGE_NAME}WebChunkActivity"),
            Register2View.VIEW_NAME to Class.forName("${PACKAGE_NAME}Register2Activity"),
            HomeView.VIEW_NAME to Class.forName("${PACKAGE_NAME}HomeActivity"),
            OnBoardingView.VIEW_NAME to Class.forName("${PACKAGE_NAME}OnBoardingActivity"),
            LoginView.VIEW_NAME to Class.forName("${PACKAGE_NAME}LoginActivity"),
            EpubContentView.VIEW_NAME to Class.forName("${PACKAGE_NAME}EpubContentActivity"),
            AboutView.VIEW_NAME to Class.forName("${PACKAGE_NAME}AboutActivity"),
            XapiPackageContentView.VIEW_NAME to Class.forName("${PACKAGE_NAME}XapiPackageContentActivity"),
            ScormPackageView.VIEW_NAME to Class.forName("${PACKAGE_NAME}ScormPackageActivity"),
            H5PContentView.VIEW_NAME to Class.forName("${PACKAGE_NAME}H5PContentActivity"),
            ContentEntryListFragmentView.VIEW_NAME to Class.forName("${PACKAGE_NAME}ContentEntryListActivity"),
            ContentEntryDetailView.VIEW_NAME to Class.forName("${PACKAGE_NAME}ContentEntryDetailActivity"))



    private abstract class UmCallbackAsyncTask<A, P, R>
    (protected var umCallback: UmCallback<R>) : AsyncTask<A, P, R>() {

        protected var error: Throwable? = null

        override fun onPostExecute(r: R) {
            if (error == null) {
                umCallback.onSuccess(r)
            } else {
                umCallback.onFailure(error)
            }
        }
    }


    fun handleActivityCreate(mContext: Activity, savedInstanceState: Bundle?) {
        init(mContext)
    }

    fun handleActivityDestroy(mContext: Activity) {}


    /**
     * Simple async task to handle getting the setup file
     * Param 0 = boolean - true to zip, false otherwise
     */
    private class GetSetupFileAsyncTask (doneCallback: UmCallback<*>, private val context: Context)
        : UmCallbackAsyncTask<Boolean, Void, String>(doneCallback as UmCallback<String>) {
        override fun doInBackground(vararg params: Boolean?): String {
            val apkFile = File(context.applicationInfo.sourceDir)
            //TODO: replace this with something from appconfig.properties
            val impl = instance

            val baseName = impl.getAppConfigString(AppConfig.KEY_APP_BASE_NAME, "", context) + "-" +
                    impl.getVersion(context)


            var apkFileIn: FileInputStream? = null
            val outDir = File(context.filesDir, "shared")
            if (!outDir.isDirectory)
                outDir.mkdirs()

            if (params[0]!!) {
                var zipOut: ZipOutputStream? = null
                val outZipFile = File(outDir, "$baseName.zip")
                try {
                    zipOut = ZipOutputStream(FileOutputStream(outZipFile))
                    zipOut.putNextEntry(ZipEntry("$baseName.apk"))
                    apkFileIn = FileInputStream(apkFile)
                    UMIOUtils.readFully(apkFileIn, zipOut, 1024)
                    zipOut.closeEntry()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    UMIOUtils.closeOutputStream(zipOut)
                    UMIOUtils.closeInputStream(apkFileIn)
                }

                return outZipFile.absolutePath
            } else {
                var fout: FileOutputStream? = null
                val outApkFile = File(outDir, "$baseName.apk")
                try {
                    apkFileIn = FileInputStream(apkFile)
                    fout = FileOutputStream(outApkFile)
                    UMIOUtils.readFully(apkFileIn, fout, 1024)
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    UMIOUtils.closeInputStream(apkFileIn)
                    UMIOUtils.closeOutputStream(fout)
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
    actual override fun go(viewName: String, args: Map<String, String?>, context: Any, flags: Int){
        val androidImplClass = viewNameToAndroidImplMap[viewName]
        val ctx = context as Context
        val argsBundle = UMAndroidUtil.mapToBundle(args)

        if (androidImplClass == null) {
            Log.wtf(TAG, "No activity for $viewName found")
            Toast.makeText(ctx, "ERROR: No Activity found for view: $viewName",
                    Toast.LENGTH_LONG).show()
            return
        }

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
            startIntent.putExtras(argsBundle)

            ctx.startActivity(startIntent)
        }
    }



    /**
     * Get a string for use in the UI
     */
    actual fun getString(messageCode: Int, context: Any): String{
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
    actual override fun getSystemLocale(context: Any): String{
        return Locale.getDefault().toString()
    }


    /**
     * Get storage directories
     *
     * @param context Platform specific context
     * @param callback Storage dir list callback
     */

    actual override fun getStorageDirs(context: Any, callback: UmResultCallback<List<UMStorageDir>>){
        Thread {
            val dirList = ArrayList<UMStorageDir>()
            val storageOptions = ContextCompat.getExternalFilesDirs(context as Context, null)
            val contentDirName = getContentDirName(context)

            var umDir = File(storageOptions[deviceStorageIndex], contentDirName!!)
            if (!umDir.exists()) umDir.mkdirs()
            dirList.add(UMStorageDir(umDir.absolutePath,
                    getString(MessageID.phone_memory, context), true,
                    isAvailable = true, isUserSpecific = false, isWritable = canWriteFileInDir(umDir.absolutePath)))

            if (storageOptions.size > 1) {
                val sdCardStorage = storageOptions[sdCardStorageIndex]
                umDir = File(sdCardStorage, contentDirName)
                if (!umDir.exists()) umDir.mkdirs()
                dirList.add(UMStorageDir(umDir.absolutePath,
                        getString(MessageID.memory_card, context), removableMedia = true,
                        isAvailable = true, isUserSpecific = false, isWritable = canWriteFileInDir(umDir.absolutePath)))
            }

            callback.onDone(dirList)
        }.start()
    }


    actual override suspend fun getStorageDirsAsync(context: Any): List<UMStorageDir?> {
        val dirList = ArrayList<UMStorageDir>()
        val storageOptions = ContextCompat.getExternalFilesDirs(context as Context, null)
        val contentDirName = getContentDirName(context)

        var umDir = File(storageOptions[deviceStorageIndex], contentDirName!!)
        if (!umDir.exists()) umDir.mkdirs()
        dirList.add(UMStorageDir(umDir.absolutePath,
                getString(MessageID.phone_memory, context), true,
                isAvailable = true, isUserSpecific = false, isWritable = canWriteFileInDir(umDir.absolutePath)))

        if (storageOptions.size > 1) {
            val sdCardStorage = storageOptions[sdCardStorageIndex]
            umDir = File(sdCardStorage, contentDirName)
            if (!umDir.exists()) umDir.mkdirs()
            dirList.add(UMStorageDir(umDir.absolutePath,
                    getString(MessageID.memory_card, context), true,
                    isAvailable = true, isUserSpecific = false, isWritable = canWriteFileInDir(umDir.absolutePath)))
        }
        return dirList
    }

    /**
     * Get an asset (from files that are in core/src/flavorName/assets)
     *
     */
    actual fun getAsset(context: Any, path: String, callback: UmCallback<InputStream>){
        var mPath = path
        if (path.startsWith("/")) {
            mPath = path.substring(1)
        }

        bgExecutorService.execute {
            try {
                callback.onSuccess((context as Context).assets.open(mPath))
            } catch (e: IOException) {
                callback.onFailure(e)
            }
        }
    }

    /**
     * Get a preference for the app
     *
     * @param key preference key as a string
     * @return value of that preference
     */
    actual override fun getAppPref(key: String, context: Any): String?{
        return getAppSharedPreferences(context as Context).getString(key, null)
    }


    /**
     * Set a preference for the app
     * @param key preference that is being set
     * @param value value to be set
     */
    override actual fun setAppPref(key: String, value: String?, context: Any){
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
    actual fun getVersion(context: Any): String{
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
    actual fun getBuildTimestamp(context: Any): Long{
        val ctx  = context as Context
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
     * @param callback callback to call when complete or if any error occurs.
     */
    actual override fun getAppSetupFile(context: Any, zip: Boolean, callback: UmCallback<*>){
        val setupFileAsyncTask = GetSetupFileAsyncTask(callback,
                context as Context)
        setupFileAsyncTask.execute(zip)
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
    actual override fun getManifestPreference(key: String, context: Any): String?{
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
        var mMimeType = mimeType;
        val ctx = context as Context
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val uri = FileProvider.getUriForFile(ctx, BuildConfig.APPLICATION_ID, File(path))
        if (mMimeType == null || mMimeType.isEmpty()) {
            mMimeType = "*/*"
        }
        intent.setDataAndType(uri, mMimeType)
        val pm = ctx.packageManager
        if (intent.resolveActivity(pm) != null) {
            ctx.startActivity(intent)
            UmCallbackUtil.onSuccessIfNotNull(callback, null)
        } else {
            UmCallbackUtil.onFailIfNotNull(callback,
                    NoAppFoundException("No activity found for mimetype", mMimeType))
        }
    }

    private fun getAppSharedPreferences(context: Context): SharedPreferences {
        if (appPreferences == null) {
            appPreferences = context.getSharedPreferences(APP_PREFERENCES_NAME,
                    Context.MODE_PRIVATE)
        }
        return appPreferences!!
    }

    @Throws(IOException::class)
    actual fun getAssetSync(context: Any, path: String): InputStream{
        var mPath = path
        if (path.startsWith("/")) {
            mPath = path.substring(1)
        }
        return (context as Context).assets.open(mPath)
    }

    /**
     * Returns the system base directory to work from
     *
     * @return
     */
    actual fun getSystemBaseDir(context: Any): String{
        return File(Environment.getExternalStorageDirectory(), getContentDirName(context))
                .absolutePath
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

        const val TAG = "UstadMobileImplAndroid"

        private const val PACKAGE_NAME = "com.ustadmobile.port.android.view."

        const val APP_PREFERENCES_NAME = "UMAPP-PREFERENCES"

        const val TAG_DIALOG_FRAGMENT = "UMDialogFrag"

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
        var path = path
        if (path.startsWith("/")) {
            path = path.substring(1)
        }

        return UMIOUtils.readStreamToByteArray((context as Context).assets.open(path))
    }

    /**
     * Get asset as an input stream asynchronously
     */
    actual suspend fun getAssetInputStreamAsync(context: Any, path: String): InputStream {
        var mPath = path
        if (path.startsWith("/")) {
            mPath = path.substring(1)
        }
       return (context as Context).assets.open(mPath);
    }


}