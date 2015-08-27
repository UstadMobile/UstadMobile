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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.ustadmobile.core.view.AppView;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

/**
 * Created by mike on 07/08/15.
 */
public class AppViewAndroid implements AppView{

    private UstadMobileSystemImplAndroid impl;

    private ProgressDialog progressDialog;

    private AlertDialog alertDialog;

    public AppViewAndroid(UstadMobileSystemImplAndroid impl) {
        this.impl = impl;
    }


    @Override
    public void showProgressDialog(final String title) {
        final Activity currentActivity = impl.getCurrentActivity();
        currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                dismissProgressDialog();

                progressDialog = ProgressDialog.show(currentActivity, title, "");
            }
        });
    }

    @Override
    public boolean dismissProgressDialog() {
        impl.getCurrentActivity().runOnUiThread(new Runnable() {
            public void run() {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            }
        });
        return progressDialog != null;
    }

    @Override
    public void showAlertDialog(final String title, final String text) {
        impl.getCurrentActivity().runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(impl.getCurrentActivity());
                builder.setMessage(text).setTitle(title);
                builder.setPositiveButton("OK", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                });
                alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    public void dismissAlertDialog() {
        impl.getCurrentActivity().runOnUiThread(new Runnable() {
            public void run(){
                if(alertDialog != null) {
                    alertDialog.dismiss();
                    alertDialog = null;
                }
            }
        });
    }

    @Override
    public void showNotification(final String text, final int length) {
        impl.getCurrentActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(impl.getCurrentActivity(), text, length).show();
            }
        });


    }
}
