package com.ustadmobile.impl;

import java.io.IOException;
import java.util.Hashtable;

/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system (e.g. Android,
 * J2ME, etc)
 * 
 * 
 * @author mike
 */
public abstract class UstadMobileSystemImpl {
    
    private static UstadMobileSystemImpl mainInstance;
    
    public static UstadMobileSystemImpl getInstance() {
        if(mainInstance == null) {
            mainInstance = UstadMobileSystemImplFactory.createUstadSystemImpl();
        }
        
        return mainInstance;
    }
    
    /**
     * Do any required startup operations: init will be called on creation
     * 
     * This must make the shared content directory if it does not already exist
     */
    public void init() {
        
    }
    
    /**
     * Provides the path to the shared content directory 
     * 
     * @return URI of the shared content directory
     */
    public abstract String getSharedContentDir();
    
    /**
     * Provides the path to content directory for a given user
     * 
     * @param username
     * 
     * @return URI of the given users content directory
     */
    public abstract String getUserContentDirectory(String username);
    
    
    /**
     * Must provide the system's default locale (e.g. en_US.UTF-8)
     * 
     * @return System locale
     */
    public abstract String getSystemLocale();
    
    /**
     * Provide information about the platform as key value pairs in a hashtable
     * 
     * @return 
     */
    public abstract Hashtable getSystemInfo();
    
    /**
     * Read the given fileURI as a string and return it 
     * 
     * @param fileURI URI to the required file
     * @param encoding encoding e.g. UTF-8
     * 
     * @return File contents as a string
     */
    public abstract String readFileAsText(String fileURI, String encoding) throws IOException;
    
    /**
     * Read the given fileURI as a string and return it - assume UTF-8 encoding
     * @param fileURI URI to the required file
     * @return Content of the file as a string with UTF-8 encoding
     */
    public String readFileAsText(String fileURI) throws IOException{
        return this.readFileAsText(fileURI, "UTF-8");
    }
    
    /**
     * Return the difference in file modification times between two files
     * 
     * @param fileURI1 
     * @param fileURI2
     * @return 
     */
    public abstract int modTimeDifference(String fileURI1, String fileURI2);
    
    /**
     * Write the given string to the given file URI.  Create the file if it does 
     * not already exist.
     * 
     * @param str Content to write to the file
     * @param fileURI URI to the required file
     * @param encoding Encoding to use for string e.g. UTF-8
     */
    public abstract void writeStringToFile(String str, String fileURI, String encoding) throws IOException;
    
    /**
     * Check to see if the given file exists
     * 
     * @param fileURI URI of the file to check
     * @return true if exists and is a file or directory, false otherwise
     * 
     * @throws IOException 
     */
    public abstract boolean fileExists(String fileURI) throws IOException;
    
    /**
     * Check to see if the given URI exists and is a directory
     * 
     * @param dirURI URI to check if existing
     * @return true if exists and is a directory, false otherwise
     * @throws IOException 
     */
    public abstract boolean dirExists(String dirURI) throws IOException;
    
    /**
     * Remove the given file
     * @param fileURI URI to be removed
     * @throws IOException 
     */
    public abstract void removeFile(String fileURI) throws IOException;
    
    /**
     * List of files and directories within a directory as an array of Strings.
     * Should give only the relative path of the name within the directory
     * 
     * @param dirURI
     * @return
     * @throws IOException 
     */
    public abstract String[] listDirectory(String dirURI) throws IOException;
    
    public abstract UMTransferJob downloadURLToFile(String url, String fileURI);
    
    public abstract void renameFile(String fromFileURI, String toFileURI);
    
    public abstract int fileSize(String fileURI);
    
    public abstract void makeDirectory(String dirURI) throws IOException;
    
    public abstract void removeRecursively(String dirURI);
    
    public abstract UMTransferJob unzipFile(String zipSrc, String dstDir);
    
}


