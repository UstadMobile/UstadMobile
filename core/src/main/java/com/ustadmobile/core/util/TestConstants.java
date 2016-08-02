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
package com.ustadmobile.core.util;

/**
 *
 * @author mike
 */
public class TestConstants {
    
    public static final String XAPI_SERVER = 
            "http://umcloud1.ustadmobile.com/umlrs";
    
    public static final String LOGIN_URL = XAPI_SERVER + "/statements?limit=1";
    
    
    public static final String CATALOG_OPDS_ROOT = "root.opds";
    
    
    public static final String REGISTER_URL = 
            "http://umcloud1.ustadmobile.com/phoneinappreg/";
    
    /**
     * When doing a network job tests need to run async otherwise android
     * will throw an annoying exception 
     * 
     * The default time to wait for a job to complete
     */
    public static final int DEFAULT_NETWORK_INTERVAL = 1000;
    
    /**
     * The default length of time to wait for a network job to complete
     */
    public static final int DEFAULT_NETWORK_TIMEOUT = 20000;

}
