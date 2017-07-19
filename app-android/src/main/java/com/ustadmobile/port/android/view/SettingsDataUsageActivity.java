package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SettingsDataUsageController;
import com.ustadmobile.core.view.SettingsDataUsageView;

public class SettingsDataUsageActivity extends UstadBaseActivity implements SettingsDataUsageView, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private SettingsDataUsageController mController;

    private Switch superNodeSwitch;
    private RadioButton setWifiOnly,setMobileData;
    private LinearLayout superNodeLayoutWrapper,wifiLayoutWrapper,mobileLayoutWrapper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_data_usage);
        mController = new SettingsDataUsageController(this);
        setUMToolbar(R.id.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        superNodeSwitch = (Switch) findViewById(R.id.superNodeEnabled);
        setWifiOnly = (RadioButton) findViewById(R.id.wifiData);
        setMobileData = (RadioButton) findViewById(R.id.mobileData);
        superNodeLayoutWrapper = (LinearLayout) findViewById(R.id.nodeWrapper);
        mobileLayoutWrapper = (LinearLayout) findViewById(R.id.mobileWrapper);
        wifiLayoutWrapper = (LinearLayout) findViewById(R.id.wifiWrapper);

        ((TextView) findViewById(R.id.reset_counter_data)).setText("Last reset: (Never)");
        ((TextView) findViewById(R.id.data_usage_data)).setText("12KB");

        mController.setView(this);
        superNodeSwitch.setOnCheckedChangeListener(this);
        setMobileData.setOnCheckedChangeListener(this);
        setWifiOnly.setOnCheckedChangeListener(this);
        mobileLayoutWrapper.setOnClickListener(this);
        wifiLayoutWrapper.setOnClickListener(this);
        superNodeLayoutWrapper.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        if(compoundButton == superNodeSwitch) {
            mController.handleSetSupernodeEnabledChanged(b);

        }else if(compoundButton==setMobileData){

            if(mController.handleMobileDataOnlyMode(b)){
                setWifiOnly.setChecked(!b);
            }else
                setWifiOnly.setChecked(!b);


        }else if(compoundButton==setWifiOnly){

            if(mController.handleWifiOnlyMode(b)){
                setMobileData.setChecked(!b);
            }else
                setMobileData.setChecked(!b);
        }
    }

    @Override
    public void setSupernodeEnabled(boolean enabled) {
        superNodeSwitch.setOnCheckedChangeListener(null);
        superNodeSwitch.setChecked(enabled);
        superNodeSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void setSupernodeSettingVisible(boolean visible) {
        superNodeSwitch.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setOnlyWiFiConnection(boolean isWiFiOnly) {
        setWifiOnly.setChecked(isWiFiOnly == setMobileData.isChecked());
    }

    @Override
    public void setOnlyMobileConnection(boolean isWiFiOnly) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_data_p2p_menu,menu);
        return super.onCreateOptionsMenu(menu);
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

        if(view==superNodeLayoutWrapper){
            if(superNodeSwitch.isChecked()){
                superNodeSwitch.setChecked(false);
            }else{
                superNodeSwitch.setChecked(true);
            }
            mController.handleSetSupernodeEnabledChanged(superNodeSwitch.isChecked());

        }else if(view==wifiLayoutWrapper){

            if(!setWifiOnly.isActivated()){
                setWifiOnly.setChecked(true);
            }
            if(mController.handleWifiOnlyMode(setWifiOnly.isActivated())){
                setMobileData.setChecked(!setWifiOnly.isActivated());
            }
        }else if(view==mobileLayoutWrapper){

            if(!setMobileData.isActivated()){
                setMobileData.setChecked(true);
            }
            if(mController.handleWifiOnlyMode(setMobileData.isActivated())){
                setWifiOnly.setChecked(!setMobileData.isActivated());
            }
        }
    }






}
