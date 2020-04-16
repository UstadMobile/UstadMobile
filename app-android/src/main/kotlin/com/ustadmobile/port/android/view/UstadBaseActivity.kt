package com.ustadmobile.port.android.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.google.android.material.snackbar.Snackbar
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
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance
import com.ustadmobile.core.view.UstadViewWithNotifications
import com.ustadmobile.core.view.UstadViewWithProgress
import com.ustadmobile.core.view.UstadViewWithSnackBar
import com.ustadmobile.port.android.impl.UserFeedbackException
import com.ustadmobile.port.android.netwokmanager.UmAppDatabaseSyncService
import com.ustadmobile.port.sharedse.util.RunnableQueue
import com.ustadmobile.sharedse.network.NetworkManagerBle
import com.ustadmobile.sharedse.network.NetworkManagerBleAndroidService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Runnable
import org.acra.ACRA
import java.lang.ref.WeakReference
import java.util.*

/**
 * Base activity to handle interacting with UstadMobileSystemImpl
 *
 *
 * Created by mike on 10/15/15.
 */
abstract class UstadBaseActivity : AppCompatActivity(), ServiceConnection, UstadViewWithNotifications, UstadViewWithSnackBar, ShakeDetector.Listener, UstadViewWithProgress {

    private var baseController: UstadBaseController<*>? = null

    /**
     * Get the toolbar that's used for the support action bar
     *
     * @return
     */
    protected lateinit var umToolbar: Toolbar

    protected lateinit var baseProgressBar: ProgressBar

    /**
     * Currently running instance of NetworkManagerBleCommon
     */
    /**
     * @return Active NetworkManagerBleCommon
     */
    val networkManagerBle = CompletableDeferred<NetworkManagerBle>()

    @Volatile
    private var bleServiceBound = false

    private var fragmentList: MutableList<WeakReference<Fragment>>? = null

    private var localeOnCreate: String? = null

    private var runAfterFileSelection: Runnable? = null

    /**
     * Can be used to check if the activity has been started.
     *
     * @return true if the activity is started. false if it has not been started yet, or it was started, but has since stopped
     */
    var isStarted = false
        private set

    private var permissionRequestRationalesShown = false

    private var afterPermissionMethodRunner: Runnable? = null

    private val runWhenServiceConnectedQueue = RunnableQueue()

    private var permissionDialogTitle: String? = null

    private var permissionDialogMessage: String? = null

    private var permissions: Array<String>? = null

    internal var selectedFileUri: Uri? = null

    internal var isOpeningFilePickerOrCamera = false

    lateinit var appUpdateManager : AppUpdateManager


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

    /**
     * Ble service connection
     */
    private val bleServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val serviceVal = (service as NetworkManagerBleAndroidService.LocalServiceBinder)
                    .service
            serviceVal.runWhenNetworkManagerReady {
                UMLog.l(UMLog.DEBUG, 0, "BleService Connection: service = $serviceVal")

                val networkManagerBleVal = serviceVal.networkManagerBle!!
                //this runs after service is ready
                networkManagerBle.complete(networkManagerBleVal)
                //networkManagerBle = serviceVal.networkManagerBle
                bleServiceBound = true
                onBleNetworkServiceBound(serviceVal.networkManagerBle!!)
                runWhenServiceConnectedQueue.setReady(true)

//                //TODO: this is being used for testing purposes only and should be removed
                UstadMobileSystemImpl.instance.networkManager = networkManagerBleVal
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bleServiceBound = false
            onBleNetworkServiceUnbound()
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
        fragmentList = ArrayList()
        super.onCreate(savedInstanceState)
        localeOnCreate = instance.getDisplayedLocale(this)


        val syncServiceIntent = Intent(this, UmAppDatabaseSyncService::class.java)
        bindService(syncServiceIntent, mSyncServiceConnection,
                Context.BIND_AUTO_CREATE or Context.BIND_ADJUST_WITH_ACTIVITY)

        //bind ble service
        val bleServiceIntent = Intent(this, NetworkManagerBleAndroidService::class.java)
        bindService(bleServiceIntent, bleServiceConnection,
                Context.BIND_AUTO_CREATE or Context.BIND_ADJUST_WITH_ACTIVITY)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetector(this)

    }

    /**
     * We intercept this to implement app-wide rotation for presentation purposes.
     *
     * See UstadMobileSystemImpl.rotationEnabled for details
     */
    override fun setContentView(layoutResID: Int) {
        if(UstadMobileSystemImpl.instance.rotationEnabled ?: false) {
            super.setContentView(R.layout.activity_rotatelayoutroot)
            LayoutInflater.from(this).inflate(layoutResID,
                    findViewById(R.id.activity_rotatelayout), true)
        }else {
            super.setContentView(layoutResID)
        }
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
        val editText = dialogView.findViewById<EditText>(R.id.feedback_edit_comment)
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
     * Display the snackbar at the bottom of the page
     *
     * @param errorMessage    message for the snackbar
     * @param actionMessageId id of action name
     * @param action          action listener
     */
    override fun showSnackBarNotification(errorMessage: String, action: () -> Unit, actionMessageId: Int) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), errorMessage, Snackbar.LENGTH_LONG)
        val impl = instance
        if (actionMessageId != 0) {
            snackbar.setAction(impl.getString(actionMessageId, this)) { action() }
            snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.accent))
        }
        snackbar.show()
    }

    /**
     * All activities descending from UstadBaseActivity bind to the network manager. This method
     * can be overriden when presenters need to use a reference to the networkmanager.
     *
     * @param networkManagerBle
     */
    protected open fun onBleNetworkServiceBound(networkManagerBle: NetworkManagerBle) {}

    protected fun onBleNetworkServiceUnbound() {

    }

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
        supportActionBar!!.setHomeButtonEnabled(true)
    }

    protected fun setProgressBar() {
        baseProgressBar = findViewById(R.id.progressBar)
        baseProgressBar.isIndeterminate = true
        baseProgressBar.scaleY = 3f
    }

    protected fun setProgressBarDeterminate(isDeterminate: Boolean) {
        baseProgressBar.isIndeterminate = !isDeterminate
    }

    protected fun setBaseController(baseController: UstadBaseController<*>) {
        this.baseController = baseController
    }


    public override fun onStart() {
        isStarted = true
        super.onStart()
    }

    public override fun onStop() {
        isStarted = false
        super.onStop()
    }

    public override fun onDestroy() {
        if (bleServiceBound) {
            unbindService(bleServiceConnection)
        }

        instance.handleActivityDestroy(this)
        if (mSyncServiceBound) {
            unbindService(mSyncServiceConnection)
        }
        shakeDetector = null
        sensorManager = null
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val impl = instance
                impl.go(impl.getAppConfigString(AppConfig.KEY_FIRST_DEST, null,
                        this), this)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        fragmentList!!.add(WeakReference<Fragment>(fragment))
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


    /**
     * Handle our own delegation of back button presses.  This allows UstadBaseFragment child classes
     * to handle back button presses if they want to.
     */
    override fun onBackPressed() {
        for (fragmentReference in fragmentList!!) {
            if (fragmentReference.get() == null)
                continue

            if (!fragmentReference.get()!!.isVisible)
                continue

            if (fragmentReference.get() is UstadBaseFragment && (fragmentReference.get() as UstadBaseFragment).canGoBack()) {
                (fragmentReference.get() as UstadBaseFragment).goBack()
                return
            }
        }

        super.onBackPressed()
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
            super.attachBaseContext(newBase.createConfigurationContext(config))
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

    /**
     * Responsible for running task after checking permissions
     *
     * @param permissions    Permission to be checked
     * @param runnable      Future task to be executed
     * @param dialogTitle   Permission dialog title
     * @param dialogMessage Permission dialog message
     */
    fun runAfterGrantingPermission(permissions: Array<String>, runnable: Runnable?,
                                   dialogTitle: String, dialogMessage: String,
                                   dialogBuilder: () -> AlertDialog = {makePermissionDialog(permissions, dialogTitle, dialogMessage) }) {
        this.afterPermissionMethodRunner = runnable

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            afterPermissionMethodRunner!!.run()
            afterPermissionMethodRunner = null
            return
        }


        this.permissionDialogMessage = dialogMessage
        this.permissionDialogTitle = dialogTitle
        this.permissions = permissions

        if (!permissionGranted(permissions)) {
            if (!permissionRequestRationalesShown) {
                val dialog = dialogBuilder.invoke()
                dialog.show()
                permissionRequestRationalesShown = true
            } else {
                permissionRequestRationalesShown = false
                ActivityCompat.requestPermissions(this, permissions, RUN_TIME_REQUEST_CODE)
            }
        } else {
            afterPermissionMethodRunner!!.run()
            afterPermissionMethodRunner = null
        }
    }

    private fun makePermissionDialog(permissions: Array<String>, dialogTitle: String,
                                     dialogMessage: String): AlertDialog {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(permissionDialogTitle)
                .setMessage(permissionDialogMessage)
                .setNegativeButton(getString(android.R.string.cancel)
                ) { dialog, _ -> dialog.dismiss() }
                .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                    runAfterGrantingPermission(permissions, afterPermissionMethodRunner,
                            dialogTitle, dialogMessage)
                }
        return builder.create()
    }


    private fun permissionGranted(permissions: Array<String>): Boolean {
        val requiredPermissions: MutableList<String> = mutableListOf<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(permission)
            }
        }

        return requiredPermissions.isEmpty()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            RUN_TIME_REQUEST_CODE -> {
                var allPermissionGranted = grantResults.size == permissions.size
                for (result in grantResults) {
                    allPermissionGranted = allPermissionGranted and (result == PackageManager.PERMISSION_GRANTED)
                }

                if (!allPermissionGranted && permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    afterPermissionMethodRunner!!.run()
                    afterPermissionMethodRunner = null
                }

                if (allPermissionGranted) {
                    afterPermissionMethodRunner!!.run()
                    afterPermissionMethodRunner = null
                    return
                }
            }
        }
    }

    /**
     * Make sure NetworkManagerBleCommon is not null when running a certain logic
     *
     * @param runnable Future task to be executed
     */
    fun runAfterServiceConnection(runnable: Runnable) {
        runWhenServiceConnectedQueue.runWhenReady(runnable)
    }

    companion object {

        private const val RUN_TIME_REQUEST_CODE = 111

        private const val FILE_SELECTION_REQUEST_CODE = 112

        private const val APP_UPDATE_REQUEST_CODE = 113
    }
}
