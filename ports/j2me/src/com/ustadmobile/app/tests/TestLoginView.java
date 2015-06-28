 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.events.ActionEvent;
import j2meunit.framework.TestCase;
import com.ustadmobile.controller.LoginController;
/**
 *
 * @author varuna
 */
public class TestLoginView extends TestCase {
    public TestLoginView(){
        setName("LoginView Test");
    }
    
    public void runTest() throws Throwable{
        assertEquals("Simple Test OK", 2, 1+1);
        LoginController loginController = new LoginController();
        loginController.show();
        Thread.sleep(10000);
        
        Button dummyButton = new Button();
        ActionEvent evt = new ActionEvent(dummyButton, 0);
        
        
    }
}
