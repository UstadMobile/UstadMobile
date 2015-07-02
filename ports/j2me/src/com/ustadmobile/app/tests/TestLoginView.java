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
import com.ustadmobile.impl.UstadMobileSystemImpl;
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
        
        com.sun.lwuit.Display.getInstance().callSeriallyAndWait(new Runnable() {
            public void run() {
                LoginController loginController = new LoginController();
                loginController.show();
                
                Command loginCmd = new Command("Login", 0);
                Button loginButton = new Button(loginCmd);
                loginButton.addActionListener(loginCmd);
                ActionEvent evt = new ActionEvent(loginCmd, 0, 0);

                //evt.getCommand();
                int id = evt.getCommand().getId();
                loginController.view.performAction(evt);
            }
        });
        assertTrue("Completed first callSerially and wait", true);
        try { Thread.sleep(2000); }
        catch(InterruptedException e) {}
        
        String server = 
                UstadMobileSystemImpl.getInstance().getAppPref("server");
        
        //Correct Login cred
        int loginReturn = loginController.authenticate(
                "karmakid02", "karmakid02",server );
        assertEquals("Authenticate logic", loginReturn, 200);

        //wrong Login cred
        int loginReturn2 = loginController.authenticate(
                "karmakid02", "karmakid022",server );
        assertEquals("Authenticate logic", loginReturn2, 401);
    }
}
