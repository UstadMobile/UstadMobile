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

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.Base64Coder;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import com.ustadmobile.port.j2me.util.J2MEIOUtils;
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
        impl.l(UMLog.DEBUG, 608, null);
        boolean success = false;
        StringBuffer logName = new StringBuffer();
        logName.append("tincan-").append(getDateLogStr()).append(".log");
        impl.l(UMLog.DEBUG, 609, logName.toString());
        
        //Check if file exists 
        String deviceDir = UstadMobileSystemImplJ2ME.getInstanceJ2ME().findSystemBaseDir();
        String tincanDir = UMFileUtil.joinPaths(new String[]{deviceDir, LOG_FOLDER});
        impl.makeDirectory(tincanDir);
        
        if (!impl.dirExists(tincanDir)){
            impl.l(UMLog.ERROR, 100, null);
        }
       
        String logPath = UMFileUtil.joinPaths(new String[]{tincanDir, logName.toString()});
        String logPathPosition = logPath + ".status";                
        
        try{
            logOut = impl.openFileOutputStream(logPath, 0);
            currentFile = logPath;
            success = true;
        }catch(Exception e){
            impl.l(UMLog.ERROR, 172, logPath, e);
            success = false;
        }
        
        if (success){
            return logPath;
        }else{
            return "";
        }
    }
    
    public boolean queueStatement(String userid, JSONObject stmt) throws IOException {
        impl.l(UMLog.DEBUG, 610,  userid );
        StringBuffer sb = new StringBuffer();
        String status = "3";
        boolean outcome = false;
        if (userid != null && userid.length() > 0){
            status = "0";
        }
        
        sb.append('u').append(':').append(userid).append(':').append("statementstart:").append(
                stmt.toString()).append(":statementend:").append(status);
        impl.l(UMLog.DEBUG, 540, null);
        if (logOut == null){
            openNewLog();
        }
        try {
            synchronized(this) {
                logOut.write(sb.toString().getBytes(UstadMobileConstants.UTF8));
                logOut.write((int)'\n');
                logOut.flush();
                outcome = true;
                impl.l(UMLog.DEBUG, 602, null);
            }
        }catch(IOException e) {
            outcome = false;
            impl.l(UMLog.ERROR, 176, null, e);
        }
        
        return outcome;
    }
    
    public int getLineNumber(String fileURI) throws IOException{
        
        FileConnection fileCon = null;
        InputStream fileIS = null;
        int lineCount = 0;
        int b;
        
        try{
            fileCon = (FileConnection)Connector.open(fileURI, Connector.READ);
            fileIS = fileCon.openInputStream();
            
            try{
                while((b = fileIS.read()) != -1) {

                    if(b == nline || b ==cret) {
                        lineCount = lineCount + 1;
                    }else{
                        continue;
                    }
                }
            }catch(Exception ire){
                impl.l(UMLog.DEBUG, 178, fileURI, ire);
            }
            
        }catch(Exception e){
            impl.l(UMLog.DEBUG, 180, fileURI, e);
        }finally{
            J2MEIOUtils.closeConnection(fileCon);
            UMIOUtils.closeInputStream(fileIS);
        }
        
        return lineCount;
    }
    
    private InputStream readLine(String fileURI, int lineNum) throws IOException {
        FileConnection fCon = null;
        InputStream fIS = null;
        try{
            fCon = (FileConnection)Connector.open(fileURI, Connector.READ_WRITE);
            fIS = fCon.openInputStream();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if (fCon != null){
                fCon.close();
            }
        }

        final StringBuffer buf = new StringBuffer();
        byte c;

        int curLine = 1;
        while (((c = (byte) fIS.read()) != -1)) {
            if (c == cret || c == nline) {
                ++curLine;
                if (curLine > lineNum) {
                    break;
                } else if (curLine < lineNum) {
                    continue;
                }
            } else if (curLine != lineNum) {
                continue;
            }
            buf.append((char) c);
        }
        return fIS;
    }
    
    public void transmitQueue() throws IOException, Exception {
        impl.l(UMLog.DEBUG, 611, null);
        
        String newName = "";
        synchronized(this) {
            try {
                UMIOUtils.closeOutputStream(logOut, true);
                newName = openNewLog();
                impl.l(UMLog.DEBUG, 612, newName);
            }catch(Exception e) {
                impl.l(UMLog.DEBUG, 182, newName, e);
                UMIOUtils.closeOutputStream(logOut);
            }
        }
        
        String[] filesToReplicate = null;
        String deviceDir = UstadMobileSystemImplJ2ME.getInstanceJ2ME().findSystemBaseDir();
        String tincanDir = UMFileUtil.joinPaths(new String[]{deviceDir, LOG_FOLDER});
        filesToReplicate = impl.listDirectory(tincanDir);
        String newLogMade = UMFileUtil.getFilename(newName);
        //Go through all log files available..
        impl.l(UMLog.DEBUG, 613, null);
        for(int i = 0; i < filesToReplicate.length; i++) {
            impl.l(UMLog.DEBUG, 614, filesToReplicate[i] );
            if(filesToReplicate[i].endsWith(".log")) {
                
                String currentLogFileURI = UMFileUtil.joinPaths(new String[]{tincanDir, 
                    filesToReplicate[i]});
                String statusFile = filesToReplicate[i] + ".status";
                String statusFileURI = UMFileUtil.joinPaths(new String[]{tincanDir, statusFile});
                int logLineNumber = 0;
                int tempLineNumber = 0;
                
                OutputStream statusOut = null;
                InputStream logIn = null;
                FileConnection logCon = null;
                String logURI = null;
                
                if (filesToReplicate[i].equals(newLogMade)){
                    //Dont do it. Skip.
                    impl.l(UMLog.DEBUG, 615, null);
                    continue;
                }
                
                if (impl.fileExists(statusFileURI)){
                    impl.l(UMLog.DEBUG, 548, null);
                    //Both temp and log file exists. There are stuff left to scan through, etc
                    
                    //1. get the log lines for temp file
                    //2. get the log lines for log file
                    //3. if log line for temp file less than log file
                    //   3.a. Add a new line and open output stream of 
                    //        temp file from the last line
                    //   3.b. Open InputStream of the log file from the line 
                    //        number of the temp file.
                    
                    logLineNumber = getLineNumber(currentLogFileURI);
                    tempLineNumber = getLineNumber(statusFileURI);
                    
                    if (tempLineNumber < logLineNumber){
                        
                        //Ready the status file: to be written.
                        //get status' file's output stream to append
                        statusOut = impl.openFileOutputStream(statusFileURI, 
                            UstadMobileSystemImpl.FILE_APPEND);
                        
                        //ToDo
                        //get log file to be read.
                        logCon = (FileConnection) Connector.open(currentLogFileURI,
                            Connector.READ);
                        //Get log file's input stream 
                        logIn = logCon.openInputStream();
                        impl.l(UMLog.DEBUG, 548, null);
                        
                        //Send log from log file IS and work on Status file OS
                        int resultCode = sendLog(logIn, statusOut, tempLineNumber);

                        if (logIn != null){
                            logIn.close();
                        }
                        if (logOut != null){
                            logOut.close();
                        }
                        if (statusOut != null){
                            statusOut.flush();
                            statusOut.close();
                        }
                        impl.l(UMLog.DEBUG, 618, null);
                        
                        switch(resultCode) {
                            case 1:
                                impl.removeRecursively(UMFileUtil.joinPaths(
                                new String[]{tincanDir,filesToReplicate[i]}));
                                String statusFileURIDone = statusFileURI + ".done";
                                impl.renameFile(statusFileURI, statusFileURIDone);
                                break;
                                
                            case 2:
                                String tempB4Delete = UMFileUtil.joinPaths(
                                new String[]{tincanDir, filesToReplicate[i] + ".origi"});
                                impl.renameFile(currentLogFileURI, tempB4Delete);
                                impl.renameFile(statusFileURI, currentLogFileURI);
                                impl.removeFile(tempB4Delete);
                                break;
                            
                            default:
                                impl.l(UMLog.DEBUG, 99, null);
                                
                        }
                        
                        continue;
                        
                    }else{
                        impl.l(UMLog.DEBUG,619, null);
                        impl.removeFile(UMFileUtil.joinPaths(new String[] {
                            tincanDir, filesToReplicate[i]}));
                        
                        String statusFileURIDone = statusFileURI + ".done";
                        
                        impl.renameFile(statusFileURI, statusFileURIDone);
                        continue;
                    }
                }
                
                //Create a blank status file if it doesnt exist.
                //If created okay
                impl.l(UMLog.DEBUG, 620, statusFileURI );
                //Ready the status file: to be written.
                //get status' file's output stream
                statusOut = impl.openFileOutputStream(statusFileURI, 0);

                //if nothing in the log file, all done, etc
                if (impl.fileSize(statusFileURI) == 0){
                    impl.l(UMLog.DEBUG, 621, null);
                    J2MEIOUtils.closeConnection(logCon);
                    
                    UMIOUtils.closeOutputStream(statusOut, true);
                    String doneLogName = filesToReplicate[i] + ".done";
                    impl.renameFile(statusFileURI, doneLogName);
                    continue;
                }

                //Get log file's input stream 
                logIn = logCon.openInputStream();
                impl.l(UMLog.DEBUG, 622, null);

                //Send log from log file IS and work on Status file OS
                int resultCode = sendLog(logIn, statusOut);

                UMIOUtils.closeInputStream(logIn);
                UMIOUtils.closeOutputStream(logOut);
                UMIOUtils.closeOutputStream(statusOut, true);
                

                impl.l(UMLog.DEBUG, 623, null);
                if (resultCode == 1){
                    //delete .log, rename .tmp to .done
                    impl.removeFile(UMFileUtil.joinPaths(new String[]{tincanDir, 
                        filesToReplicate[i]}));

                    String statusFileURIDone = statusFileURI + ".done";
                    impl.renameFile(statusFileURI, statusFileURIDone);
                }else if (resultCode == 2){
                    String tempB4Delete = UMFileUtil.joinPaths(new String[]{
                        tincanDir, filesToReplicate[i]}) + ".origi";


                    impl.renameFile(UMFileUtil.joinPaths(new String[]{
                        tincanDir, filesToReplicate[i]}), tempB4Delete);


                    impl.renameFile(statusFileURI, 
                            UMFileUtil.joinPaths(new String[]{tincanDir, 
                                    filesToReplicate[i]}));


                    impl.removeFile(tempB4Delete);
                }else{
                    impl.l(UMLog.ERROR, 98, statusFileURI );
                }

            }
            
        }
        
        
    }
    
    public int sendLog(InputStream logIn, OutputStream statusOut) throws Exception{
        return sendLog(logIn, statusOut, 0);
    }
    
    public int sendLog(InputStream logIn, OutputStream statusOut, 
            int logLineNumber) throws IOException, Exception{
        impl.l(UMLog.DEBUG, 624,null);
        
         if (logLineNumber < 0) {
            impl.l(UMLog.DEBUG, 625, null);
            logLineNumber = 0;
        }
        //send the log - return how many bytes were sent through
        
        // read byte by byte until finding \n ... then
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int b;
        int sent = 0;
        int max_tries = 10;
        int trial=0;
        byte[] lineBytes;
        boolean noErrors = true;
        int currentLineNumber = 0;
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
                    ++currentLineNumber;
                    
                    int return_code = 400;
                    lineBytes = bout.toByteArray(); 
                    //Done with bout
                    bout.close();
                    bout = new ByteArrayOutputStream();
                    if (currentLineNumber <= logLineNumber) {
                        continue;
                    }
                    String logLine = new String(lineBytes);
                    String logLineWOCode="";
                    String doneTries = "";
                    while(trial < max_tries){
                        if(logLine.endsWith(":0") || logLine.endsWith(":2")){
                            //Time to try this.
                            impl.l(UMLog.DEBUG, 626, null);
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
                            impl.l(UMLog.DEBUG, 627, null);
                            //Not a valid line. Put it in the status file
                            logLine = logLine.substring(0, logLine.length()-1);
                            logLine = logLine + "3";
                            lineBytes = logLine.getBytes();
                            statusOut.write(lineBytes);
                            statusOut.write(nline);
                            trial = max_tries + 10;
                            break; //I want to break free
                        }else if (logLine.endsWith(":1")){
                            impl.l(UMLog.DEBUG, 628, null);
                            //Already sent
                            lineBytes = logLine.getBytes();
                            statusOut.write(lineBytes);
                            statusOut.write(nline);
                            break;
                        }else{
                            //Unknown
                            impl.l(UMLog.DEBUG, 629, null);
                            logLine = logLine + ":03";
                            lineBytes = logLine.getBytes();
                            statusOut.write(lineBytes);
                            statusOut.write(nline);
                            break;
                        }
                        
                        statementUsername = logLine.substring(2, logLine.indexOf(":statementstart:"));
                        
                        //on J2ME we don't really need a context to get the current username - it's a static singleton
                        String password = impl.getAppPref("password-"+statementUsername, this);
                        
                        if (statementUsername == null || statementUsername.length() == 0){
                            //Blank username bro
                            impl.l(UMLog.DEBUG, 97, null);
                            logLine = logLine + ":03";
                            lineBytes = logLine.getBytes();
                            statusOut.write(lineBytes);
                            statusOut.write(nline);
                            
                            break;
                        }else{
                            impl.l(UMLog.DEBUG, 630, null);
                        }
                        
                        String encodedUserAndPass="Basic "+ Base64Coder.encodeString(
                            statementUsername + ':'+password);
                        tinCanHeaders.put("Authorization", encodedUserAndPass);

                        String statementString = logLine.substring(
                                logLine.indexOf("statementstart:"),
                                logLine.indexOf(":statementend:"));
                        statementString = statementString.substring("statementstart:".length(), statementString.length());
                        byte[] statementBytes = statementString.getBytes();

//                        String tincanEndpointURL = 
//                                FileUtils.joinPath(UstadMobileDefaults.DEFAULT_XAPI_SERVER, 
//                                        "statements");

                        String tincanEndpointURL = UMFileUtil.joinPaths(new String[]{
                            UstadMobileDefaults.DEFAULT_XAPI_SERVER, "statements"});
                        
                        //        UstadMobileDefaults.DEFAULT_XAPI_STATEMENT_SERVER;
                        //impl.l(UMLog.DEBUG, 558, "POST-in log line" );
                        
                        HTTPResult result = impl.makeRequest(tincanEndpointURL, 
                                tinCanHeaders, null, "POST", statementBytes);
                        
                        //HTTPResult result = HTTPUtils.makeHTTPRequest(tincanEndpointURL,
                        //        null, tinCanHeaders, "POST", statementBytes);
                        return_code = result.getStatus();
                        String serverSays = new String(result.getResponse(), 
                            UstadMobileConstants.UTF8);

                        if (return_code == 200){
                            impl.l(UMLog.DEBUG, 631, null);
                            logLine = logLine.substring(0, logLine.length()-1);
                            logLine = logLine + "1";
                            lineBytes = logLine.getBytes();
                            statusOut.write(lineBytes);
                            statusOut.write(nline);

                            break;
                        }else if (return_code == 401 || 
                            return_code == 404 || return_code == 403){
                            impl.l(UMLog.DEBUG, 632, null);
                            if (trial + 2 > max_tries){
                                impl.l(UMLog.DEBUG, 633, null);
                                noErrors = false;
                                //Maxed out
                                logLine = logLine.substring(0, logLine.length()-1);
                                logLine = logLine + "2";
                                lineBytes = logLine.getBytes();
                                statusOut.write(lineBytes);
                                statusOut.write(nline);
                                trial = trial + 10;
                                break;
                            }
                            trial = trial + 1;
                            //Got to keep on trying 
                        }else{
                            impl.l(UMLog.DEBUG, 634, null);
                            
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
            impl.l(UMLog.DEBUG, 634, null);
            return 1;
        }else{
            impl.l(UMLog.DEBUG, 635, null);
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
            impl.l(UMLog.DEBUG, 636,null);
            transmitQueue();//send the logs up
        } catch (Exception e){
            impl.l(UMLog.DEBUG, 96, null, e);
        }
    }
    
    
    
}
