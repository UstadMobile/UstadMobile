/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.view;

import com.ustadmobile.controller.LoginController;

/**
 *
 * @author varuna
 */
public interface LoginView extends UstadView {
    public void setController(LoginController controller);
    
    public void showDialog(String title, String text);
}
