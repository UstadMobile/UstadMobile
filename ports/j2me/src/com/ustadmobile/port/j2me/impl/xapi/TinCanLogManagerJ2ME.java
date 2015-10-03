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
import com.ustadmobile.core.app.Base64;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.j2me.app.AppPref;
import com.ustadmobile.port.j2me.app.FileUtils;
import com.ustadmobile.port.j2me.app.HTTPUtils;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Hashtable;
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
       impl = UstadMobileSystemImpl.getInstance();
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
        append0IfLessThan10(sb, f);
        f = cal.get(Calendar.MILLISECOND);
        sb.append('.').append(f);
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
            //FileUtils.writeStringToFile("0", logPathPosition, false);
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
        String status = "3";
        if (userid != null && userid != ""){
            status = "0";
        }
        sb.append('u').append(userid).append(':').append("statementstart:").append(
                stmt.toString()).append(":statementend:").append(status);
        
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
      
        String testJSON = "";
        testJSON = "{\"actor\":{\"mbox\":\"mailto:student1@ustadmobile.com\",\"name\":\"Student One\",\"objectType\":\"Agent\"},\"object\":{\"definition\":{\"description\":{\"en-US\":\"Motivational\"},\"name\":{\"en-US\":\"Motivational\"},\"type\":\"http://adlnet.gov/expapi/activities/module\"},\"id\":\"http://www.ustadmobile.com/um-tincan/activities/ThisIsTheUniqueElpID/Motivational\",\"objectType\":\"Activity\"},\"result\":{\"duration\":\"PT0H0M2S\"},\"verb\":{\"display\":{\"en-US\":\"experienced\"},\"id\":\"http://adlnet.gov/expapi/verbs/experienced\"}}";
        
        JSONObject testJSONObj = new JSONObject(testJSON);
        String JSON2String = testJSONObj.toString();
        
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
        String newLogMade = FileUtils.getBaseName(newName);
        //Go through all log files available..
        for(int i = 0; i < filesToReplicate.length; i++) {
            if(filesToReplicate[i].endsWith(".log")) {
                
                String potentialTempFile = filesToReplicate[i] + ".status";
                String potentialTempFileURI = FileUtils.joinPath(tincanDir, 
                        potentialTempFile);
                if (FileUtils.checkDir(potentialTempFileURI)){
                    //Both temp and log file exists. There are stuff left to scan through, etc
                    
                }
                String currentFile = filesToReplicate[i];
                
                if (filesToReplicate[i].equals(newLogMade)){
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
                    int resultCode = sendLog(logIn, statusOut);
                    
                    if (logIn != null){
                        logIn.close();
                    }
                    if (logOut != null){
                        logOut.close();
                    }
                    if (resultCode == 1){
                        //delete .log, rename .tmp to .done
                        FileUtils.deleteRecursively(
                                FileUtils.joinPath(tincanDir, 
                                        filesToReplicate[i]), false);
                        String statusFileURIDone = statusFileURI + ".done";
                        FileUtils.renameFileOrDir(statusFileURI,
                                statusFileURIDone, Connector.READ_WRITE, false);
                    }else if (resultCode == 2){
                        String tempB4Delete = FileUtils.joinPath(tincanDir, 
                                        filesToReplicate[i]) + ".origi";
                        
                        FileUtils.renameFileOrDir( 
                                FileUtils.joinPath(tincanDir, 
                                        filesToReplicate[i]),
                                tempB4Delete,
                                Connector.READ_WRITE, false);
                        
                        FileUtils.renameFileOrDir(statusFileURI, 
                                FileUtils.joinPath(tincanDir, 
                                        filesToReplicate[i]), 
                                Connector.READ_WRITE, false);
                        
                        FileUtils.deleteRecursively(tempB4Delete, false);
                        
                    }else{
                        //-\_(^-^)_/-
                    }
                    
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
        boolean noErrors = true;
        //X-Experience-API-Version
        Hashtable tinCanHeaders = new Hashtable();
        tinCanHeaders.put("X-Experience-API-Version", "1.0.1");
        String statementUsername = "";
        
        //AppPref.addSetting("password-"+currentUsername, password);
        
        
        
        while((b = logIn.read()) != -1) {
            //System.out.println("output:");
            String line = new String(bout.toByteArray());
            trial = 0;
            
                if(b == nline || b ==cret) {
                    int return_code = 400;
                    lineBytes = bout.toByteArray(); 
                    //Done with bout
                    bout.close();
                    bout = new ByteArrayOutputStream();
                    
                    String logLine = new String(lineBytes);
                    String logLineWOCode="";
                    String doneTries = "";
                    while(trial < max_tries){
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
                        }else if (logLine.endsWith(":3")){
                            //Not a valid line. Put it in the status file
                            logLine = logLine.substring(0, logLine.length()-1);
                            logLine = logLine + "3";
                            lineBytes = logLine.getBytes();
                            statusOut.write(lineBytes);
                            statusOut.write(nline);
                            trial = max_tries + 10;
                            break; //I want to break free
                        }else if (logLine.endsWith(":1")){
                            //Already sent
                            lineBytes = logLine.getBytes();
                            statusOut.write(lineBytes);
                            statusOut.write(nline);
                            break;
                        }else{
                            //Unknown.
                            logLine = logLine + ":03";
                            lineBytes = logLine.getBytes();
                            statusOut.write(lineBytes);
                            statusOut.write(nline);
                            break;
                        }
                        
                        statementUsername = logLine.substring(2, logLine.indexOf(":statementstart:"));
                        String password = impl.getAppPref("password-"+statementUsername);
                        
                        if (statementUsername == null || statementUsername == ""){
                            //Blank username bro
                            logLine = logLine + ":03";
                            lineBytes = logLine.getBytes();
                            statusOut.write(lineBytes);
                            statusOut.write(nline);
                            
                            break;
                        }
                        
                        String encodedUserAndPass="Basic "+ Base64.encode(statementUsername,
                            password);
                        tinCanHeaders.put("Authorization", encodedUserAndPass);

                        String statementString = logLine.substring(
                                logLine.indexOf("statementstart:"),
                                logLine.indexOf(":statementend:"));
                        statementString = statementString.substring("statementstart:".length(), statementString.length());
                        byte[] statementBytes = statementString.getBytes();
                        String tincanEndpointURL = 
                                "http://umcloud1.ustadmobile.com/umlrs/statements/";

                        HTTPResult result = HTTPUtils.makeHTTPRequest(tincanEndpointURL,
                                null, tinCanHeaders, "POST", statementBytes);
                        return_code = result.getStatus();

                        if (return_code == 200){
                            logLine = logLine.substring(0, logLine.length()-1);
                            logLine = logLine + "1";
                            lineBytes = logLine.getBytes();
                            statusOut.write(lineBytes);
                            statusOut.write(nline);

                            break;
                        }else if (return_code == 401 || 
                            return_code == 400 || return_code == 403){

                            if (trial + 2 > max_tries){
                                noErrors = false;
                                //Maxed out
                                logLine = logLine.substring(0, logLine.length()-1);
                                logLine = logLine + "2";
                                lineBytes = logLine.getBytes();
                                statusOut.write(lineBytes);
                                statusOut.write(nline);
                                trial = trial + 10;
                            }
                            trial = trial + 1;
                            //Got to keep on trying 
                        }else{
                            //Not a valid line. Put it in the status file
                            logLine = logLine.substring(0, logLine.length()-1);
                            logLine = logLine + "3";
                            lineBytes = logLine.getBytes();
                            statusOut.write(lineBytes);
                            statusOut.write(nline);
                            trial = max_tries + 10;
                            break; //I want to break free
                        }
                    }


                }else {
                    bout.write(b);
                    //continue;
                }
            
        }
        
        if (noErrors){
            return 1;
        }else{
            return 2;
        }
        
        //Next steps
        //1. Convert the logOut (.temp file) to a .done file.
        //   delete the .log file - We dont need it nomore
        //2. If .done file needs to be re sent AND end of line reached:
        //   rename the original .log file to .log.origi
        //   rename the .done file to .log 
        //   delete the original .log.origi
        
        
        //return sent;
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
