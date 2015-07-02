/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.impl;

/**
 *
 * @author mike
 */
public abstract class HTTPResult {
    
    private byte[] response;
    
    private int status;
    
    public HTTPResult(byte[] response, int status) {
        this.response = response;
        this.status = status;
    }
    
    public abstract String[] getHTTPHeaders();
    
    /**
     * 
     * @param value
     * @return 
     */
    public abstract String getHeaderValue(String value);
    
}
