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


import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by mike on 07/07/15.
 */
public class CatalogViewAndroid implements CatalogView {
    private CatalogController controller;

    private CatalogActivity activity;

    private static Map<Integer, CatalogViewAndroid> viewMap;

    private static int idCounter = 0;

    private int viewId;

    private CatalogOPDSFragment fragment;



    static {
        viewMap = new HashMap<Integer, CatalogViewAndroid>();
    }

    public CatalogViewAndroid() {
        viewId = CatalogViewAndroid.idCounter;
        CatalogViewAndroid.idCounter++;
        viewMap.put(new Integer(viewId), this);
    }

    public int getViewId() {
        return viewId;
    }

    public static CatalogViewAndroid getViewById(int id) {
        return viewMap.get(new Integer(id));
    }

    public void setCatalogViewActivity(CatalogActivity activity) {
        this.activity = activity;
    }

    public void setFragment(CatalogOPDSFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void setController(CatalogController catalogController) {
        this.controller = catalogController;
    }

    @Override
    public CatalogController getController() {
        return controller;
    }

    @Override
    public void showConfirmDialog(String title, String message, String positiveChoice, String negativeChoice, final int commandId) {
        //android.support.v4.
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton(positiveChoice, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                controller.handleConfirmDialogClick(true, commandId);
            }
        });
        builder.setNegativeButton(negativeChoice, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                controller.handleConfirmDialogClick(false, commandId);
            }
        });
        builder.create().show();
    }

    @Override
    public void showContainerContextMenu(UstadJSOPDSItem ustadJSOPDSItem) {

    }

    @Override
    public void hideContainerContextMenu() {

    }

    @Override
    public void setEntryStatus(String s, int i) {

    }

    @Override
    public void updateDownloadAllProgress(int i, int i1) {

    }

    @Override
    public void setDownloadEntryProgressVisible(String s, boolean b) {

    }

    @Override
    public void updateDownloadEntryProgress(String s, int i, int i1) {

    }

    @Override
    public void show() {
        UstadMobileSystemImplAndroid impl = UstadMobileSystemImplAndroid.getInstanceAndroid();
        if(impl.getCurrentContext() instanceof CatalogActivity) {
            ((CatalogActivity)impl.getCurrentContext()).setCurrentOPDSCatalogFragment(this);
        }else {
            impl.startActivityForViewId(CatalogActivity.class, this.viewId);
        }
    }

    @Override
    public boolean isShowing() {
        if(UstadMobileSystemImplAndroid.getInstanceAndroid().getCurrentActivity() instanceof CatalogActivity && activity != null) {
            if(activity.getCurrentFragment() instanceof CatalogOPDSFragment) {
                return ((CatalogOPDSFragment)activity.getCurrentFragment()).getCatalogView() == this;
            }
        }

        return false;
    }
}
