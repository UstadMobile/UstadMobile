/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.view;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.*;
import com.ustadmobile.core.controller.CatalogController;
import java.util.Hashtable;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import java.io.IOException;

/**
 *
 * @author varuna
 */
public class CatalogViewJ2ME extends Form implements CatalogView, ActionListener {

    public static int OPDSCMDID_OFFSET = 30;
    
    public static int MENUCMD_OFFSET = 10;
    
    private int CMD_REFRESH = 0;
    private int CMD_DOWNLOAD_ALL = 1;
    private UstadJSOPDSEntry[] entries;
    private CatalogController controller;
    //private UstadJSOPDSFeed feed;
    boolean acquisition = false;
    
    private Hashtable entryIdToButtons;
        
    public CatalogViewJ2ME() {
        Label spaceLabel = new Label(" ");
        addComponent(spaceLabel);
        
        //Set Layout of the form.
        BoxLayout boxLayout = new BoxLayout(BoxLayout.Y_AXIS);
        setLayout(boxLayout);
        entryIdToButtons = new Hashtable();
        addCommandListener(this);
    }
    
    public void show() {
        UstadMobileSystemImplJ2ME.getInstanceJ2ME().handleFormShow(this);
        super.show();
    }
    
    public void setController(CatalogController controller) {
        this.controller = controller;
        entries = this.controller.getModel().opdsFeed.entries;
        entryIdToButtons.clear();
        setTitle(this.controller.getModel().opdsFeed.title);
        
        int i;
        for(i=0; i<entries.length; i++){
            String title = entries[i].title;
            Command entry = new Command(title, i+OPDSCMDID_OFFSET);
            OPDSItemButton entryButton = new OPDSItemButton(entry);
            entryButton.addActionListener(this);
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

    public void showDialog(String title, String text) {
        //alert box
    }

    public void actionPerformed(ActionEvent evt) {
        int cmdId = evt.getCommand().getId();
        if(cmdId >= OPDSCMDID_OFFSET) {
            UstadJSOPDSEntry clickedEntry = controller.getModel().opdsFeed.entries[cmdId - OPDSCMDID_OFFSET];
            this.controller.handleClickEntry(clickedEntry);
        }else if(cmdId >= MENUCMD_OFFSET) {
            this.controller.handleClickMenuItem(cmdId - MENUCMD_OFFSET);
        }
    }

    public void showDialog(String title, String text, int commandId) {
        //Display.getInstance().callSerially(null);
        //To change body of generated methods, choose Tools | Templates.
    }

    public void showContainerContextMenu(UstadJSOPDSItem item) {
    }

    public void hideContainerContextMenu() {
    }

    public void setEntryStatus(String entryId, int status) {
    }

    public void updateDownloadAllProgress(int loaded, int total) {
    }

    public void setDownloadEntryProgressVisible(String entryId, boolean visible) {
    }

    public void updateDownloadEntryProgress(String entryId, int loaded, int total) {
        OPDSItemButton entryButton = 
            (OPDSItemButton)entryIdToButtons.get(entryId);
        //update with percentage
        entryButton.updateProgress((loaded/total)*100);
    }

    public CatalogController getController() {
        return controller;
    }

    public void showConfirmDialog(String title, String message, String positiveChoice, String negativeChoice, int commandId) {
        //ToDo: This
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
        for(int i = 0; i < menuOptions.length; i++) {
            addCommand(new Command(menuOptions[i], i + MENUCMD_OFFSET));
        }
    }
    
    
    
    
}
