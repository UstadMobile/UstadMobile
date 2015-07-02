/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.impl;

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
    
}
