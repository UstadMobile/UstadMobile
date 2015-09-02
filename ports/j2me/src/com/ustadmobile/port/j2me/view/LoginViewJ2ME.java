/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.view;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
/**
 *
 * @author varuna
 */
public class LoginViewJ2ME extends Form implements LoginView, ActionListener {

    private int CMD_LOGIN = 0;
    
    final private TextField usernameField; 
    
    final private TextField passwordField;
    
    private LoginController controller;
    
    public LoginViewJ2ME() {
        setTitle("Login");
        
        usernameField = new TextField();
        addComponent(usernameField);
        
        passwordField = new TextField();
        addComponent(passwordField);
        
        Label spaceLabel = new Label(" ");
        addComponent(spaceLabel);
        
        Command loginCmd = new Command("Login", CMD_LOGIN);
        Button loginButton = new Button(loginCmd);
        loginButton.addActionListener(this);
        this.addComponent(loginButton);
    }
    
    public void show() {
        UstadMobileSystemImpl.getInstance();
        UstadMobileSystemImplJ2ME.getInstanceJ2ME().handleFormShow(this);
        super.show();
    }
    
    public void setController(LoginController controller) {
        this.controller = controller;
    }
    
    public void actionPerformed(ActionEvent evt) {
        int id = evt.getCommand().getId();
        if(id == CMD_LOGIN) {
            this.controller.handleClickLogin(usernameField.getText(),
                passwordField.getText());
        }
    }

    public boolean isShowing() {
        return this.isVisible();
    }
    
}
