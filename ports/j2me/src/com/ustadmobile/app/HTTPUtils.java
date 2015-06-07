/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app;

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
    
    public static String sendPost(String url, Hashtable optionalParameters) 
            throws IOException {
        HttpConnection httpConn = null;
        InputStream is = null;
        OutputStream os = null;
        StringBuffer sb = new StringBuffer();
        
        if(url == null){ //Testing..
            url = "http://54.77.18.106:8621/";
        }

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
        ByteArrayOutputStream outputStream = null;
        
        try{
            
            httpURLConnection = (HttpConnection) Connector.open(url);
            httpURLConnection.setRequestMethod(HttpConnection.GET); //Get method
            file = (FileConnection) Connector.open(destDir + filename);
            if (file.exists()) {
                //No need to re create it.
            } else {
                file.create();
            }

            if (httpURLConnection.getResponseCode() != HttpConnection.HTTP_OK) {
                throw new IOException("Request to " + url + " is not HTTP 200 OK");
            }

            inputURLStream = httpURLConnection.openInputStream();
            outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            //int bytesRead = inputURLStream.read(buffer, 0, buffer.length);
            int bytesRead = -1;
            while ((bytesRead = inputURLStream.read(buffer, 0, buffer.length)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                file.openOutputStream().write(buffer);
            }
            
            
            //file.openOutputStream().write(outputStream.toByteArray());

            file.setReadable(true);
            System.gc();
            
        }catch(Exception e){
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
            if(outputStream != null){
                outputStream.close();
            }
        }
        
    }

}
