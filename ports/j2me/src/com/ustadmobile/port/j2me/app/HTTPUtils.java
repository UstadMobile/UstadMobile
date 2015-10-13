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
package com.ustadmobile.port.j2me.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import com.ustadmobile.core.app.Base64;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.URLTextUtil;
import javax.microedition.pim.FieldFullException;

/**
 *
 * @author varuna
 */
public class HTTPUtils {

    public HTTPUtils() {
    }
    
    public static ByteArrayInputStream getHTTPBytes(String url) throws IOException{
        HttpConnection c = null;
        InputStream is = null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        c = (HttpConnection) Connector.open(
                url, Connector.READ, true);
        c.setRequestMethod(HttpConnection.GET); //Get method
        
        
        if (c.getResponseCode() != HttpConnection.HTTP_OK) {
            throw new IOException("Request to " + url + " is not HTTP 200 OK");
        }

        is = c.openInputStream();
        byte[] buf = new byte[1024];
        int bytesRead = -1;
        while ((bytesRead = is.read(buf)) != -1) {
            bout.write(buf, 0, bytesRead);
        }
        is.close();
        is = null;

        ByteArrayInputStream bais = new ByteArrayInputStream(
                bout.toByteArray());

        bout.close();
        return bais;
    }
    
    /***  After setup, attributes of the HttpConnection object can be retrieved 
     * using various get methods.
    ***/
    public static void getConnectionInformation(HttpConnection hc) {

        System.out.println("");
        System.out.println("Request Method for this connection is " + 
                hc.getRequestMethod());
        System.out.println("URL in this connection is " + hc.getURL());
        System.out.println("Protocol for this connection is " +
                hc.getProtocol()); // It better be HTTP:)
        System.out.println("This object is connected to " + hc.getHost() + 
                " host");
        System.out.println("HTTP Port in use is " + hc.getPort());
        System.out.println("Query parameter in this request are  " +
                hc.getQuery());

    }
    
    public static int basicAuth(String url, String username,
            String password, Hashtable headers){
        return basicAuth(url, username, password, headers, false);
    }
    
    public static int basicAuth(String url, String username,
            String password, Hashtable headers, boolean POST){
        
        HttpConnection httpConn = null;
        if(url == null){
            return -1;
        }
        try{
            // Open an HTTP Connection object
            httpConn = (HttpConnection)Connector.open(url);
            // Setup HTTP Request to GET/POST
            if(POST){
                httpConn.setRequestMethod(HttpConnection.POST);
            }else{
                httpConn.setRequestMethod(HttpConnection.GET);
            }
            if (username.equals("") || username.equals(null)){
                return 401;
            }
            String encodedUserAndPass="Basic "+ Base64.encode(username,
                    password);
            httpConn.setRequestProperty("Authorization", encodedUserAndPass);
            Enumeration keys = headers.keys();
            String key, value;
            //boolean firstAmp = true;
            while(keys.hasMoreElements()) {
                    key = keys.nextElement().toString();
                    value = headers.get(key).toString();
                    if(key != "" && value != ""){
                        httpConn.setRequestProperty(key, value);
                    }
            }
            
            int response_code=httpConn.getResponseCode();  
            return response_code;

            
        }catch(IOException e){  
            e.printStackTrace();
        }finally{
            if(httpConn!=null){  
                try {  
                    httpConn.close();  
                } catch (IOException ex) {  
                    ex.printStackTrace();  
                }  
            }  
            
        }
        return -1;
    }
    
    public static void httpDebug(String msg){
        UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.DEBUG, 800, msg);
        
    }
    
    public static String sendPost(String url, Hashtable optionalParameters) 
            throws IOException {
        HttpConnection httpConn = null;
        InputStream is = null;
        OutputStream os = null;
        StringBuffer sb = new StringBuffer();

        try {
            
            // Open an HTTP Connection object
            httpConn = (HttpConnection)Connector.open(url);
            // Setup HTTP Request to POST
            httpConn.setRequestMethod(HttpConnection.POST);

            httpConn.setRequestProperty("User-Agent",
              "Profile/MIDP-1.0 Confirguration/CLDC-1.0");
            httpConn.setRequestProperty("Accept_Language","en-US");
            //Content-Type is must to pass parameters in POST Request
            httpConn.setRequestProperty("Content-Type", 
                    "application/x-www-form-urlencoded");
            
            String params = null;
            Enumeration keys = optionalParameters.keys();
            String key, value;
            boolean firstAmp = true;
            while(keys.hasMoreElements()) {
                    key = keys.nextElement().toString();
                    value = optionalParameters.get(key).toString();
                    if (firstAmp){
                        params = key + "=" + value;
                        firstAmp=false;
                    }else{
                        params = params + "&"+ key + "=" + value;
                    }
            }
            
            //Content-Length to be set
            httpConn.setRequestProperty("Content-length", 
                    String.valueOf(params.getBytes().length));
            
            // This function retrieves the information of this connection
            getConnectionInformation(httpConn);

            os = httpConn.openOutputStream();

            os.write(params.getBytes());

            /**Caution: os.flush() is controversial. It may create unexpected 
                behavior on certain mobile devices. 
                * Try it out for your mobile device **/
            

            //os.flush();
            //os.close();

            // Read Response from the Server

            int response_code=httpConn.getResponseCode();  
            if(response_code==HttpConnection.HTTP_OK){  
                sb.append("Success");
            }  

        } catch(IOException e){  
            sb.append("Network Problem : " + e.getMessage()); 
        }finally{
            if(is!=null){  
                try {  
                    is.close();  
                } catch (IOException ex) {  
                    ex.printStackTrace();  
                }  
            }  
            if(os!=null){  
                try {  
                    os.close();  
                } catch (IOException ex) {  
                    ex.printStackTrace();  
                }  
            }  
        }
        return sb.toString();

    }
    
    public static String getBaseName(String url){
        int pos = url.lastIndexOf('/');
        return url.substring(pos + 1);
    }
    
    public static void downloadURLToFile(String url, String destDir, 
            String filename) throws Exception {
        //Validate the url
        
        //Validate the filename
        if(filename.equals("") || filename.equals(null)){
            filename = FileUtils.getBaseName(url);
        }
        
        HttpConnection httpURLConnection = null;
        Exception exception=null;
        FileConnection file = null;
        InputStream inputURLStream = null;
        OutputStream fileOutputStream = null;
        String fileToCreate="";
        try{
            
            httpURLConnection = (HttpConnection) Connector.open(url);
            httpURLConnection.setRequestMethod(HttpConnection.GET); //Get method

            fileToCreate = FileUtils.joinPath(destDir, filename);

            file = (FileConnection) 
                    Connector.open(fileToCreate, Connector.READ_WRITE);
            
            if(file.exists()) {
                
                //ToDo: Handle resume and or delete existing
                file.delete();
                file.create();
                
                //fileOutputStream = file.openOutputStream(file.fileSize());
                fileOutputStream = file.openOutputStream();
                
            }else {
                file.create();
                fileOutputStream = file.openOutputStream();
            }
            
            if (httpURLConnection.getResponseCode() != HttpConnection.HTTP_OK){
                throw new IOException("Request to " + url + 
                        " is not HTTP 200 OK");
            }
            
            inputURLStream = httpURLConnection.openInputStream();
            byte[] buffer = new byte[1024];

            //int bytesRead = inputURLStream.read(buffer, 0, buffer.length);
            int bytesRead = -1;
            while ((bytesRead = inputURLStream.read(buffer, 0, buffer.length)) != -1) {
            //while ((bytesRead = inputURLStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            
            System.gc();
            
        }catch(Exception e){
            e.printStackTrace();
            exception.equals(e);
        }finally{
            if(exception != null){
                throw exception;
                //Throw Exception
            }
            if (httpURLConnection != null){
                httpURLConnection.close();
            }
            if ( file != null){
                file.close();
            }
            if (inputURLStream != null){
                inputURLStream.close();
            }            
            if (fileOutputStream != null){
                fileOutputStream.close();
            }
        }
        
    }

}
