package com.ustadmobile.port.android.panic

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.port.android.view.SplashScreenActivity
import kotlin.random.Random

/**
 * Manage hiding and unhiding the app. This can be done in response to a Panic Trigger. The app will
 * show
 */
class HidingManager {

    private val Context.componentNames: Pair<ComponentName, ComponentName>
        get() = ComponentName(this, SplashScreenActivity::class.java) to
            ComponentName(this, NotepadActivity::class.java)


    fun storeUnhideCode(systemImpl: UstadMobileSystemImpl, context: Context, overwrite: Boolean = false) {
        val currentKey = systemImpl.getAppPref(PREFKEY_RESTORE_CODE, context)
        if(currentKey == null || overwrite) {
            val key = Random.nextInt(0, 99999)
            systemImpl.setAppPref(PREFKEY_RESTORE_CODE, key.toString(), context)
        }
    }

    fun getUnhideCode(systemImpl: UstadMobileSystemImpl, context: Context) : String{
        val currentKey = systemImpl.getAppPref(PREFKEY_RESTORE_CODE, context)
        if(currentKey == null) {
            storeUnhideCode(systemImpl, context)
        }

        return systemImpl.getAppPref(PREFKEY_RESTORE_CODE, context)
            ?: throw IllegalStateException("Key must have been stored by now!")
    }

    fun hide(context: Context) {
        val (mainComp, notepadComp) = context.componentNames

        if(context.packageManager.getComponentEnabledSetting(mainComp) == COMPONENT_ENABLED_STATE_ENABLED) {
            stopServices(context)
            NotificationManagerCompat.from(context).cancelAll()

            Log.i(
                "UstadPanicResponse", "Services stopped, now try to hide !"
            )
            context.packageManager.setComponentEnabledSetting(mainComp,
                COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

            context.packageManager.setComponentEnabledSetting(notepadComp,
                COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }
    }

    fun unhide(context: Context) {
        val (mainComp, notepadComp) = context.componentNames

        if(context.packageManager.getComponentEnabledSetting(mainComp) == COMPONENT_ENABLED_STATE_DISABLED) {
            context.packageManager.setComponentEnabledSetting(notepadComp,
                COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

            context.packageManager.setComponentEnabledSetting(mainComp,
                COMPONENT_ENABLED_STATE_ENABLED, 0)
        }
    }

    companion object {

        const val PREFKEY_RESTORE_CODE = "restore_key"

        private fun stopServices(context: Context) {
            try {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SERVICES
                )

                packageInfo.services.forEach  { service ->
                    val intent = Intent()
                    intent.component = ComponentName(context, service.name)
                    context.stopService(intent)
                }

            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }

    }

}