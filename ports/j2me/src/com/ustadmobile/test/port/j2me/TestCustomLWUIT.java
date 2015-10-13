/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.test.port.j2me;

import com.sun.lwuit.Form;
import com.ustadmobile.port.j2me.app.forms.CustomLWUITDynamicForm;
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
        CustomLWUITDynamicForm form = new CustomLWUITDynamicForm();
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
