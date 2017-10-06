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
package com.ustadmobile.test.core;


/* $if umplatform == 1 $
        import android.test.ActivityInstrumentationTestCase2;
        import com.toughra.ustadmobile.UstadMobileActivity;
   $endif$ */
/* $if umplatform == 2  $
    import j2meunit.framework.TestCase;
 $else$ */

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.TestUtils;
import com.ustadmobile.core.util.UMUtil;

import junit.framework.TestCase;

/* $endif$ */

/**
 *
 * @author mike
 */
/* $if umplatform == 1  $
public class TestPreferences extends ActivityInstrumentationTestCase2<UstadMobileActivity> {
 $else$ */
public abstract class TestPreferences extends TestCase {
/* $endif */
   
    
    public TestPreferences() {
        /* $if umplatform == 1 $ 
        super(UstadMobileActivity.class);
        $endif */
    }
    
    public void testPreferences() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Object context = UMContextGetter.getContext(this);
        TestUtils utils = new TestUtils();
        
        impl.setActiveUser(utils.getTestProperty(TestUtils.PROP_TESTUSER), context);
        
        String[] testKeys = new String[]{"TestKey1", "TestKey2", "TestKey3"};
        String[] testValues = new String[]{"answeris42", "questionis6x7", "beacat"};
        
        impl.setUserPref(testKeys[0], testValues[0], context);
        impl.setAppPref(testKeys[2], testValues[2], context);

        assertEquals("Can retrieve set user value", testValues[0],
                impl.getUserPref(testKeys[0], context));
        assertEquals("Can retrieve set app value", testValues[2],
                impl.getAppPref(testKeys[2], context));
        
        String[] prefKeys = impl.getUserPrefKeyList(context);
        int keyIndex = UMUtil.getIndexInArray(testKeys[0], prefKeys);
        assertTrue("User pref set key is in list of keys", keyIndex != -1);
        
        prefKeys = impl.getAppPrefKeyList(context);
        keyIndex = UMUtil.getIndexInArray(testKeys[2], prefKeys);
        assertTrue("App pref set key is in list of keys", keyIndex != -1);
        
        impl.setActiveUser(utils.getTestProperty(TestUtils.PROP_TESTUSER) + "other",context);
        assertEquals("After changing user preference no longer present", null, 
            impl.getUserPref(testKeys[0], context));
        assertEquals("Can retrieve set app value after new user logged in", 
                testValues[2], impl.getAppPref(testKeys[2], context));
        
        impl.setActiveUser(utils.getTestProperty(TestUtils.PROP_TESTUSER), context);
        assertEquals("After logging in value is present", testValues[0],
                impl.getUserPref(testKeys[0], context));

        impl.setUserPref(testKeys[0], null, context);
        assertEquals("Can delete user preference", null, 
                impl.getUserPref(testKeys[0], context));
        
        assertEquals("After delete key is not in user pref list", -1, 
            UMUtil.getIndexInArray(testKeys[0], impl.getUserPrefKeyList(context)));
        
        
        impl.setAppPref(testKeys[2], null, context);
        assertEquals("Can delete app pref", null, impl.getAppPref(testKeys[2], context));
        assertEquals("After delete key is not in app pref list", -1,
            UMUtil.getIndexInArray(testKeys[2], impl.getAppPrefKeyList(context)));
    }
    
    public void runTest(){
        this.testPreferences();
    }
    
}
