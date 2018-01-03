/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.controller;

import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.util.UMUtil;

import java.util.Hashtable;

/**
 * Simple class implementing our own Comparer interface to enable sorting entries
 * by the time they were last accessed by a given user
 * 
 * @author mike
 */
public class CatalogLastAccessTimeComparer implements UMUtil.Comparer{
    
    private Hashtable lastAccessTimes;
    
    final private Object context;
    
    final private boolean descOrder;

    public static final int SORT_DESC = 8;
    
    public CatalogLastAccessTimeComparer(int flags, Object context) {
        lastAccessTimes = new Hashtable();
        this.context = context;
        descOrder = (flags & SORT_DESC) == SORT_DESC;
    }

    private final long getLastAccessTime(String id) {
        if(lastAccessTimes.contains(id)) {
            return ((Long)lastAccessTimes.get(id)).longValue();
        }else {
            long lastAccessTime = ContainerController.getContainerLastOpenedTime(id, 
                context);
            lastAccessTimes.put(id, new Long(lastAccessTime));
            return lastAccessTime;
        }
    }
    
    public int compare(Object o1, Object o2) {
        long diff = getLastAccessTime(((UstadJSOPDSEntry)o1).getItemId()) -
            getLastAccessTime(((UstadJSOPDSEntry)o2).getItemId());
        
        if(descOrder) diff *= -1;

        if(diff < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }else if(diff > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }else {
            return (int)diff;
        }
        
    }
    
}
