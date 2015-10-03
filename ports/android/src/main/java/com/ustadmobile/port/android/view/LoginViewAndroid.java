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

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by mike on 07/07/15.
 */
public class LoginViewAndroid implements LoginView, View.OnClickListener {
    private LoginController controller;

    private LoginActivity activity;

    private static Map<Integer, LoginViewAndroid> viewMap;
    private static int idCounter = 0;

    private static int viewId;

    protected String title;

    protected String buttonText;

    protected String usernameHint;

    protected String passwordHint;

    protected String registerNameHint;

    protected String registerPhoneNumHint;

    protected String registerMaleLabel;

    protected String registerFemaleLabel;

    protected String registerButtonText;

    static {
        viewMap = new HashMap<Integer, LoginViewAndroid>();
    }
    
    
    public LoginViewAndroid() {
        viewId = LoginViewAndroid.idCounter;
        LoginViewAndroid.idCounter++;

        viewMap.put(new Integer(viewId), this);
    }
    

    public static LoginViewAndroid getViewById(int id) {
        return viewMap.get(new Integer(id));
    }

    public void setLoginViewActivity(LoginActivity activity) {
        this.activity = activity;
    }

    @Override
    public void setController(LoginController loginController) {
        this.controller = loginController;
    }

    @Override
    public void show() {
        final UstadMobileSystemImplAndroid impl = UstadMobileSystemImplAndroid.getInstanceAndroid();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Intent startIntent = new Intent(impl.getCurrentContext(), LoginActivity.class);
                startIntent.putExtra(UstadMobileSystemImplAndroid.EXTRA_VIEWID, viewId);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                impl.getCurrentActivity().startActivity(startIntent);
            }
        });

    }


    @Override
    public void onClick(View v) {
        int clickedID = v.getId();
        switch(clickedID) {
            case R.id.login_button:
                String username = ((EditText)activity.findViewById(R.id.login_username)).getText().toString();
                String password = ((EditText)activity.findViewById(R.id.login_password)).getText().toString();
                controller.handleClickLogin(username, password);
                break;
            case R.id.login_registerbutton:
                Hashtable userVals = new Hashtable();
                int selectedCountryNum = ((Spinner)activity.findViewById(
                        R.id.login_registercountry)).getSelectedItemPosition();

                userVals.put(LoginController.REGISTER_COUNTRY,
                        new Integer(UstadMobileConstants.COUNTRYDIALINGCODES[selectedCountryNum]));
                userVals.put(LoginController.REGISTER_PHONENUM,
                        ((EditText) activity.findViewById(R.id.login_registerphonenum)).getText().toString());
                userVals.put(LoginController.REGISTER_NAME,
                        ((EditText)activity.findViewById(R.id.login_registername)).getText().toString());
                int genderSelectedId = ((RadioGroup)activity.findViewById(R.id.login_registergenderradiogroup)).getCheckedRadioButtonId();
                userVals.put(LoginController.REGISTER_GENDER,
                        genderSelectedId == R.id.login_register_radio_female ? "f" : "m");
                controller.handleClickRegister(userVals);
        }

    }

    @Override
    public boolean isShowing() {
        return UstadMobileSystemImplAndroid.getInstanceAndroid().getCurrentActivity() instanceof LoginActivity;
    }

    public void setTitle(String title) {
        this.title = title;
        if(activity != null) {
            activity.setTitle(title);
        }
    }

    @Override
    public void setUsernameHint(String usernameHint) {
        this.usernameHint = usernameHint;
    }

    @Override
    public void setPasswordHint(String passwordHint) {
        this.passwordHint = passwordHint;
    }

    @Override
    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    @Override
    public void setRegisterPhoneNumberHint(String phoneNumberHint) {
        this.registerPhoneNumHint = phoneNumberHint;
    }

    @Override
    public void setRegisterNameHint(String nameHint) {
        this.registerNameHint = nameHint;
    }

    @Override
    public void setRegisterGenderMaleLabel(String maleLabel) {
        this.registerMaleLabel = maleLabel;
    }

    @Override
    public void setRegisterGenderFemaleLabel(String femaleLabel) {
        this.registerFemaleLabel = femaleLabel;
    }

    @Override
    public void setRegisterButtonText(String registerButtonText) {
        this.registerButtonText = registerButtonText;
    }
}
