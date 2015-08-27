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

import com.ustadmobile.port.j2me.app.controller.UstadMobileAppController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import gnu.classpath.java.util.zip.ZipEntry;
import gnu.classpath.java.util.zip.ZipInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author varuna
 */
public class ZipUtils {

    private static final int BUFFER = 2048;
    
    public ZipUtils() {
    }
    
    public static class Entry{
        String filetype;
        String name;
        Entry(String filetype, String name){
            this.filetype = filetype;
            this.name = name;
        }
    }
    
    public static String[] listFiles(String zipFile) throws IOException{
        Vector listFilesVector = new Vector();
        FileConnection fileCon = null;
        InputStream is = null;
        ZipEntry entry = null;
        try {
            fileCon = (FileConnection) Connector.open(zipFile, Connector.READ);
            is = fileCon.openInputStream();

            if (zipFile.toLowerCase().endsWith("zip") 
            	|| zipFile.toLowerCase().endsWith("jar") 
            		|| zipFile.toLowerCase().endsWith("elp") 
            			|| zipFile.toLowerCase().endsWith("epub")){
                try {
                    ZipInputStream zis = new ZipInputStream(is);
                    while ((entry = zis.getNextEntry()) != null) {
                        if (entry.isDirectory()) {
                            //listFilesHashtable.put(entry, new Entry("dir", entry.getName()));
                            listFilesVector.addElement(entry.getName());
                            continue;
                        }
                        //listFilesHashtable.put(entry, new Entry("file", entry.getName()));
                        listFilesVector.addElement(entry.getName());
                    }
                    zis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } 
        } finally {
            if (is != null) {
                is.close();
            }
            if (fileCon != null) {
                fileCon.close();
            }
        }

        String[] a = null;
        a = FileUtils.vectorToStringArray(listFilesVector);
        
        return a;

    }
    
    public static boolean unZipSingleFile(String zipFile, String fileInZip) 
            throws IOException{
        return unZipSingleFile(zipFile, fileInZip, null);
    }
    
    public static boolean unZipSingleFile(String zipFile, String fileInZip, 
            String extractDir) throws IOException{
        Vector listFilesVector = new Vector();
        FileConnection fileCon = null;
        InputStream is = null;
        ZipEntry entry;
        //extractDir = FileUtils.getFolderPath(extractDir); // if needed.
        try {
            fileCon = (FileConnection) Connector.open(zipFile, Connector.READ);
            is = fileCon.openInputStream();

            if (zipFile.toLowerCase().endsWith("zip") 
            	|| zipFile.toLowerCase().endsWith("jar") 
            		|| zipFile.toLowerCase().endsWith("elp") 
            			|| zipFile.toLowerCase().endsWith("epub")){
                try {
                    ZipInputStream zis = new ZipInputStream(is);
                    while ((entry = zis.getNextEntry()) != null) {

                        listFilesVector.addElement(entry.getName());
                        
                        if (entry.getName().equals(fileInZip)){
                            //unzip this file.
 
                            int bytesRead;
                            byte buffer[] = new byte[BUFFER];
                            
                            
                            if(extractDir == null){
                                String sharedContentDir = UstadMobileSystemImpl.getInstance().getSharedContentDir();
                                extractDir = FileUtils.joinPath(sharedContentDir, "test");
                            }
                            
                            OutputStream outputStream = null;
                            FileConnection currentDirCon = null;
                            FileConnection currentFileCon = null;

                            //Prepare and make the extracted directory.
                            try {
                                currentDirCon = (FileConnection) 
                                    Connector.open(extractDir, Connector.READ_WRITE);
                                if (!currentDirCon.exists()) {
                                    currentDirCon.mkdir();
                                }
                                currentDirCon.setWritable(true);
                            } catch (Error e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                    if ( currentDirCon != null ){
                                            currentDirCon.close();
                                    }
                            }
                            
                            
                            //Create the file and write to it.
                            String extractFile = 
                                    FileUtils.joinPath(extractDir, FileUtils.getBaseName(fileInZip));
                            try {
                                currentFileCon = (FileConnection)
                                        Connector.open(extractFile, Connector.READ_WRITE);
                                if (!currentFileCon.exists()){
                                    currentFileCon.create();
                                }
                                outputStream = currentFileCon.openOutputStream();
                                while ((bytesRead = zis.read(buffer, 0, BUFFER)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (outputStream != null ){
                                        outputStream.close();
                                }
                                if (currentFileCon != null){
                                        currentFileCon.close();
                                }
                            }
                            
                        }
                    }
                    zis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } 
        } finally {
            if (is != null) {
                is.close();
            }
            if (fileCon != null) {
                fileCon.close();
            }
        }

        //Putting the file list vector into a string array.
        String[] a = null;
        a = FileUtils.vectorToStringArray(listFilesVector);
        
        return true;

    }
    
    public static ZipInputStream readFileFromZip (String zipFile, String fileInZip)
            throws IOException{
        Vector listFilesVector = new Vector();
        FileConnection fileCon = null;
        InputStream is = null;
        ZipEntry entry;
        //extractDir = FileUtils.getFolderPath(extractDir); // if needed.
        try {
            fileCon = (FileConnection) Connector.open(zipFile, Connector.READ);
            is = fileCon.openInputStream();

            if (zipFile.toLowerCase().endsWith("zip") 
            	|| zipFile.toLowerCase().endsWith("jar") 
            		|| zipFile.toLowerCase().endsWith("elp") 
            			|| zipFile.toLowerCase().endsWith("epub")){
                try {
                    ZipInputStream zis = new ZipInputStream(is);
                    while ((entry = zis.getNextEntry()) != null) {

                        listFilesVector.addElement(entry.getName());
                        
                        if (entry.getName().equals(fileInZip)){
                            //unzip this file.
 
                            int bytesRead;
                            byte buffer[] = new byte[BUFFER];
                            
                            OutputStream outputStream = null;
                            InputStream inputStream = null;

                            try {
                                //FileUtils.getFileContents(fileInZip);
                                //FileUtils.getFileBytes(fileInZip);
                                
                                
                                /*while ((bytesRead = zis.read(buffer, 0, BUFFER)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }*/
                                
                                //inputStream  = convertZipInputStreamToInputStream(zis);
                                return zis;
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (outputStream != null ){
                                        outputStream.close();
                                }
                            }
                            
                        }
                    }
                    zis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } 
        } finally {
            if (is != null) {
                is.close();
            }
            if (fileCon != null) {
                fileCon.close();
            }
        }
        
        return null;

    }
    
    private static InputStream convertZipInputStreamToInputStream(ZipInputStream in) 
            throws IOException
    {
        final int BUFFER = 2048;
        int count = 0;
        byte data[] = new byte[BUFFER];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while ((count = in.read(data, 0, BUFFER)) != -1) {
            out.write(data);
        }       
        InputStream is = new ByteArrayInputStream(out.toByteArray());
        return is;
    }
    
    
    public static boolean unZipFile(String zipFile, String extractFolderURI)
            throws Exception{
        return unZipFile(zipFile, extractFolderURI, null);
    }
    
    public static boolean unZipFile(String zipFile, String extractFolderURI,
            InputStream is) throws Exception {
        ZipEntry entry = null;
        //boolean overwriteall = false;
        boolean overwriteall = true;
        FileConnection zipFileCon = null;
        FileConnection zipDirCon = null;
        FileConnection c = null;
        FileConnection unzipDirCon = null;
        
        try {
            if (is == null){
                c = (FileConnection) Connector.open(zipFile);
                is = c.openInputStream();
            }

        } catch (Exception e) {
            System.out.println("Could not open url: " + zipFile + " Exception: " + e.getMessage());
        }finally{
        	/*
                if (c != null){
                        c.close();
                }
        	*/
        }

        //If no extracted folder is given, auto create a folder
        //with the zip's name in the same  directory.
        if(extractFolderURI == null || extractFolderURI == ""){
        	extractFolderURI = zipFile.substring(0, zipFile.toLowerCase().lastIndexOf('.'));
        }

        //Open the zip file (the one to be unzipped)..
        try {
            zipFileCon = (FileConnection) Connector.open(zipFile, Connector.READ_WRITE);
            zipFileCon.setWritable(true);
        } catch (Exception e) {
            //Failed to open
            //Failed to setWritable
        } finally {
        	if (zipFileCon != null ){
        		zipFileCon.close();
        	}
        }

        //Prepare and make the extracted directory.
        try {
            unzipDirCon = (FileConnection) Connector.open(extractFolderURI, Connector.READ_WRITE);
            if (!unzipDirCon.exists()) {
                unzipDirCon.mkdir();
            }
            unzipDirCon.setWritable(true);
        } catch (Error e) {
        } catch (Exception e) {
        } finally {
        	if ( unzipDirCon != null ){
        		unzipDirCon.close();
        	}
        }
        
        ZipInputStream zis = null;
        zis = new ZipInputStream(is); //GNU Public License
        String zipsDirURI;
        while ((entry = zis.getNextEntry()) != null) {
            zipsDirURI = null;
            //Create directory if the next entry is a directory. (Nothing to extract really..)
            if (entry.isDirectory()) {
            	FileConnection zipsDirCon = null;
                try {
                    //newDirStr = extractFolderURI + "/" + entry;
                    zipsDirURI = FileUtils.joinPath(extractFolderURI, entry.toString());
                    
                    zipsDirCon = (FileConnection) Connector.open(zipsDirURI, 
                    	Connector.READ_WRITE);
                    if (!zipsDirCon.exists()) {
                        zipsDirCon.mkdir();
                    }
                    zipsDirCon.setWritable(true);
                } catch (Error e) {
                } catch (Exception e) {
                } finally {
                	if(zipsDirCon != null){
                		zipsDirCon.close();
                	}
                }
            	// continue to the next loop..
                continue;
            }

            OutputStream outputStream = null;
            
            int bytesRead;
            byte buffer[] = new byte[BUFFER];

            //Extract the current file..
            String currentFileURI = FileUtils.joinPath(extractFolderURI, entry.toString());
            FileConnection currentFileCon = null;
            try {
                currentFileCon = (FileConnection) Connector.open(currentFileURI, Connector.READ_WRITE);
            } catch (Exception e) {
            }

            if (currentFileCon.exists() && !overwriteall) {
                //Will skip this file is overwriteall is set to false
                //Need a better way of automatically handling this. 
                //Default behaviour: Overwrite all.
                continue; 
            } 
            try {
            	//Create the file and set it ready for writing (unzipping)..
                if (!currentFileCon.exists()) {
                    currentFileCon.create();
                }
                currentFileCon.setWritable(true);
            } catch (Exception e) {
            }

            try {
                outputStream = currentFileCon.openOutputStream();
                while ((bytesRead = zis.read(buffer, 0, BUFFER)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {

            } finally {
            	if (outputStream != null ){
            		outputStream.close();
            	}
            	if (currentFileCon != null){
            		currentFileCon.close();
            	}
            }

        }
        if (zis != null){
        	zis.close();

        }
    		
        return true;
    }
    
}
