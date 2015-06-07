/*
 * UstadJS is for common functions used between App and Content.
 * 
 */
package com.ustadmobile.app;

/**
 *
 * @author varuna
 */
public class UstadJS {
    
    public static final String getExtension(String filename) {
        int dotPos = filename.lastIndexOf('.');
        return dotPos != -1 ? filename.substring(dotPos + 1) : null;
    }
    
}
