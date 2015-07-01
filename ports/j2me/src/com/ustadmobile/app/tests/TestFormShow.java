/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import com.sun.lwuit.Form;
import j2meunit.framework.TestCase;
import com.ustadmobile.app.forms.TestForm;
import com.ustadmobile.app.forms.TestForm2;
import com.ustadmobile.controller.LoginController;
import com.ustadmobile.impl.UstadMobileSystemImpl;
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
                
                /*
                Form f = new Form();
                f = TestForm2.loadTestForm();
                f.show();
                f.setTitle("in the thread 2..");
                f.show();
                * */
            }
        });
        try { Thread.sleep(2000); }
        catch(InterruptedException e) {}
        
        assertEquals("Login Form Test Show OK", "OK",
                "OK");
        
    }
    
}
