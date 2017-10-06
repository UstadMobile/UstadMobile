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

import com.ustadmobile.core.util.UMFileUtil;

import junit.framework.TestCase;

/* $endif$ */

/**
 *
 * @author mike
 */
public class TestUMFileResolveLink extends TestCase{
    
    public TestUMFileResolveLink() {
        
    }
    
    public void setUp() {
    }
    
    public void tearDown() {
    }
    
    public void testUMFileUtilResolveLink() {
        assertEquals("Absolute path returns same path back",
            "http://www.server2.com/somewhere", 
            UMFileUtil.resolveLink("http://server1.com/some/place", 
                "http://www.server2.com/somewhere"));
        assertEquals("Can resolve prtocol only link",
            "http://www.server2.com/somewhere",
            UMFileUtil.resolveLink("http://server1.com/some/place", 
                "//www.server2.com/somewhere"));
        assertEquals("Can resolve relative to server link",
            "http://server1.com/somewhere",
            UMFileUtil.resolveLink("http://server1.com/some/place", 
                "/somewhere"));
        assertEquals("Can handle basic relative path",
            "http://server1.com/some/file.jpg",
            UMFileUtil.resolveLink("http://server1.com/some/other.html", 
                "file.jpg"));
        assertEquals("Can handle .. in relative path",
            "http://server1.com/file.jpg",
            UMFileUtil.resolveLink("http://server1.com/some/other.html", 
                "../file.jpg"));
    }

    public void runTest(){
        this.testUMFileUtilResolveLink();
    }
    
}
