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
