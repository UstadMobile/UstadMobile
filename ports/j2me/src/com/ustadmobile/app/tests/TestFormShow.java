/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import com.sun.lwuit.Form;
import j2meunit.framework.TestCase;
import com.ustadmobile.app.forms.TestForm;

/**
 *
 * @author varuna
 */
public class TestFormShow extends TestCase{
    
    public TestFormShow(){
        setName("Testing LWUIT Form Show");
    }
    
    public void runTest() throws Throwable{
    
        Form f = new Form();
        f = TestForm.loadTestForm();
        f.show();
        assertEquals("Form Show OK", "OK",
                "OK");
        
        
        
    }
    
}
