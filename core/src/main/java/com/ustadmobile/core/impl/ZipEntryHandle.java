/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.impl;

/**
 *
 * @author mike
 */
public interface ZipEntryHandle {
    
    
    /**
     * Returns the uncompressed size of the data or -1 if not known
     * 
     * @return the uncompressed size of the data or -1 if not known
     */
    public long getSize();
    
    /**
     * Returns the name of the entry
     * 
     * @return the name of the entry
     */
    public String getName(); 
    
    /**
     * Returns true if this entry is a direectory entry.  A directory entry is
     * defined as one whose name ends with a '/'
     * 
     * @return true if this is a directory entry
     */
    public boolean isDirectory();
    
}
