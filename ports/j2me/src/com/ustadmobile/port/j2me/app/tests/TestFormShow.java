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
package com.ustadmobile.port.j2me.app.tests;

import com.sun.lwuit.Form;
import j2meunit.framework.TestCase;
import com.ustadmobile.port.j2me.app.forms.TestForm;
import com.ustadmobile.port.j2me.app.forms.TestForm2;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import javax.microedition.lcdui.Display;
import java.lang.Thread;

/**
 *
 * @author varuna
 */
public class TestFormShow extends TestCase{
    
    public TestFormShow(){
        setName("Testing LWUIT Form Show");
    }
    public class ItsThreadTime implements Runnable {
        public void run(){
            //DO IT
            Form f = new Form();
            f = TestForm.loadTestForm();
            f.show();
            f.setTitle("in the tread..");
        }
    }
    
    public class ItsThreadTime2 implements Runnable {
        public void run(){
            LoginController loginController = new LoginController();
            loginController.show();
        }
    }
    
    public void runTest() throws Throwable{
        com.sun.lwuit.Display.getInstance().callSeriallyAndWait(new Runnable() {
            public void run() {
                Form f = new Form();
                f = TestForm.loadTestForm();
                f.show();
                f.setTitle("in the thread..");
            }
        });
        assertTrue("Completed first callSerially and wait", true);
        try { Thread.sleep(2000); }
        catch(InterruptedException e) {}
        
        //final LoginController loginController = new LoginController();
        com.sun.lwuit.Display.getInstance().callSeriallyAndWait(new Runnable() {
            public void run() {               
                final LoginController loginController = new LoginController();
                loginController.show();
            }
        });
        try { Thread.sleep(2000); }
        catch(InterruptedException e) {}
        
        assertEquals("Login Form Test Show OK", "OK",
                "OK");
        
    }
    
}
