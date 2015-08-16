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
package com.ustadmobile.core.controller;

import com.ustadmobile.core.app.Base64;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.CatalogModel;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.core.view.ViewFactory;
import com.ustadmobile.core.util.UMFileUtil;
import java.io.IOException;
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
    
    /**
     * Try and login with the given username and password
     * 
     * @param username Username to authenticate as
     * @param password Password to authenticate with
     * @param url xAPI statements endpoint to authenticate against
     * @return HTTP OK 200 if OK, 403 for unauthorized
     * @throws IOException if something goes wrong talking to server
     */
    public static int authenticate(String username, String password, String url) throws IOException{
        Hashtable headers = new Hashtable();
        headers.put("X-Experience-API-Version", "1.0.1");
        String encodedUserAndPass="Basic "+ Base64.encode(username,
                    password);
        headers.put("Authorization", encodedUserAndPass);
        HTTPResult authResult = UstadMobileSystemImpl.getInstance().makeRequest(
                url, headers, null, "GET");
        return authResult.getStatus();

    }
    
    public void handleClickLogin(final String username, final String password) {
        final LoginView myView = view;
        Thread loginThread = new Thread() {
            public void run() {
                String serverBaseURL = 
                UstadMobileSystemImpl.getInstance().getAppPref("server",
                UstadMobileDefaults.DEFAULT_XAPI_SERVER);
                String serverURL = UMFileUtil.joinPaths(new String[]{serverBaseURL, 
                    "statements?limit=1"});

                int result = 0;
                IOException ioe = null;

                try {
                    result = LoginController.authenticate(username, password, 
                        serverURL);
                }catch(IOException e) {
                    ioe = e;
                }
                
                UstadMobileSystemImpl.getInstance().getAppView().dismissProgressDialog();

                if(result == 401 | result == 403) {
                    UstadMobileSystemImpl.getInstance().getAppView().showAlertDialog("Login Error", 
                        "Wrong username/password: please check");
                }else if(result != 200) {
                    UstadMobileSystemImpl.getInstance().getAppView().showAlertDialog(
                        "Login Error", "Network error: please check you are online");
                }else {
                    //make a new catalog controller and show it for the users base directory
                    //Add username to UserPreferences.
                    UstadMobileSystemImpl.getInstance().setActiveUser(username);
                    UstadMobileSystemImpl.getInstance().setActiveUserAuth(password);
                    
                    try {
                        CatalogController userCatalog = CatalogController.makeUserCatalog(
                            UstadMobileSystemImpl.getInstance());
                        userCatalog.show();
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        UstadMobileSystemImpl.getInstance().getAppView().showProgressDialog("Authenticating");
        loginThread.start();
    }
    
    public void show() {
        this.view = ViewFactory.makeLoginView();
        this.view.setController(this);
        this.view.show();
    }
    
    public void hide() {
        
    }
}
