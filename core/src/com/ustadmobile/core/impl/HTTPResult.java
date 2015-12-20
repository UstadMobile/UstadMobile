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
package com.ustadmobile.core.impl;

import com.ustadmobile.core.util.UMFileUtil;
import java.io.IOException;
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
    
    public static final String GET = "GET";
    
    
    public static final int HTTP_SIZE_NOT_GIVEN = -1;
    
    public static final int HTTP_SIZE_IO_EXCEPTION = -2;
    
    /**
     * 
     * @param response The byte response data from the server
     * @param status the response code returned by the server
     * @param responseHeaders the headers returned by the server in a hashtable (all keys lower case)
     */
    public HTTPResult(byte[] response, int status, Hashtable responseHeaders) {
        this.response = response;
        this.status = status;
        
        //put all headers into lower case to make them case insensitive
        if(responseHeaders != null) {
            this.responseHeaders = new Hashtable();
            String headerName;
            Enumeration keys = responseHeaders.keys();
            while(keys.hasMoreElements()) {
                headerName = (String)keys.nextElement();
                this.responseHeaders.put(headerName.toLowerCase(), 
                    responseHeaders.get(headerName));
            }
        }
    }
    
    /**
     * Get a list of all the HTTP headers that have been provided for this
     * request
     * 
     * @return String array of available http headers
     */
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
     * Provides all responses headers (with the header itself in **lower case** 
     * in a hashtable
     * @return 
     */
    public Hashtable getResponseHeaders() {
        return responseHeaders;
    }
    
    /**
     * Get the suggested filename for this HTTP Request: if the HTTP request
     * has a content-disposition header we will use it to provide the filename;
     * otherwise we will use the filename portion of the URL 
     * 
     * @param url The entire URL 
     * 
     * @return Filename suggested by content-disposition if any; otherwise the filename portion of the URL
     */
    public String getSuggestedFilename(String url) {
        String suggestedFilename = null;
        
        if(responseHeaders != null && responseHeaders.containsKey("content-disposition")) {
            Object dispositionHeaderStr = responseHeaders.get("content-disposition");
            UMFileUtil.TypeWithParamHeader dispositionHeader = 
                UMFileUtil.parseTypeWithParamHeader((String)dispositionHeaderStr);
            if(dispositionHeader.params != null && dispositionHeader.params.containsKey("filename")) {
                suggestedFilename = UMFileUtil.filterFilename(
                    (String)dispositionHeader.params.get("filename"));
            }
        }
        
        
        if(suggestedFilename == null){
            suggestedFilename = UMFileUtil.getFilename(url);
        }
        
        return suggestedFilename;   
    }
    
    /**
     * Return the size of this http request as per content-length.  This can
     * be used in combination with the HEAD request method to request the content
     * length without actually downloading the content itself
     * 
     * @see HTTPResult#HTTP_SIZE_IO_EXCEPTION
     * @see HTTPResult#HTTP_SIZE_NOT_GIVEN
     * 
     * @return The content length in bytes if successful, an error flag < 0 otherwise
     */
    public int getContentLength() {
        int retVal = HTTP_SIZE_NOT_GIVEN;
        String contentLengthStr = getHeaderValue("content-length");
        if(contentLengthStr != null) {
            retVal = Integer.parseInt(contentLengthStr);
        }
        return retVal;
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
        return this.response;
    }
    
}
