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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.AppViewChoiceListener;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

/**
 * Created by mike on 07/08/15.
 */
public class AppViewAndroid implements AppView{

    private UstadMobileSystemImplAndroid impl;

    private ProgressDialog progressDialog;

    private AlertDialog alertDialog;

    private AlertDialog choiceDialog;

    private AlertDialog confirmDialog;

    private UstadView view;

    private Context context;

    private static class AppViewAsyncTask extends android.os.AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }

    public AppViewAndroid(UstadMobileSystemImplAndroid impl, Context context) {
        this.impl = impl;
        this.context = context;
        if(context instanceof UstadView) {
            view = (UstadView)context;
        }
    }


    @Override
    public void showProgressDialog(final String title) {
        new AppViewAsyncTask() {
            @Override
            protected void onPostExecute(Void aVoid) {
                dismissProgressDialog();
                progressDialog = ProgressDialog.show(context, title, "");
            }
        }.execute();
    }

    @Override
    public void setProgressDialogTitle(final String title) {
        new AppViewAsyncTask() {
            @Override
            protected void onPostExecute(Void aVoid) {
                if(progressDialog != null) {
                    progressDialog.setTitle(title);
                }
            }
        }.execute();
    }

    @Override
    public boolean dismissProgressDialog() {
        new AppViewAsyncTask(){
            @Override
            protected void onPostExecute(Void aVoid) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }
        }.execute();
        return progressDialog != null;
    }

    @Override
    public void showAlertDialog(final String title, final String text) {
        new AppViewAsyncTask() {
            @Override
            protected void onPostExecute(Void aVoid) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(text).setTitle(title);
                String buttonText = UstadMobileSystemImpl.getInstance().getString(MessageID.ok,
                        context);
                builder.setPositiveButton(buttonText, new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                });
                alertDialog = builder.create();
                alertDialog.show();

            }
        }.execute();
    }

    @Override
    public void dismissAlertDialog() {
        view.runOnUiThread(new Runnable() {
            public void run(){
                if(alertDialog != null) {
                    alertDialog.dismiss();
                    alertDialog = null;
                }
            }
        });
    }

    /**
     *
     * @param title
     * @param text
     * @param positiveButtonText
     * @param negativeButtonText
     * @param cmdId
     * @param listener
     */
    public void showConfirmDialog(final String title, final String text, final String positiveButtonText,
                                  final String negativeButtonText, final int cmdId,
                                  final AppViewChoiceListener listener) {
        new AppViewAsyncTask() {
            @Override
            protected void onPostExecute(Void aVoid) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(text).setTitle(title);

                DialogInterface.OnClickListener clickListener= new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int choice) {
                        listener.appViewChoiceSelected(cmdId, choice == DialogInterface.BUTTON_POSITIVE ?
                                CHOICE_POSITIVE : CHOICE_NEGATIVE);
                        confirmDialog = null;
                    }
                };

                builder.setPositiveButton(positiveButtonText, clickListener);
                builder.setNegativeButton(negativeButtonText, clickListener);

                confirmDialog = builder.create();
                confirmDialog.show();
            }
        }.execute();
    }

    @Override
    public void dismissConfirmDialog() {
        if(confirmDialog != null) {
            new AppViewAsyncTask() {
                @Override
                protected void onPostExecute(Void aVoid) {
                    confirmDialog.dismiss();
                }
            }.execute();
        }
    }

    @Override
    public void showConfirmDialog(int title, int text, int positiveButtonText, int negativeButtonText, int cmdId, AppViewChoiceListener listener) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        showConfirmDialog(impl.getString(title, context), impl.getString(text, context),
                impl.getString(positiveButtonText, context),
                impl.getString(negativeButtonText, context), cmdId, listener);
    }

    @Override
    public void showNotification(final String text, final int length) {
        new AppViewAsyncTask(){
            @Override
            protected void onPostExecute(Void aVoid) {
                Toast.makeText(context, text, length).show();
            }
        }.execute();
    }

    @Override
    public void showChoiceDialog(final String title, final String[] choices, final int commandId, final AppViewChoiceListener listener) {
        new AppViewAsyncTask(){
            @Override
            protected void onPostExecute(Void aVoid) {
                if(choiceDialog != null) {
                    choiceDialog.dismiss();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(title).setItems(choices, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listener.appViewChoiceSelected(commandId, which);
                    }
                });
                choiceDialog = builder.create();
                choiceDialog.show();
            }
        }.execute();
    }

    @Override
    public void dismissChoiceDialog() {
        new AppViewAsyncTask(){
            @Override
            protected void onPostExecute(Void aVoid) {
                if(choiceDialog != null) {
                    choiceDialog.dismiss();
                    choiceDialog = null;
                }
            }
        }.execute();
    }
}
