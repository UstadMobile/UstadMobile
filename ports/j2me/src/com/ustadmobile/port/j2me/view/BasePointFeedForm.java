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
import com.ustadmobile.core.U;
import com.ustadmobile.core.controller.BasePointController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

/**
 *
 * @author mike
 */
public class BasePointFeedForm extends Form implements SelectionListener, ActionListener {
    
    TextField urlTextField;
    
    TextField titleTextField;
    
    BasePointViewJ2ME basePointView;
    
    ComboBox presetComboBox;
    
    public static final int CMDID_OK = 1;
    
    public static final int CMDID_CANCEL = 2;
    
    
    public BasePointFeedForm(String title, BasePointViewJ2ME basePointView) {
        super(title);
        this.basePointView = basePointView;
    }
    
    public void initComponent() {
        if(urlTextField == null) {
            presetComboBox = new ComboBox(
                basePointView.getBasePointController().getFeedList(
                BasePointController.OPDS_FEEDS_INDEX_TITLE));
            presetComboBox.addSelectionListener(this);
            addComponent(presetComboBox);
            
            setLayout(new BoxLayout(BoxLayout.Y_AXIS));
            urlTextField = new TextField();
            addComponent(urlTextField);
            
            titleTextField = new TextField();
            addComponent(titleTextField);
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            addCommand(new Command(impl.getString(U.id.ok), CMDID_OK));
            addCommand(new Command(impl.getString(U.id.cancel), CMDID_CANCEL));
            addCommandListener(this);
        }
        
        
    }

    public void selectionChanged(int oldSelected, int newSelected) {
        basePointView.getBasePointController().handleFeedPresetSelected(newSelected);
    }

    public void actionPerformed(ActionEvent ae) {
        basePointView.dismissFeedDialog(ae.getCommand().getId());
    }
    
    
}
