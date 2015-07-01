 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
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
        
        
    }
}
