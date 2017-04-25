package com.ustadmobile.port.android.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.BasePointController;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerAndroid;
import com.ustadmobile.nanolrs.android.service.XapiStatementForwardingService;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Base activity to handle interacting with UstadMobileSystemImpl
 *
 * Created by mike on 10/15/15.
 */
public abstract class UstadBaseActivity extends AppCompatActivity implements ServiceConnection {

    private String mUILocale;

    private int mUIDirection = UstadMobileConstants.DIR_LTR;

    private UstadBaseController baseController;

    private boolean handleUIStringsOnResume = true;

    protected Toolbar umToolbar;

    private int[] appMenuCommands;

    private String[] appMenuLabels;

    private XapiStatementForwardingService mNanoLrsService;

    private String displayName;

    private List<WeakReference<Fragment>> fragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //bind to the LRS forwarding service
        Intent lrsForwardIntent = new Intent(this, XapiStatementForwardingService.class);
        bindService(lrsForwardIntent, mLrsServiceConnection, Context.BIND_AUTO_CREATE);

        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityCreate(this, savedInstanceState);
        fragmentList = new ArrayList<>();
        super.onCreate(savedInstanceState);
    }

    private ServiceConnection mLrsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            XapiStatementForwardingService.XapiStatementForwardingBinder binder = (XapiStatementForwardingService.XapiStatementForwardingBinder)iBinder;
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

    public boolean onCreateOptionsMenu(Menu menu) {
        if(displayName != null) {
            MenuItem displayNameItem = menu.add(Menu.NONE, Menu.NONE, 0, displayName);
        }

        if(appMenuCommands != null && appMenuLabels != null) {
            for(int i = 0; i < appMenuLabels.length; i++) {
                menu.add(Menu.NONE, appMenuCommands[i], i + 10, appMenuLabels[i]);
            }
            return true;
        }else {
            return super.onCreateOptionsMenu(menu);
        }
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

    protected void setUMToolbar() {
        setUMToolbar(R.id.um_toolbar);
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
        super.onStart();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(handleUIStringsOnResume) {
            String sysLocale = UstadMobileSystemImpl.getInstance().getLocale();
            if(mUILocale != null && !mUILocale.equals(sysLocale)) {
                //the locale has changed - we need to update the ui
                baseController.setUIStrings();
            }

            mUILocale = new String(sysLocale);
        }
    }

    public void onStop() {
        super.onStop();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStop(this);
    }

    public void onDestroy() {
        super.onDestroy();
        unbindService(mLrsServiceConnection);
        PersistenceManagerAndroid.getInstanceAndroid().releaseHelperForContext(this);
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityDestroy(this);
    }

    public Object getContext() {
        return this;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                UstadMobileSystemImpl.getInstance().go(BasePointView.VIEW_NAME,
                        BasePointController.makeDefaultBasePointArgs(this), this);
                return true;
            case R.id.action_finish:
                finish();
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


}
