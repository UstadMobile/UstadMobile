/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.view;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.ustadmobile.opds.UstadJSOPDSFeed;
import com.ustadmobile.opds.*;
import com.ustadmobile.controller.CatalogController;

/**
 *
 * @author varuna
 */
public class CatalogViewJ2ME extends Form implements CatalogView, ActionListener {

    private int CMD_REFRESH = 0;
    private int CMD_DOWNLOAD_ALL = 9999999;
    private UstadJSOPDSEntry[] entries;
    private CatalogController controller;
    //private UstadJSOPDSFeed feed;
    boolean acquisition = false;
        
    public CatalogViewJ2ME() {
        /*
        textField = new TextField();
        addComponent(textField);
        
        * */
        
        Label spaceLabel = new Label(" ");
        addComponent(spaceLabel);
        
        
        
    }
    
    public void setController(CatalogController controller) {
        this.controller = controller;
        entries = this.controller.getModel().opdsFeed.entries;
        int i;
        for(i=0; i<entries.length; i++){
            String title = entries[i].title;
            Command entry = new Command(title, i+1);
            Button entryButton = new Button(entry);
            this.addComponent(entryButton);
            Label spaceLabel = new Label(" ");
            addComponent(spaceLabel);
        }
        Command refreshCmd = new Command("Refresh", CMD_REFRESH);
        Button refreshButton = new Button(refreshCmd);
        refreshButton.addActionListener(this);
        this.addComponent(refreshButton);
        
        Label spaceLabel = new Label(" ");
        addComponent(spaceLabel);
        
        if (acquisition){
            Command downloadAll = new Command("Download All", CMD_DOWNLOAD_ALL);
            Button downloadAllButton = new Button(downloadAll);
            downloadAllButton.addActionListener(this);
            this.addComponent(downloadAllButton); 
       }
        
    }

    public void showDialog(String title, String text) {
        
    }

    public void actionPerformed(ActionEvent evt) {
        if(evt.getCommand().getId() == CMD_REFRESH){
            this.controller = this.controller.makeDeviceCatalog();
            this.controller.show();
            //this.controller.handleClickRefresh();
        }else{
            int entryid = evt.getCommand().getId() - 1;
            if (entryid > 0){
                int a=0;
                //make a new epub controller and show it.
            }
            
        }
        
    }

    public void showDialog(String title, String text, int commandId) {
        throw new UnsupportedOperationException("Not supported yet."); 
        //To change body of generated methods, choose Tools | Templates.
    }

    public void showContainerContextMenu(UstadJSOPDSItem item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void hideContainerContextMenu() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setEntryStatus(String entryId, int status) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void updateDownloadAllProgress(int loaded, int total) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setDownloadEntryProgressVisible(String entryId, boolean visible) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void updateDownloadEntryProgress(String entryId, int loaded, int total) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
