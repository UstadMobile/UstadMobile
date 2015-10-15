package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

/**
 * Base activity to handle interacting with UstadMobileSystemImpl
 *
 * Created by mike on 10/15/15.
 */
public abstract class UstadBaseActivity extends AppCompatActivity {

    private String mUILocale;

    private UstadBaseController baseController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityCreate(this, savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    protected void setToolbarUM() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.um_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
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
        String sysLocale = UstadMobileSystemImpl.getInstance().getLocale();
        if(mUILocale != null && !mUILocale.equals(sysLocale)) {
            //the locale has changed - we need to update the ui
            baseController.setUIStrings();
        }

        mUILocale = new String(sysLocale);
    }

    public void onStop() {
        super.onStop();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStop(this);
    }

    public void onDestroy() {
        super.onDestroy();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityDestroy(this);
    }



}
