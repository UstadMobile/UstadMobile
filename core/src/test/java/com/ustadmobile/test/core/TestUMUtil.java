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

import com.ustadmobile.core.util.UMUtil;

import junit.framework.TestCase;

/* $if umplatform == 2  $
    import j2meunit.framework.TestCase;
 $else$ */
/* $endif$ */


/**
 *
 * @author mike
 */
public class TestUMUtil extends TestCase{
    
    public void testUMUtilSort() {
        Integer five = new Integer(5);
        Integer three = new Integer(3);
        Integer seven = new Integer(7);
        
        Integer arr[] = new Integer[]{five, three, seven};
        UMUtil.Comparer comparer = new UMUtil.Comparer() {
            public int compare(Object o1, Object o2) {
                return ((Integer)o1).intValue() - ((Integer)o2).intValue();
            }
        };
        UMUtil.bubbleSort(arr, comparer);
        
        assertEquals("Sorted item0", three, arr[0]);
        assertEquals("sorted item1", five, arr[1]);
        assertEquals("Sorted item2", seven, arr[2]);
    }

    public void runTest(){
        testUMUtilSort();
    }
    
}
