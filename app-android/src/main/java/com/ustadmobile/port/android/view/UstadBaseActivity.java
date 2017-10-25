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
import android.view.Menu;
import android.view.MenuItem;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerAndroid;
import com.ustadmobile.nanolrs.android.service.XapiStatementForwardingService;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;

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

    private int mUIDirection = UstadMobileConstants.DIR_LTR;

    private UstadBaseController baseController;

    private boolean handleUIStringsOnResume = true;

    protected Toolbar umToolbar;

    private int[] appMenuCommands;

    private String[] appMenuLabels;

    private XapiStatementForwardingService mNanoLrsService;

    private NetworkServiceAndroid networkServiceAndroid;

    private String displayName;

    private List<WeakReference<Fragment>> fragmentList;

    private boolean localeChanged = false;

    private String localeOnCreate = null;

    private boolean isStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //bind to the LRS forwarding service
        Intent lrsForwardIntent = new Intent(this, XapiStatementForwardingService.class);
        bindService(lrsForwardIntent, mLrsServiceConnection, Context.BIND_AUTO_CREATE);


        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityCreate(this, savedInstanceState);
        fragmentList = new ArrayList<>();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UstadMobileSystemImplAndroid.ACTION_LOCALE_CHANGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocaleChangeBroadcastReceiver,
                intentFilter);
        super.onCreate(savedInstanceState);
        localeOnCreate = UstadMobileSystemImpl.getInstance().getDisplayedLocale(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(localeChanged) {
            if(UMUtil.hasDisplayedLocaleChanged(localeOnCreate, this)) {
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



    private ServiceConnection mLrsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            XapiStatementForwardingService.XapiStatementForwardingBinder binder =
                    (XapiStatementForwardingService.XapiStatementForwardingBinder)iBinder;
            mNanoLrsService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mNanoLrsService = null;
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





    public int getDirection() {
        return mUIDirection;
    }

    protected void setDirectionFromSystem() {
        setDirection(UstadMobileSystemImpl.getInstance().getDirection());
    }

    public void setUIStrings() {

    }

    public void setDirection(int dir) {
        if(dir != mUIDirection) {
            UMAndroidUtil.setDirectionIfSupported(findViewById(android.R.id.content),
                    UstadMobileSystemImpl.getInstance().getDirection());
            mUIDirection = dir;
        }
    }

    public void setHandleUIStringsOnResume(boolean handleUIStringsOnResume) {
        this.handleUIStringsOnResume = handleUIStringsOnResume;
    }

    public boolean isHandleUIStringsOnResume() {
        return this.handleUIStringsOnResume;
    }

    protected void setUMToolbar(int toolbarID) {
        umToolbar = (Toolbar)findViewById(toolbarID);
        setSupportActionBar(umToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    public void setAppMenuCommands(String[] labels, int[] ids) {
        this.appMenuLabels = labels;
        this.appMenuCommands = ids;
        supportInvalidateOptionsMenu();
    }

    //TODO: Fully disable this properly
    public boolean onCreateOptionsMenu(Menu menu) {
//        if(appMenuCommands != null && appMenuLabels != null) {
//            for(int i = 0; i < appMenuLabels.length; i++) {
//                menu.add(Menu.NONE, appMenuCommands[i], i + 10, appMenuLabels[i]);
//            }
//            return true;
//        }else {
//            return super.onCreateOptionsMenu(menu);
//        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean handleClickAppMenuItem(MenuItem item, UstadBaseController controller) {
        return UstadBaseController.handleClickAppMenuItem(item.getItemId(), getContext());
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
        UstadMobileSystemImpl.getInstance().handleSave();
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
        UstadMobileSystemImpl.getInstance().handleSave();
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
        unbindService(mLrsServiceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocaleChangeBroadcastReceiver);
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityDestroy(this);
    }

    public Object getContext() {
        return this;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                UstadMobileSystemImpl.getInstance().go(CoreBuildConfig.FIRST_DESTINATION, this);
                return true;


        }

        if(handleClickAppMenuItem(item, baseController)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        supportInvalidateOptionsMenu();
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
