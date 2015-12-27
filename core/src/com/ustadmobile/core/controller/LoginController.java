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

import com.ustadmobile.core.U;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.Base64Coder;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.core.view.UstadView;
import java.io.IOException;
import java.util.Hashtable;


/* $if umplatform == 2  $
    import org.json.me.JSONObject;
 $else$ */
    import org.json.JSONObject;
/* $endif$ */


/**
 * 
 * @author varuna
 */
public class LoginController extends UstadBaseController{
    
    //private LoginView view;
    public LoginView view;
    
    public static final String REGISTER_COUNTRY = "country";
    
    public static final String REGISTER_PHONENUM = "phonenumber";
    
    public static final String REGISTER_NAME = "name";
    
    public static final String REGISTER_GENDER = "gender";
    
    public static final String REGISTER_USERNAME = "username";
    
    public static final String REGISTER_PASSWORD = "password";
    
    public static final String REGISTER_EMAIL = "email";
    
    public static final String REGISTER_REGCODE = "regcode";
    
    public LoginController(Object context) {
        super(context);
    }
    
    
    public static LoginController makeControllerForView(LoginView view) {
        LoginController ctrl = new LoginController(view.getContext());
        ctrl.setView(view);
        return ctrl;
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
        String encodedUserAndPass = "Basic " + Base64Coder.encodeString(
            username + ':' + password);
        headers.put("Authorization", encodedUserAndPass);
        
        HTTPResult authResult = UstadMobileSystemImpl.getInstance().makeRequest(
                url, headers, null);
        return authResult.getStatus();

    }
    
    /**
     * Removes the credentials of the current user from the system
     */
    public static void handleLogout(Object context) {
        //delete the active user
        UstadMobileSystemImpl.getInstance().go(LoginView.class, new Hashtable(), 
            context);
    }
    
    
    /**
     * Register a new user
     * @param userInfoParams Hashtable with 
     *  phonenumber Mandatory: must start with +countrycode
     *  name  optional - simple string of username
     *  gender "m" or "f"  - optional - can be blank
     * 
     * @param url HTTP endpoint from which to register the user
     * @return Hashtable with 
     */
    public static String registerNewUser(Hashtable userInfoParams, String url) throws IOException{
        Hashtable headers = new Hashtable();
        headers.put("UM-In-App-Registration-Version", "1.0.1");
        HTTPResult registrationResult = UstadMobileSystemImpl.getInstance().makeRequest(url, 
            headers, userInfoParams, "POST");
        if(registrationResult.getStatus() != 200) {
            throw new IOException("Registration error: code " 
                    + registrationResult.getStatus());
        }
        
        String serverSays = new String(registrationResult.getResponse(), "UTF-8");
        return serverSays;
    }
    
    /**
     * Attempt to get the country of the user by looking up their IP address.
     * This uses the api of https://freegeoip.net/ and sends a JSON request
     * 
     * @param serverURL e.g. http://freegeoip.net/json/
     * @throws IOException if something goes wrong with the lookup
     * @return two letter country code of the country based on user's IP address.
     */
    public static String getCountryCode(String serverURL) throws IOException{
        String retVal = null;
        try {
            HTTPResult httpResponse = UstadMobileSystemImpl.getInstance().makeRequest(
                serverURL, new Hashtable(), new Hashtable(), "GET");
            JSONObject jsonResp = new JSONObject(new String(httpResponse.getResponse(), 
                "UTF-8"));
            retVal = jsonResp.getString("country_code");
        }catch(Exception e) {
            throw new IOException(e.toString());
        }
        
        return retVal;
    }
    
    public static int getCountryIndexByCode(String countryCode) {
        for(int i = 0; i < UstadMobileConstants.COUNTRYCODES.length; i++) {
            if(countryCode.equals(UstadMobileConstants.COUNTRYCODES[i])) {
                return i;
            }
        }
        
        return -1;
    }
    
    
    public void handleAdvanceCheckboxToggled(boolean checked) {
        view.setAdvancedSettingsVisible(checked);
    }
    
    /**
     * Handle when the user clicks the registration button - register a new 
     * account, then bring them into the app
     * 
     * @param userInfoParams Hashtable with
     *  LoginController.REGISTER_COUNTRY - Integer object with country code
     *  LoginController.REGISTER_PHONENUM - String with phone number not including country code
     *  LoginController.REGISTER_NAME - String containing name
     *  LoginController.REGISTER_GENDER - String with 'm' or 'f'
     *  UstadMobileSystemImpl.PREFKEY_XAPISERVER - The URL of the xAPI server: 
     *  regserver can be relative to this.
     */
    public void handleClickRegister(final Hashtable userInfoParams) {
        final String xAPIServer = (String)userInfoParams.get(
            UstadMobileSystemImpl.PREFKEY_XAPISERVER);
        updateXAPIServer(xAPIServer);
        
        final LoginController thisCtrl = this;
        final Object ctx = context;
        Thread registerThread = new Thread() {
            public void run() {
                String serverURL = UstadMobileSystemImpl.getInstance().getAppPref("regserver",
                        UstadMobileDefaults.DEFAULT_REGISTER_SERVER, context);
                serverURL = UMFileUtil.resolveLink(xAPIServer, serverURL);
                
                StringBuffer phoneNumSB = new StringBuffer().append('+').append(
                    userInfoParams.get(LoginController.REGISTER_COUNTRY));
                
                String userPhoneNum = userInfoParams.get(
                        LoginController.REGISTER_PHONENUM).toString();
                
                //chop off leading zeros from the supplied phone number
                int phoneNumStart = 0;
                char currentChar;
                for(; phoneNumStart < userPhoneNum.length(); phoneNumStart++) {
                    currentChar = userPhoneNum.charAt(phoneNumStart);
                    if(!(currentChar == '0' || currentChar == ' ')) {
                        break;
                    }
                }
                
                phoneNumStart = Math.min(phoneNumStart, userPhoneNum.length()-2);
                
                phoneNumSB.append(userPhoneNum.substring(phoneNumStart)+1);
                userInfoParams.put(LoginController.REGISTER_PHONENUM, 
                    phoneNumSB.toString());
                
                try {
                    String serverResponse = registerNewUser(userInfoParams, serverURL);
                    JSONObject obj = new JSONObject(serverResponse);
                    String newUsername = obj.getString("username");
                    String newPassword = obj.getString("password");
                    thisCtrl.handleUserLoginAuthComplete(newUsername, newPassword);
                }catch(Exception e) {
                    UstadMobileSystemImpl.getInstance().getAppView(ctx).dismissProgressDialog();
                    UstadMobileSystemImpl.getInstance().getAppView(ctx).showNotification(
                        UstadMobileSystemImpl.getInstance().getString(U.id.err_registering_new_user)
                        + e.toString(), AppView.LENGTH_LONG);
                    e.printStackTrace();
                }
                
            }
        };
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.getAppView(ctx).showProgressDialog(impl.getString(U.id.registering));
        registerThread.start();
    }
    
    protected void updateXAPIServer(String newServer) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String currentServer = impl.getAppPref(
                    UstadMobileSystemImpl.PREFKEY_XAPISERVER,
                    UstadMobileDefaults.DEFAULT_XAPI_SERVER, context);
        
        //See if the user has changed hte xAPI Server
        if(!currentServer.equals(newServer)) {
            impl.setAppPref(UstadMobileSystemImpl.PREFKEY_XAPISERVER, newServer, 
                context);
        }
    }
    
    
    public void handleClickLogin(final String username, final String password, final String xAPIServer) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        updateXAPIServer(xAPIServer);
        
        Thread loginThread = new Thread() {
            public void run() {
                impl.getLogger().l(UMLog.DEBUG, 303, null);
                String serverURL = UMFileUtil.joinPaths(new String[]{xAPIServer, 
                    "statements?limit=1"});

                int result = 0;
                IOException ioe = null;

                try {
                    result = LoginController.authenticate(username, password, 
                        serverURL);
                }catch(IOException e) {
                    ioe = e;
                }
                
                impl.getAppView(context).dismissProgressDialog();

                if(result == 401 | result == 403) {
                    impl.getAppView(context).showAlertDialog(impl.getString(U.id.error), 
                        impl.getString(U.id.wrong_user_pass_combo));
                }else if(result != 200) {
                    UstadMobileSystemImpl.getInstance().getAppView(context).showAlertDialog(
                        impl.getString(U.id.error), impl.getString(U.id.login_network_error));
                }else {
                    UstadMobileSystemImpl.getInstance().setActiveUser(username, context);
                    UstadMobileSystemImpl.getInstance().setActiveUserAuth(password, context);
                    UstadMobileSystemImpl.getInstance().go(BasePointView.class, 
                        BasePointController.makeDefaultBasePointArgs(context), context);
                }
            }
        };
        UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.DEBUG, 302, null);
        impl.getAppView(context).showProgressDialog(impl.getString(U.id.authenticating));
        loginThread.start();
    }
    
    /**
     * Utility merge of what happens after a user is logged in through username/password
     * and what happens after they are newly registered etc.
     */
    private void handleUserLoginAuthComplete(final String username, final String password) {
        UstadMobileSystemImpl.getInstance().setActiveUser(username, context);
        UstadMobileSystemImpl.getInstance().setActiveUserAuth(password, context);
        UstadMobileSystemImpl.getInstance().go(BasePointView.class, 
            BasePointController.makeDefaultBasePointArgs(context), context);
    }    
    
    /**
     * Used when a view is somehow otherwise created e.g. by a smartphone OS
     * and we're working the other way around
     * 
     * @param view 
     */
    public void setView(UstadView view) {
        this.view = (LoginView)view;
    }
    
    public UstadView getView() {
        return this.view;
    }
    
    public void hide() {
        
    }

    public void setUIStrings() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        view.setTitle(impl.getString(U.id.login));
        view.setButtonText(impl.getString(U.id.login));
        view.setUsernameHint(impl.getString(U.id.username));
        view.setPasswordHint(impl.getString(U.id.password));
        view.setRegisterButtonText(impl.getString(U.id.register));
        view.setRegisterNameHint(impl.getString(U.id.name));
        view.setRegisterPhoneNumberHint(impl.getString(U.id.phone_number));
        view.setRegisterGenderMaleLabel(impl.getString(U.id.male));
        view.setRegisterGenderFemaleLabel(impl.getString(U.id.female));
        view.setDirection(UstadMobileSystemImpl.getInstance().getDirection());
        view.setAdvancedLabel(impl.getString(U.id.advanced));
        view.setServerLabel(impl.getString(U.id.server));
        String xAPIURL = impl.getAppPref(
                    UstadMobileSystemImpl.PREFKEY_XAPISERVER,
                    UstadMobileDefaults.DEFAULT_XAPI_SERVER, context);
        view.setXAPIServerURL(xAPIURL);
    }
}
