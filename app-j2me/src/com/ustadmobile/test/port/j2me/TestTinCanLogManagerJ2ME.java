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
package com.ustadmobile.test.port.j2me;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.Base64Coder;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.port.j2me.app.AppPref;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import com.ustadmobile.port.j2me.impl.xapi.TinCanLogManagerJ2ME;
import com.ustadmobile.port.j2me.util.J2MEIOUtils;
import com.ustadmobile.test.core.TestConstants;
import com.ustadmobile.test.core.UMContextGetter;
import j2meunit.framework.TestCase;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.m3g.Appearance;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 *
 * @author varuna
 */
public class TestTinCanLogManagerJ2ME extends TestCase {
    public TestTinCanLogManagerJ2ME(){
        setName("Test TinCan Log Manager Test");
    }
    
    public JSONObject makeRandomStatement(String username){
        return makeRandomStatement(username, false);
    }
    
    public JSONObject makeRandomStatement(String username, boolean invalid){
        JSONObject stmtObject = new JSONObject();
        String tinCanID= "epub:J2ME-Testing";
        String duration = "PT0H0M1S";
        String name = "UnitTesting";
        String desc = "This is the J2ME Unit tests running..";
        String xAPIServer = UstadMobileDefaults.DEFAULT_XAPI_SERVER;
        JSONObject actorObj = null;
        try {
            actorObj = new JSONObject();
            JSONObject accountObj = new JSONObject();
            accountObj.put("homePage", xAPIServer);
            accountObj.put("name", username);
            actorObj.put("account", accountObj);
            actorObj.put("objectType", "Agent");
        }catch(JSONException e) {
            //seriously... this should never happen putting strings together
            UstadMobileSystemImpl.l(UMLog.ERROR, 195, null, e);
        }
        
        try {
            stmtObject.put("actor", actorObj);
            
            JSONObject activityDef = new JSONObject();
            activityDef.put("type", "http://adlnet.gov/expapi/activities/module");
            if (invalid){
                activityDef.put("name", name);
                activityDef.put("description", desc);
            }
            
            JSONObject objectDef = new JSONObject();
            objectDef.put("id", tinCanID);
            objectDef.put("definition", activityDef);
            objectDef.put("objectType", "Activity");
            stmtObject.put("object", objectDef);

            
            JSONObject verbDef = new JSONObject();
            verbDef.put("id", "http://adlnet.gov/expapi/verbs/experienced");
            JSONObject verbDisplay = new JSONObject();
            verbDisplay.put("en-US", "experienced");
            verbDef.put("display", verbDisplay);
            stmtObject.put("verb", verbDef);
            
            String stmtDuration = duration;
            JSONObject resultDef = new JSONObject();
            resultDef.put("duration", stmtDuration);
            stmtObject.put("result", resultDef);
            
            String newUUID = UMTinCanUtil.generateUUID();
            try {
                //stmt.append("id", UMTinCanUtil.generateUUID());
                stmtObject.put("id", newUUID);
            } catch (JSONException ex) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 639 , null, ex);
            }
            
        }catch(JSONException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 199, tinCanID, e);
        }
        //String stmt = stmtObject.toString(); //for testing..
        return stmtObject;
        
    }
    
    public void runTest() throws Throwable{
        /*Before we run the init method, we need to stop the log manager
        timer tasks to run when testing tincan statemnets.*/
        TinCanLogManagerJ2ME.AUTOSTART = false;
        
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.SCHEDULE_DELAY = 10*1000; //run every 10 seconds
        Object context = UMContextGetter.getContext(this);
        impl.init(context);
        impl.setActiveUser(TestConstants.LOGIN_USER, context);
        impl.setActiveUserAuth(TestConstants.LOGIN_PASS, context);
   
        /* Set up the tincanlogmanager */
        String deviceDir = UstadMobileSystemImplJ2ME.getInstanceJ2ME().findSystemBaseDir();
        String testLogDir = UMFileUtil.joinPaths(new String[]{deviceDir, "test-xapi"});
        impl.removeRecursively(testLogDir);
        impl.makeDirectory(testLogDir);
        
        String correctEndpoint = UstadMobileDefaults.DEFAULT_XAPI_SERVER;
        String incorrectEndpoint = UstadMobileDefaults.DEFAULT_XAPI_SERVER + "wrong_url";
        
        Hashtable tinCanHeaders = new Hashtable();
        tinCanHeaders.put("X-Experience-API-Version", "1.0.1");
        /* Construct the Request's Authentication */
        String encodedUserAndPass="Basic "+ Base64Coder.encodeString(
            TestConstants.LOGIN_USER + ':'+TestConstants.LOGIN_PASS);
        tinCanHeaders.put("Authorization", encodedUserAndPass);
        
        TinCanLogManagerJ2ME tinManager = new TinCanLogManagerJ2ME(testLogDir, correctEndpoint);
        //tinManager.transmitQueue();
        
        
        /* TEST CASES */
        
        /* 1. Send 5 random statemets. Verify that they were actually sent */
//        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER)); // * 5
//        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER));
//        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER));
//        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER));
//        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER));
//        
//        tinManager.transmitQueue();
//        
//        /*Get the UUIDs of the stuff sent */
        String [] filesInTestLogDir;
        
        filesInTestLogDir = impl.listDirectory(testLogDir);
        impl.l(UMLog.DEBUG, 613, null);
        String[] doneFiles = null;
        InputStream logIn = null;
        FileConnection logCon = null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int b;
        byte[] lineBytes;
        Vector doneUUIDs= new Vector();
        String[] doneUUIDsString;
//        
//        for(int i = 0; i < filesInTestLogDir.length; i++) {
//            /* Process done log files */
//            if(filesInTestLogDir[i].endsWith(".done")) {
//                String currentLogFileURI = UMFileUtil.joinPaths(new String[]{testLogDir, 
//                                                filesInTestLogDir[i]});
//                /*Read the log file line by line and get the UUID */
//                //get log file to be read.
//                logCon = (FileConnection) Connector.open(currentLogFileURI,
//                    Connector.READ);
//
//                //Get log file's input stream 
//                logIn = logCon.openInputStream();
//                
//                /* Read the whole line */
//                while((b = logIn.read()) != -1) {
//                    
//                    if(b == '\n' || b == '\r') {
//                        lineBytes = bout.toByteArray(); 
//                        /*Don't need bout anymore- we got line (bytes) from it*/
//                        bout.close();
//
//                        bout = new ByteArrayOutputStream();
//                        String logLine = new String(lineBytes);
//                        String statementString = logLine.substring(
//                            logLine.indexOf(":statementstart:"),
//                            logLine.indexOf(":statementend:"));
//                        statementString = statementString.substring(
//                            ":statementstart:".length(), statementString.length());
//                        
//                        JSONObject stmt = new JSONObject(statementString);
//                        String stmtString = stmt.toString();
//                        String id = stmt.getString("id");
//                        doneUUIDs.addElement(id);
//                        
//                    }else{
//                        /*Write character b to Byte array output stream */
//                        bout.write(b);
//                    }
//                }
//                UMIOUtils.closeInputStream(logIn);
//                J2MEIOUtils.closeConnection(logCon);
//                //logCon.close(); //Temp disabled to reuse ?
//                UMIOUtils.closeOutputStream(bout);                
//            }
//            UMIOUtils.closeOutputStream(bout);  
//        }
        
//        doneUUIDsString = new String[doneUUIDs.size()];
//        doneUUIDs.copyInto(doneUUIDsString);
        String checkStatementId = "statementId";
        String url;
        int statusCode;
        boolean allStatementsSent = false;
        
//        for(int i=0; i<doneUUIDsString.length;i++){
//            statusCode=0;
//            url = UstadMobileDefaults.DEFAULT_XAPI_SERVER + "/statements/" + "?" + checkStatementId + "=" + doneUUIDsString[i];
//            HTTPResult result = impl.makeRequest(url, tinCanHeaders, null);
//            statusCode = result.getStatus();
//            switch (statusCode){
//                case 200:
//                    allStatementsSent = true;
//                    break;
//                default:
//                    allStatementsSent = false;       
//            }
//        }
//
//        assertTrue("Valid statements Sent and Received on Server", allStatementsSent);
 
//        /*2. Send a Mixture of Good and Bad statements, check status/log file for codes */
//        
//        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER, true)); // * 5
//        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER, false));
//        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER, true));
//        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER, false));
//        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER, true));
//        
//        tinManager.transmitQueue();
//        doneFiles = null;
//        logIn = null;
//        logCon = null;
//        bout = new ByteArrayOutputStream();
//
//        boolean goodStatementsSent = false;
//        boolean badStatementsNotSent = false;
//        filesInTestLogDir = impl.listDirectory(testLogDir);

//        for(int i = 0; i < filesInTestLogDir.length; i++) {
//            /* Process done log files */
//            if(filesInTestLogDir[i].endsWith(".done")) {
//                String currentLogFileURI = UMFileUtil.joinPaths(new String[]{testLogDir, 
//                                                filesInTestLogDir[i]});
//                /*Read the log file line by line and get the UUID */
//                //get log file to be read.
//                logCon = (FileConnection) Connector.open(currentLogFileURI,
//                    Connector.READ);
//
//                //Get log file's input stream 
//                logIn = logCon.openInputStream();
//                
//                /* Read the whole line */
//                while((b = logIn.read()) != -1) {
//                    
//                    if(b == '\n' || b == '\r') {
//                        lineBytes = bout.toByteArray(); 
//                        /*Don't need bout anymore- we got line (bytes) from it*/
//                        bout.close();
//
//                        bout = new ByteArrayOutputStream();
//                        String logLine = new String(lineBytes);
//                        String statementString = logLine.substring(
//                            logLine.indexOf(":statementstart:"),
//                            logLine.indexOf(":statementend:"));
//                        statementString = statementString.substring(
//                            ":statementstart:".length(), statementString.length());
//                        
//                        JSONObject stmt = new JSONObject(statementString);
//                        String stmtString = stmt.toString();
//                        String id = stmt.getString("id");
//                        if (logLine.endsWith(":1")){ //Send OK
//                            //Supposed to be sent okay
//                            goodStatementsSent = false;
//                            statusCode=0;
//                            url = UstadMobileDefaults.DEFAULT_XAPI_SERVER + "/statements/" + "?" + checkStatementId + "=" + id;
//                            HTTPResult result = impl.makeRequest(url, tinCanHeaders, null);
//                            statusCode = result.getStatus();
//                            switch (statusCode){
//                                case 200:
//                                    goodStatementsSent = true;
//                                    break;
//                                default:
//                                    allStatementsSent = false;       
//                            }
//                           
//                        }else if(logLine.endsWith(":03")){ //Not sent
//                            //Supposed to not be sent
//                            badStatementsNotSent = false;
//                            statusCode=0;
//                            url = UstadMobileDefaults.DEFAULT_XAPI_SERVER + "/statements/" + "?" + checkStatementId + "=" + id;
//                            HTTPResult result = impl.makeRequest(url, tinCanHeaders, null);
//                            statusCode = result.getStatus();
//                            switch (statusCode){
//                                case 200:
//                                    break;
//                                default:
//                                    badStatementsNotSent = true;       
//                            }
//                        }
//                        
//                        
//                        
//                    }else{
//                        /*Write character b to Byte array output stream */
//                        bout.write(b);
//                    }
//                }
//                UMIOUtils.closeInputStream(logIn);
//                logIn = null;
//                logCon.close();
//                logCon = null;
//            }
//        }
//        
//        assertTrue("Good statements sent", goodStatementsSent);
//        assertTrue("Bad statements not sent", badStatementsNotSent);

        /*3. Wrong server, try again kind of thing */
        TinCanLogManagerJ2ME tinManager2 = new TinCanLogManagerJ2ME(testLogDir, incorrectEndpoint);
        
        tinManager2.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER)); // * 5
        tinManager2.transmitQueue();
        
        doneFiles = null;
        logIn = null;
        logCon = null;
        bout = new ByteArrayOutputStream();
        boolean statementDoesntExist = false;
        filesInTestLogDir = impl.listDirectory(testLogDir);
        
        for(int i = 0; i < filesInTestLogDir.length; i++) {
            /* Process done log files */
            if(filesInTestLogDir[i].endsWith(".log")) {
                String currentLogFileURI = UMFileUtil.joinPaths(new String[]{testLogDir, 
                                                filesInTestLogDir[i]});
                
                if (impl.fileSize(currentLogFileURI) == 0){
                    impl.l(UMLog.DEBUG, 621, null);
                    String doneLogName = filesInTestLogDir[i] + ".done";
                    String doneLogNameURI = UMFileUtil.joinPaths(new String[]{testLogDir, 
                                                doneLogName});
                    impl.renameFile(currentLogFileURI, doneLogNameURI);
                    continue;
                }
                
                /*Read the log file line by line and get the UUID */
                //get log file to be read.
                logCon = (FileConnection) Connector.open(currentLogFileURI,
                    Connector.READ);

                //Get log file's input stream 
                logIn = logCon.openInputStream();
                
                /* Read the whole line */
                while((b = logIn.read()) != -1) {
                    
                    if(b == '\n' || b == '\r') {
                        lineBytes = bout.toByteArray(); 
                        /*Don't need bout anymore- we got line (bytes) from it*/
                        bout.close();

                        bout = new ByteArrayOutputStream();
                        String logLine = new String(lineBytes);
                        String statementString = logLine.substring(
                            logLine.indexOf(":statementstart:"),
                            logLine.indexOf(":statementend:"));
                        statementString = statementString.substring(
                            ":statementstart:".length(), statementString.length());
                        
                        JSONObject stmt = new JSONObject(statementString);
                        String stmtString = stmt.toString();
                        String id = stmt.getString("id");
                        
                        if (logLine.endsWith(":2")){ //Gotta retry
                            statusCode=0;
                            url = UstadMobileDefaults.DEFAULT_XAPI_SERVER + "/statements/" + "?" + checkStatementId + "=" + id;
                            HTTPResult result = impl.makeRequest(url, tinCanHeaders, null);
                            String response = new String(result.getResponse());
                            statusCode = result.getStatus();
                            switch (statusCode){
                                case 404:
                                    statementDoesntExist = true;
                                    break;
                                default:
                                    statementDoesntExist = false;       
                            }
                           
                        }
                        
                    }else{
                        /*Write character b to Byte array output stream */
                        bout.write(b);
                    }
                }
                UMIOUtils.closeInputStream(logIn);
                logCon.close();
                UMIOUtils.closeOutputStream(bout);                
            }
            UMIOUtils.closeOutputStream(bout);  
        }
        
        assertTrue("No internet: Statement Not in cloud", statementDoesntExist);
        
        
        tinManager2.tincanEndpointURL = correctEndpoint;
        tinManager2.transmitQueue();
        
        doneFiles = null;
        logIn = null;
        logCon = null;
        bout = new ByteArrayOutputStream();
        boolean statementDoesExist = false;
        filesInTestLogDir = impl.listDirectory(testLogDir);
        
        for(int i = 0; i < filesInTestLogDir.length; i++) {
            /* Process done log files */
            if(filesInTestLogDir[i].endsWith(".done")) {
                String currentLogFileURI = UMFileUtil.joinPaths(new String[]{testLogDir, 
                                                filesInTestLogDir[i]});
                
                /* Remove empty done file*/
                if (impl.fileSize(currentLogFileURI) == 0){
                    String doneLogName = filesInTestLogDir[i];
                    String doneLogNameURI = UMFileUtil.joinPaths(new String[]{testLogDir, 
                                                doneLogName});
                    impl.removeFile(doneLogNameURI);
                    continue;
                }
                
                /*Read the log file line by line and get the UUID */
                //get log file to be read.
                logCon = (FileConnection) Connector.open(currentLogFileURI,
                    Connector.READ);

                //Get log file's input stream 
                logIn = logCon.openInputStream();
                
                /* Read the whole line */
                while((b = logIn.read()) != -1) {
                    
                    if(b == '\n' || b == '\r') {
                        lineBytes = bout.toByteArray(); 
                        /*Don't need bout anymore- we got line (bytes) from it*/
                        bout.close();

                        bout = new ByteArrayOutputStream();
                        String logLine = new String(lineBytes);
                        String statementString = logLine.substring(
                            logLine.indexOf(":statementstart:"),
                            logLine.indexOf(":statementend:"));
                        statementString = statementString.substring(
                            ":statementstart:".length(), statementString.length());
                        
                        JSONObject stmt = new JSONObject(statementString);
                        String stmtString = stmt.toString();
                        String id = stmt.getString("id");
                        
                        if (logLine.endsWith(":1")){ //Succeeded!
                            statusCode=0;
                            url = UstadMobileDefaults.DEFAULT_XAPI_SERVER + "/statements/" + "?" + checkStatementId + "=" + id;
                            HTTPResult result = impl.makeRequest(url, tinCanHeaders, null);
                            String response = new String(result.getResponse());
                            statusCode = result.getStatus();
                            switch (statusCode){
                                case 200:
                                    statementDoesExist = true;
                                    break;
                                default:
                                    statementDoesExist = false;       
                            }
                           
                        }
                        
                    }else{
                        /*Write character b to Byte array output stream */
                        bout.write(b);
                    }
                }
                UMIOUtils.closeInputStream(logIn);
                logCon.close();
                UMIOUtils.closeOutputStream(bout);                
            }
            UMIOUtils.closeOutputStream(bout);  
        }
        
        assertTrue("Internet: Statement Now Exists in the cloud", statementDoesExist);
        
        
        
        /* 1. Send 5 random statemets. Verify that they were actually sent */
        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER)); // * 5
        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER));
        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER));
        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER));
        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER));
        
        tinManager.transmitQueue();
      
        /*Get the UUIDs of the stuff sent */
        filesInTestLogDir = impl.listDirectory(testLogDir);
        
        for(int i = 0; i < filesInTestLogDir.length; i++) {
            /* Process done log files */
            if(filesInTestLogDir[i].endsWith(".done")) {
                String currentLogFileURI = UMFileUtil.joinPaths(new String[]{testLogDir, 
                                                filesInTestLogDir[i]});
                /*Read the log file line by line and get the UUID */
                //get log file to be read.
                logCon = (FileConnection) Connector.open(currentLogFileURI,
                    Connector.READ);

                //Get log file's input stream 
                logIn = logCon.openInputStream();
                
                /* Read the whole line */
                while((b = logIn.read()) != -1) {
                    
                    if(b == '\n' || b == '\r') {
                        lineBytes = bout.toByteArray(); 
                        /*Don't need bout anymore- we got line (bytes) from it*/
                        bout.close();

                        bout = new ByteArrayOutputStream();
                        String logLine = new String(lineBytes);
                        String statementString = logLine.substring(
                            logLine.indexOf(":statementstart:"),
                            logLine.indexOf(":statementend:"));
                        statementString = statementString.substring(
                            ":statementstart:".length(), statementString.length());
                        
                        JSONObject stmt = new JSONObject(statementString);
                        String stmtString = stmt.toString();
                        String id = stmt.getString("id");
                        doneUUIDs.addElement(id);
                        
                    }else{
                        /*Write character b to Byte array output stream */
                        bout.write(b);
                    }
                }
                UMIOUtils.closeInputStream(logIn);
                J2MEIOUtils.closeConnection(logCon);
                //logCon.close(); //Temp disabled to reuse ?
                UMIOUtils.closeOutputStream(bout);                
            }
            UMIOUtils.closeOutputStream(bout);  
        }
        
        doneUUIDsString = new String[doneUUIDs.size()];
        doneUUIDs.copyInto(doneUUIDsString);
        checkStatementId = "statementId";
        
        
        for(int i=0; i<doneUUIDsString.length;i++){
            statusCode=0;
            url = UstadMobileDefaults.DEFAULT_XAPI_SERVER + "/statements/" + "?" + checkStatementId + "=" + doneUUIDsString[i];
            HTTPResult result = impl.makeRequest(url, tinCanHeaders, null);
            statusCode = result.getStatus();
            switch (statusCode){
                case 200:
                    allStatementsSent = true;
                    break;
                default:
                    allStatementsSent = false;       
            }
        }

        assertTrue("Valid statements Sent and Received on Server", allStatementsSent);
 
        /*2. Send a Mixture of Good and Bad statements, check status/log file for codes */
        
        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER, true)); // * 5
        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER, false));
        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER, true));
        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER, false));
        tinManager.queueStatement(TestConstants.LOGIN_USER, makeRandomStatement(TestConstants.LOGIN_USER, true));
        
        tinManager.transmitQueue();
        doneFiles = null;
        logIn = null;
        logCon = null;
        bout = new ByteArrayOutputStream();

        boolean goodStatementsSent = false;
        boolean badStatementsNotSent = false;
        filesInTestLogDir = impl.listDirectory(testLogDir);

        for(int i = 0; i < filesInTestLogDir.length; i++) {
            /* Process done log files */
            if(filesInTestLogDir[i].endsWith(".done")) {
                String currentLogFileURI = UMFileUtil.joinPaths(new String[]{testLogDir, 
                                                filesInTestLogDir[i]});
                /*Remove empty done files*/
                if (impl.fileSize(currentLogFileURI) == 0){
                    impl.l(UMLog.DEBUG, 621, null);
                    String doneLogName = filesInTestLogDir[i];
                    String doneLogNameURI = UMFileUtil.joinPaths(new String[]{testLogDir, 
                                                doneLogName});
                    impl.removeFile(doneLogNameURI);
                    continue;
                }
                /*Read the log file line by line and get the UUID */
                //get log file to be read.
                logCon = (FileConnection) Connector.open(currentLogFileURI,
                    Connector.READ);

                //Get log file's input stream 
                logIn = logCon.openInputStream();
                
                /* Read the whole line */
                while((b = logIn.read()) != -1) {
                    
                    if(b == '\n' || b == '\r') {
                        lineBytes = bout.toByteArray(); 
                        /*Don't need bout anymore- we got line (bytes) from it*/
                        bout.close();

                        bout = new ByteArrayOutputStream();
                        String logLine = new String(lineBytes);
                        String statementString = logLine.substring(
                            logLine.indexOf(":statementstart:"),
                            logLine.indexOf(":statementend:"));
                        statementString = statementString.substring(
                            ":statementstart:".length(), statementString.length());
                        
                        JSONObject stmt = new JSONObject(statementString);
                        String stmtString = stmt.toString();
                        String id = stmt.getString("id");
                        if (logLine.endsWith(":1")){ //Send OK
                            //Supposed to be sent okay
                            goodStatementsSent = false;
                            statusCode=0;
                            url = UstadMobileDefaults.DEFAULT_XAPI_SERVER + "/statements/" + "?" + checkStatementId + "=" + id;
                            HTTPResult result = impl.makeRequest(url, tinCanHeaders, null);
                            statusCode = result.getStatus();
                            switch (statusCode){
                                case 200:
                                    goodStatementsSent = true;
                                    break;
                                default:
                                    allStatementsSent = false;       
                            }
                           
                        }else if(logLine.endsWith(":03")){ //Not sent
                            //Supposed to not be sent
                            badStatementsNotSent = false;
                            statusCode=0;
                            url = UstadMobileDefaults.DEFAULT_XAPI_SERVER + "/statements/" + "?" + checkStatementId + "=" + id;
                            HTTPResult result = impl.makeRequest(url, tinCanHeaders, null);
                            statusCode = result.getStatus();
                            switch (statusCode){
                                case 200:
                                    break;
                                default:
                                    badStatementsNotSent = true;       
                            }
                        }
                        
                        
                        
                    }else{
                        /*Write character b to Byte array output stream */
                        bout.write(b);
                    }
                }
                UMIOUtils.closeInputStream(logIn);
                logIn = null;
                logCon.close();
                logCon = null;
            }
        }
        
        assertTrue("Good statements sent", goodStatementsSent);
        assertTrue("Bad statements not sent", badStatementsNotSent);

        
        assertEquals("Simple Test OK", 2, 1+1);
    }
}
