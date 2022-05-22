package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.content.*
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.View
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.squareup.seismic.ShakeDetector
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.port.android.impl.UserFeedbackException
import com.ustadmobile.port.android.util.ext.getUstadLocaleSetting
import com.ustadmobile.sharedse.network.NetworkManagerBle
import org.acra.ACRA
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.util.*

/**
 * Base activity to handle interacting with UstadMobileSystemImpl
 *
 *
 * Created by mike on 10/15/15.
 */
abstract class UstadBaseActivity : AppCompatActivity(), UstadView, ShakeDetector.Listener,
    BleNetworkManagerProvider, DIAware
{

    override val di by closestDI()

    /**
     * Get the toolbar that's used for the support action bar
     *
     * @return
     */
    protected lateinit var umToolbar: Toolbar

    protected lateinit var baseProgressBar: ProgressBar

    private var localeOnCreate: String? = null

    lateinit var appUpdateManager: AppUpdateManager

    private val systemImpl: UstadMobileSystemImpl by instance()

    private val appUpdatedListener: InstallStateUpdatedListener by lazy {
        object : InstallStateUpdatedListener {
            override fun onStateUpdate(installState: InstallState) {
                when {
                    installState.installStatus() == InstallStatus.DOWNLOADED -> updateCompleted()
                    installState.installStatus() == InstallStatus.INSTALLED -> appUpdateManager.unregisterListener(this)
                    else -> print("InstallStateUpdatedListener: state: " + installState.installStatus())
                }
            }
        }
    }

    private fun updateCompleted() {

        Toast.makeText(
                this,
                getText(R.string.downloaded),
                Toast.LENGTH_SHORT
        ).show()
    }


    /**
     * TODO: This should not be done on every onResume of every activity. It could go to HomeActivity
     */
    private fun checkForAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // Request the update.
                try {
                    val installType = when {
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateType.FLEXIBLE
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
                        else -> null
                    }
                    if (installType == AppUpdateType.FLEXIBLE) appUpdateManager.registerListener(appUpdatedListener)

                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            installType!!,
                            this,
                            APP_UPDATE_REQUEST_CODE)
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private var shakeDetector: ShakeDetector? = null
    private var sensorManager: SensorManager? = null
    internal var feedbackDialogVisible = false


    //The devMinApi21 flavor has SDK Min 21, but other flavors have a lower SDK
    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        //enable webview debugging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        super.onCreate(savedInstanceState)
        localeOnCreate = systemImpl.getDisplayedLocale(this)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetector(this)

    }

    override fun hearShake() {

        if (feedbackDialogVisible) {
            return
        }

        feedbackDialogVisible = true
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.send_feedback)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.view_feedback_layout, null)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.feedback_edit_comment)
        builder.setView(dialogView)
        builder.setPositiveButton(R.string.send) { dialogInterface, whichButton ->
            ACRA.errorReporter.handleSilentException(UserFeedbackException(editText.text.toString()))
            Toast.makeText(this, R.string.feedback_thanks, Toast.LENGTH_LONG).show()
            dialogInterface.cancel()
        }
        builder.setNegativeButton(R.string.cancel) { dialogInterface, i -> dialogInterface.cancel() }
        builder.setOnDismissListener { feedbackDialogVisible = false }
        builder.setOnCancelListener { feedbackDialogVisible = false }
        val dialog = builder.create()
        dialog.show()

    }



    /**
     * All activities descending from UstadBaseActivity bind to the network manager. This method
     * can be overriden when presenters need to use a reference to the networkmanager.
     *
     * @param networkManagerBle
     */
    protected open fun onBleNetworkServiceBound(networkManagerBle: NetworkManagerBle) {}


    override fun onResume() {
        super.onResume()
        if (systemImpl.hasDisplayedLocaleChanged(localeOnCreate, this)) {
            Handler().postDelayed({ this.recreate() }, 200)
        }

        if (shakeDetector != null && sensorManager != null) {
            shakeDetector?.start(sensorManager)
        }
    }

    override fun onPause() {
        super.onPause()
        shakeDetector?.stop()
    }


    protected fun setUMToolbar(toolbarID: Int) {
        umToolbar = findViewById<View>(toolbarID) as Toolbar
        setSupportActionBar(umToolbar)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    public override fun onDestroy() {
        shakeDetector = null
        sensorManager = null
        super.onDestroy()
    }

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        val snackBar = Snackbar.make(findViewById(R.id.coordinator_layout), message, Snackbar.LENGTH_LONG)
        if (actionMessageId != 0) {
            snackBar.setAction(systemImpl.getString(actionMessageId, this)) { action() }
            snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.secondaryColor))
        }

        snackBar.anchorView = findViewById(R.id.bottom_nav_view)
        snackBar.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == APP_UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this,
                        "App Update failed, please try again on the next app launch.",
                        Toast.LENGTH_SHORT)
                        .show()
            }
        }

    }


    override fun attachBaseContext(newBase: Context) {
        val res = newBase.resources
        val config = res.configuration
        val languageSetting = newBase.getUstadLocaleSetting()

        val locale = if (languageSetting == UstadMobileSystemCommon.LOCALE_USE_SYSTEM)
            Locale.getDefault()
        else
            Locale(languageSetting)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }


    companion object {

        private const val APP_UPDATE_REQUEST_CODE = 113
    }
}