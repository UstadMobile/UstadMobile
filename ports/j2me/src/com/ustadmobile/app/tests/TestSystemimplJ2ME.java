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
        int modTimeDifference = (int) ustadMobileSystemImpl.modTimeDifference(
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
        long fileSize = -1;
        fileSize = ustadMobileSystemImpl.fileSize(fileURI2);
        if ((int)fileSize != -1){
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
