/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.view;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import com.ustadmobile.port.j2me.util.J2MEIOUtils;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;
/**
 *
 * @author varuna
 */
public class LoginViewJ2ME extends Form implements LoginView, ActionListener {

    private int CMD_LOGIN = 0;
    
    final private TextField usernameField; 
    
    final private TextField passwordField;
    
    final private Button loginButton;
    
    private LoginController controller;
    
    private String loginHint;
    
    private String passwordHint;
    
    private String buttonText;
    
    public LoginViewJ2ME() {
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        
        usernameField = new TextField();
        addComponent(usernameField);
        
        passwordField = new TextField();
        addComponent(passwordField);
        
        Label spaceLabel = new Label(" ");
        addComponent(spaceLabel);
        
        Command loginCmd = new Command("", CMD_LOGIN);
        loginButton = new Button(loginCmd);
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

    public void setUsernameHint(String loginHint) {
        
    }

    public void setPasswordHint(String passwordHint) {
    }

    public void setButtonText(String buttonText) {
        loginButton.setText(buttonText);
    }
    
    
    
}
