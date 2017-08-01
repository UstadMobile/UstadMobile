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
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.impl.UstadMobileDefaults;
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
    
    private TextField usernameField; 
    
    private TextField passwordField;
    
    private Button loginButton;
    
    private Label versionLabel;
    
    private LoginController controller;
    
    private String loginHint;
    
    private String passwordHint;
    
    private String buttonText;
        
    private TextField xAPIServerField;
    
    public LoginViewJ2ME(Hashtable args, Object context) {
        super(args, context);
        xAPIServerField = new TextField();
    }
    
    public void initComponent() {
        if(usernameField == null) {
            setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        
            usernameField = new TextField();
            addComponent(usernameField);

            passwordField = new TextField();
            addComponent(passwordField);

            Label spaceLabel = new Label(" ");
            addComponent(spaceLabel);


            Command loginCmd = new Command(UstadMobileSystemImpl.getInstance().getString(
                MessageID.login, getContext()), CMD_LOGIN);
            loginButton = new Button(loginCmd);
            loginButton.addActionListener(this);
            this.addComponent(loginButton);

            versionLabel = new Label();
            this.addComponent(versionLabel);
            
            

            controller = LoginController.makeControllerForView(this);
            controller.setUIStrings();
            setUIStrings();
        }
    }
    
    
    
    public void show() {
        UstadMobileSystemImpl.getInstance();
        UstadMobileSystemImplJ2ME.getInstanceJ2ME().handleFormShow(this);
        super.show();
    }
    
    public void setUIStrings() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        loginButton.setText(impl.getString(MessageID.login, getContext()));
        setTitle(impl.getString(MessageID.login, getContext()));
    }
    
    public void setController(LoginController controller) {
        this.controller = controller;
        controller.setUIStrings();
    }
    
    public void actionPerformed(ActionEvent evt) {
        int id = evt.getCommand().getId();
        if(id == CMD_LOGIN) {
            this.controller.handleClickLogin(usernameField.getText(),
                passwordField.getText(), xAPIServerField.getText());
        }
    }

    public boolean isShowing() {
        return this.isVisible();
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel.setText(versionLabel);
    }
    
    public void setXAPIServerURL(String xAPIServerURL) {
        xAPIServerField.setText(xAPIServerURL);
    }

    public void setAdvancedSettingsVisible(boolean visible) {
        
    }

}
