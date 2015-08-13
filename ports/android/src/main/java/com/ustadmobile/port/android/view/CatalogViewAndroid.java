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
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
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

    private Map<OPDSEntryCard, EntryProgressUpdateRunnable> updateRunnableMap;

    /**
     * Map that holds the acquisition status of each entry in the form of OPDS ID -> Status
     */
    private Map<String, Integer> acquisitionStatusMap;

    private static int idCounter = 0;

    private int viewId;

    private CatalogOPDSFragment fragment;

    private UstadJSOPDSEntry[] selectedEntries;


    static {
        viewMap = new HashMap<Integer, CatalogViewAndroid>();
    }

    public CatalogViewAndroid() {
        viewId = CatalogViewAndroid.idCounter;
        CatalogViewAndroid.idCounter++;
        viewMap.put(new Integer(viewId), this);
        updateRunnableMap = new HashMap<OPDSEntryCard, EntryProgressUpdateRunnable>();
        acquisitionStatusMap = new HashMap<String, Integer>();
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
    public void setEntryStatus(final String entryId, final int status) {
        acquisitionStatusMap.put(entryId, new Integer(status));
        if(fragment != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    fragment.getEntryCardByOPDSID(entryId).setOPDSEntryOverlay(status);
                }
            });
        }
    }

    public int getEntryStatus(String entryId) {
        Integer intVal = acquisitionStatusMap.get(entryId);
        if(intVal != null) {
            return intVal.intValue();
        }else {
            return -1;
        }
    }

    @Override
    public void updateDownloadAllProgress(int i, int i1) {

    }

    @Override
    public void setDownloadEntryProgressVisible(String entryId, boolean visible) {
        OPDSEntryCard card = this.fragment.getEntryCardByOPDSID(entryId);
        if(card != null) {
            card.setProgressBarVisible(visible);
        }

        if(visible) {
            updateRunnableMap.put(card, new EntryProgressUpdateRunnable(card));
        }else {
            updateRunnableMap.remove(card);
        }
    }

    @Override
    public void updateDownloadEntryProgress(String entryId, int loaded, int total) {
        OPDSEntryCard card = this.fragment.getEntryCardByOPDSID(entryId);
        if(card != null && updateRunnableMap.containsKey(card)) {
            EntryProgressUpdateRunnable runnable = updateRunnableMap.get(card);
            int progressPercent = Math.round(((float)loaded/(float)total) * OPDSEntryCard.PROGRESS_ENTRY_MAX);
            runnable.setProgress(progressPercent);
            activity.runOnUiThread(runnable);
        }
    }

    @Override
    public UstadJSOPDSEntry[] getSelectedEntries() {
        return this.selectedEntries != null ? this.selectedEntries : new UstadJSOPDSEntry[0];
    }

    @Override
    public void setSelectedEntries(UstadJSOPDSEntry[] entries) {
        UstadJSOPDSFeed thisFeed = getController().getModel().opdsFeed;
        for(int i = 0; i < thisFeed.entries.length; i++) {
            boolean isSelected = false;
            for(int j = 0; j < entries.length; j++) {
                if(thisFeed.entries[i].id.equals(entries[j].id)) {
                    isSelected = true;
                    break;
                }
            }

            fragment.getEntryCardByOPDSID(thisFeed.entries[i].id).setSelected(isSelected);
        }

        this.selectedEntries = entries;
    }

    /**
     * Call this method from the fragment when the user has changed their selection
     * @param selectedEntries
     */
    public void handleUserSelectedEntryChange(UstadJSOPDSEntry[] selectedEntries) {
        this.selectedEntries = selectedEntries;
    }

    public class EntryProgressUpdateRunnable implements Runnable {

        private OPDSEntryCard card;

        private int progress;

        public EntryProgressUpdateRunnable(OPDSEntryCard card) {
            this.card = card;
            this.progress = 0;
        }

        public synchronized void setProgress(int progress) {
            this.progress = progress;
        }

        public void run() {
            synchronized (this) {
                card.setDownloadProgressBarProgress(progress);
            }
        }
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
