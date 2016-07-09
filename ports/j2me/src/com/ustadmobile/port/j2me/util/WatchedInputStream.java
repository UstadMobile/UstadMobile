/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.util;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import gnu.classpath.java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 *
 * @author mike
 */
public class WatchedInputStream extends FilterInputStream{ 

    
    public static Vector openStreams = new Vector();
    
    public static Vector streamNames = new Vector();
    
    private String name;
    
    public WatchedInputStream(InputStream in) {
        this(in, in.toString());
    }
    
    public WatchedInputStream(InputStream in, String name) {
        super(in);
        this.name = name;
        addToActiveStreams();
    }
    
    public static void printActiveStreams() {
        StringBuffer sb = new StringBuffer('[');
        WeakReference ref;
        for(int i = 0; i <openStreams.size(); i++) {
            ref = (WeakReference)openStreams.elementAt(i);
            if(ref.get() != null) {
                sb.append(i).append(':').append(streamNames.elementAt(i)).append(',');
            }
        }
        sb.append(']');
        UstadMobileSystemImpl.l(UMLog.DEBUG, 605, sb.toString());
    }
    
    private static int getNumStreams() {
        WeakReference ref;
        int numStreams = 0;
        for(int i = 0; i < openStreams.size(); i++) {
            ref = (WeakReference)openStreams.elementAt(i);
            if(ref != null) {
                numStreams++;
            }
        }
        
        return numStreams;
    }
    
    private void addToActiveStreams() {
        openStreams.addElement(new WeakReference(this));
        streamNames.addElement(name);
        UstadMobileSystemImpl.l(UMLog.INFO, 399, getNumStreams() + " : +" + this.name);
        printActiveStreams();
    }
    
    private void removeFromActiveStreams() {
        WeakReference ref;
        for(int i = 0; i < openStreams.size(); i++) {
            ref = (WeakReference)openStreams.elementAt(i);
            if(ref.get() == this) {
                openStreams.removeElementAt(i);
                streamNames.removeElementAt(i);
                UstadMobileSystemImpl.l(UMLog.INFO, 398, getNumStreams() + " : -"  +this.name);
                return;
            }
        }
    }

    public void close() throws IOException {
        super.close();
        removeFromActiveStreams();
    }

    
}
