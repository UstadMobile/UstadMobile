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
package com.ustadmobile.impl;

import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author mike
 */
public class HTTPResult {
    
    private byte[] response;
    
    private int status;
    
    private Hashtable responseHeaders;
    
    /**
     * 
     * @param response The byte response data from the server
     * @param status the response code returned by the server
     * @param responseHeaders the headers returned by the server in a hashtable
     */
    public HTTPResult(byte[] response, int status, Hashtable responseHeaders) {
        this.response = response;
        this.status = status;
        this.responseHeaders = responseHeaders;
    }
    
    public String[] getHTTPHeaderKeys() {
        Enumeration e   = responseHeaders.keys();
        String[] headerKeys = new String[responseHeaders.size()];
        int index = 0;
        
        while(e.hasMoreElements()) {
            headerKeys[index] = e.nextElement().toString();
            index++;
        }
        
        return headerKeys;
    }
    
    /**
     * 
     * @param value
     * @return 
     */
    public String getHeaderValue(String key) {
        Object valObj = responseHeaders.get(key);
        if(valObj != null) {
            return valObj.toString();
        }else {
            return null;
        }
    }
    
    public int getStatus(){
        return status;
    }
    
    public byte[] getResponse(){
        ByteArrayInputStream bais = new ByteArrayInputStream(
                    this.response);
        return this.response;
    }
    
}
