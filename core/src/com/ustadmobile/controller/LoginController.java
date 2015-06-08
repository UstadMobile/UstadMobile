/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.controller;

import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.view.LoginView;

/**
 *
 * @author varuna
 */
public class LoginController {
    
    private LoginView view;
    
    public LoginController() {
        
    }
    
    public static int authenticate(String username, String password, String url) {
        
        return 400;
    }
    
    public void handleClickLogin(String username, String password) {
        String server = "blah";//is actually done by impl.getPreference
        int result = LoginController.authenticate(username, password, server);
        if(result != 200) {
            this.view.showDialog("Error", "Login failed: please try again");
        }else {
            //make a new catalog controller and show it for the users base directory
        }
    }
    
    public void show() {
        this.view = (LoginView)UstadMobileSystemImpl.getInstance().makeView("Login");
    }
    
    public void hide() {
        
    }
}
