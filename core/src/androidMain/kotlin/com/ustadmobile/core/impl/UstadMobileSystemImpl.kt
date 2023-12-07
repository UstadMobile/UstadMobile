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

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.navigation.*
import com.russhwolf.settings.Settings
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.io.ext.isGzipped
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.format
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
 * @param applicationContext This must be the context for the activity. Using any other context
 *        won't work because Android's per-app language settings will not apply to the aplpication
 *        context, and systemImpl is used extensively to get localized strings.
 * @param settings : Multiplatform settings object. Used for app preferences.
 * @author mike, kileha3
 */
actual open class UstadMobileSystemImpl(
    private val applicationContext: Context,
    settings: Settings,
    langConfig: SupportedLanguagesConfig,
) : UstadMobileSystemCommon(settings, langConfig) {

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

            val baseName =  (context.applicationInfo.metaData
                .getString("com.ustadmobile.shareappbasename") ?: "ustad")  + "-" +
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
    @Deprecated("Replaced with nav controller")
    actual override fun go(viewName: String, args: Map<String, String?>, context: Any,
                           flags: Int, ustadGoOptions: UstadGoOptions) {

        throw IllegalStateException("This should not be used")
    }

    override fun getString(stringResource: StringResource): String {
        return stringResource.getString(applicationContext)
    }

    override fun formatString(stringResource: StringResource, vararg args: Any): String {
        return stringResource.format(args).stringRes.getString(applicationContext)
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


    actual companion object {

        const val TAG = "UstadMobileImplAndroid"

        private const val PACKAGE_NAME = "com.ustadmobile.port.android.view."

        const val APP_PREFERENCES_NAME = "UMAPP-PREFERENCES"

        const val TAG_DIALOG_FRAGMENT = "UMDialogFrag"


    }


}