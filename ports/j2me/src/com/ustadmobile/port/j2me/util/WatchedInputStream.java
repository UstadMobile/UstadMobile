/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.util;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 *
 * @author mike
 */
public class WatchedInputStream extends InputStream{ 

    private InputStream in;
    
    public static Vector openStreams = new Vector();
    
    public static Vector streamNames = new Vector();
    
    private String name;
    
    public WatchedInputStream(InputStream in) {
        this.in = in;
        this.name = in.toString();
        addToActiveStreams();
    }
    
    public WatchedInputStream(InputStream in, String name) {
        this.in = in;
        this.name = name;
        addToActiveStreams();
    }
    
    public static void printActiveStreams() {
        for(int i = 0; i <openStreams.size(); i++) {
            System.out.println(i + ": " + streamNames.elementAt(i));
        }
    }
    
    private void addToActiveStreams() {
        openStreams.addElement(this);
        streamNames.addElement(name);
        UstadMobileSystemImpl.l(UMLog.INFO, 399, "#Active Streams: " 
            + openStreams.size() + " : +"  +this.name);
        System.out.println("ADD: " + name);
        //printActiveStreams();
    }
    
    private void removeFromActiveStreams() {
        int index = openStreams.indexOf(this);
        if(index == -1) {
            //already closed - do nothing
            return;
        }
        
        openStreams.removeElementAt(index);
        streamNames.removeElementAt(index);
        
        UstadMobileSystemImpl.l(UMLog.INFO, 398, "#Active Streams: " 
            + openStreams.size() + " : -"  +this.name);
        System.out.println("REMOVE: " + name);
        //printActiveStreams();
    }
    
    public boolean markSupported() {
        return in.markSupported(); //To change body of generated methods, choose Tools | Templates.
    }

    public synchronized void reset() throws IOException {
        in.reset(); //To change body of generated methods, choose Tools | Templates.
    }

    public synchronized void mark(int readlimit) {
        in.mark(readlimit); //To change body of generated methods, choose Tools | Templates.
    }

    public void close() throws IOException {
        in.close();
        removeFromActiveStreams();
    }

    public int available() throws IOException {
        return in.available();
    }

    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len); //To change body of generated methods, choose Tools | Templates.
    }

    public int read(byte[] b) throws IOException {
        return in.read(b); //To change body of generated methods, choose Tools | Templates.
    }

    public int read() throws IOException {
        return in.read();
    }
    
}
