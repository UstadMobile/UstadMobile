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
package com.ustadmobile.core.view;

import com.ustadmobile.core.controller.LoginController;

/**
 *
 * @author varuna
 */
public interface LoginView extends UstadView {
    
    public static final int SECTION_LOGIN = 0;
    
    public static final int SECTION_REGISTER = 1;
    
    public static final int SECTION_JOINCLASS = 2;
    
    public void setController(LoginController controller);
    
    /**
     * Set the title of the view
     * 
     * @param title title to be set
     */
    public void setTitle(String title);
  
    
    /**
     * Set the hint text (e.g. text which is in background before text is
     * actually entered for the username text field
     * 
     * @param loginHint 
     */
    public void setUsernameHint(String loginHint);
    
    /**
     * Set the password hint text (e.g. text in background before any text is
     * actually entered)
     * 
     * @param passwordHint 
     */
    public void setPasswordHint(String passwordHint);
    
    /**
     * Set the text to appear on the login button
     * 
     * @param buttonText 
     */
    public void setButtonText(String buttonText);
    
    /**
     * Set the text to appear as the hint for the phone number
     * 
     * @param phoneNumberHint
     */
    public void setRegisterPhoneNumberHint(String phoneNumberHint);
    
    /**
     * The hint for the registration name field
     * 
     * @param nameHint 
     */
    public void setRegisterNameHint(String nameHint);
    
    /**
     * The hint for the request username field
     * 
     * @param usernameHint 
     */
    public void setRegisterUsernameHint(String usernameHint);
    
    /**
     * The hint for the user to request a password
     * 
     * @param passwordHint
     */
    public void setRegisterPasswordHint(String passwordHint);
    
    /**
     * The hint for the user to include their email with registration
     * 
     * @param registerEmailHint 
     */
    public void setRegisterEmailHint(String registerEmailHint);
    
    /**
     * The hint for the user to enter their registration code
     * 
     * @param registerRegcodHint 
     */
    public void setRegisterRegcodeHint(String registerRegcodHint);
    
    /**
     * The label for the registration form - male gender label
     * @param maleLabel 
     */
    public void setRegisterGenderMaleLabel(String maleLabel);
    
    /**
     * The label for the registration form - female gender label
     * @param femaleLabel 
     */
    public void setRegisterGenderFemaleLabel(String femaleLabel);
    
    /**
     * The text to appear on the registration button
     * 
     * @param registerButtonText 
     */
    public void setRegisterButtonText(String registerButtonText);
    
    /**
     * The text that will appear for users to select the server.
     */
    public void setServerLabel(String serverLabel);
    
    /**
     * The actual xAPI server to use
     */
    public void setXAPIServerURL(String xAPIServerURL);
    
    /**
     * The text that will appear for users to see advanced settings
     * @param advancedLabel 
     */
    public void setAdvancedLabel(String advancedLabel);
    
    public void setAdvancedSettingsVisible(boolean visible);
    
    /**
     * Footer label that appears at the bottom underneath login to show the
     * version
     * 
     * @param versionLabel Version information to show in label
     */
    public void setVersionLabel(String versionLabel);
    
    
}
