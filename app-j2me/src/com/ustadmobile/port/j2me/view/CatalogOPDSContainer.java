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
import com.sun.lwuit.Component;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Display;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.util.LocaleUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.CatalogView;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author mike
 */
public class CatalogOPDSContainer extends UstadViewContainerJ2ME implements CatalogView, ActionListener, Runnable, ControllerReadyListener{

    
    private CatalogController controller;
    
    private UstadJSOPDSEntry[] entries;
    
    final private Hashtable entryIdToButtons;
    
    final private int CMD_REFRESH = 0;
    
    final private int CMD_DOWNLOAD_ALL = 1;
    
    final private int CMD_CONFIRM_OK = 2;
    
    final private int CMD_CONFIRM_CANCEL = 3;
    
    final private int CMD_DELETE_ENTRY = 4;
    
    public static final int CMDID_ADD = 5;
    
    public static final int CMDID_REMOVEFEED = 6;
    
    public static final int CMDID_BROWSEBUTTON = 7;
    
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
    
    private String browseButtonURL;
    
    boolean acquisition = false;
        
    private Command[] extraCommands;
        
    private Command browseCommand;
    
    /**
     * Form used for adding new feeds to the user's list
     */
    private CatalogAddFeedForm feedForm;
    
    private boolean browseButtonVisible;
    
    private boolean addOptionAvailable;
    
    private boolean deleteOptionAvailable;

    
    public CatalogOPDSContainer(Hashtable args, Object context, UstadViewFormJ2ME ustadForm) {
        super(args, context, ustadForm);
        entryIdToButtons = new Hashtable();
        statusesToUpdate = new Hashtable();
        catalogURL = (String)args.get(CatalogController.KEY_URL);
        resMode = ((Integer)args.get(CatalogController.KEY_RESMOD)).intValue();
        fetchFlags = ((Integer)args.get(CatalogController.KEY_FLAGS)).intValue();
        if(args.containsKey(CatalogController.KEY_BROWSE_BUTTON_URL)) {
            browseButtonURL = (String)args.get(CatalogController.KEY_BROWSE_BUTTON_URL);
        }
        
    }
    
    protected void initComponent() { 
        super.initComponent();
        if(browseCommand == null) {
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            browseCommand = new Command(impl.getString(MessageID.browse_feeds, 
                    getContext()),CMDID_BROWSEBUTTON);
            setLayout(new BoxLayout(BoxLayout.Y_AXIS));        
            loadCatalog();
        }
    }
    
    
    public void loadCatalog() {
        //loadCatalog(catalogURL, resMode);
        CatalogController.makeControllerForView(this, getArgs(), this);
    }
    
    /*
    public void loadCatalog(String url, int resourceMode) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        //impl.getAppView(getContext()).showProgressDialog(impl.getString(MessageIDConstants.loading));
        CatalogController.makeControllerForView(this, url, resourceMode, 
                fetchFlags, browseButtonURL, this);
    }
    */
    
    public void controllerReady(final UstadController controller, final int flags) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Display.getInstance().callSerially(new Runnable() {
            public void run() {
                if(controller != null) {
                    setController((CatalogController)controller);
                }else {
                    String errMsg = LocaleUtil.formatMessage(
                        impl.getString(MessageID.course_catalog_load_error, getContext()),
                        "Catalog controller");
                    impl.getAppView(getContext()).showAlertDialog(impl.getString(
                        MessageID.error, getContext()),errMsg);
                }
            }
        });
    }
    
    public void setController(CatalogController controller) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        controller.setUIStrings();
        
        this.controller = controller;
        entries = this.controller.getModel().opdsFeed.entries;
        entryIdToButtons.clear();
        removeAll();
        
        //TODO: Make the title change as you change tabs
        //setTitle(this.controller.getModel().opdsFeed.title);
        
        int i;
        int entryStatus = -1;
        int alignment = isRTL() ? Component.RIGHT : Component.LEFT;
        
        for(i=0; i<entries.length; i++){
            String title = entries[i].title;
            Command entryCmd = new Command(title);
            OPDSItemButton entryButton = new OPDSItemButton(entryCmd, entries[i]);
            entryButton.setAllStylesAlignment(alignment);
            
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
        
        if (acquisition){
            Command downloadAll = new Command(impl.getString(
                    MessageID.download_all,getContext()), CMD_DOWNLOAD_ALL);
            Button downloadAllButton = new Button(downloadAll);
            addComponent(downloadAllButton); 
        }
        repaint();
        
        getUstadForm().invalidateTitle();
        controller.loadThumbnails();
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
        Object component = evt.getComponent();
        if(component != null && component instanceof OPDSItemButton) {
            this.controller.handleClickEntry(
                    ((OPDSItemButton)component).getEntry().id);
        }
        
        switch(cmdId) {
            case CMD_CONFIRM_OK:
            case CMD_CONFIRM_CANCEL:
                confirmDialog.setVisible(false);
                confirmDialog.dispose();
                confirmDialog = null;
                this.controller.handleConfirmDialogClick(cmdId == CMD_CONFIRM_OK, 
                    this.confirmDialogCmdId);
                break;
            case CMD_DELETE_ENTRY:
                OPDSItemButton selectedButton = getFocusedButton();
                if(selectedButton != null) {
                    
                    this.controller.handleClickDeleteEntries(
                        new UstadJSOPDSEntry[]{selectedButton.getEntry()});
                }else {
                    UstadMobileSystemImpl.getInstance().getAppView(getContext()).showNotification(
                        UstadMobileSystemImpl.getInstance().getString(
                                MessageID.nothing_selected, getContext()),
                        AppView.LENGTH_LONG);
                }
                break;
            case CMD_REFRESH:
                refresh();
                break;
            case CMDID_ADD:
                controller.handleClickAdd();
                break;
            case CMDID_BROWSEBUTTON:
//                controller.handleClickBrowseButton();
                break;
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

    public void setEntrythumbnail(String entryId, String iconFileURI) {
        final OPDSItemButton opdsItem = (OPDSItemButton)entryIdToButtons.get(entryId);
        
        Image iconImg;
        InputStream in = null;
        try {
            in = UstadMobileSystemImpl.getInstance().openFileInputStream(iconFileURI);
            iconImg = Image.createImage(in);
            in.close();
            
            int h = iconImg.getHeight();
            
            
            int heightAvail = (opdsItem.getHeight() - opdsItem.getStyle().getPadding(TOP) - 
                opdsItem.getStyle().getPadding(BOTTOM))*2;
            
            if(h > heightAvail) {
                iconImg = iconImg.scaledHeight(heightAvail);
            }
            
            if(Display.getInstance().isEdt()) {
                opdsItem.setIcon(iconImg);
                revalidate();
            }else {
                final Image img = iconImg;
                Display.getInstance().callSerially(new Runnable() {
                    public void run() {
                        opdsItem.setIcon(img);
                        revalidate();
                    }
                });
            }
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 123, entryId +" : " + iconFileURI, e);
        }finally {
            UMIOUtils.closeInputStream(in);
        }
        
    }

    /**
     * Entry backgrounds are not used on J2ME to avoid running over memory limits.
     * Using it will have no effect on J2ME
     * 
     * @param entryId
     * @param backgroundFileURI 
     */
    public void setEntryBackground(String entryId, String backgroundFileURI) {
        //Not implemented on J2ME
    }

    public void setCatalogBackground(String backgroundFileURI) {
        //Not implemented on J2ME
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
    

    public void onCreateMenuCommands(Vector cmdVector) {
        super.onCreateMenuCommands(cmdVector);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(browseButtonVisible) {
            cmdVector.insertElementAt(browseCommand, 0);
        }
        
        if(deleteOptionAvailable) {
            cmdVector.addElement(new Command(impl.getString(MessageID.delete,
                    getContext()), CMD_DELETE_ENTRY));
        }
        
        if(addOptionAvailable) {
            cmdVector.addElement(new Command(impl.getString(MessageID.add_library,
                    getContext()),CMDID_ADD));
        }
        
        cmdVector.addElement(new Command(impl.getString(MessageID.refresh,
                getContext()),CMD_REFRESH));
        
        int i;
        if(extraCommands != null) {
            for(i = 0; i < extraCommands.length; i++) {
                cmdVector.addElement(extraCommands[i]);
            }
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
    
    public void refresh() {
        loadCatalog();
    }

    public void showAddFeedDialog() {
        feedForm = new CatalogAddFeedForm(
            UstadMobileSystemImpl.getInstance().getString(MessageID.add_library,
                    getContext()), this);
        feedForm.show();
    }
    
    public void setAddFeedDialogURL(String url) {
        feedForm.urlTextField.setText(url);
    }

    public String getAddFeedDialogURL() {
        return feedForm.urlTextField.getText();
    }

    public String getAddFeedDialogTitle() {
        return feedForm.titleTextField.getText();
    }

    public void setAddFeedDialogTitle(String title) {
        feedForm.titleTextField.setText(title);
    }

    public void setAddFeedDialogTextFieldsVisible(boolean visible) {
        feedForm.setTextFieldsVisible(visible);
    }
    
    

    
    void dismissFeedDialog(int cmdId) {
        if(cmdId == CatalogAddFeedForm.CMDID_OK) {
            String feedURL = getAddFeedDialogURL();
            String title = getAddFeedDialogTitle();
            controller.handleAddFeed(feedURL, title);
        }
        getUstadForm().show();
        feedForm = null;
    }

    public void setBrowseButtonVisible(boolean buttonVisible) {
        browseButtonVisible = buttonVisible;
        getUstadForm().invalidateMenuCommands();
    }

    public void setBrowseButtonLabel(String browseButtonLabel) {
        
    }

    public void setDeleteOptionAvailable(boolean deleteOptionAvailable) {
        this.deleteOptionAvailable = deleteOptionAvailable;
        getUstadForm().invalidateMenuCommands();
    }

    public void setAddOptionAvailable(boolean addOptionAvailable) {
        this.addOptionAvailable = addOptionAvailable;
        getUstadForm().invalidateMenuCommands();
    }
    
    
    

}
