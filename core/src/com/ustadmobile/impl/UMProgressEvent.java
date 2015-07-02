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
public interface UMProgressEvent {
    
    public static final int TYPE_PROGRESS = 0;
    
    public static final int TYPE_COMPLETE = 1;
    
    public int getType();
    
    public int getJobLength();
    
    public int getProgress();
    
    public int getStatusCode();
    
    
}
