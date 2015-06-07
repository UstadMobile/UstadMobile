package com.ustadmobile.impl;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Created by mike on 07/06/15.
 */
public class UstadMobileSystemImplAndroid extends com.ustadmobile.impl.UstadMobileSystemImpl{
    @Override
    public String getSharedContentDir() {
        return null;
    }

    @Override
    public String getUserContentDirectory(String s) {
        return null;
    }

    @Override
    public String getSystemLocale() {
        return null;
    }

    @Override
    public Hashtable getSystemInfo() {
        return null;
    }

    @Override
    public String readFileAsText(String filename, String encoding) throws IOException {
        IOException ioe = null;
        try {
            FileInputStream fin = new FileInputStream(filename);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            int bytesRead = -1;
            byte[] buf = new byte[1024];
            while((bytesRead = fin.read(buf, 0, buf.length)) != -1) {
                bout.write(buf, 0, bytesRead);
            }
        }catch(IOException e) {
            ioe = e;
        }finally {
            
        }


    }

    @Override
    public int modTimeDifference(String s, String s1) {
        return 0;
    }

    @Override
    public void writeStringToFile(String s, String s1, String s2) throws IOException {

    }

    @Override
    public boolean fileExists(String s) throws IOException {
        return false;
    }

    @Override
    public boolean dirExists(String s) throws IOException {
        return false;
    }

    @Override
    public void removeFile(String s) throws IOException {

    }

    @Override
    public String[] listDirectory(String s) throws IOException {
        return new String[0];
    }

    @Override
    public UMTransferJob downloadURLToFile(String s, String s1, Hashtable hashtable) {
        return null;
    }

    @Override
    public void renameFile(String s, String s1) {

    }

    @Override
    public int fileSize(String s) {
        return 0;
    }

    @Override
    public void makeDirectory(String s) throws IOException {

    }

    @Override
    public void removeRecursively(String s) {

    }

    @Override
    public UMTransferJob unzipFile(String s, String s1) {
        return null;
    }

    @Override
    public void setActiveUser(String s) {

    }

    @Override
    public void setUserPref(String s, String s1) {

    }

    @Override
    public String getUserPref(String s, String s1) {
        return null;
    }

    @Override
    public String[] getPrefKeyList() {
        return new String[0];
    }

    @Override
    public void saveUserPrefs() {

    }
}
