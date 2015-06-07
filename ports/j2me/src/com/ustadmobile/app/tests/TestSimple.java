/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import j2meunit.framework.TestCase;

/**
 *
 * @author varuna
 */
public class TestSimple extends TestCase {
    public TestSimple(){
        setName("TestSimple Test");
    }
    
    public void runTest() throws Throwable{
        assertEquals("Simple Test OK", 2, 1+1);
        
    }
}
