package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.UserSettingsController2;
import com.ustadmobile.core.view.UserSettingsView2;

public class UserSettingsActivity2 extends UstadBaseActivity implements UserSettingsView2,View.OnClickListener{
    private UserSettingsController2 mController;
    private FrameLayout accountSection,dataUsageSection,languageSection,logoutSection,actionCaptureImage,actionEditProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mController = new UserSettingsController2(this);
        setUMToolbar(R.id.setting_tool_bar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((TextView)findViewById(R.id.toolbarTitle)).setText(R.string.settings);

        accountSection= (FrameLayout) findViewById(R.id.account_section_holder);
        dataUsageSection= (FrameLayout) findViewById(R.id.data_usage_section_holder);
        languageSection= (FrameLayout) findViewById(R.id.language_section_holder);
        logoutSection= (FrameLayout) findViewById(R.id.logout_section_holder);
        actionCaptureImage= (FrameLayout) findViewById(R.id.capture_camera_icon);
        actionEditProfile= (FrameLayout) findViewById(R.id.edit_user_profile_holder);

        int defaultIconColor= ContextCompat.getColor(this,R.color.primary);
        ((ImageView) findViewById(R.id.account_icon)).setColorFilter(defaultIconColor);
        ((ImageView) findViewById(R.id.data_usage_icon)).setColorFilter(defaultIconColor);
        ((ImageView) findViewById(R.id.language_icon)).setColorFilter(defaultIconColor);
        ((ImageView) findViewById(R.id.logout_icon)).setColorFilter(defaultIconColor);

        ((TextView)findViewById(R.id.user_profile_full_name)).setText("Student/Username");

        accountSection.setOnClickListener(this);
        dataUsageSection.setOnClickListener(this);
        languageSection.setOnClickListener(this);
        logoutSection.setOnClickListener(this);
        actionCaptureImage.setOnClickListener(this);
        actionEditProfile.setOnClickListener(this);
    }

    @Override
    public void setActiveLanguage(String language) {

    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public int getDirection() {
        return 0;
    }

    @Override
    public void setDirection(int dir) {

    }

    @Override
    public void setAppMenuCommands(String[] labels, int[] ids) {

    }

    @Override
    public void setUIStrings() {

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.language_section_holder:
                mController.handleClickLanguage();
                break;
        }


        if(view==accountSection){
            //TODO: implement navigation to account settings
        }else if(view==dataUsageSection){
            mController.handleClickDataSettings();
        }else if(view==logoutSection){
            mController.handleClickLogout();
        }else if(view==actionCaptureImage){
            //TODO:implement image capture functionality
        }else if(view==actionEditProfile){
            //TODO: go to edit page
            new WelcomeDialogFragment().show(getSupportFragmentManager(),"");
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
