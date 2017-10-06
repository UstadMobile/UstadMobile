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

/* $if umplatform == 2  $
    import j2meunit.framework.TestCase;
 $else$ */

import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.test.core.buildconfig.TestConstants;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Hashtable;

/* $endif$ */

/**
 *
 * @author mike
 */
public abstract class TestLoginRegisterUser extends TestCase {
    
    public TestLoginRegisterUser() {
        
    }
    
    public void testLoginRegister() throws IOException{
        Hashtable registerParams = new Hashtable();
        registerParams.put("phonenumber", "+9641234567");
        registerParams.put("gender", "f");
        registerParams.put("name", "Unit Testing");
        
        String serverSays = LoginController.registerNewUser(registerParams, 
            TestConstants.REGISTER_URL);
        assertNotNull("Can register user: server says " + serverSays, 
            serverSays);
    }
  
    public void runTest() throws IOException{
        this.testLoginRegister();
    }
    
}
