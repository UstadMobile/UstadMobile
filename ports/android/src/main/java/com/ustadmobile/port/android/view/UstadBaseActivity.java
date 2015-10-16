package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;

/**
 * Base activity to handle interacting with UstadMobileSystemImpl
 *
 * Created by mike on 10/15/15.
 */
public abstract class UstadBaseActivity extends AppCompatActivity {

    private String mUILocale;

    private int mUIDirection = UstadMobileConstants.DIR_LTR;

    private UstadBaseController baseController;

    private boolean handleUIStringsOnResume = true;

    private Toolbar umToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityCreate(this, savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    public int getDirection() {
        return mUIDirection;
    }

    protected void setDirectionFromSystem() {
        setDirection(UstadMobileSystemImpl.getInstance().getDirection());
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
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityDestroy(this);
    }

    public Object getContext() {
        return this;
    }



}
