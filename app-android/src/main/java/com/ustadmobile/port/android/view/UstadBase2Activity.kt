package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance
import com.ustadmobile.core.view.UstadBaseFeedbackMessageView
import com.ustadmobile.port.android.netwokmanager.UmAppDatabaseSyncService
import com.ustadmobile.sharedse.network.NetworkManagerBle
import com.ustadmobile.sharedse.network.NetworkManagerBleAndroidService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CompletableDeferred
import java.util.*

/**
 * Created by @kileha3 on 05/2/20.
 */
abstract class UstadBase2Activity : AppCompatActivity(), UstadBaseFeedbackMessageView{

    private var mSyncServiceBound = false

    /**
     * @return Active NetworkManagerBleCommon
     */
    val networkManagerBle = CompletableDeferred<NetworkManagerBle>()

    @Volatile
    private var bleServiceBound = false

    private var localeOnCreate: String? = null


    private val mSyncServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mSyncServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mSyncServiceBound = false
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
                networkManagerBle.complete(networkManagerBleVal)
                bleServiceBound = true
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bleServiceBound = false
        }
    }


    override fun onResume() {
        super.onResume()
        if (instance.hasDisplayedLocaleChanged(localeOnCreate, this)) {
            Handler().postDelayed({ this.recreate() }, 200)
        }
    }

    /**
     * Handle all feedback message within the app anchored on bottom nav
     */
    override fun showFeedbackMessage(message: String, actionMessageId: Int, action: () -> Unit) {
        val snackBar = Snackbar.make(coordinator_layout, message, Snackbar.LENGTH_LONG)
        if (actionMessageId != 0) {
            snackBar.setAction(instance.getString(actionMessageId, this)) { action() }
            snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.accent))
        }
        snackBar.anchorView = bottom_nav_view
        snackBar.show()
    }


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

        //bind ble service
        val bleServiceIntent = Intent(this, NetworkManagerBleAndroidService::class.java)
        bindService(bleServiceIntent, bleServiceConnection,
                Context.BIND_AUTO_CREATE or Context.BIND_ADJUST_WITH_ACTIVITY)

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

    public override fun onDestroy() {
        if (bleServiceBound) {
            unbindService(bleServiceConnection)
        }

        instance.handleActivityDestroy(this)
        if (mSyncServiceBound) {
            unbindService(mSyncServiceConnection)
        }
        super.onDestroy()
    }

}
