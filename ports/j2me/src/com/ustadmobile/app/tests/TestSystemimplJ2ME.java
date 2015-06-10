/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;


import com.ustadmobile.app.DeviceRoots;
import com.ustadmobile.app.FileUtils;
import com.ustadmobile.app.HTTPUtils;
import com.ustadmobile.impl.UstadMobileSystemImpl;
import j2meunit.framework.TestCase;

import com.ustadmobile.impl.UstadMobileSystemImplFactory;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author varuna
 */
public class TestSystemimplJ2ME extends TestCase {
    private UstadMobileSystemImpl ustadMobileSystemImpl;
    public TestSystemimplJ2ME(){
        setName("TestSystemimplJ2ME Test");
    }
    
    public void runTest() throws Throwable{
        ustadMobileSystemImpl = UstadMobileSystemImplFactory.createUstadSystemImpl();
        assertEquals("Simple Test OK", 2, 1+1);
        String getSharedContentDir = ustadMobileSystemImpl.getSharedContentDir();
        if (getSharedContentDir.toLowerCase().endsWith("ustadmobilecontent/") 
                || getSharedContentDir.toLowerCase().endsWith(
                                        "ustadmobilecontent")){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        
        //Null for now.
        String getUserContentDirectory = 
                ustadMobileSystemImpl.getUserContentDirectory("varuna");
        assertEquals("Testing getUserContent Directory", null, getUserContentDirectory);
        
        String getSystemLocale = ustadMobileSystemImpl.getSystemLocale();
        //assertEquals("testing getSystemLocale:", "en-US", getSystemLocale);
        if(getSystemLocale != null){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        
        Hashtable getSystemInfo = ustadMobileSystemImpl.getSystemInfo();
        if (getSystemInfo.isEmpty()){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
        
        DeviceRoots mainRoot = FileUtils.getBestRoot();
        String fileURI = FileUtils.joinPath(mainRoot.path, "hello.txt");
        
        
        //modTimeDifference
        String url = "http://www.ustadmobile.com/hello.txt";        
        HTTPUtils.downloadURLToFile(url, getSharedContentDir, "");
        
        String fileURI2 = FileUtils.joinPath(getSharedContentDir, "hello.txt");
        int modTimeDifference = ustadMobileSystemImpl.modTimeDifference(
                fileURI2, fileURI);
        
        if ( modTimeDifference != -1 ){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        
        //writeStringToFile
        //readFileAsText
        String toWrite = "UstadMobile";
        ustadMobileSystemImpl.writeStringToFile(toWrite, fileURI2, "");
        String readFileAsText = ustadMobileSystemImpl.readFileAsText(fileURI2);
        if (readFileAsText.toLowerCase().indexOf("ustadmobile") >= 0 ){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        
        //fileSize
        int fileSize = -1;
        fileSize = ustadMobileSystemImpl.fileSize(fileURI2);
        if (fileSize != -1){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        
        //makeDirectory
        String newDir = FileUtils.joinPath(getSharedContentDir, "newdir");
        ustadMobileSystemImpl.makeDirectory(newDir);
        if(FileUtils.checkDir(newDir)){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        
        //fileExists
        if(ustadMobileSystemImpl.fileExists(fileURI2)){
            assertTrue(true);
        }else{
            assertTrue(false);
        }

        //renameFile
        String fileURIFrom = FileUtils.joinPath(getSharedContentDir, "from.txt");
        FileConnection fileCon = null;
        fileCon = (FileConnection) Connector.open(fileURIFrom,
                Connector.READ_WRITE);
        if (!fileCon.exists()){
            fileCon.create();
        }
        if (fileCon!=null){
            fileCon.close();
        }
        String fileURITo = FileUtils.joinPath(getSharedContentDir, "to.txt");
        ustadMobileSystemImpl.renameFile(fileURIFrom, fileURITo);
        if (FileUtils.checkFile(fileURITo)){
            assertTrue(true);
        }else{
            assertTrue(false);
        }

        
        //listDirectory
        String dirList[] = ustadMobileSystemImpl.listDirectory(getSharedContentDir);
        boolean found = false;
        for (int i=0; i<dirList.length; i++){
            if(dirList[i].toLowerCase().equals("hello.txt")){
                found = true;
                break;
            }
        }
        
        if (found){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        
        //String fileURI3 = FileUtils.joinPath(getSharedContentDir, "to.txt");
        String fileURI3 = FileUtils.joinPath(getSharedContentDir, "to.txt");
        ustadMobileSystemImpl.removeFile(fileURI3);
        if (ustadMobileSystemImpl.fileExists(fileURI3)){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
        
        //removeRecursively
        //Set it up
        FileConnection fc = null;
        String fileURI4 = FileUtils.joinPath(getSharedContentDir, "recursivetest");
        String fileURI4d = FileUtils.joinPath(fileURI4, "d");
        String fileURI4f = FileUtils.joinPath(fileURI4, "f");
        String fileURI4df = FileUtils.joinPath(fileURI4d, "f");
        
        fc = (FileConnection) Connector.open(fileURI4,
                Connector.READ_WRITE);
        if (!fc.exists()){
            fc.mkdir();
            fc.close();
        }
        if (fc != null){
            fc.close();
        }
        
        fc = (FileConnection) Connector.open(fileURI4d, 
                Connector.READ_WRITE);
        if (!fc.exists()){
            fc.mkdir();
            fc.close();
        }
        if (fc != null){
            fc.close();
        }
        fc = (FileConnection) Connector.open(fileURI4f, Connector.READ_WRITE);
        if (!fc.exists()){
            fc.create();
            fc.close();
        }
        if (fc != null){
            fc.close();
        }
        fc = (FileConnection) Connector.open(fileURI4df, Connector.READ_WRITE);
        if (!fc.exists()){
            fc.create();
            fc.close();
        }
        if (fc != null){
            fc.close();
        }
        
        //Get rid of it.
        ustadMobileSystemImpl.removeRecursively(fileURI4);
        if (FileUtils.checkDir(fileURI4)){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
        
    }
     
}
