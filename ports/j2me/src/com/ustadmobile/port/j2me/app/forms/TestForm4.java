/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.app.forms;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.ustadmobile.port.j2me.view.OPDSItemButton;
import java.util.Hashtable;


/**
 *
 * @author varuna
 */
public class TestForm4 extends Form implements ActionListener{
    
    private int CMD_RANDOM1 = 1;
    private int CMD_RANDOM2 = 2;
   
    private Hashtable entryIdToButtons;
    
    public TestForm4() {
        
        //Set Layout of the form.
        BoxLayout boxLayout = new BoxLayout(BoxLayout.Y_AXIS);
        setLayout(boxLayout);
        
        setTitle("Test Form 4");

        Command button1Cmd = new Command("Button1", CMD_RANDOM1);
        OPDSItemButton opdsButton1 = new OPDSItemButton(button1Cmd);
        opdsButton1.addActionListener(this);
        this.addComponent(opdsButton1);
        
        Command button2Cmd = new Command("Button2", CMD_RANDOM1);
        OPDSItemButton opdsButton2 = new OPDSItemButton(button2Cmd);
        opdsButton2.addActionListener(this);
        this.addComponent(opdsButton2);
        
        entryIdToButtons = new Hashtable();
        entryIdToButtons.put("button1", opdsButton1);
        entryIdToButtons.put("button2", opdsButton2);
        
    }
    
    public void updateProgress(int percentage){
        OPDSItemButton button1 = (OPDSItemButton) 
                entryIdToButtons.get("button1");
        button1.updateProgress(percentage);
    }
    
   
    public void actionPerformed(ActionEvent evt) {
        int id = evt.getCommand().getId();
        if(evt.getCommand().getId() == CMD_RANDOM1) {
           System.out.println("Random button 1 clicked!");
        }
        if (evt.getCommand().getId() == CMD_RANDOM2) {
           System.out.println("Random button 2 clicked!");
        }
    }
    
}
