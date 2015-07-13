/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.controller;

import com.ustadmobile.app.Base64;
import com.ustadmobile.impl.HTTPResult;
import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.model.CatalogModel;
import com.ustadmobile.opds.UstadJSOPDSFeed;
import com.ustadmobile.view.LoginView;
import com.ustadmobile.view.ViewFactory;
import java.util.Hashtable;

/**
 * 
 * @author varuna
 */
public class LoginController implements UstadController{
    
    //private LoginView view;
    public LoginView view;
    
    public LoginController() {
        
    }
    
    public static int authenticate(String username, String password, String url) {
        Hashtable headers = new Hashtable();
        headers.put("X-Experience-API-Version", "1.0.1");
        String encodedUserAndPass="Basic "+ Base64.encode(username,
                    password);
        headers.put("Authorization", encodedUserAndPass);
        HTTPResult authResult = UstadMobileSystemImpl.getInstance().makeRequest(
                url, headers, null, "GET");
        return authResult.getStatus();

    }
    
    public void handleClickLogin(String username, String password) {
        String serverURL = 
                UstadMobileSystemImpl.getInstance().getAppPref("server");
        int result = LoginController.authenticate(username, password, serverURL);
        if(result != 200) {
            this.view.showDialog("Error", "Login failed: please try again");
        }else {
            //make a new catalog controller and show it for the users base directory
            //Add username to UserPreferences.
            UstadMobileSystemImpl.getInstance().setUserPref("username", username);
            UstadMobileSystemImpl.getInstance().setUserPref("password", password);
            //UstadMobileSystemImpl.getInstance().setActiveUser(username);
            
            //get the feed.
            UstadJSOPDSFeed userFeed = null;
            
            CatalogModel catalogModel = new CatalogModel(userFeed);
            CatalogController catalogController = new CatalogController(catalogModel);
            catalogController.show();
            
        }
    }
    
    public void show() {
        this.view = ViewFactory.makeLoginView();
        this.view.setController(this);
        this.view.show();
    }
    
    public void hide() {
        
    }
}
