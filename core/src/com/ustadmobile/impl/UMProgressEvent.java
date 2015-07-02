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
public class UMProgressEvent {
    
    private int evtType;
    
    private int progress;
    
    private int jobLength;
    
    private int statusCode;
    
    public static final int TYPE_PROGRESS = 0;
    
    public static final int TYPE_COMPLETE = 1;
    
    public UMProgressEvent() {
        
    }
    
    public UMProgressEvent(int evtType, int progress, int jobLength, int statusCode) {
        this.evtType = evtType;
        this.progress = progress;
        this.jobLength = jobLength;
        this.statusCode = statusCode;
    }
    
    
    public int getEvtType() {
        return this.evtType;
    }
    
    public int getJobLength() {
        return this.jobLength;
    }
    
    public int getProgress() {
        return this.progress;
    }
    
    public int getStatusCode() {
        return this.statusCode;
    }
    
    
}
