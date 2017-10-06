/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.sharedse.view;

import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.port.sharedse.controller.EnrollStudentController;

/**
 *
 * @author varuna
 */
public interface EnrollStudentView extends UstadView {

    public static final String VIEW_NAME = "EnrollStudent";

    public void setController(EnrollStudentController mController);
    
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
    
    
}
