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
    
    /*AUTO START: This by default without change will be set to 
    True which means that the tincanlog manager will be initiated 
    on init*/
    public static boolean AUTOSTART = true;
    
    /*This is the endpoint that the tincanLog manager will use
    to send/queue the statemnets. We have it here such that we can
    alter it. It should ideally get it from the implementations' 
    getXAPIEndpoint, or explicitly set by test cases
    */
    public static String tincanEndpointURL;
    
    /*
    The destination will be the folder where the log manager works
    against- looking for log files and processing. We have this 
    such that we can give the logmanager differnet folders to fetch
    especially the ability to give a different folder while testing
    */
    public static String tincanDir;
    
    /**
     * Outputstream connected to the current logging output file
     */
    private OutputStream logOut;
    
    /*implementation*/
    UstadMobileSystemImpl impl = null;
    
    /* Log folder name */
    public static final String LOG_FOLDER = "xapi";
    
    /*Send Log file status*/
    public static final int SEND_LOG_SUCCESS = 0;
    public static final int SEND_LOG_ERROR = 1;
    
    /* Maximum Tries for the statement defined here */
    public static final int MAX_TRIES = 10;
    
    /*Status codes */
    public static final String STATEMENT_VALID = "0";
    public static final String STATEMENT_INVALID = "3";
    public static final String STATEMENT_VALID_RETRY = "2";
    public static final String STATEMENT_VALID_SENT = "1";
    public static final String NOT_A_STATEMENT= "03";
    
    /*Log file line bits*/
    public static final String STATEMENT_START_PREFIX = ":statementstart:";
    public static final String STATEMENT_END_SUFFIX = ":statementend:";
    
    /* Log file name bits*/
    public static final String LOG_FILE_PREFIX = "tincan-";
    public static final String LOG_FILE_EXTENSION = ".log";
    public static final String DONE_FILE_EXTENSION = ".done";
    public static final String STATUS_FILE_EXTENSION=".status";
    
    /**
     * Constructor with directory and endpoint as parameter. This will set 
     * where the tincan log manager is supposed to look for log files
     * @param directory
     * @param endpoint 
     */
    public TinCanLogManagerJ2ME(String directory, String endpoint){
        tincanDir = directory;
        tincanEndpointURL = endpoint;
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
    
    /** Appends Zero to the String buffer if number given is less than 0.
     * 
     * @param sb StringBuffer: The string buffer given
     * @param num int: The number 
     * @return StringBuffer: the altered stringbuffer
     */
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
    
    /**
     * Opens a new log file in the log directory. 
     * @return String : the path of the newly created log file
     * @throws IOException 
     */
    public String openNewLog() throws IOException {
        impl.l(UMLog.DEBUG, 608, null);
        boolean success = false;
        StringBuffer logName = new StringBuffer();
        logName.append(LOG_FILE_PREFIX).append(getDateLogStr()
                                                ).append(LOG_FILE_EXTENSION);
        impl.l(UMLog.DEBUG, 609, logName.toString());
       
        String logPath = UMFileUtil.joinPaths(
                new String[]{tincanDir, logName.toString()});
        try{
            /* Open new log file - not appending - new*/
            logOut = impl.openFileOutputStream(logPath, 0);
            currentFile = logPath;
            success = true; //in creating new log file
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
    
    /**
     *  Queues the statement in the current opened log file. It will always 
     * create a new log file if there isn't a current file open (or it has 
     * already been processed)
     * @param userid
     * @param stmt
     * @return true/false of weather the statement went in the log file
     * @throws IOException 
     */
    public boolean queueStatement(String userid, JSONObject stmt) throws IOException {
        impl.l(UMLog.DEBUG, 610,  userid );
        StringBuffer sb = new StringBuffer();
        String status = STATEMENT_INVALID;
        boolean outcome = false;
        if (userid != null && userid.length() > 0){
            status = STATEMENT_VALID;
        }
        
        String stmtStr = stmt.toString();
        sb.append('u').append(':').append(userid).append(STATEMENT_START_PREFIX).append(
                stmtStr).append(STATEMENT_END_SUFFIX).append(status);
        impl.l(UMLog.DEBUG, 540, null);
        if (logOut == null){
            //closing any opened log file
            //UMIOUtils.closeOutputStream(logOut, true);
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
    
    /**
     *  Gets total number of lines in the file given.
     * @param fileURI The file's complete path
     * @return integer of the number of lines in that file
     * @throws IOException 
     */
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
    
    /**
     * Starts the transmission of the log files in the directory and 
     * processes it.
     * 
     * @throws IOException
     * @throws Exception 
     */
    public void transmitQueue() throws IOException, Exception {
        impl.l(UMLog.DEBUG, 611, null);
        
        String newName = "";
        synchronized(this) {
            try {
                //closing any opened log file
                UMIOUtils.closeOutputStream(logOut, true);
                //open a new log:
                newName = openNewLog();
                impl.l(UMLog.DEBUG, 612, newName);
            }catch(Exception e) {
                impl.l(UMLog.DEBUG, 182, newName, e);
                UMIOUtils.closeOutputStream(logOut);
            }
        }
        
        /* Create a new log */
        String newLogMade = UMFileUtil.getFilename(newName);
        
        /* Loop in the log directory file by file. 
        There most likely will be other log files and 
        status files that need processing.
        */
        String[] filesToReplicate = null;
        filesToReplicate = impl.listDirectory(tincanDir);
        impl.l(UMLog.DEBUG, 613, null);
        String[] doneFiles = null;
        
        for(int i = 0; i < filesToReplicate.length; i++) {
            impl.l(UMLog.DEBUG, 614, filesToReplicate[i] );
            
            /* Clean-up : Remove done files from previous rus */
            if(filesToReplicate[i].endsWith(DONE_FILE_EXTENSION)){
                String doneFileURI = UMFileUtil.joinPaths(new String[]{tincanDir,
                    filesToReplicate[i]});
                impl.removeFile(doneFileURI);
                
            }
            /* Process log files */
            else if(filesToReplicate[i].endsWith(LOG_FILE_EXTENSION)) {
                /*If file is a log file..*/
                String currentLogFileURI = UMFileUtil.joinPaths(new String[]{tincanDir, 
                                                filesToReplicate[i]});
                String statusFile = filesToReplicate[i] + STATUS_FILE_EXTENSION;
                String statusFileURI = UMFileUtil.joinPaths(new String[]{tincanDir, 
                                                statusFile});
                
                int logLineNumber = 0;
                int statusLineNumber = 0;
                
                OutputStream statusOut = null;
                InputStream logIn = null;
                FileConnection logCon = null;
                
                if (filesToReplicate[i].equals(newLogMade)){
                    /*The current file is the new log file we just made.
                    Dont do it. Skip this iteration.
                    */
                    impl.l(UMLog.DEBUG, 615, null);
                    continue; //to the next log file
                }
                
                /* Check if the current Log file has anything to work on.
                If its empty then we can mark this log file as "done and
                continue to the next file
                */
                if (impl.fileSize(currentLogFileURI) == 0){
                    impl.l(UMLog.DEBUG, 621, null);
                    String doneLogName = filesToReplicate[i] + ".done";
                    String doneLogNameURI = UMFileUtil.joinPaths(new String[]{tincanDir, 
                                                doneLogName});
                    impl.renameFile(currentLogFileURI, doneLogNameURI);
                    continue;
                }
                
                if (impl.fileExists(statusFileURI)){
                    /*Both status and log file exists. There are statement 
                    logs left to scan through..
                    */
                    impl.l(UMLog.DEBUG, 548, null);
                    
                    /* Logic:
                        1. Get the log lines for status file and the log 
                        file
                        2. If log line for temp file less than log file, start
                           processing the log file and status file.
                        3. Send logs one by one and populate status file till
                           the end of the log file
                        4. a.If all success, delete the .log file,
                                rename the .status file to .log.done
                           b.If some retries needed, make the status file the 
                                .log file and remove the original .log file
                        5. Delete the .done files after log directory processing 
                    */
                    
                    //1. 
                    logLineNumber = getLineNumber(currentLogFileURI);
                    statusLineNumber = getLineNumber(statusFileURI);
                    //2.
                    if (statusLineNumber < logLineNumber){
                        
                        /*Ready the status file: to be written.
                        get status' file's output stream to append
                        */
                        statusOut = impl.openFileOutputStream(statusFileURI, 
                            UstadMobileSystemImpl.FILE_APPEND);
                        
                        //get log file to be read.
                        logCon = (FileConnection) Connector.open(currentLogFileURI,
                            Connector.READ);
                        
                        //Get log file's input stream 
                        logIn = logCon.openInputStream();
                        impl.l(UMLog.DEBUG, 548, null);
                        
                        /*Send statements from log file's inputstream at status
                        file's line number append Status file via outputstream.*/
                        //3.
                        int resultCode = sendLog(logIn, statusOut, statusLineNumber);

                         
                        /* Next steps
                        a. Convert the .log to a .done file.
                           delete the .log file - We dont need it nomore
                        b. If .done file needs to be re sent AND end of line reached:
                           rename the original .log file to .log.origi
                           rename the .done file to .log 
                           delete the original .log.origi */
                        
                        UMIOUtils.closeInputStream(logIn);
                        UMIOUtils.closeOutputStream(logOut);
                        logOut = null;//Added
                        UMIOUtils.closeOutputStream(statusOut, true);
                        
                        impl.l(UMLog.DEBUG, 618, null);
                        
                        switch(resultCode) {
                            //4a.
                            case SEND_LOG_SUCCESS:
                                /* If all statements in log file sent OK, 
                                Move the log file to a .done file
                                */
                                impl.removeRecursively(UMFileUtil.joinPaths(
                                    new String[]{tincanDir,filesToReplicate[i]}));//remove .log
                                String statusFileURIDone = statusFileURI + ".done";
                                impl.renameFile(statusFileURI, statusFileURIDone);//rename .status to .done
                                
                                break;
                            //4b.
                            case SEND_LOG_ERROR:
                                /* If not every statement got sent, 
                                we need the status file with its statuses to be
                                retried in the form of the log file. 
                                */
                                String tempB4Delete = UMFileUtil.joinPaths(
                                new String[]{tincanDir, filesToReplicate[i] + ".origi"});
                                impl.renameFile(currentLogFileURI, tempB4Delete); //rename .log to .log.origi
                                impl.renameFile(statusFileURI, currentLogFileURI);//rename .status to .log
                                impl.removeFile(tempB4Delete);//remove .log.origi
                                break; 
                                
                            default:
                                impl.l(UMLog.DEBUG, 99, null);
                                
                        }
                        continue; // to the next log file
                        
                    }else{
                        /*Status (processed statements) file exists but its 
                        count is equal to its corresponding log- which means all
                        statements from the log file have been processed.
                        We can move the status file in being a done file (no 
                        further processing needed); and eventually deleted*/
                        impl.l(UMLog.DEBUG,619, null);
                        impl.removeFile(UMFileUtil.joinPaths(new String[] {
                            tincanDir, filesToReplicate[i]})); //remove .log file
                        
                        String statusFileURIDone = statusFileURI + ".done";
                        impl.removeFile(statusFileURIDone); //remove .status.done if it exists
                        impl.renameFile(statusFileURI, statusFileURIDone);//rename .status to .status.done
                        continue; // to the next log file.
                    }
                }
                
                /* The Status file does NOT exist for the current Log File */               
                impl.l(UMLog.DEBUG, 620, statusFileURI );
                /*Create the statusFile and get its Output Stream for it to
                be written*/
                statusOut = impl.openFileOutputStream(statusFileURI, 0);
               
                //get log file to be read.
                logCon = (FileConnection) Connector.open(currentLogFileURI,
                    Connector.READ);
    
                //Get log file's input stream 
                logIn = logCon.openInputStream();
                impl.l(UMLog.DEBUG, 622, null);

                //Send log from log file IS and work on Status file OS
                int resultCode = sendLog(logIn, statusOut);

                UMIOUtils.closeInputStream(logIn);
                UMIOUtils.closeOutputStream(logOut);
                logOut = null; //Added
                UMIOUtils.closeOutputStream(statusOut, true);

                impl.l(UMLog.DEBUG, 623, null);
                switch (resultCode) {
                    case SEND_LOG_SUCCESS:
                        //delete .log, rename .tmp to .done
                        impl.removeFile(UMFileUtil.joinPaths(new String[]{tincanDir,
                                            filesToReplicate[i]})); //remove .log
                        String statusFileURIDone = statusFileURI + ".done";
                        impl.renameFile(statusFileURI, statusFileURIDone); //rename .status .log
                        break;
                    case SEND_LOG_ERROR:
                        String tempB4Delete = UMFileUtil.joinPaths(new String[]{
                            tincanDir, filesToReplicate[i]}) + ".origi";
                        impl.renameFile(UMFileUtil.joinPaths(new String[]{
                            tincanDir, filesToReplicate[i]}), tempB4Delete); //rename .log .log.origi
                        impl.renameFile(statusFileURI,
                                UMFileUtil.joinPaths(new String[]{tincanDir, 
                                    filesToReplicate[i]})); //rename .status .log 
                        impl.removeFile(tempB4Delete); //remove .log.origi
                        break;
                    default:
                        impl.l(UMLog.ERROR, 98, statusFileURI );
                        break;
                }
                
            }//end of scan log dir  
            else if(filesToReplicate[i].endsWith(STATUS_FILE_EXTENSION)) {
                /*
                Check if status file is empty. If it is - move it to done- it 
                will get deleted in the next iteration..
                Check if there is log file associated with the status file:
                If yes, skip the log will will be processed with this status file
                If not then rename the status file to log file so next iteration
                can handle it.
                If everything got sent, rename status to done
                
                */
                /*Status file URI..*/
                String currentStatusFileURI = UMFileUtil.joinPaths(new String[]{tincanDir, 
                                                filesToReplicate[i]});
                
                String logFile = filesToReplicate[i].substring(0,
                        filesToReplicate[i].length() - STATUS_FILE_EXTENSION.length());
                String logFileURI = UMFileUtil.joinPaths(new String[]{tincanDir, 
                                                logFile});
                
                /* Check if the current Status file has anything to work on.
                If its empty then we can mark this log file as "done and
                continue to the next file
                */
                if (impl.fileSize(currentStatusFileURI) == 0){
                    impl.l(UMLog.DEBUG, 621, null);
                    String doneLogName = filesToReplicate[i] + ".done";
                    String doneLogNameURI = UMFileUtil.joinPaths(new String[]{tincanDir, 
                                                doneLogName});
                    impl.renameFile(currentStatusFileURI, doneLogNameURI);
                    continue;
                }
                
                if (impl.fileExists(logFileURI)){
                    continue; //We will process the status file when its log file comes
                }else{
                    //Rename status file to log file
                    //Next iteration will check this log file and mark as done
                    //if all the statements have been already sent or will 
                    //process further if more processing needed.
                    impl.renameFile(currentStatusFileURI, logFileURI);
                }
                
                
                
                
            }
        }//end of scan dir
    }
    
    /* sendLog: this method is responsible for sending the logs from 
    * log file's input stream from the start if empty status file. 
    * @param logIn Log file's Input stream
    * @param statusOut Status File's Output stream
    * @return 1  if all good, 2 if not good
    */
    public int sendLog(InputStream logIn, OutputStream statusOut) throws Exception{
        return sendLog(logIn, statusOut, 0);
    }
    
    
    /* sendLog: this method is responsible for sending the logs from 
    * log file's input stream from the start if empty status file. Else it will
    * start from the line number reached by status file and append status file 
    * with status of the logs
    * @param logIn Log file's Input stream
    * @param statusOut Status File's Output stream
    * @param logLineNumber Statu File's line count corresponding to where 
    *                      statements have been sent from the log file.
    * @return SEND_LOG_SUCCESS (0) if all good, SEND_LOG_ERROR (1) if not good
    */
    public int sendLog(InputStream logIn, OutputStream statusOut, 
            int logLineNumber) throws IOException, Exception{
        impl.l(UMLog.DEBUG, 624,null);
        
        /* No Status File line count. Starting from the top */
         if (logLineNumber < 0) {
            impl.l(UMLog.DEBUG, 625, null);
            logLineNumber = 0;
        }
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int b;
        int trial=0;
        byte[] lineBytes;
        boolean noErrors = true;
        int currentLineNumber = 0;
        Hashtable tinCanHeaders = new Hashtable();
        tinCanHeaders.put("X-Experience-API-Version", "1.0.1");
        String statementUsername = "";

        //AppPref.addSetting("password-"+currentUsername, password);
        
        /* Read the whole line */
        while((b = logIn.read()) != -1) {
            trial = 0;
            if(b == nline || b ==cret) {
                ++currentLineNumber;
                int return_code = 400;
                lineBytes = bout.toByteArray(); 
                /*Don't need bout anymore- we got line (bytes) from it*/
                bout.close();
                
                bout = new ByteArrayOutputStream();
                if (currentLineNumber <= logLineNumber) {
                    /* Skip this loop iteration until we're at 
                    the same line as status file*/
                    continue;
                }
                String logLine = new String(lineBytes);

                /* Statement's log line will try to be sent with max tries*/
                RETRIES:
                while(trial < MAX_TRIES){

                    /* Pre-processing
                     * Check statement's Status if worth processing.. */
                    if(logLine.endsWith(":" + STATEMENT_VALID) || 
                            logLine.endsWith(":" + STATEMENT_VALID_RETRY )){                            
                        /* New statement that needs to be sent or retried */
                        impl.l(UMLog.DEBUG, 626, null);

                    }else if (logLine.endsWith(":" + STATEMENT_INVALID)){
                        /* Statement isn't stored properly in the log file
                        as per validation in queueStatement().We wouldnt be 
                        able to send it (eg: no user specified in it).

                        This statement log needs to be put in the status
                        file to maintain line count between log file and 
                        status file. Unless we process these statement logs
                        in this file further, we cannot properly send them. 
                        The statement itself is at fault here.
                        */
                        impl.l(UMLog.DEBUG, 627, null);
                        /*Not a valid line. Put it in the status file */
                        lineBytes = logLine.getBytes();
                        statusOut.write(lineBytes);
                        statusOut.write(nline);
                        trial = MAX_TRIES + 10;
                        break; // out of max retries loop
                    }else if (logLine.endsWith(":" + STATEMENT_VALID_SENT)){
                        /* Statement log already sent */
                        impl.l(UMLog.DEBUG, 628, null);
                        /* Adding to status file */ 
                        lineBytes = logLine.getBytes();
                        statusOut.write(lineBytes);
                        statusOut.write(nline);
                        break; // out of the max retries loop
                    }else if(logLine.endsWith(":" + NOT_A_STATEMENT)){
                        /* Line Doesn't end with anything VALID, INVALID, 
                        VALID_RETRY or VALID_SENT
                        */
                        impl.l(UMLog.DEBUG, 629, null);
                        lineBytes = logLine.getBytes(); 
                        statusOut.write(lineBytes);
                        statusOut.write(nline);
                        break; // out of the max retries loop
                    }else{
                        /* Unknown log line.
                        /* Adding to status file NOT_A_STATEMENT */
                        logLine = logLine + ":" + NOT_A_STATEMENT;
                        impl.l(UMLog.DEBUG, 629, null);
                        lineBytes = logLine.getBytes(); 
                        statusOut.write(lineBytes);
                        statusOut.write(nline);
                        break; //out of the max retries loop
                    }

                    /* Check username in the statement log */
                    statementUsername = logLine.substring(2, logLine.indexOf(STATEMENT_START_PREFIX));                        
                    //on J2ME we don't really need a context to get the current username - it's a static singleton- wtm
                    String password = impl.getAppPref("password-"+statementUsername, this);
                    //NOTE: This wouldn't give desirable results:
                    // if another user is logged in and this log is for some other user
                    // TODO: Account for this. Look up password by username (if not sure like so)

                    if (statementUsername == null || 
                            statementUsername.length() == 0){
                        /*Blank username given*/
                        impl.l(UMLog.DEBUG, 97, null);
                        logLine = logLine + ":" + NOT_A_STATEMENT;
                        lineBytes = logLine.getBytes();
                        statusOut.write(lineBytes);
                        statusOut.write(nline);
                        break; // break out of max tries loop
                    }else{
                        impl.l(UMLog.DEBUG, 630, null);
                    }

                    /* Construct the Request's Authentication */
                    String encodedUserAndPass="Basic "+ Base64Coder.encodeString(
                        statementUsername + ':'+password);
                    tinCanHeaders.put("Authorization", encodedUserAndPass);

                    String statementString = logLine.substring(
                            logLine.indexOf(STATEMENT_START_PREFIX),
                            logLine.indexOf(STATEMENT_END_SUFFIX));
                    statementString = statementString.substring(
                            STATEMENT_START_PREFIX.length(), statementString.length());
                    byte[] statementBytes = statementString.getBytes();

                    /* From constructor we use the endpoint to create the
                    statements end point url*/
                    String tincanStatementsEndpointURL = UMFileUtil.joinPaths(new String[]{
                        tincanEndpointURL, UstadMobileDefaults.DEFAULT_XAPI_STATEMENTS_PATH});
                    HTTPResult result = impl.makeRequest(tincanStatementsEndpointURL, 
                            tinCanHeaders, null, "POST", statementBytes);

                    return_code = result.getStatus();
                    /*String serverSays = new String(result.getResponse(), 
                        UstadMobileConstants.UTF8);*/

                    switch (return_code){
                        case 200: //Success
                            /*Success. Statement sent*/
                            impl.l(UMLog.DEBUG, 631, null);
                            logLine = logLine.substring(0, logLine.length()-1);
                            logLine = logLine + STATEMENT_VALID_SENT;
                            lineBytes = logLine.getBytes();
                            statusOut.write(lineBytes);
                            statusOut.write(nline);
                            break RETRIES;
                        case 400: //Bad Request Could be Stmt/Request
                        case 412: //Precondition Fail    
                            impl.l(UMLog.DEBUG, 641, null);
                            logLine = logLine.substring(0, logLine.length()-2 );
                            logLine = logLine + ":" + NOT_A_STATEMENT;
                            lineBytes = logLine.getBytes();
                            statusOut.write(lineBytes);
                            statusOut.write(nline);
                            break RETRIES;
                        case 401: //UnAuthorized
                        case 403: //Forbidden
                        case 404: //Not Found 
                            impl.l(UMLog.DEBUG, 632, null);
                            if (trial + 2 > MAX_TRIES){
                                /*Tries maxed out*/
                                impl.l(UMLog.DEBUG, 633, null);
                                noErrors = false;
                                logLine = logLine.substring(0, logLine.length()-1);
                                logLine = logLine + STATEMENT_VALID_RETRY;
                                lineBytes = logLine.getBytes();
                                statusOut.write(lineBytes);
                                statusOut.write(nline);
                                trial = trial + 10;
                                break RETRIES; // break out of max tries loop
                            }
                            /* Increment tries */
                            trial = trial + 1;
                            break;
                        case 409: //Coflict
                            /* Conflict in the current statement log */
                            logLine = logLine.substring(0, logLine.length()-1);
                            logLine = logLine + STATEMENT_VALID_SENT;
                            lineBytes = logLine.getBytes();
                            statusOut.write(lineBytes);
                            statusOut.write(nline);
                            break RETRIES; //break out of max tries loop
                        
                        case 500: //Server Error
                            /*Server error. Must keep trying*/
                            break RETRIES;
                        default:
                            
                    }

                }


            }else {
                /*Write character b to Byte array output stream */
                bout.write(b);
            }
            
        }
        
        if (noErrors){
            impl.l(UMLog.DEBUG, 634, null);
            return SEND_LOG_SUCCESS;
        }else{
            impl.l(UMLog.DEBUG, 635, null);
            return SEND_LOG_ERROR;
        }
       
    }

    /**
     * Run method of the log manager
     */
    public void run() {
        try {
            impl.l(UMLog.DEBUG, 636,null);
            transmitQueue();//send the logs up
        } catch (Exception e){
            impl.l(UMLog.DEBUG, 96, null, e);
        }
    }
    
    
    
}
