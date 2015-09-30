/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.port.j2me.impl.xapi;

import com.sun.lwuit.io.util.BufferedOutputStream;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.j2me.app.FileUtils;
import com.ustadmobile.port.j2me.app.HTTPUtils;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.TimerTask;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import org.json.me.*;

/**
 *
 * @author mike
 */
public class TinCanLogManagerJ2ME extends TimerTask{
    
    String currentFile;
    
    static int nline = (int)'\n';
    
    static int cret = (int)'\r';
    
    /**
     * Outputstream connected to the current logging output file
     */
    private OutputStream logOut;
    UstadMobileSystemImpl impl = null;
    static final String LOG_FOLDER = "xapi";
    
    public TinCanLogManagerJ2ME() {
       impl = new UstadMobileSystemImplJ2ME();
    }
    
    
    
    /**
     * Format a string to be used in the filename for the activity and debug 
     * names.
     * 
     * @return String formatted for date log file names yyyy-mm-dd
     */ 
    public static StringBuffer getDateLogStr() {
        Calendar cal = Calendar.getInstance();
        StringBuffer sb = new StringBuffer();
        sb.append(cal.get(Calendar.YEAR));
        int f = cal.get(Calendar.MONTH);
        append0IfLessThan10(sb, f).append(f);
        f = cal.get(Calendar.DAY_OF_MONTH);
        append0IfLessThan10(sb, f).append(f);
        f = cal.get(Calendar.HOUR_OF_DAY);
        append0IfLessThan10(sb, f);
        f = cal.get(Calendar.MINUTE);
        /*
        append0IfLessThan10(sb, f);
        f = cal.get(Calendar.MILLISECOND);
        sb.append('.').append(f);
        */        
        String a = sb.toString();
        
        return sb;
    }
    
    private static StringBuffer append0IfLessThan10(StringBuffer sb, int num) {
        if(num < 10) {
            sb.append(0);
        }
        return sb;
    }
    
    
    /**
     * if num is less than ten add a preceding 0
     * @param num 
     */
    private static String pad1(int num) {
        if(num >= 10) {
            return String.valueOf(num);
        }else {
            return "0" + num;
        }
    }
    
    public String openNewLog() throws IOException {
        boolean success = false;
        StringBuffer logName = new StringBuffer();
        logName.append("tincan-").append(getDateLogStr()).append(".log");
        
        //Check if file exists 
        String deviceDir = UstadMobileSystemImplJ2ME.getInstanceJ2ME().findSystemBaseDir();
        String tincanDir = FileUtils.joinPath(deviceDir, LOG_FOLDER);
        boolean xapi_created = FileUtils.createFileOrDir(tincanDir, Connector.READ_WRITE, true);
        if (!xapi_created){
            impl.l(UMLog.DEBUG, 800, "Unable to create xapi folder in phone memory");
        }
       
        String logPath = FileUtils.joinPath(tincanDir, logName.toString());
        String logPathPosition = logPath + ".status";
        if (!FileUtils.checkFile(logPath)){
            FileUtils.createFileOrDir(logPath, Connector.READ_WRITE, false);
        }
        if (!FileUtils.checkFile(logPathPosition)){
            FileUtils.createFileOrDir(logPathPosition, Connector.READ_WRITE, false);
            FileUtils.writeStringToFile("0", logPathPosition, false);
        }
        
        
        FileConnection fCon = null;
        try{
            fCon = (FileConnection)Connector.open(logPath, Connector.READ_WRITE);
            logOut = fCon.openOutputStream();
            currentFile = logPath;
            success = true;
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Unable to make logPath file connection");
            impl.l(UMLog.DEBUG, 800, 
                    "Unable to make logPath file Connection and OutputStream");
            
            success = true;
        }finally{
            if (fCon != null){
                fCon.close();
            }
        }
        
        // logOut = ... open file output stream 
        if (success){
            return logPath;
        }else{
            return "";
        }
    }
    
    public boolean queueStatement(String userid, JSONObject stmt) {
        StringBuffer sb = new StringBuffer();
        sb.append(userid).append(':').append(stmt.toString());
        
        try {
            synchronized(this) {
                logOut.write(sb.toString().getBytes(UstadMobileConstants.UTF8));
                logOut.write((int)'\n');
                logOut.flush();
            }
        }catch(IOException e) {
            impl.l(UMLog.DEBUG, 800, "Unable to queueStatement");
            //log me etc
        }
        
        
        return false;
    }
    
    public void transmitQueue() throws IOException, Exception {
        String newName = "";
        synchronized(this) {
            try {
                if (logOut != null){
                    logOut.flush();
                    logOut.close();
                }
                newName = openNewLog();
            }catch(IOException e) {
                if (logOut != null){
                    logOut.close();
                }
                impl.l(UMLog.DEBUG, 800, "Unable to transmitQueue");
                //log me etc.
            }
        }
        
        String[] filesToReplicate = null;
        String deviceDir = UstadMobileSystemImplJ2ME.getInstanceJ2ME().findSystemBaseDir();
        String tincanDir = FileUtils.joinPath(deviceDir, LOG_FOLDER);
        filesToReplicate = impl.listDirectory(tincanDir);
        //filesToReplicate = impl.listDirectory(currentFile)
        //filesToReplicate = listDir(appdir /tincan);
        
        //Go through all log files available..
        for(int i = 0; i < filesToReplicate.length; i++) {
            if(filesToReplicate[i].endsWith(".log")) {
                
                if (filesToReplicate[i].equals(newName)){
                    //Dont do it. Skip.
                    continue;
                }
                int readFrom = 0;
                String statusFile = filesToReplicate[i].toString() + ".status";
                String statusFileURI = FileUtils.joinPath(tincanDir, statusFile);
                OutputStream statusOut = null;
                FileConnection statusCon = null;
                
                //Create a blank status file if it doesnt exist.
                if (!FileUtils.checkFile(statusFileURI)){
                    FileUtils.createFileOrDir(statusFileURI, Connector.READ_WRITE, false);
                }
                
                //If created okay
                if (FileUtils.checkFile(statusFileURI)){
                    
                    //Ready the status file: to be written.
                    statusCon = (FileConnection)Connector.open(statusFileURI, Connector.READ_WRITE);
                    
                    //get status' file's output stream
                    statusOut = statusCon.openOutputStream();

                    InputStream logIn = null;
                    FileConnection logCon = null;
                    
                    logCon = (FileConnection) Connector.open(
                            FileUtils.joinPath(tincanDir, filesToReplicate[i]),
                            Connector.READ);
                    
                    
                    //if nothing in the log file, all done, etc
                    if (logCon.availableSize() == 0){
                        if (logCon != null){
                            logCon.close();
                        }
                        if (statusOut != null){
                            statusOut.close();
                        }
                        String doneLogName = filesToReplicate[i].toString()
                                + ".done";
                        statusCon.rename(doneLogName);
                        statusCon.close();
                        continue;
                    }
                    if (statusCon != null){
                        statusCon.close();
                    }
                    
                    //Get log file's input stream 
                    logIn = logCon.openInputStream();
                    
                    //Send log from log file IS and work on Status file OS
                    sendLog(logIn, statusOut);
                    
                    
                }                
            }
            
        }
        
        
    }
    
    public boolean updatePosition(String positionFileURI, long position) throws IOException, Exception{
        if (FileUtils.checkFile(positionFileURI)){
            long currentPosition = Long.parseLong(FileUtils.getFileContents(positionFileURI));
            long newPosition = currentPosition + position;
            if (FileUtils.writeStringToFile(String.valueOf(newPosition), positionFileURI, false)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    
    public int sendLog(InputStream logIn, OutputStream statusOut) throws IOException, Exception{
        //send the log - return how many bytes were sent through
        
        // read byte by byte until finding \n ... then
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int b;
        int sent = 0;
        int max_tries = 10;
        int trial=0;
        byte[] lineBytes;
        while((b = logIn.read()) != -1) {
            System.out.println("output:");
            trial = 0;
            while(trial < max_tries){
                if(b == nline || b ==cret) {
                    int return_code = 400;
                    lineBytes = bout.toByteArray(); 
                    //Done with bout
                    bout.close();
                    bout = new ByteArrayOutputStream();
                    
                    String logLine = new String(lineBytes);
                    String logLineWOCode="";
                    String doneTries = "";
                    if(logLine.endsWith(":0") || logLine.endsWith(":2")){
                        //Time to try this.
                        
                        System.out.println("New log / needs re trial");
                        
                        /*
                        logLineWOCode = logLine.substring(0, logLine.length()-2);
                        doneTries = logLineWOCode.substring(logLine.lastIndexOf(':'),
                                logLineWOCode.length());
                        if (doneTries != ""){
                            int triesDone = Integer.parseInt(doneTries);
                            if(triesDone > max_tries){
                                statusOut.write(lineBytes);
                                break; //I want to break free 
                            }
                        }
                        */
                        
                        //Time to send the log line
                        
                        
                        
                        
                    }else{
                        //Not a valid line. Put it in the status file
                        logLine = logLine.substring(0, logLine.length()-1);
                        logLine = logLine + "3";
                        lineBytes = logLine.getBytes();
                        statusOut.write(lineBytes);
                        trial = max_tries + 10;
                        break; //I want to break free
                    }
                    
                    String statementString = logLine.substring(
                            logLine.indexOf("statementstart:"),
                            logLine.indexOf(":statementend:"));
                    byte[] statementBytes = statementString.getBytes();
                    String tincanEndpointURL = 
                            "http://umcloud1.ustadmobile.com/umlrs/statements/";

                    HTTPResult result = HTTPUtils.makeHTTPRequest(tincanEndpointURL,
                            null, null, "POST", statementBytes);
                    return_code = result.getStatus();
                    
                    if (return_code == 200){

                        trial = max_tries + 10;
                    }else if (return_code == 401 || 
                        return_code == 400 || return_code == 403){
                        trial = trial + 1;
                        //Got to keep on trying 
                    }else{
                        //Not a valid line. Put it in the status file
                        logLine = logLine.substring(0, logLine.length()-1);
                        logLine = logLine + "3";
                        lineBytes = logLine.getBytes();
                        statusOut.write(lineBytes);
                        trial = max_tries + 10;
                        break; //I want to break free
                    }



                }else {
                    bout.write(b);
                }
            }
        }
        
        return sent;
    }

    public void run() {
        try {
            transmitQueue();//send the logs up
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    
}
