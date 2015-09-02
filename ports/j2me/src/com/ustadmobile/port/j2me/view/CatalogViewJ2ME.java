/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.view;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.ustadmobile.core.opds.*;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AppView;
import java.util.Hashtable;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import java.util.Enumeration;

/**
 *
 * @author varuna
 */
public class CatalogViewJ2ME extends Form implements CatalogView, ActionListener, Runnable {

    public static int OPDSCMDID_OFFSET = 30;
    
    public static int MENUCMD_OFFSET = 10;
    
    final private int CMD_REFRESH = 0;
    final private int CMD_DOWNLOAD_ALL = 1;
    
    final private int CMD_CONFIRM_OK = 2;
    
    final private int CMD_CONFIRM_CANCEL = 3;
    
    final private int CMD_DELETE_ENTRY = 4;
    
    private Dialog confirmDialog;
    
    private UstadJSOPDSEntry[] entries;
    private CatalogController controller;

    boolean acquisition = false;
    
    final private Hashtable entryIdToButtons;
    
    private int confirmDialogCmdId = 0;
        
    /**
     * A list of status items to update
     */
    final private Hashtable statusesToUpdate;
    
    /**
     * Flags for what things we have to do if the run method is called
     */
    private int runFlags = 0;
    
    /**
     * Flag to indicate when run method is called we should run a status update on
     * the entries contained in statusesToUpdate
     */
    private static final int RUN_STATUSUPDATE = 1;
        
    public CatalogViewJ2ME() {
        Label spaceLabel = new Label(" ");
        addComponent(spaceLabel);
        
        //Set Layout of the form.
        BoxLayout boxLayout = new BoxLayout(BoxLayout.Y_AXIS);
        setLayout(boxLayout);
        entryIdToButtons = new Hashtable();
        statusesToUpdate = new Hashtable();        
        addCommandListener(this);
    }
    
    public void show() {
        UstadMobileSystemImplJ2ME.getInstanceJ2ME().handleFormShow(this);
        super.show();
    }
    
    public void setController(CatalogController controller) {
        if(this.controller != null) {
            return;
        }
        
        this.controller = controller;
        entries = this.controller.getModel().opdsFeed.entries;
        entryIdToButtons.clear();
        setTitle(this.controller.getModel().opdsFeed.title);
        
        int i;
        for(i=0; i<entries.length; i++){
            String title = entries[i].title;
            Command entryCmd = new Command(title, i+OPDSCMDID_OFFSET);
            OPDSItemButton entryButton = new OPDSItemButton(entryCmd, entries[i]);
            //entryButton.addActionListener(this);
            addComponent(entryButton);
            entryIdToButtons.put(entries[i].id, entryButton);
        }
        
        Label spaceLabel = new Label(" ");
        addComponent(spaceLabel);
        
        Command refreshCmd = new Command("Refresh", CMD_REFRESH);
        Button refreshButton = new Button(refreshCmd);
        refreshButton.addActionListener(this);
        this.addComponent(refreshButton);
        
        
        if (acquisition){
            Command downloadAll = new Command("Download All", CMD_DOWNLOAD_ALL);
            Button downloadAllButton = new Button(downloadAll);
            downloadAllButton.addActionListener(this);
            this.addComponent(downloadAllButton); 
       }
        
    }

    public void actionPerformed(ActionEvent evt) {
        int cmdId = evt.getCommand().getId();
        if(cmdId >= OPDSCMDID_OFFSET) {
            UstadJSOPDSEntry clickedEntry = controller.getModel().opdsFeed.entries[cmdId - OPDSCMDID_OFFSET];
            this.controller.handleClickEntry(clickedEntry);
        }else if(cmdId >= MENUCMD_OFFSET) {
            this.controller.handleClickMenuItem(cmdId - MENUCMD_OFFSET);
        }else if(cmdId == CMD_CONFIRM_OK || cmdId == CMD_CONFIRM_CANCEL) {
            confirmDialog.setVisible(false);
            confirmDialog.dispose();
            confirmDialog = null;
            this.controller.handleConfirmDialogClick(cmdId == CMD_CONFIRM_OK, 
                this.confirmDialogCmdId);
        }else if(cmdId == CMD_DELETE_ENTRY) {
            OPDSItemButton selectedButton = getFocusedButton();
            if(selectedButton != null) {
                this.controller.handleClickDeleteEntries(
                    new UstadJSOPDSEntry[]{selectedButton.getEntry()});
            }else {
                UstadMobileSystemImpl.getInstance().getAppView().showNotification(
                        "No Entry Selected!", AppView.LENGTH_LONG);
            }
        }
    }
    
    /**
     * Find the button which is currently focused - if any
     * @return 
     */
    public OPDSItemButton getFocusedButton() {
        Enumeration e = entryIdToButtons.keys();
        OPDSItemButton currentButton;
        while(e.hasMoreElements()) {
            currentButton = (OPDSItemButton)entryIdToButtons.get(e.nextElement());
            if(currentButton.hasFocus()) {
                return currentButton;
            }
        }
        
        return null;
    }

    public void showContainerContextMenu(UstadJSOPDSItem item) {
    }

    public void hideContainerContextMenu() {
    }

    public synchronized void addRunFlag(int flag) {
        runFlags = runFlags | flag;
    }
    
    public synchronized void setRunFlags(int runFlags) {
        this.runFlags = runFlags;
    }
    
    public synchronized int getRunFlags() {
        return this.runFlags;
    }
    
    public void setEntryStatus(String entryId, int status) {
        if(Display.getInstance().isEdt()) {
            ((OPDSItemButton)entryIdToButtons.get(entryId)).setAcquisitionStatus(status);
        }else {
            statusesToUpdate.put(entryIdToButtons.get(entryId), new Integer(status));
            addRunFlag(RUN_STATUSUPDATE);
            Display.getInstance().callSerially(this);
        }
    }
    
    public void run() {
        int flags = getRunFlags();
        if((flags & RUN_STATUSUPDATE) == RUN_STATUSUPDATE) {
            Enumeration e = statusesToUpdate.keys();
            OPDSItemButton btn;
            int status;
            while(e.hasMoreElements()) {
                btn = (OPDSItemButton)e.nextElement();
                status = ((Integer)statusesToUpdate.get(btn)).intValue();
                btn.setAcquisitionStatus(status);
            }
            statusesToUpdate.clear();
        }
        
        setRunFlags(0);
    }

    public void updateDownloadAllProgress(int loaded, int total) {
    }

    public void setDownloadEntryProgressVisible(String entryId, boolean visible) {
        ((OPDSItemButton)entryIdToButtons.get(entryId)).setProgressBarVisible(visible);
    }

    public void updateDownloadEntryProgress(String entryId, int loaded, int total) {
        OPDSItemButton entryButton = 
            (OPDSItemButton)entryIdToButtons.get(entryId);
        entryButton.updateProgress((int) (((float)loaded/(float)total) * 100));
    }

    public CatalogController getController() {
        return controller;
    }

    public void showConfirmDialog(String title, String message, String positiveChoice, String negativeChoice, int commandId) {
        //TODO: this can be called twice: should not be happening.  See why is the actionPerformed event double firing?
        if(confirmDialog != null) {
            UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.INFO, 306, null);
            return;
        }
        
        this.confirmDialogCmdId = commandId;
        
        confirmDialog = new Dialog(title);
        confirmDialog.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        TextArea dialogText = new TextArea(message);
        dialogText.setEditable(false);
        dialogText.setFocusable(false);
        confirmDialog.addComponent(dialogText);
        
        Button okButton = new Button(new Command(positiveChoice, CMD_CONFIRM_OK));
        okButton.addActionListener(this);
        confirmDialog.addComponent(okButton);
        
        Button cancelButton = new Button(new Command(negativeChoice, CMD_CONFIRM_CANCEL));
        cancelButton.addActionListener(this);
        confirmDialog.addComponent(cancelButton);
        confirmDialog.showPacked(BorderLayout.CENTER, false);
    }

    public boolean isShowing() {
        return this.isVisible();
    }
    
    /**
     * Set the entries that are to be marked as selected
     * 
     * @param entries Array of entries to be marked as selected
     */
    public void setSelectedEntries(UstadJSOPDSEntry[] entries){
        //ToDo
    }
    /**
     * Get the entries that have been selected by the user (e.g. by long press)
     * 
     * @return Array of entries selected by the user
     */
    public UstadJSOPDSEntry[] getSelectedEntries(){
        return null;
    }
    
    /**
     * Sets the options available in the menu (this could be a drawer, J2ME menu, etc)
     * 
     * When an item is clicked/tapped called controller.handleMenuItemClick(index)
     * 
     * @param String array of options to show in the menu
     */
    public void setMenuOptions(String[] menuOptions){
        removeAllCommands();
        
        Command deleteCommand = new Command("Delete", CMD_DELETE_ENTRY);
        addCommand(deleteCommand);
        
        for(int i = 0; i < menuOptions.length; i++) {
            addCommand(new Command(menuOptions[i], i + MENUCMD_OFFSET));
        }
            
        
        
    }
}
