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

package com.ustadmobile.core.util;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipFileHandle;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author mike
 */
public class UMIOUtils {
    
    public static final int HTTP_SIZE_NOT_GIVEN = -1;
    
    public static final int HTTP_SIZE_IO_EXCEPTION = -2;

    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
    
    /**
     * Close the given input stream if not null
     * 
     * @param in An input stream to close
     */
    public static final void closeInputStream(InputStream in) {
        try {
            if(in != null) {
                in.close();
            }
        }catch(IOException e) {
        }
    }
    
    /**
     * Close the given output stream if not null
     * 
     * @param out An input stream to close
     * @param flush if true - flush the stream before closing
     */
    public static final void closeOutputStream(OutputStream out, boolean flush) {
        try {
            if(out != null) {
                if(flush)
                    out.flush();
                out.close();
            }
        }catch(IOException e) {
            
        }
    }
    
    /**
     * Close the output stream give if not null.  Will not flush the output stream:
     *  e.g. the same as calling closeOutputSteam(out, false)
     * 
     * @param out 
     */
    public static final void closeOutputStream(OutputStream out) {
        closeOutputStream(out, false);
    }
    
    /**
     * Close the given ZipHandle
     * 
     * @param zip The ZipFileHandle to close
     */
    public static final void closeZipFileHandle(ZipFileHandle zip) {
        try {
            if(zip != null) {
                zip.close();
            }
        }catch(IOException e) {
            
        }
    }
    
    /**
     * Read from the given input stream and write to the given output stream.  
     * This will not close the streams themselves
     */
    public static final void readFully(InputStream in, OutputStream out, int bufsize) throws IOException{
        byte[] buf = new byte[bufsize];
        int bytesRead;
        
        while((bytesRead = in.read(buf)) != -1) {
            out.write(buf, 0, bytesRead);
        }
        out.flush();
    }

    public static final void readFully(InputStream in, OutputStream out) throws IOException{
        readFully(in, out, DEFAULT_BUFFER_SIZE);
    }

    public static final String readStreamToString(InputStream in, int bufsize) throws IOException{
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        readFully(in, bout, bufsize);
        in.close();

        return new String(bout.toByteArray(), "UTF-8");
    }

    public static final String readStreamToString(InputStream in) throws IOException{
        return readStreamToString(in, DEFAULT_BUFFER_SIZE);
    }

    public static final byte[] readStreamToByteArray(InputStream in, int bufsize) throws IOException{
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        readFully(in, bout, bufsize);
        return bout.toByteArray();
    }

    public static final byte[] readStreamToByteArray(InputStream in) throws IOException {
        return readStreamToByteArray(in, DEFAULT_BUFFER_SIZE);
    }



    /**
     * Read from the given input stream and return a string
     *
     * @param in Input Stream to read from
     * @param encoding Encoding to use
     * @return String from the given input stream in the given encoding
     * @throws IOException
     */
    public static final String readToString(InputStream in, String encoding) throws IOException{
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        readFully(in, bout, 1024);
        in.close();
        return new String(bout.toByteArray(), encoding);
    }

    
    public static final void throwIfNotNullIO(IOException e) throws IOException{
        if(e != null) {
            throw e;
        }
    }
    
    public static final void throwIfNotNullXPE(XmlPullParserException xe) throws XmlPullParserException {
        if(xe != null) {
            throw xe;
        }
    }
    
    
    /**
     * Logs and throws the given exception if it is not null
     * 
     * @param e Exception (Null if no exception happened)
     * @param level Exception level 
     * @param code Exception code
     * @param message Message if any
     * 
     * @see UMLog
     * @throws IOException 
     */
    public static final void logAndThrowIfNotNullIO(IOException e, int level, int code, String message) throws IOException{
        if(e != null) {
            UstadMobileSystemImpl.l(level, code, message, e);
            throw e;
        }
    }
    
    public static final void throwIfNotNull(Exception e) throws Exception {
        if(e != null) {
            throw e;
        }
    }
    
    /**
     * Tests to see if it is possible to make a child file in the given directory
     * by creating a small text file called umtestfile.txt with the content "OK"
     * 
     * This might be needed when fileconnection.canWrite returns an incorrect 
     * value for whether or not the directory itself is writable for files.
     * 
     * @param dirURI Directory to test
     * @return true if possible to create child files, false otherwise
     */
    public static boolean canWriteChildFile(String dirURI) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        boolean canWriteChild = false;
        try {
            String childFileURI = UMFileUtil.joinPaths(new String[] {dirURI,
                "umtestfile.txt"});
            impl.writeStringToFile("OK", childFileURI, UstadMobileConstants.UTF8);
            canWriteChild = true;
            impl.removeFile(childFileURI);
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.INFO, 353, dirURI);
        }
        return canWriteChild;
    }
    
}
