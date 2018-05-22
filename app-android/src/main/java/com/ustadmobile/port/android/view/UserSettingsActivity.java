package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.UserSettingsController;
import com.ustadmobile.core.model.UserSettingItem;
import com.ustadmobile.core.view.UserSettingsView;


public class UserSettingsActivity extends UstadBaseActivity implements UserSettingsView{

    UserSettingsController settingsController;

    UserSettingItem[] settingItems;

    String[] availableLanguages;

    static final String FRAGMENT_TAG_SETTINGSLIST = "SETTINGSLIST";

    static final String FRAGMENT_TAG_LANGLIST = "LANGLIST";

    public static final int CMD_LOGOUT_ID = 5001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        setUMToolbar(R.id.um_toolbar);


        settingsController = UserSettingsController.makeControllerForView(this);
        setBaseController(settingsController);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.user_settings_fragment_container,
                UserSettingsListFragment.newInstance(), FRAGMENT_TAG_SETTINGSLIST).commit();
        }
    }

    @Override
    public void setSettingsTitle(String title) {
        setTitle(title);
    }

    @Override
    public void setSettingsList(UserSettingItem[] items) {
        this.settingItems = items;
        Fragment currentFrag = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_SETTINGSLIST);
        if(currentFrag != null) {
            ((UserSettingsListFragment)currentFrag).updateSettingsList();
        }
    }

    @Override
    public void setLanguageList(String[] languages) {
        this.availableLanguages = languages;
    }

    @Override
    public void showSettingsList() {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_SETTINGSLIST);
        if(frag == null) {
            frag = UserSettingsListFragment.newInstance();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.user_settings_fragment_container,
            frag, FRAGMENT_TAG_SETTINGSLIST).commit();
    }

    @Override
    public void showLanguageList() {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_LANGLIST);
        if(frag == null) {
            frag = UserSettingsLanguageFragment.newInstance();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.user_settings_fragment_container,
                frag, FRAGMENT_TAG_LANGLIST).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem item = menu.add(Menu.NONE, CMD_LOGOUT_ID, 0, "Logout tmp");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case CMD_LOGOUT_ID:
                settingsController.handleClickLogout();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }
}
