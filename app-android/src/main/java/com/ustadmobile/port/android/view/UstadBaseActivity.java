package com.ustadmobile.port.android.view;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.netwokmanager.UmAppDatabaseSyncService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Base activity to handle interacting with UstadMobileSystemImpl
 *
 * Created by mike on 10/15/15.
 */
public abstract class UstadBaseActivity extends AppCompatActivity implements ServiceConnection {

    private UstadBaseController baseController;

    protected Toolbar umToolbar;

    private List<WeakReference<Fragment>> fragmentList;

    private boolean localeChanged = false;

    private String localeOnCreate = null;

    private boolean isStarted = false;

    private ServiceConnection mSyncServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSyncServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mSyncServiceBound = false;
        }
    };

    private boolean mSyncServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //bind to the LRS forwarding service
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityCreate(this, savedInstanceState);
        fragmentList = new ArrayList<>();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UstadMobileSystemImplAndroid.ACTION_LOCALE_CHANGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocaleChangeBroadcastReceiver,
                intentFilter);
        super.onCreate(savedInstanceState);
        localeOnCreate = UstadMobileSystemImpl.getInstance().getDisplayedLocale(this);




        Intent syncServiceIntent = new Intent(this, UmAppDatabaseSyncService.class);
        bindService(syncServiceIntent, mSyncServiceConnection,
                Context.BIND_AUTO_CREATE|Context.BIND_ADJUST_WITH_ACTIVITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(localeChanged) {
            if(UstadMobileSystemImpl.getInstance().hasDisplayedLocaleChanged(localeOnCreate, this)) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recreate();
                    }
                }, 200);
            }
        }
    }

    /**
     * Handles internal locale changes. When the user changes the locale using the system settings
     * Android will take care of destroying and recreating the activity.
     */
    private BroadcastReceiver mLocaleChangeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()) {
                case UstadMobileSystemImplAndroid.ACTION_LOCALE_CHANGE:
                    localeChanged = true;
                    break;
            }
        }
    };

    /**
     * UstadMobileSystemImpl will bind certain services to each activity (e.g. HTTP, P2P services)
     * If needed the child activity can override this method to listen for when the service is ready
     *
     * @param name
     * @param iBinder
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    protected void setUMToolbar(int toolbarID) {
        umToolbar = (Toolbar)findViewById(toolbarID);
        setSupportActionBar(umToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /**
     * Get the toolbar that's used for the support action bar
     *
     * @return
     */
    protected Toolbar getUMToolbar() {
        return umToolbar;
    }


    protected void setBaseController(UstadBaseController baseController) {
        this.baseController = baseController;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        isStarted = true;
        super.onStart();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStart(this);
    }

    public void onStop() {
        isStarted = false;
        super.onStop();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStop(this);
    }

    /**
     * Can be used to check if the activity has been started.
     *
     * @return true if the activity is started. false if it has not been started yet, or it was started, but has since stopped
     */
    public boolean isStarted() {
        return isStarted;
    }

    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocaleChangeBroadcastReceiver);
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityDestroy(this);

        if(mSyncServiceBound) {
            unbindService(mSyncServiceConnection);
        }
    }

    public Object getContext() {
        return this;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                impl.go(impl.getAppConfigString(AppConfig.KEY_FIRST_DEST, null, this), this);
                return true;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        fragmentList.add(new WeakReference<>(fragment));
    }



    /**
     * Handle our own delegation of back button presses.  This allows UstadBaseFragment child classes
     * to handle back button presses if they want to.
     */
    @Override
    public void onBackPressed() {
        for(WeakReference<Fragment> fragmentReference : fragmentList) {
            if(fragmentReference.get() == null)
                continue;

            if(!fragmentReference.get().isVisible())
                continue;

            if(fragmentReference.get() instanceof UstadBaseFragment && ((UstadBaseFragment)fragmentReference.get()).canGoBack()) {
                ((UstadBaseFragment)fragmentReference.get()).goBack();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        final Resources res = newBase.getResources();
        final Configuration config = res.getConfiguration();
        String languageSetting = UstadMobileSystemImpl.getInstance().getLocale(newBase);
        UstadMobileSystemImpl.l(UMLog.DEBUG, 652, "Base Activity: set language to  '"
                + languageSetting + "'");

        if(Build.VERSION.SDK_INT >= 17) {
            Locale locale = languageSetting.equals(UstadMobileSystemImpl.LOCALE_USE_SYSTEM)
                    ? Locale.getDefault() : new Locale(languageSetting);
            config.setLocale(locale);
            super.attachBaseContext(newBase.createConfigurationContext(config));
        }else {
            super.attachBaseContext(newBase);
        }
    }
}
