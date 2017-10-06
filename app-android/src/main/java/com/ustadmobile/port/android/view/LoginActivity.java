/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */

package com.ustadmobile.port.android.view;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

import java.util.WeakHashMap;

public class LoginActivity extends UstadBaseActivity implements LoginView, View.OnClickListener, CheckBox.OnCheckedChangeListener {

    private int viewId;

    private LoginController mLoginController;
 
    protected String mTitle;

    protected String mXAPIServer;

    protected String mVersionLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.umactivity_login);

        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityCreate(this, savedInstanceState);

        mLoginController = LoginController.makeControllerForView(this);
        setBaseController(mLoginController);

        setTitle(R.string.login);

        Toolbar toolbar = (Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        if(Build.VERSION.SDK_INT >= 21) {
            findViewById(R.id.login_sliding_tabs).setElevation(10);
            toolbar.setElevation(10);
        }

        ViewPager viewPager = (ViewPager)findViewById(R.id.login_pager);
        viewPager.setAdapter(new LoginPagerAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = (TabLayout)findViewById(R.id.login_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        mLoginController.setUIStrings();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void setController(LoginController controller) {
        setBaseController(controller);
    }

    @Override
    public void setTitle(String title) {
        mTitle = title;
        super.setTitle(title);
    }

    @Override
    public void setUIStrings() {

    }

    @Override
    public void setVersionLabel(String versionLabel) {
        mVersionLabel = versionLabel;
    }

    @Override
    public void setXAPIServerURL(String xAPIServerURL) {
        this.mXAPIServer = xAPIServerURL;
        View xAPITextView = findViewById(R.id.login_xapi_server);
        if(xAPITextView != null) {
            ((EditText)xAPITextView).setText(xAPIServerURL);
        }
    }

    @Override
    public void setAdvancedSettingsVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;
        findViewById(R.id.login_server_label).setVisibility(visibility);
        findViewById(R.id.login_xapi_server).setVisibility(visibility);
    }

    @Override
    public Object getContext() {
        return this;
    }

    private String getEditTextVal(int viewId) {
        return ((EditText)findViewById(viewId)).getText().toString();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        String xAPIServer = ((EditText)findViewById(R.id.login_xapi_server)).getText().toString();
        if(id == R.id.login_button) {
            String username = ((EditText)findViewById(R.id.login_username)).getText().toString();
            String password = ((EditText)findViewById(R.id.login_password)).getText().toString();
            mLoginController.handleClickLogin(username, password, xAPIServer);
        }else if(id == R.id.login_registerbutton) {
//            Hashtable userVals = new Hashtable();
//            int selectedCountryNum = ((Spinner)findViewById(R.id.login_registercountry)).getSelectedItemPosition();
//
//            userVals.put(LoginController.REGISTER_COUNTRY,
//                    new Integer(UstadMobileConstants.COUNTRYDIALINGCODES[selectedCountryNum]));
//            userVals.put(LoginController.REGISTER_PHONENUM,
//                    ((EditText) findViewById(R.id.login_registerphonenum)).getText().toString());
//            userVals.put(LoginController.REGISTER_NAME,
//                    ((EditText)findViewById(R.id.login_registername)).getText().toString());
//            int genderSelectedId = ((RadioGroup)findViewById(R.id.login_registergenderradiogroup)).getCheckedRadioButtonId();
//            userVals.put(LoginController.REGISTER_GENDER,
//                    genderSelectedId == R.id.login_register_radio_female ? "f" : "m");
//            userVals.put(LoginController.REGISTER_USERNAME, getEditTextVal(R.id.login_registerusername));
//            userVals.put(LoginController.REGISTER_PASSWORD, getEditTextVal(R.id.login_registerpassword));
//            userVals.put(LoginController.REGISTER_EMAIL, getEditTextVal(R.id.login_registeremail));
//            userVals.put(LoginController.REGISTER_REGCODE, getEditTextVal(R.id.login_registerregcode));
//            userVals.put(UstadMobileSystemImpl.PREFKEY_XAPISERVER, xAPIServer);
//            mLoginController.handleClickRegister(userVals);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
        mLoginController.handleAdvanceCheckboxToggled(value);
    }

    public class LoginPagerAdapter extends FragmentStatePagerAdapter {

        private WeakHashMap<Integer, LoginFragment> fragmentMap;

        private int[] tabTitles = new int[] {MessageID.login, MessageID.register};

        public LoginPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentMap = new WeakHashMap<>();
        }

        @Override
        public Fragment getItem(int position) {
            LoginFragment fragment = fragmentMap.get(position);
            if(fragment == null) {
                fragment = LoginFragment.newInstance(position);
                fragmentMap.put(position, fragment);
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return UstadMobileSystemImpl.getInstance().getString(
                    tabTitles[position], LoginActivity.this);
        }
    }



}
