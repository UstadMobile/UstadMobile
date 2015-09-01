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
package com.ustadmobile.port.j2me.view;

import com.ustadmobile.core.view.AppView;
import com.sun.lwuit.*;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.plaf.Border;
import com.sun.lwuit.events.*;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author mike
 */
public class AppViewJ2ME implements AppView, ActionListener, Runnable {

    /**
     * Progress Dialog
     */
    private Dialog progressDialog;
    
    private Dialog alertDialog;
    
    private static int CMDID_CLOSE_ALERT = 20;
    
    private UstadMobileSystemImplJ2ME impl;
    
    private NotificationPainter nxPainter;
    
    private Timer nxTimer;
    
    private WeakReference nxForm;
    
    /**
     * Flags of tasks that we need to run when called from callSerially in the UI thread
     */
    private int runTasks = 0;
    
    private static final int RUN_HIDENX = 1;
    
    
    public AppViewJ2ME(UstadMobileSystemImplJ2ME impl) {
        progressDialog = null;
        this.impl = impl;
    }
    
    public void showProgressDialog(String title) {
        dismissAlertDialog();
        progressDialog = new Dialog();
        Button loadingButton = new Button(title);
        loadingButton.getStyle().setBorder(Border.createEmpty());
        loadingButton.getSelectedStyle().setBorder(Border.createEmpty());
        progressDialog.addComponent(loadingButton);
        progressDialog.showPacked(BorderLayout.CENTER, false);        
    }

    public boolean dismissProgressDialog() {
        if(progressDialog != null) {
            progressDialog.setVisible(false);
            progressDialog.dispose();
            progressDialog = null;
            return true;
        }else {
            return false;
        }
    }

    public void showAlertDialog(String title, String text) {
        alertDialog = new Dialog();
        alertDialog.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        alertDialog.setTitle(title);
        TextArea textArea = new TextArea(text);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        alertDialog.addComponent(textArea);
        
        Button okButton = new Button(new Command("OK", CMDID_CLOSE_ALERT));
        alertDialog.addComponent(okButton);
        okButton.addActionListener(this);
        alertDialog.showPacked(BorderLayout.CENTER, false);
        okButton.setFocus(true);
    }

    public void dismissAlertDialog() {
        if(alertDialog != null) {
            alertDialog.setVisible(false);
            alertDialog.dispose();
            alertDialog = null;
        }
    }

    public void showNotification(String text, int length) {
        nxPainter = new NotificationPainter();
        nxPainter.setText(text);
        Form currentForm = impl.getCurrentForm();
        nxForm = new WeakReference(currentForm);
        currentForm.setGlassPane(nxPainter);
        nxTimer = new Timer();
        nxTimer.schedule(new HideNotificationTask(this), 4000);
    }
    
    private void hideNotification() {
        addRunTask(RUN_HIDENX);
        Display.getInstance().callSerially(this);
    }
    
    protected synchronized void addRunTask(int task) {
        runTasks = runTasks | task;
    }
    
    protected synchronized void setRunTasks(int tasks) {
        runTasks = tasks;
    }
    
    public void run() {
        if((runTasks & RUN_HIDENX) == RUN_HIDENX) {
            Object formObj = nxForm.get();
            if(formObj != null) {
                ((Form)formObj).setGlassPane(null);
            }
            
            if(nxTimer != null) {
                nxTimer.cancel();
                nxTimer = null;
            }
        }
        
        setRunTasks(0);
    }

    public void actionPerformed(ActionEvent ae) {
        int cmdId = ae.getCommand().getId();
        if(cmdId == CMDID_CLOSE_ALERT) {
            dismissAlertDialog();
        }
    }

    
    public class HideNotificationTask extends TimerTask {

        private AppViewJ2ME host;
        
        public HideNotificationTask(AppViewJ2ME host) {
            this.host = host;
        }
        
        public void run() {
            host.hideNotification();
        }
        
    }
}
