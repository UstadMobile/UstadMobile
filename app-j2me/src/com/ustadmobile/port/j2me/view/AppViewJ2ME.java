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
import com.ustadmobile.core.view.AppViewChoiceListener;
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
    
    private Button progressDialogLoadingButton;
    
    private Dialog alertDialog;
    
    private Dialog choiceDialog;
    
    private static final int CHOICEDIALOG_CMD_OFFSET = 50;
    
    private static final int CMDID_CLOSE_ALERT = 20;
    
    private UstadMobileSystemImplJ2ME impl;
    
    private NotificationPainter nxPainter;
    
    private Timer nxTimer;
    
    private WeakReference nxForm;
    
    /**
     * Flags of tasks that we need to run when called from callSerially in the UI thread
     */
    private int runTasks = 0;
    
    private static final int RUN_HIDENX = 1;
    
    private static final int RUN_DISMISS_PROGRESS = 2;
    
    private static final int RUN_DISMISS_ALERT = 4;
        
    private Command okCommand;
    
    private AppViewChoiceListener choiceListener;
    
    private int choiceDialogComamndId = 0;
    
    public AppViewJ2ME(UstadMobileSystemImplJ2ME impl) {
        progressDialog = null;
        this.impl = impl;
        okCommand = new Command("OK", CMDID_CLOSE_ALERT);
    }

    public void showChoiceDialog(final String title, final String[] choices, final int commandId, final AppViewChoiceListener listener) {
        this.choiceListener = listener;
        this.choiceDialogComamndId = commandId;
        final AppViewJ2ME appView = this;
        
        Display.getInstance().callSerially(new Runnable() {
            public void run() {
                boolean isNew = choiceDialog == null;
                if(isNew) {
                    choiceDialog = new Dialog(title);
                    choiceDialog.setAutoDispose(false);
                    choiceDialog.setScrollable(true);
                    choiceDialog.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
                }else {
                    choiceDialog.setTitle(title);
                    choiceDialog.removeAll();
                }
                
                Button b;
                Button firstButton = null;
                for(int i = 0; i < choices.length; i++) {
                    b = new Button(new Command(choices[i], i+CHOICEDIALOG_CMD_OFFSET));
                    b.addActionListener(appView);
                    choiceDialog.addComponent(b);
                    if(i == 0) firstButton = b;
                }
                
                if(isNew) {
                    choiceDialog.show(10, 10, 10, 10, true, false);
                }else {
                    choiceDialog.setFocused(firstButton);
                    choiceDialog.repaint();
                }
            }
        });
        
        
    }

    public void dismissChoiceDialog() {
        if(choiceDialog == null) {
            return;
        }
        
        Display.getInstance().callSerially(new Runnable() {
            public void run() {
                if(choiceDialog != null) {
                    choiceDialog.dispose();
                    choiceDialog = null;
                }
            }
        });
    }
    
    
    
    
    public void showProgressDialog(final String title) {
        Display.getInstance().callSerially(new Runnable() {
            public void run() {
                if(progressDialog == null) {
                    progressDialog = new Dialog();
                }

                progressDialog.removeAll();
                progressDialog.removeAllCommands();

                progressDialog.setAutoDispose(true);
                progressDialogLoadingButton = new Button(title);
                progressDialogLoadingButton.getStyle().setBorder(Border.createEmpty());
                progressDialogLoadingButton.getSelectedStyle().setBorder(Border.createEmpty());
                progressDialog.addComponent(progressDialogLoadingButton);
                progressDialog.showPacked(BorderLayout.CENTER, false);      
            }
        });
    }

    /**
     * @{inheritDoc}
     */
    public void setProgressDialogTitle(final String title) {
        if(progressDialogLoadingButton != null) {
            Display.getInstance().callSerially(new Runnable() {
               public void run() {
                   progressDialogLoadingButton.setText(title);
               } 
            });
        }
    }
    
    

    public boolean dismissProgressDialog() {
        if(progressDialog == null) {
            return false;
        }
        
        Display.getInstance().callSerially(new Runnable(){
            public void run() {
                if(progressDialog != null) {
                    progressDialog.dispose();
                    progressDialog = null;
                    progressDialogLoadingButton = null;
                }
            }
        });
        
        return true;
    }
    
    public void dismissAll() {
        dismissAlertDialog();
        dismissProgressDialog();
    }

    public void showAlertDialog(final String title, final String text) {
        final AppViewJ2ME appView = this;
        Display.getInstance().callSerially(new Runnable() {
            public void run() {
                if(alertDialog == null) {
                    alertDialog = new Dialog();
                }

                alertDialog.removeAll();
                alertDialog.removeAllCommands();

                alertDialog.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
                alertDialog.setTitle(title);
                TextArea textArea = new TextArea(text);
                textArea.setEditable(false);
                textArea.setFocusable(false);
                alertDialog.addComponent(textArea);
                alertDialog.setAutoDispose(false);
                alertDialog.addCommand(okCommand);
                alertDialog.addCommandListener(appView);
                alertDialog.showPacked(BorderLayout.CENTER, false);
            }
        });
    }

    public void dismissAlertDialog() {
        Display.getInstance().callSerially(new Runnable() {
            public void run() {
                if(alertDialog != null) {
                    alertDialog.dispose();
                    alertDialog = null;
                }
            }
        });
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
    
    protected synchronized int getRunTasks() {
        return runTasks;
    }
    
    public void run() {
        int tasks = getRunTasks();
        if((tasks & RUN_HIDENX) == RUN_HIDENX) {
            Object formObj = nxForm.get();
            if(formObj != null) {
                ((Form)formObj).setGlassPane(null);
            }
            
            if(nxTimer != null) {
                nxTimer.cancel();
                nxTimer = null;
            }
        }
        
        if((tasks & RUN_DISMISS_PROGRESS) == RUN_DISMISS_PROGRESS) {
            dismissProgressDialog();
        }
        
        if((tasks & RUN_DISMISS_ALERT) == RUN_DISMISS_ALERT) {
            dismissAlertDialog();
        }
        
        setRunTasks(0);
    }

    public void actionPerformed(ActionEvent ae) {
        int cmdId = ae.getCommand().getId();
        if(cmdId == CMDID_CLOSE_ALERT) {
            dismissAlertDialog();
        }else if(cmdId >= CHOICEDIALOG_CMD_OFFSET) {
            int choiceChosen = cmdId - CHOICEDIALOG_CMD_OFFSET;
            if(choiceListener != null) {
                choiceListener.appViewChoiceSelected(choiceDialogComamndId, 
                        choiceChosen);
            }
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
