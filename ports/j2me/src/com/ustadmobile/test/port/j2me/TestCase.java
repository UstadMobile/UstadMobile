/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.test.port.j2me;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

/**
 * This extension of TestCase is designed to make unit testing a little more 
 * vocal and make it more likely you can spot where the code mysteriously died
 * on the J2ME device
 * 
 * @author mike
 */
public class TestCase extends j2meunit.framework.TestCase {

    public static final int DEFAULT_STR_LENLIMIT = 72;
    
    public String trimStr(Object o, int lengthLimit) {
        if(o == null) {
            return "null";
        }
        
        String str = o.toString();
        if(str.length() > lengthLimit) {
            str = str.substring(0, lengthLimit) + "...";
        }
        
        return str;
    }
    
    public String trimStr(Object o) {
        return trimStr(o, DEFAULT_STR_LENLIMIT);
    }
    
    public void assertTrue(String string, boolean bln) {
        UstadMobileSystemImpl.l(UMLog.INFO, 355, string + ':' + bln);
        super.assertTrue(string, bln); //To change body of generated methods, choose Tools | Templates.
    }

    public void assertSame(String string, Object o, Object o1) {
        UstadMobileSystemImpl.l(UMLog.INFO, 357, string + ':' + trimStr(o) + '=' + trimStr(o1));
        super.assertSame(string, o, o1); //To change body of generated methods, choose Tools | Templates.
    }
 
    public void assertNull(String string, Object o) {
        UstadMobileSystemImpl.l(UMLog.INFO, 359, string + ':' + trimStr(o));
        super.assertNull(string, o); //To change body of generated methods, choose Tools | Templates.
    }

    public void assertNotNull(String string, Object o) {
        UstadMobileSystemImpl.l(UMLog.INFO, 361, string + ':' + trimStr(o));
        super.assertNotNull(string, o); //To change body of generated methods, choose Tools | Templates.
    }

    public void assertEquals(String string, Object o, Object o1) {
        UstadMobileSystemImpl.l(UMLog.INFO, 363, string + ':' + trimStr(o) + '=' + trimStr(o1));
        super.assertEquals(string, o, o1); //To change body of generated methods, choose Tools | Templates.
    }

    public void assertEquals(String string, long l, long l1) {
        UstadMobileSystemImpl.l(UMLog.INFO, 365, string + ':' +l + '=' + l1);
        super.assertEquals(string, l, l1); //To change body of generated methods, choose Tools | Templates.
    }

    
}
