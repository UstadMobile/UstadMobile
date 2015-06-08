/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.view;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.ustadmobile.controller.LoginController;
/**
 *
 * @author varuna
 */
public class LoginViewJ2ME extends Form implements LoginView, ActionListener {

    private int CMD_LOGIN = 0;
    
    private TextField usernameField; 
    
    private TextField passwordField;
    
    public LoginViewJ2ME() {
        usernameField = new TextField();
        addComponent(usernameField);
        
        passwordField = new TextField();
        addComponent(passwordField);
        
        Command loginCmd = new Command("Login", CMD_LOGIN);
        Button loginButton = new Button(loginCmd);
        loginButton.addActionListener(this);
        this.addComponent(loginButton);
        
    }
    
    private LoginController controller;
    
    public void setController(LoginController controller) {
        this.controller = controller;
    }

    public void showDialog(String title, String text) {
        
    }

    public void actionPerformed(ActionEvent evt) {
        if(evt.getCommand().getId() == CMD_LOGIN) {
            this.controller.handleClickLogin(usernameField.getText(),
                passwordField.getText());
        }
    }
    
}
