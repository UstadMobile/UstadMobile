/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ustadmobile.port.j2me.impl;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.j2me.app.SerializedHashtable;
import com.ustadmobile.port.j2me.impl.xapi.TinCanLogManagerJ2ME;
import com.ustadmobile.port.j2me.util.J2MEIOUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

/**
 *
 * @author mike
 */
public class UMLogJ2ME extends UMLog{

    private OutputStream logOut = null;
    
    private SocketConnection socketConn;
    
    private OutputStream socketOut;
    
    private OutputStream fileOut;
    
    private boolean remoteSocketConnected;
    
    public UMLogJ2ME() {
        logOut = System.out;
        remoteSocketConnected = false;
    }
    
    public synchronized void setOutputDest(PrintStream dest) {
        this.logOut = dest;
    }
    
    
    /**
     * Whether or not this log stream is connected to a remote socket
     * 
     * @return true if this is connected to a remote socket, false otherwise
     */
    public boolean isRemoteSocketConnected() {
        return remoteSocketConnected;
    }
    
    public synchronized void connectLogToSocket(String serverName) throws IOException{
        closeSocketConn();
        SocketConnection socketConnection = null;
        try {
            socketConnection = (SocketConnection)Connector.open("socket://" 
                + serverName);
            socketOut = socketConnection.openOutputStream();
            logOut = new PrintStream(socketOut);
            remoteSocketConnected = true;
            System.out.println("Connected socket");
        }catch(IOException e) {
            System.out.println("Exception connecting socket!");
            e.printStackTrace();
            closeSocketConn();
            throw e;
        }   
    }
    
    public synchronized void connectLogToFile(String fileDest) throws IOException {
        closeLogFileConnection();
        fileOut = UstadMobileSystemImpl.getInstance().openFileOutputStream(fileDest, 0);
        logOut = new PrintStream(fileOut);
    }
    
    public void closeLogFileConnection() {
        if(fileOut != null) {
            UMIOUtils.closeOutputStream(fileOut);
            logOut = System.out;
            fileOut = null;
        }
    }
    
    public boolean connectLogToSharedDir(Object context) throws IOException{
        UMStorageDir[] sharedDirs = UstadMobileSystemImplJ2ME.getInstance().getStorageDirs(
            CatalogController.SHARED_RESOURCE, context);
        
        UMStorageDir sdCardDir = null;
        for(int i = 0; i < sharedDirs.length; i++) {
            if(sharedDirs[i].isRemovableMedia() && sharedDirs[i].isAvailable()) {
                sdCardDir = sharedDirs[i];
                break;
            }
        }
        
        UMStorageDir dirToUse = sdCardDir != null ? sdCardDir : sharedDirs[0];
        String fileName = TinCanLogManagerJ2ME.getDateLogStr().append("-umlog.txt").toString();
        connectLogToFile(UMFileUtil.joinPaths(new String[] { dirToUse.getDirURI(), 
            fileName}));
        
        return false;
    }
    
    public synchronized void closeSocketConn() {
        logOut = System.out;
        remoteSocketConnected = false;
        UMIOUtils.closeOutputStream(socketOut);
        J2MEIOUtils.closeConnection(socketConn);
    }

    public synchronized void l(int level, int code, String message) {
        StringBuffer sb = new StringBuffer();
        sb.append(":codelu:");
        sb.append(code);
        sb.append(" : ");
        sb.append(message);
        sb.append("\n");
        try {
            logOut.write(sb.toString().getBytes());
            logOut.flush();
        }catch(IOException e) {
            System.err.println("Exception sending log line");
            e.printStackTrace();
        }
    }

    public synchronized void l(int level, int code, String message, Exception exception) {
        StringBuffer sb = new StringBuffer();
        sb.append(":codelu:");
        sb.append(code);
        sb.append(" : ");
        sb.append(message);
        sb.append("/Exception: ");
        sb.append(exception.getClass().getName());
        sb.append(" Message: ");
        sb.append(exception.getMessage());
        sb.append("/toString ");
        sb.append(exception.toString());
        sb.append('\n');
        try {
            logOut.write(sb.toString().getBytes());
            logOut.flush();
        }catch(IOException e) {
            System.err.println("Exception sending log line");
            e.printStackTrace();
        }
        exception.printStackTrace();
    }
    
    

}