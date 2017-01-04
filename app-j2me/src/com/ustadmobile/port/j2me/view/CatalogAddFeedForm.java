/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.view;

import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Command;
import com.sun.lwuit.Form;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.SelectionListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

/**
 *
 * @author mike
 */
public class CatalogAddFeedForm extends Form implements SelectionListener, ActionListener {
    
    TextField urlTextField;
    
    TextField titleTextField;
    
    CatalogOPDSContainer opdsContainer;
    
    ComboBox presetComboBox;
    
    public static final int CMDID_OK = 1;
    
    public static final int CMDID_CANCEL = 2;
    
    
    public CatalogAddFeedForm(String title, CatalogOPDSContainer opdsContainer) {
        super(title);
        this.opdsContainer = opdsContainer;
    }
    
    public void initComponent() {
        if(urlTextField == null) {
            presetComboBox = new ComboBox(
                opdsContainer.getController().getFeedList(
                CatalogController.OPDS_FEEDS_INDEX_TITLE));
            presetComboBox.addSelectionListener(this);
            addComponent(presetComboBox);
            
            setLayout(new BoxLayout(BoxLayout.Y_AXIS));
            urlTextField = new TextField();
            
            titleTextField = new TextField();
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            addCommand(new Command(impl.getString(MessageIDConstants.ok), CMDID_OK));
            addCommand(new Command(impl.getString(MessageIDConstants.cancel), CMDID_CANCEL));
            addCommandListener(this);
        }
        
        
    }
    
    public void setTextFieldsVisible(boolean visible) {
        if(visible && !contains(urlTextField)) {
            addComponent(urlTextField);
            addComponent(titleTextField);
        }
        
        if(!visible && contains(urlTextField)) {
            removeComponent(urlTextField);
            removeComponent(titleTextField);
        }
    }

    public void selectionChanged(int oldSelected, int newSelected) {
        opdsContainer.getController().handleFeedPresetSelected(newSelected);
    }

    public void actionPerformed(ActionEvent ae) {
        opdsContainer.dismissFeedDialog(ae.getCommand().getId());
    }
    
    
}
