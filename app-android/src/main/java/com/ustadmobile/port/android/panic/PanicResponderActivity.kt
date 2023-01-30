package com.ustadmobile.port.android.panic

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.port.android.presenter.PanicButtonSettingsPresenter.Companion.PREF_CLEAR_APP_DATA
import com.ustadmobile.port.android.presenter.PanicButtonSettingsPresenter.Companion.PREF_LOCK_AND_EXIT
import com.ustadmobile.port.android.presenter.PanicButtonSettingsPresenter.Companion.PREF_UNINSTALL_THIS_APP
import info.guardianproject.panic.PanicResponder
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

/**
 * Activity that will respond to a PanicKit trigger. Roughly as per:
 * https://github.com/guardianproject/FakePanicResponder/blob/master/src/info/guardianproject/fakepanicresponder/ResponseActivity.java
 */
class PanicResponderActivity: Activity(), DIAware {

    override val di by closestDI()

    val systemImpl: UstadMobileSystemImpl by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(LOGTAG_PANIC_RESPONSE, "PanicResponderActivity created")
        if (PanicResponder.receivedTriggerFromConnectedApp(this)) {
            Log.i(LOGTAG_PANIC_RESPONSE, "Panic Trigger is from connected app")
            if (systemImpl.getAppPref(PREF_UNINSTALL_THIS_APP)?.toBoolean() == true) {
                Log.i(LOGTAG_PANIC_RESPONSE, "Panic trigger should hide app")
                HidingManager().hide(this)
            }

            if (systemImpl.getAppPref(PREF_CLEAR_APP_DATA)?.toBoolean() == true) {
                Log.i(LOGTAG_PANIC_RESPONSE, "Panic trigger should delete all app data")
                PanicResponder.deleteAllAppData(this)
            }

            if (systemImpl.getAppPref(PREF_LOCK_AND_EXIT)?.toBoolean() == true) {
                Log.i(LOGTAG_PANIC_RESPONSE, "Panic trigger should exit app")
                ExitActivity.exitAndRemoveFromRecentApps(this)
            }
        } else if (PanicResponder.shouldUseDefaultResponseToTrigger(this)) {
            Log.i(LOGTAG_PANIC_RESPONSE, "Panic trigger should use default response for trigger")
            if (systemImpl.getAppPref(PREF_LOCK_AND_EXIT)?.toBoolean() == true) {
                ExitActivity.exitAndRemoveFromRecentApps(this)
            }
        }

        finish()
    }

    companion object {

        const val LOGTAG_PANIC_RESPONSE = "UstadPanicResponse"

    }

}