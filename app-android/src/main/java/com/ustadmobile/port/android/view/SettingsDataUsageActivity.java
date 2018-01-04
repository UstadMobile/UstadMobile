package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.ActionMenuItemView;
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
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SettingsDataSyncListView;
import com.ustadmobile.core.view.SettingsDataUsageView;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;

import listener.ActiveSyncListener;

public class SettingsDataUsageActivity extends UstadBaseActivity implements
        SettingsDataUsageView, CompoundButton.OnCheckedChangeListener, View.OnClickListener,
        ActiveSyncListener {

    private SettingsDataUsageController mController;

    private Switch superNodeSwitch;
    private RadioButton setWifiOnly,setMobileData;
    private LinearLayout superNodeLayoutWrapper,wifiLayoutWrapper,mobileLayoutWrapper;

    private boolean isSyncHappening = false;

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

        UstadMobileSystemImplSE.getInstanceSE().addActiveSyncListener(this);
    }

    @Override
    public void onDestroy() {
        UstadMobileSystemImplSE.getInstanceSE().removeActiveSyncListener(this);
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

        //If Sync Now Menu Action Button Icon is pressed.
        if(item.getItemId() == R.id.settings_data_usage_sync_now){
            try {
                disableSyncButton();
                mController.triggerSync();

            } catch (Exception e) {
                e.printStackTrace();
                //Update view that Sync could not be started.. TODO
            }
        }

        if(item.getItemId() == R.id.syncList){
            //go To new ListView
            UstadMobileSystemImpl.getInstance().go(SettingsDataSyncListView.VIEW_NAME, null, this);
        }

        return true;
    }

    public void disableSyncButton(){
        ActionMenuItemView syncButton = (ActionMenuItemView) findViewById(R.id.settings_data_usage_sync_now);
        syncButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_sync_grey_24dp));
        syncButton.setEnabled(false);
        syncButton.setClickable(false);
    }

    public void enableSyncButton(){
        ActionMenuItemView syncButton = (ActionMenuItemView) findViewById(R.id.settings_data_usage_sync_now);
        syncButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_sync_black_24dp));
        syncButton.setEnabled(true);
        syncButton.setClickable(true);
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

    @Override
    public boolean isSyncHappening(Object context) {
        return this.isSyncHappening;
    }

    @Override
    public void setSyncHappening(boolean happening, Object context) {
        this.isSyncHappening = happening;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //stuff that updates ui
                if(isSyncHappening == false){
                    enableSyncButton();
                }else{
                    disableSyncButton();
                }
            }
        });


    }
}
