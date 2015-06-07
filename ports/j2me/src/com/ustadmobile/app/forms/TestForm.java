/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.forms;

import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextField;
import com.sun.lwuit.layouts.BoxLayout;

/**
 *
 * @author varuna
 */
public class TestForm {
    
    private static Form f;
    
    public TestForm(Form d){
        f = d;
    }
    public TestForm(){
        
    }
    public static Form loadTestForm(){
        
        f = new Form("Hello, LWUIT!");
        f.setTitle("Test Fom");
        
        /** Initialising the form as a box layout**/
        BoxLayout bLayout = new BoxLayout(BoxLayout.Y_AXIS);
        f.setLayout(bLayout);
        
        /** Display and render the form elements.**/
        Label courseIDLabel = new Label("Label");
        f.addComponent(courseIDLabel);
        
        TextField idTextField = new TextField();
        f.addComponent(idTextField);
        return f;
    }
    
}
