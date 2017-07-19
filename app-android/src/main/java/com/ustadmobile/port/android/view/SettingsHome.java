package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.UserSettingsController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.UserSettingItem;
import com.ustadmobile.core.view.SettingsDataUsageView;
import com.ustadmobile.core.view.UserSettingsView;

public class SettingsHome extends UstadBaseActivity implements View.OnClickListener, UserSettingsView{

    private LinearLayout data_usage_settings;

    private UserSettingsController settingsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);
        data_usage_settings= (LinearLayout) findViewById(R.id.data_usage);

        setUMToolbar(R.id.toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((TextView)findViewById(R.id.toolbarTitle)).setText(R.string.settings);

        data_usage_settings.setOnClickListener(this);

        settingsController = UserSettingsController.makeControllerForView(this);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return true;
    }

    @Override
    public void onClick(View view) {

        if(view==data_usage_settings){
            UstadMobileSystemImpl.getInstance().go(SettingsDataUsageView.VIEW_NAME, null, this);

        }
    }

    @Override
    public void setSettingsTitle(String title) {

    }

    @Override
    public void setSettingsList(UserSettingItem[] items) {

    }

    @Override
    public void setLanguageList(String[] languages) {

    }

    @Override
    public void showSettingsList() {

    }

    @Override
    public void showLanguageList() {

    }
}
