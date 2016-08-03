/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.sharedse.impl;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

/**
 *
 * @author mike
 */
public abstract class UstadMobileSystemImplSE extends UstadMobileSystemImpl {


    /**
     * @inheritDoc
     */
    @Override
    public HTTPResult makeRequest(String httpURL, Hashtable headers, Hashtable postParams, String method, byte[] postBody) throws IOException {
        URL url = new URL(httpURL);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();

        if(headers != null) {
            Enumeration e = headers.keys();
            while(e.hasMoreElements()) {
                String headerField = e.nextElement().toString();
                String headerValue = headers.get(headerField).toString();
                conn.setRequestProperty(headerField, headerValue);
            }
        }
        //conn.setRequestProperty("Connection", "close");

        conn.setRequestMethod(method);

        if("POST".equals(method)) {
            if(postBody == null && postParams != null && postParams.size() > 0) {
                //we need to write the post params to the request
                StringBuilder sb = new StringBuilder();
                Enumeration e = postParams.keys();
                boolean firstParam = true;
                while(e.hasMoreElements()) {
                    String key = e.nextElement().toString();
                    String value = postParams.get(key).toString();
                    if(firstParam) {
                        firstParam = false;
                    }else {
                        sb.append('&');
                    }
                    sb.append(URLEncoder.encode(key, "UTF-8")).append('=');
                    sb.append(URLEncoder.encode(value, "UTF-8"));
                }

                postBody = sb.toString().getBytes();
            }else if(postBody == null) {
                throw new IllegalArgumentException("Cant make a post request with no body and no parameters");
            }

            conn.setDoOutput(true);
            OutputStream out = conn.getOutputStream();
            out.write(postBody);
            out.flush();
            out.close();
        }

        conn.connect();

        int contentLen = conn.getContentLength();
        int statusCode = conn.getResponseCode();
        InputStream in = statusCode < 400 ? conn.getInputStream() : conn.getErrorStream();
        byte[] buf = new byte[1024];
        int bytesRead = 0;
        int bytesReadTotal = 0;

        //do not read more bytes than is available in the stream
        int bytesToRead = Math.min(buf.length, contentLen != -1 ? contentLen : buf.length);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        if(!method.equalsIgnoreCase("HEAD")) {
            while((contentLen != -1 ? (bytesRead < contentLen) : true)  && (bytesRead = in.read(buf, 0, contentLen == -1 ? buf.length : Math.min(buf.length, contentLen - bytesRead))) != -1) {
                bout.write(buf, 0, bytesRead);
            }
        }

        in.close();

        Hashtable responseHeaders = new Hashtable();
        Iterator<String> headerIterator = conn.getHeaderFields().keySet().iterator();
        while(headerIterator.hasNext()) {
            String header = headerIterator.next();
            if(header == null) {
                continue;//a null header is the response line not header; leave that alone...
            }

            String headerVal = conn.getHeaderField(header);
            responseHeaders.put(header.toLowerCase(), headerVal);
        }

        byte[] resultBytes = bout.toByteArray();
        HTTPResult result = new HTTPResult(resultBytes, statusCode,
                responseHeaders);

        return result;
    }

}
