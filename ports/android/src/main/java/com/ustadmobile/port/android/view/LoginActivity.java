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

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.U;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.core.view.ViewFactory;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.WeakHashMap;

public class LoginActivity extends AppCompatActivity implements LoginView, View.OnClickListener {

    private int viewId;

    private LoginController mLoginController;

    protected String mTitle;

    protected String mUsernameHint;

    protected String mPasswordHint;

    protected String mButtonText;

    protected String mRegisterPhoneNumberHint;

    protected String mRegisterNameHint;

    protected String mRegisterGenderMaleLabel;

    protected String mRegisterGenderFemaleLable;

    protected String mRegisterButtonText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.umactivity_login);

        UstadMobileSystemImplAndroid.handleActivityCreate(this, savedInstanceState);


        mLoginController = LoginController.makeControllerForView(this);
        setTitle("Login");

        Toolbar toolbar = (Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        ViewPager viewPager = (ViewPager)findViewById(R.id.login_pager);
        viewPager.setAdapter(new LoginPagerAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = (TabLayout)findViewById(R.id.login_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void onStart() {
        super.onStart();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStart(this);
    }

    public void onStop() {
        super.onStop();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStop(this);
    }

    public void onDestroy() {
        super.onDestroy();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityDestroy(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setController(LoginController controller) {

    }

    @Override
    public void setTitle(String title) {
        mTitle = title;
        super.setTitle(title);
    }

    @Override
    public void setUsernameHint(String loginHint) {
        mUsernameHint = loginHint;
    }

    @Override
    public void setPasswordHint(String passwordHint) {
        mPasswordHint = passwordHint;
    }

    @Override
    public void setButtonText(String buttonText) {
        mButtonText = buttonText;
    }

    @Override
    public void setRegisterPhoneNumberHint(String phoneNumberHint) {
        mRegisterPhoneNumberHint = phoneNumberHint;
    }

    @Override
    public void setRegisterNameHint(String nameHint) {
        mRegisterNameHint = nameHint;
    }

    @Override
    public void setRegisterGenderMaleLabel(String maleLabel) {
        mRegisterGenderMaleLabel = maleLabel;
    }

    @Override
    public void setRegisterGenderFemaleLabel(String femaleLabel) {
        mRegisterGenderFemaleLable = femaleLabel;
    }

    @Override
    public void setRegisterButtonText(String registerButtonText) {
        mRegisterButtonText = registerButtonText;
    }

    @Override
    public void show() {

    }

    @Override
    public boolean isShowing() {
        return false;
    }

    @Override
    public Object getContext() {
        return this;
    }

    @Override
    public void onClick(View view) {

    }

    public class LoginPagerAdapter extends FragmentStatePagerAdapter {

        private WeakHashMap<Integer, LoginFragment> fragmentMap;

        private int[] tabTitles = new int[] {U.id.login, U.id.register};

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
            return UstadMobileSystemImpl.getInstance().getString(tabTitles[position]);
        }
    }



}
