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

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.Base64Coder;
import com.ustadmobile.core.util.HTTPCacheDir;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.DialogResultListener;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.core.view.RegistrationView;
import com.ustadmobile.core.view.UstadView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Hashtable;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
/* $endif$ */


/**
 * The Login Controller: manages Login View and has static methods to handle logging a user out
 *
 * This can be used as an activity of it's own or as a dialog. In case of a dialog the hosting
 * activity will want to know when the login process has been completed. For this to work the
 * hosting view (e.g. BasePointView, CatalogEntryView, etc) itself *MUST* implement DialogResultListener.
 *
 * @author varuna
 */
public class LoginController extends UstadBaseController{
    
    public LoginView view;
    
    public static final String REGISTER_COUNTRY = "country";
    
    public static final String REGISTER_PHONENUM = "phonenumber";
    
    public static final String REGISTER_NAME = "name";
    
    public static final String REGISTER_GENDER = "gender";
    
    public static final String REGISTER_USERNAME = "username";
    
    public static final String REGISTER_PASSWORD = "password";
    
    public static final String REGISTER_EMAIL = "email";
    
    public static final String REGISTER_REGCODE = "regcode";

    //Hashed user authentication to cache in case they login next time when offline
    public static final String PREFKEY_AUTHCACHE_PREFIX = "um-authcache-";

    private DialogResultListener resultListener;

    public static final int RESULT_LOGIN_SUCCESSFUL = 1;

    public static final int RESULT_CLICK_REGISTER = 2;

    public LoginController(Object context) {
        super(context);
    }

    /**
     * Make a controller for the given view.
     *
     * @param view
     * @return
     */
    public static LoginController makeControllerForView(LoginView view) {
        LoginController ctrl = new LoginController(view.getContext());
        ctrl.setView(view);
        return ctrl;
    }

    public static String encodeBasicAuth(String username, String password) {
        return "Basic " + Base64Coder.encodeString(username +
                ':' + password);
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
    public static int authenticate(String username, String password,
                                   String url) throws IOException{
        Hashtable headers = new Hashtable();
        headers.put("X-Experience-API-Version", "1.0.1");
        headers.put("Authorization", LoginController.encodeBasicAuth(username, password));
        
        HTTPResult authResult = UstadMobileSystemImpl.getInstance().makeRequest(
                url, headers, null);
        return authResult.getStatus();

    }
    
    /**
     * Makes a hashtable with the HTTP auth username and password set
     * 
     * @param username
     * @param password
     * @return 
     */
    public static Hashtable makeAuthHeaders(String username, String password) { 
        Hashtable ht = new Hashtable();
        String encodedUserAndPass="Basic "+ Base64Coder.encodeString(
            username + ':' + password);
        ht.put("Authorization", encodedUserAndPass);
        return ht;
    }
    
    /**
     * Get the role from the UMCloud server
     * 
     * @param username auth username to use with request
     * @param password auth password to use with request
     * @param url the API endpoint url
     * @return
     * @throws IOException 
     */
    public static final String getRole(String username, String password, String url) throws IOException {
        String role = null;
        
        Hashtable headers = LoginController.makeAuthHeaders(username, password);
        
        HTTPResult roleResult = UstadMobileSystemImpl.getInstance().makeRequest(
            url, headers, null);
        if(roleResult.getStatus() == 200) {
            try {
                JSONObject obj = new JSONObject(new String(roleResult.getResponse(), 
                    "UTF-8"));
                role = obj.optString("role");
            }catch(JSONException j) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 185, url, j);
                throw new IOException(j.toString());
            }
        }
        
        return role;
    }
    
    /**
     * 
     * @param username
     * @param password
     * @param url
     * @return
     * @throws IOException 
     */
    public static String getTeacherClassList(String username, String password, String url) throws IOException {
        JSONObject result = null;
        String classListStr = null;
        
        HTTPResult classListResult = UstadMobileSystemImpl.getInstance().makeRequest(url, 
            makeAuthHeaders(username, password), null);
        if(classListResult.getStatus() == 200) {
            classListStr = new String(classListResult.getResponse(), "UTF-8");
            try {
                JSONArray classArray = new JSONArray(classListStr);
            }catch(JSONException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 187, 
                    url+ '/' +  classListStr, e);
                classListStr = null;
            }
            
        }
        
        return classListStr;
    }
    
    public static String getJSONArrayResult(String username, String password, String url) throws IOException {
        JSONArray arr;
        String classListStr = null;
        
        HTTPResult classListResult  = UstadMobileSystemImpl.getInstance().makeRequest(url, 
            makeAuthHeaders(username, password), null);
        if(classListResult.getStatus() == 200) {
            classListStr = new String(classListResult.getResponse(), "UTF-8");
            try {
                arr = new JSONArray(classListStr);
            }catch(JSONException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 189, 
                    url+ '/' +  classListStr, e);
                classListStr = null;
            }
        }
        
        return classListStr;
    }
    
    /**
     * Removes the credentials of the current user from the system and goes to the next view
     * specified
     *
     * @param context system context object
     * @param destAfterLogout (Optional) destination view to go to after login
     */
    public static void handleLogout(Object context, String destAfterLogout) {
        //delete the active user
        UstadMobileSystemImpl.getInstance().setActiveUser(null, context);
        UstadMobileSystemImpl.getInstance().setActiveUserAuth(null, context);

        UstadMobileSystemImpl.getInstance().go(destAfterLogout, context);
    }

    /**
     * Removes the credentials of the current user from the system and goes to the LoginView
     *
     * @param context
     */
    public static void handleLogout(Object context) {
        handleLogout(context, LoginView.VIEW_NAME);
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
            String serverResponse = new String(registrationResult.getResponse());
            UstadMobileSystemImpl.l(UMLog.ERROR, 83, registrationResult.getStatus() + ';' +
                    serverResponse);
            String errorMessage = "General error: try again later";
            if(registrationResult.getStatus() >= 400 && registrationResult.getStatus() < 500) {
                //there may be useful info for the user - e.g. username taken etc
                
            }


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

    public void handleClickRegister() {
        UstadMobileSystemImpl.getInstance().go(RegistrationView.VIEW_NAME, getContext());
        if(view != null && view instanceof DismissableDialog){
            ((DismissableDialog)view).dismiss();
        }
    }
    
    protected void updateXAPIServer(String newServer) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String currentServer = impl.getAppPref(
                UstadMobileSystemImpl.PREFKEY_XAPISERVER,
                CoreBuildConfig.DEFAULT_XAPI_SERVER, context);
        
        //See if the user has changed hte xAPI Server
        if(!currentServer.equals(newServer)) {
            impl.setAppPref(UstadMobileSystemImpl.PREFKEY_XAPISERVER, newServer, 
                context);
        }
    }

    /**
     * Authenticates login locally and proceeds with the next Activity
     * @param username
     * @param password
     * @param dbContext
     * @return
     */
    public boolean handleLoginLocally(String username, String password, Object dbContext){
        boolean result = UstadMobileSystemImpl.getInstance().handleLoginLocally(username, password, dbContext);
        if(result) {
            if (resultListener != null) {
                resultListener.onDialogResult(RESULT_LOGIN_SUCCESSFUL, (DismissableDialog) view, null);
            } else {
                UstadMobileSystemImpl.getInstance().go(CoreBuildConfig.FIRST_DESTINATION, context);
            }
        }
        return result;
    }
    /**
     * Handles what happens when in the app the login button is clicked.
     * @param username
     * @param password
     * @param xAPIServer
     */
    public void handleClickLogin(final String username, final String password,
                                 final String xAPIServer) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        updateXAPIServer(xAPIServer);
        
        Thread loginThread = new Thread() {
            public void run() {
                impl.getLogger().l(UMLog.DEBUG, 303, null);
                String serverURL = UMFileUtil.joinPaths(new String[]{xAPIServer, 
                    "statements?limit=1"});

                int result = 0;
                String role = null;
                String teacherClassList = null;
                IOException ioe = null;
                boolean authPassed = false;

                try {
                    result = LoginController.authenticate(username, password,
                            serverURL);
                }catch(IOException e) {
                    ioe = e;
                }

                if(result == 200) {
                    authPassed = true;
                    //encrypt and cache the authentication result
                    String authHashed = impl.hashAuth(getContext(), password);
                    impl.setAppPref(PREFKEY_AUTHCACHE_PREFIX + username, authHashed, getContext());

                    impl.setActiveUser(username, getContext());
                    impl.setActiveUserAuth(password, getContext());
                }

                if(result == 0 || result >= 500) {
                    //check the cache
                    String storedAuth = impl.getAppPref(PREFKEY_AUTHCACHE_PREFIX + username, getContext());
                    String authHashed = impl.hashAuth(getContext(), password);
                    if(storedAuth != null && authHashed != null && storedAuth.equals(authHashed)) {
                        //authentication was stored and this matches what we know from before
                        authPassed = true;
                    }
                }


                if(result == 401 | result == 403) {
                    impl.getAppView(context).dismissProgressDialog();
                    impl.getAppView(context).showAlertDialog(
                            impl.getString(MessageID.error, getContext()),
                            impl.getString(MessageID.wrong_user_pass_combo, getContext()));
                }else if(!authPassed) {
                    impl.getAppView(context).dismissProgressDialog();
                    UstadMobileSystemImpl.getInstance().getAppView(context).showAlertDialog(
                            impl.getString(MessageID.error, getContext()),
                            impl.getString(MessageID.login_network_error, getContext()));
                }else {
                    impl.setActiveUser(username, context);
                    impl.setActiveUserAuth(password, context);

                    //Added by Varuna:
                    //create a user locally:
                    impl.createUserLocally(username, password, null, getContext());

                    impl.getAppView(context).setProgressDialogTitle("Checking user role");
                    // try and find the role
                    //TODO
                    try {
                        role = LoginController.getRole(username, password,
                            UMFileUtil.resolveLink(
                                xAPIServer, UstadMobileDefaults.DEFAULT_ROLE_ENDPOINT));

                        if(role != null && role.equals(UstadMobileConstants.ROLE_TEACHER)) {
                            impl.getAppView(context).setProgressDialogTitle("Loading teacher classes");
                            teacherClassList = LoginController.getJSONArrayResult(
                                    username, password,
                                    UMFileUtil.resolveLink(xAPIServer,
                                        UstadMobileDefaults.DEFAULT_CLASSLIST_ENDPOINT));
                            if(teacherClassList != null) {
                                impl.setUserPref("teacherclasslist",
                                        teacherClassList, context);
                            }
                        }

                        if(teacherClassList != null) {
                            try {
                                JSONArray classArr = new JSONArray(teacherClassList);
                                for(int i = 0; i < classArr.length(); i++) {
                                    JSONObject classObj = classArr.getJSONObject(i);
                                    String classID = classObj.getString("id");
                                    loadClassListToPrefs(classID, xAPIServer, context);
                                }
                            }catch(JSONException e) {
                                //this should never happen - if it did... getTeacherClassList would have return null
                            }
                        }

                    }catch(IOException e) {
                        ioe = e;
                    }


                    if(role != null) {
                        UstadMobileSystemImpl.getInstance().setUserPref("role", 
                            role, context);
                    }

                    impl.getAppView(context).dismissProgressDialog();

                    if(resultListener != null) {
                        resultListener.onDialogResult(RESULT_LOGIN_SUCCESSFUL, (DismissableDialog)view, null);
                    }else {
                        UstadMobileSystemImpl.getInstance().go(CoreBuildConfig.FIRST_DESTINATION,
                                context);
                    }
                }
            }
        };
        UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.DEBUG, 302, null);
        impl.getAppView(context).showProgressDialog(
                impl.getString(MessageID.authenticating, getContext()));
        loginThread.start();
    }

    public static void loadClassListToPrefs(String classId, String xapiServer, Object context) throws IOException{
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String classURL = UMFileUtil.resolveLink(
                xapiServer,
                UstadMobileDefaults.DEFAULT_STUDENTLIST_ENDPOINT)
                + classId;
        String studentListJSON = LoginController.getJSONArrayResult(impl.getActiveUser(context),
                impl.getActiveUserAuth(context), classURL);
        if(studentListJSON != null) {
            impl.setUserPref("studentlist."+classId, studentListJSON, context);
        }
    }
    
    /**
     * Utility merge of what happens after a user is logged in through username/password
     * and what happens after they are newly registered etc.
     */
    private void handleUserLoginAuthComplete(final String username, final String password) {
        UstadMobileSystemImpl.getInstance().setActiveUser(username, context);
        UstadMobileSystemImpl.getInstance().setActiveUserAuth(password, context);
        UstadMobileSystemImpl.getInstance().go(CoreBuildConfig.FIRST_DESTINATION, context);
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
        view.setVersionLabel(impl.getVersion(context) + " - " +
                HTTPCacheDir.makeHTTPDate(CoreBuildConfig.BUILD_TIME_MILLIS));
        String xAPIURL = impl.getAppPref(
                    UstadMobileSystemImpl.PREFKEY_XAPISERVER,
                    impl.isHttpsSupported() ? CoreBuildConfig.DEFAULT_XAPI_SERVER : CoreBuildConfig.DEFAULT_XAPI_SERVER_NOSSL,
                    context);
        if(xAPIURL.equals(CoreBuildConfig.DEFAULT_XAPI_SERVER_NOSSL) && impl.isHttpsSupported())
            xAPIURL = CoreBuildConfig.DEFAULT_XAPI_SERVER;
        view.setXAPIServerURL(xAPIURL);
    }

    public DialogResultListener getResultListener() {
        return resultListener;
    }

    public void setResultListener(DialogResultListener resultListener) {
        this.resultListener = resultListener;
    }
}

