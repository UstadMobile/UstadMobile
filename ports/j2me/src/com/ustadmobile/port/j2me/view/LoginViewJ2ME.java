/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

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
import java.util.Hashtable;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;

/**
 *
 * @author varuna
 */
public class LoginViewJ2ME extends UstadViewFormJ2ME implements LoginView, ActionListener {

    private int CMD_LOGIN = 0;
    
    final private TextField usernameField; 
    
    final private TextField passwordField;
    
    final private Button loginButton;
    
    private LoginController controller;
    
    private String loginHint;
    
    private String passwordHint;
    
    private String buttonText;
    
    public LoginViewJ2ME(Hashtable args, Object context) {
        super(args, context);
        
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
        
        controller = new LoginController(getContext());
        controller.setViewStrings(this);
    }
    
    public void show() {
        UstadMobileSystemImpl.getInstance();
        UstadMobileSystemImplJ2ME.getInstanceJ2ME().handleFormShow(this);
        super.show();
    }
    
    public void setController(LoginController controller) {
        this.controller = controller;
        controller.setViewStrings(this);
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

    public void setRegisterPhoneNumberHint(String phoneNumberHint) {
    }

    public void setRegisterNameHint(String nameHint) {
    }

    public void setRegisterGenderMaleLabel(String maleLabel) {
    }

    public void setRegisterGenderFemaleLabel(String femaleLabel) {
    }

    public void setRegisterButtonText(String registerButtonText) {
    }

   
    
    
}
