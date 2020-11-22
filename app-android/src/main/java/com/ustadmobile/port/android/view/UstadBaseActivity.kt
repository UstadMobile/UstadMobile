package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.content.*
import android.hardware.SensorManager
import android.net.Uri
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
import androidx.appcompat.view.ContextThemeWrapper
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
import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadViewWithNotifications
import com.ustadmobile.core.view.UstadViewWithProgress
import com.ustadmobile.port.android.impl.UserFeedbackException
import com.ustadmobile.port.android.netwokmanager.UmAppDatabaseSyncService
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Runnable
import org.acra.ACRA
import org.kodein.di.DIAware
import org.kodein.di.android.di
import java.util.*

/**
 * Base activity to handle interacting with UstadMobileSystemImpl
 *
 *
 * Created by mike on 10/15/15.
 */
abstract class UstadBaseActivity : AppCompatActivity(), ServiceConnection, UstadViewWithNotifications,
        UstadView, ShakeDetector.Listener, UstadViewWithProgress, BleNetworkManagerProvider, DIAware {

    override val di by di()

    private var baseController: UstadBaseController<*>? = null


    override var loading: Boolean = false
        get() = false
        set(value) {
            //TODO: set this on the main activity
            field = value
        }

    /**
     * Get the toolbar that's used for the support action bar
     *
     * @return
     */
    protected lateinit var umToolbar: Toolbar

    protected lateinit var baseProgressBar: ProgressBar

    @Volatile
    private var bleServiceBound = false

    private var localeOnCreate: String? = null

    private var runAfterFileSelection: Runnable? = null

    private var permissionRequestRationalesShown = false

    private var permissionDialogTitle: String? = null

    private var permissionDialogMessage: String? = null

    internal var selectedFileUri: Uri? = null

    internal var isOpeningFilePickerOrCamera = false

    lateinit var appUpdateManager: AppUpdateManager


    private val mSyncServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mSyncServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mSyncServiceBound = false
        }
    }

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
        appUpdateManager = AppUpdateManagerFactory.create(this);
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

    private var mSyncServiceBound = false


    private var shakeDetector: ShakeDetector? = null
    private var sensorManager: SensorManager? = null
    internal var feedbackDialogVisible = false

    override val viewContext: Any
        get() = this


    //The devMinApi21 flavor has SDK Min 21, but other flavors have a lower SDK
    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        //enable webview debugging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        //bind to the LRS forwarding service
        instance.handleActivityCreate(this, savedInstanceState)
        super.onCreate(savedInstanceState)
        localeOnCreate = instance.getDisplayedLocale(this)


        val syncServiceIntent = Intent(this, UmAppDatabaseSyncService::class.java)
        bindService(syncServiceIntent, mSyncServiceConnection,
                Context.BIND_AUTO_CREATE or Context.BIND_ADJUST_WITH_ACTIVITY)

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
            ACRA.getErrorReporter().handleSilentException(UserFeedbackException(editText.text.toString()))
            Toast.makeText(this, R.string.feedback_thanks, Toast.LENGTH_LONG).show()
            dialogInterface.cancel()
        }
        builder.setNegativeButton(R.string.cancel) { dialogInterface, i -> dialogInterface.cancel() }
        builder.setOnDismissListener { feedbackDialogVisible = false }
        builder.setOnCancelListener { feedbackDialogVisible = false }
        val dialog = builder.create()
        dialog.show()

    }

    override fun showBaseProgressBar(showProgress: Boolean) {
        runOnUiThread {
            baseProgressBar.visibility = if (showProgress) View.VISIBLE else View.INVISIBLE
        }
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
        if (instance.hasDisplayedLocaleChanged(localeOnCreate, this)) {
            Handler().postDelayed({ this.recreate() }, 200)
        }

        if (shakeDetector != null && sensorManager != null) {
            shakeDetector?.start(sensorManager)
        }
    }

    override fun onPause() {
        super.onPause()
        if (shakeDetector != null) {
            shakeDetector?.stop()
        }
    }

    /**
     * UstadMobileSystemImpl will bind certain services to each activity (e.g. HTTP, P2P services)
     * If needed the child activity can override this method to listen for when the service is ready
     *
     * @param name
     * @param iBinder
     */
    override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {

    }

    override fun onServiceDisconnected(name: ComponentName) {

    }

    protected fun setUMToolbar(toolbarID: Int) {
        umToolbar = findViewById<View>(toolbarID) as Toolbar
        setSupportActionBar(umToolbar)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    protected fun setProgressBar() {
        baseProgressBar = findViewById(R.id.progressBar)
        baseProgressBar.isIndeterminate = true
        baseProgressBar.scaleY = 3f
    }

    protected fun setProgressBarDeterminate(isDeterminate: Boolean) {
        baseProgressBar.isIndeterminate = !isDeterminate
    }

    public override fun onDestroy() {
        if (mSyncServiceBound) {
            unbindService(mSyncServiceConnection)
        }
        shakeDetector = null
        sensorManager = null
        super.onDestroy()
    }

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        val snackBar = Snackbar.make(coordinator_layout, message, Snackbar.LENGTH_LONG)
        if (actionMessageId != 0) {
            snackBar.setAction(instance.getString(actionMessageId, this)) { action() }
            snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.secondaryColor))
        }
        snackBar.anchorView = bottom_nav_view
        snackBar.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == FILE_SELECTION_REQUEST_CODE) {
                selectedFileUri = data?.data
                runAfterFileSelection?.run()
                runAfterFileSelection = null
            }
        }
        if (requestCode == APP_UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this,
                        "App Update failed, please try again on the next app launch.",
                        Toast.LENGTH_SHORT)
                        .show()
            }
        }

    }


    //The devMinApi21 flavor has SDK Min 21, but other flavors have a lower SDK
    @SuppressLint("ObsoleteSdkInt")
    override fun attachBaseContext(newBase: Context) {
        val res = newBase.resources
        val config = res.configuration
        val languageSetting = instance.getLocale(newBase)

        if (Build.VERSION.SDK_INT >= 17) {
            val locale = if (languageSetting == UstadMobileSystemCommon.LOCALE_USE_SYSTEM)
                Locale.getDefault()
            else
                Locale(languageSetting)
            config.setLocale(locale)

            if(Build.VERSION.SDK_INT > 24) {
                //Using createNewConfigurationContext will cause CompanionDeviceManager to crash
                applyOverrideConfiguration(config)
                super.attachBaseContext(newBase)
            }else {
                super.attachBaseContext(newBase.createConfigurationContext(config))
            }
        } else {
            super.attachBaseContext(newBase)
        }
    }


    override fun showNotification(notification: String, length: Int) {
        runOnUiThread { Toast.makeText(this, notification, length).show() }
    }


    @SuppressLint("ObsoleteSdkInt")
    protected fun runAfterFileSection(runnable: java.lang.Runnable, vararg mimeTypes: String) {
        this.runAfterFileSelection = runnable

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        } else {
            val mimeTypesStr = StringBuilder()
            for (mimeType in mimeTypes) {
                mimeTypesStr.append(mimeType).append("|")
            }
            intent.type = mimeTypesStr.substring(0, mimeTypesStr.length - 1)
        }
        startActivityForResult(Intent.createChooser(intent, ""),
                FILE_SELECTION_REQUEST_CODE)
    }


    companion object {

        private const val RUN_TIME_REQUEST_CODE = 111

        private const val FILE_SELECTION_REQUEST_CODE = 112

        private const val APP_UPDATE_REQUEST_CODE = 113
    }
}