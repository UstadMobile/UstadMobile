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

import com.ustadmobile.core.impl.UstadMobileConstants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import org.json.me.*;

/**
 *
 * @author mike
 */
public class TinCanLogManagerJ2ME {
    
    String currentFile;
    
    static int nline = (int)'\n';
    
    static int cret = (int)'\r';
    
    /**
     * Outputstream connected to the current logging output file
     */
    private OutputStream logOut;
    
    public TinCanLogManagerJ2ME() {
        
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
    
    public void openNewLog() throws IOException {
        StringBuffer logName = new StringBuffer();
        logName.append("tincan-").append(getDateLogStr()).append(".log");
        
        // logOut = ... open file output stream 
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
            //log me etc
        }
        
        
        return false;
    }
    
    public void transmitQueue() {
        synchronized(this) {
            try {
                logOut.flush();
                logOut.close();
                openNewLog();
            }catch(IOException e) {
                //log me etc.
            }
        }
        
        String[] filesToReplicate = null;
        //filesToReplicate = listDir(appdir /tincan);
        
        for(int i = 0; i < filesToReplicate.length; i++) {
            if(filesToReplicate[i].endsWith(".log")) {
                int readFrom = 0;
            
                //check and see if there is a .log.position file
                
            }
            
        }
        
    }
    
    public int sendLog(InputStream logIn) throws IOException{
        //send the log - return how many bytes were sent through
        
        // read byte by byte until finding \n ... then
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int b;
        int sent = 0;
        byte[] lineBytes;
        while((b = logIn.read()) != -1) {
            if(b == nline || b ==cret) {
                lineBytes = bout.toByteArray();
                String logLine = new String(lineBytes);
                
                //try and send it to the XAPI server using HTTP
                
                //if successful
                sent += (lineBytes.length + 1); //to include the newline char
            }else {
                bout.write(b);
            }
        }
        
        
        
        
        return sent;
    }
    
    
    
    
}
