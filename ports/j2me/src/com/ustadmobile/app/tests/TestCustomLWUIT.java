/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import com.sun.lwuit.Form;
import com.ustadmobile.app.forms.TestForm3;
import com.ustadmobile.app.forms.TestForm4;
import j2meunit.framework.TestCase;


/**
 *
 * @author varuna
 */
public class TestCustomLWUIT extends TestCase {
    public TestCustomLWUIT(){
        setName("Test Custom LWUIT Test");
    }
    
    public void runTest() throws Throwable{
        assertEquals("Simple Test OK", 2, 1+1);
        /*
        com.sun.lwuit.Display.getInstance().callSeriallyAndWait(new Runnable() {
            public void run() {
                Form f = new Form();
                f = TestForm3.loadTestForm();
                f.show();
                f.setTitle("Custom LWUIT Test..");
            }
        });
        assertTrue("Completed first callSerially and wait", true);
        try { Thread.sleep(2000); }
        catch(InterruptedException e) {}
       */
        
        /*
        Form f = new Form();
        f = TestForm3.loadTestForm();
        f.show();
        f.setTitle("Custom LWUIT Test..");
        int count = 0;
        while(count < 101){
            //update progress
            f.setTitle("Custom LWUIT Test ("+count+"%)..");
            count = count + 10;
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        Thread.sleep(1000);
        
        assertTrue("Completed first callSerially and wait", true);
        */
        
        TestForm4 form = new TestForm4();
        form.show();
        form.setTitle("Custom LWUIT Test..");
        int count = 0;
        while(count < 101){
            //update progress
            form.setTitle("Custom LWUIT Test ("+count+"%)..");
            count = count + 10;
            try {
                form.updateProgress(count);
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        Thread.sleep(1000);
        assertTrue("Completed first callSerially and wait", true);
    }
}
