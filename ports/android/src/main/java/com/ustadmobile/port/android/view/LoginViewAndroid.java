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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

import java.util.HashMap;
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
        Button loginButton = (Button)activity.findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);
    }

    @Override
    public void setController(LoginController loginController) {
        this.controller = loginController;
    }

    @Override
    public void showDialog(String s, String s1) {

    }

    @Override
    public void performAction(Object o) {

    }

    @Override
    public void show() {
        UstadMobileSystemImplAndroid impl = UstadMobileSystemImplAndroid.getInstanceAndroid();
        impl.startActivityForViewId(LoginActivity.class, this.viewId);
    }


    @Override
    public void onClick(View v) {
        String username = ((EditText)activity.findViewById(R.id.login_username)).getText().toString();
        String password = ((EditText)activity.findViewById(R.id.login_password)).getText().toString();
        controller.handleClickLogin(username, password);
    }
}
