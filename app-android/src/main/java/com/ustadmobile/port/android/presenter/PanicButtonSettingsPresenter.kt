package com.ustadmobile.port.android.presenter

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.port.android.panic.HidingManager
import com.ustadmobile.port.android.util.ext.getActivityContext
import com.ustadmobile.port.android.view.PanicButtonSettingsView
import info.guardianproject.panic.Panic
import info.guardianproject.panic.PanicResponder
import org.kodein.di.DI
import org.kodein.di.instance


class PanicTriggerApp(
    val packageName: String,
    val simpleName: String,
    val appIcon: Drawable?,
)

class PanicButtonSettingsPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: PanicButtonSettingsView,
    di: DI,
): UstadBaseController<PanicButtonSettingsView>(
    context, arguments, view, di
)  {

    private val systemImpl: UstadMobileSystemImpl by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        val androidContext = (context as Context)

        val triggerAppNone = PanicTriggerApp(Panic.PACKAGE_NAME_NONE,
            androidContext.getString(R.string.None),
            AppCompatResources.getDrawable(androidContext, R.drawable.ic_empty))
        val packageManager = androidContext.packageManager
        val triggerResponders = PanicResponder.resolveTriggerApps(packageManager)
        val selectedTriggerPackageName = PanicResponder.getTriggerPackageName(androidContext)

        val triggerList = listOf(triggerAppNone) + triggerResponders.map {
            PanicTriggerApp(it.activityInfo.packageName,
                it.activityInfo.loadLabel(packageManager).toString(), it.loadIcon(packageManager))
        }
        view.unhideCode = HidingManager().getUnhideCode(systemImpl)
        view.panicTriggerAppList = triggerList
        view.selectedTriggerApp = triggerList.firstOrNull {
            it.packageName  == selectedTriggerPackageName
        } ?: triggerAppNone



    }

    fun onSelectTriggerApp(panicTriggerApp: PanicTriggerApp) {
        val activity = (context as Context).getActivityContext()
        PanicResponder.setTriggerPackageName(activity,
            panicTriggerApp.packageName)
        view.selectedTriggerApp = panicTriggerApp
    }

    fun onChangeLockAndExit(enabled: Boolean) {
        systemImpl.setAppPref(PREF_LOCK_AND_EXIT, enabled.toString())
    }

    fun onChangeClearAppData(enabled: Boolean) {
        systemImpl.setAppPref(PREF_CLEAR_APP_DATA, enabled.toString())
    }

    fun onChangeUninstallThisApp(enabled: Boolean) {
        systemImpl.setAppPref(PREF_UNINSTALL_THIS_APP, enabled.toString())
    }

    companion object {

        const val PREF_LOCK_AND_EXIT = "pref_lock_and_exit"
        const val PREF_CLEAR_APP_DATA = "pref_clear_app_data"
        const val PREF_UNINSTALL_THIS_APP = "pref_uninstall_this_app"


    }

}