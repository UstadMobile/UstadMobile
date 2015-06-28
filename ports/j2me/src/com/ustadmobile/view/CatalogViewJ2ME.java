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
    private final UstadJSOPDSEntry[] entries;
    
    //private TextField textField;
    
    public CatalogViewJ2ME() {
        /*
        textField = new TextField();
        addComponent(textField);
        
        * */
        Command refreshCmd = new Command("Refresh", CMD_REFRESH);
        Button refreshButton = new Button(refreshCmd);
        refreshButton.addActionListener(this);
        this.addComponent(refreshButton);
        
        entries = feed.entries;
        int i;
        for(i=0; i<entries.length; i++){
            String title = entries[i].title;
            Command entry = new Command(title, i+1);
            Button entryButton = new Button(entry);
            this.addComponent(entryButton);
        }
        
    }
    
    private CatalogController controller;
    private UstadJSOPDSFeed feed;
    
    public void setController(CatalogController controller) {
        this.controller = controller;
    }

    public void showDialog(String title, String text) {
        
    }

    public void actionPerformed(ActionEvent evt) {
        if(evt.getCommand().getId() == CMD_REFRESH){
            this.controller = this.controller.makeDeviceCatalog();
            //this.controller.handleClickRefresh();
        }else{
            int entryid = evt.getCommand().getId() - 1;
            if (entryid > 0){
                int a=0;
                //make a new epub controller and show it.
            }
            
        }
        
    }

    public void setFeed(UstadJSOPDSFeed feed) {
        this.feed = feed;
    }

    
}
