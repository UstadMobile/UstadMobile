/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.app;

import com.sun.lwuit.html.DocumentInfo;
import com.sun.lwuit.html.DocumentRequestHandler;
import gnu.classpath.java.util.zip.ZipEntry;
import gnu.classpath.java.util.zip.ZipInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author mike
 */
public class ZipEPUBRequestHandler implements DocumentRequestHandler{

    public InputStream resourceRequested(DocumentInfo docInfo) {
        InputStream retVal = null;
        FileConnection conn = null;
        String url = docInfo.getUrl();
        try {
            int zipSepPos = url.indexOf('!');
            if(zipSepPos != -1) {
                conn = (FileConnection)Connector.open(url.substring(0, zipSepPos),
                    Connector.READ);
                String fileInZip = url.substring(zipSepPos + 1);
                InputStream fileIn = conn.openInputStream();
                ZipInputStream zipIn = new ZipInputStream(fileIn);
                ZipEntry currentEntry = null;
                String currentName = null;
                
                while((currentEntry = zipIn.getNextEntry()) != null) {
                    currentName = currentEntry.getName();
                    if(currentName.equals(fileInZip)) {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int bytesRead = -1;
                        while((bytesRead = zipIn.read(buf)) != -1) {
                            buffer.write(buf, 0, bytesRead);
                        }
                        zipIn.close();
                        return new ByteArrayInputStream(buffer.toByteArray());
                    }
                }
            }else {
                conn = (FileConnection)Connector.open(docInfo.getUrl(), 
                    Connector.READ);
                retVal = conn.openInputStream();
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
        
        return retVal;
    }
    
}
