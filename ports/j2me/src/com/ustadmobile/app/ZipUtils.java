/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app;

import gnu.classpath.java.util.zip.ZipEntry;
import gnu.classpath.java.util.zip.ZipInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author varuna
 */
public class ZipUtils {

    public ZipUtils() {
    }
    /*
    public Hashtable listFiles(String zipFile) throws IOException{
		Hashtable listFilesHashtable = new Hashtable();
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
                            listFilesHashtable.put(entry, new Entry("dir", entry.getName()));
                            continue;
                        }
                        listFilesHashtable.put(entry, new Entry("file", entry.getName()));
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

        return listFilesHashtable;

	}
    */
    public static boolean unZipFile(String zipFile, String extractFolderURI) throws Exception {
        ZipEntry entry = null;
        //boolean overwriteall = false;
        boolean overwriteall = true;
        FileConnection zipFileCon = null;
        FileConnection zipDirCon = null;
        InputStream is = null;
        FileConnection c = null;
        FileConnection unzipDirCon = null;
        
        try {
            c = (FileConnection) Connector.open(zipFile);
            is = c.openInputStream();

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


        //set buffer size (2k)
        int BUFFER = 2048;
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
