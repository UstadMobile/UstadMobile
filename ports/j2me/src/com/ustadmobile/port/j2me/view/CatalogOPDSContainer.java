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

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Display;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.ustadmobile.core.U;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.util.LocaleUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.CatalogView;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author mike
 */
public class CatalogOPDSContainer extends UstadViewContainerJ2ME implements CatalogView, ActionListener, Runnable, ControllerReadyListener{

    
    public static final int OPDSCMDID_MAX = 999;
    
    public static int OPDSCMDID_OFFSET = 30;
    
    public static int MENUCMD_OFFSET = 10;
    
    private CatalogController controller;
    
    private UstadJSOPDSEntry[] entries;
    
    final private Hashtable entryIdToButtons;
    
    
    final private int CMD_REFRESH = 0;
    
    final private int CMD_DOWNLOAD_ALL = 1;
    
    final private int CMD_CONFIRM_OK = 2;
    
    final private int CMD_CONFIRM_CANCEL = 3;
    
    final private int CMD_DELETE_ENTRY = 4;
    
    public static final int CMDID_ADDFEED = 5;
    
    public static final int CMDID_REMOVEFEED = 6;
    
    private int confirmDialogCmdId = 0;

    private Dialog confirmDialog;
    
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
    
    private String catalogURL;
    private int resMode;
    private int fetchFlags;
    
    boolean acquisition = false;
    
    private Command[] menuCommands;
    
    private Command[] extraCommands;
    
    public CatalogOPDSContainer(Hashtable args, Object context, UstadViewFormJ2ME ustadForm) {
        super(args, context, ustadForm);
        
        Label spaceLabel = new Label(" ");
        addComponent(spaceLabel);
        
        //Set Layout of the form.
        BoxLayout boxLayout = new BoxLayout(BoxLayout.Y_AXIS);
        setLayout(boxLayout);
        
        entryIdToButtons = new Hashtable();
        statusesToUpdate = new Hashtable();
        catalogURL = (String)args.get(CatalogController.KEY_URL);
        resMode = ((Integer)args.get(CatalogController.KEY_RESMOD)).intValue();
        fetchFlags = ((Integer)args.get(CatalogController.KEY_FLAGS)).intValue();
        loadCatalog();
    }
    
    public void loadCatalog() {
        loadCatalog(catalogURL, resMode);
    }
    
    public void loadCatalog(String url, int resourceMode) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        CatalogController.makeControllerForView(this, url, resourceMode, 
                fetchFlags, this);
    }
    
    public void controllerReady(final UstadController controller, final int flags) {
        if(controller != null) {
            Display.getInstance().callSerially(new Runnable() {
                public void run() {
                    setController((CatalogController)controller);
                }
            });
        }else {
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            String errMsg = LocaleUtil.formatMessage(impl.getString(U.id.course_catalog_load_error),
                    "Catalog controller");
            impl.getAppView(getContext()).showAlertDialog(impl.getString(U.id.error),
                errMsg);
        }
    }
    
    public void setController(CatalogController controller) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        /*
        if(this.controller != null) {
            return;
        }
        */
        
        controller.setUIStrings();
        
        this.controller = controller;
        entries = this.controller.getModel().opdsFeed.entries;
        entryIdToButtons.clear();
        removeAll();
        
        //TODO: Make the title change as you change tabs
        //setTitle(this.controller.getModel().opdsFeed.title);
        
        int i;
        int entryStatus = -1;
        
        for(i=0; i<entries.length; i++){
            String title = entries[i].title;
            Command entryCmd = new Command(title, i+OPDSCMDID_OFFSET);
            OPDSItemButton entryButton = new OPDSItemButton(entryCmd, entries[i]);
            
            entryStatus = controller.getEntryAcquisitionStatus(entries[i].id);
            if(entryStatus != -1) {
                entryButton.setAcquisitionStatus(entryStatus);
            }

            if(entryStatus == CatalogEntryInfo.ACQUISITION_STATUS_INPROGRESS) {
                entryButton.setProgressBarVisible(true);
            }
            
            addComponent(entryButton);
            entryIdToButtons.put(entries[i].id, entryButton);
        }
        
        Label spaceLabel = new Label(" ");
        addComponent(spaceLabel);
        
        Command refreshCmd = new Command(impl.getString(U.id.refresh), CMD_REFRESH);
        Button refreshButton = new Button(refreshCmd);
        this.addComponent(refreshButton);
        
        
        if (acquisition){
            Command downloadAll = new Command(impl.getString(U.id.download_all), CMD_DOWNLOAD_ALL);
            Button downloadAllButton = new Button(downloadAll);
            this.addComponent(downloadAllButton); 
        }
        repaint();
        
        getUstadForm().invalidateTitle();
    }
    
    public String getTitle() {
        if(controller != null) {
            return controller.getModel().opdsFeed.title;
        }else {
            return null;
        }
    }
    
    public void actionPerformed(ActionEvent evt) {
        int cmdId = evt.getCommand().getId();
        if(cmdId >= OPDSCMDID_OFFSET && cmdId < OPDSCMDID_MAX) {
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
                UstadMobileSystemImpl.getInstance().getAppView(getContext()).showNotification(
                    UstadMobileSystemImpl.getInstance().getString(U.id.nothing_selected),
                    AppView.LENGTH_LONG);
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

    /**
     * Set the additional commands for this particular container (e.g. add
     * feed to list etc)
     * 
     * @param extraCommands 
     */
    public void setExtraCommands(Command[] extraCommands) {
        this.extraCommands = extraCommands;
    }
    

    /**
     * Sets the options available in the menu (this could be a drawer, J2ME menu, etc)
     * 
     * When an item is clicked/tapped called controller.handleMenuItemClick(index)
     * 
     * @param String array of options to show in the menu
     */
    public void setMenuOptions(String[] menuOptions){
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        //removeAllCommands();
        
        menuCommands = new Command[menuOptions.length+1];
        menuCommands[0] = new Command(impl.getString(U.id.delete), 
                CMD_DELETE_ENTRY);
        
        for(int i = 0; i < menuOptions.length; i++) {
            menuCommands[i+1] = new Command(menuOptions[i], i + MENUCMD_OFFSET);
        }
        
        getUstadForm().invalidateMenuCommands();
    }

    public void onCreateMenuCommands(Vector cmdVector) {
        if(menuCommands == null) {
            return;//not ready yet
        }
        
        int i;
        if(extraCommands != null) {
            for(i = 0; i < extraCommands.length; i++) {
                cmdVector.addElement(extraCommands[i]);
            }
        }
        
        for(i = 0; i < menuCommands.length; i++) {
            cmdVector.addElement(menuCommands[i]);
        }
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
    

    /**
     * Get the entries that have been selected by the user (e.g. by long press)
     * 
     * @return Array of entries selected by the user
     */
    public UstadJSOPDSEntry[] getSelectedEntries(){
        Enumeration e = entryIdToButtons.keys();
        String id;
        OPDSItemButton button;
        while(e.hasMoreElements()) {
            id = (String)e.nextElement();
            button = (OPDSItemButton)entryIdToButtons.get(id);
            if(button.hasFocus()) {
                return new UstadJSOPDSEntry[] {button.getEntry()};
            }
        }
        
        return new UstadJSOPDSEntry[0];
    }
    
    
    /**
     * Set the entries that are to be marked as selected
     * 
     * @param entries Array of entries to be marked as selected
     */
    public void setSelectedEntries(UstadJSOPDSEntry[] entries){
        //Things aren't really selected on J2ME - do nothing
    }

    
    public void onDestroy() {        
        super.onDestroy();
        if(controller != null) {
            controller.handleViewDestroy();
        }
    }

}
