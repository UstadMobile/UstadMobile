/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.forms;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextField;
import com.sun.lwuit.layouts.BoxLayout;
import com.ustadmobile.view.OPDSItemButton;
/**
 *
 * @author varuna
 */
public class TestForm3 {
    
    private static Form f;
    private static int CMD_RANDOM=1;
    
    public TestForm3(Form d){
        f = d;
    }
    public TestForm3(){
        
    }
    public static Form loadTestForm(){
        
        f = new Form("Hello, LWUIT!");
        f.setTitle("Test Fom");
        
        /** Initialising the form as a box layout**/
        BoxLayout bLayout = new BoxLayout(BoxLayout.Y_AXIS);
        f.setLayout(bLayout);
        
        /*
         * Command loginCmd = new Command("Login", CMD_LOGIN);
        Button loginButton = new Button(loginCmd);
        loginButton.addActionListener(this);
        this.addComponent(loginButton);
         */
        
        OPDSItemButton opdsButton1 = new OPDSItemButton("Button 1");
        //opdsButton1.updateProgress(25);
        f.addComponent(opdsButton1);
        
        OPDSItemButton opdsButton2 = new OPDSItemButton("Button 2");
        //opdsButton2.updateProgress(75);
        f.addComponent(opdsButton2);
        
        //opdsButton.updateProgress(75, null);
        
        
        return f;
    }
    
    public void updateProress(int percentage){
        
    }
    
}
