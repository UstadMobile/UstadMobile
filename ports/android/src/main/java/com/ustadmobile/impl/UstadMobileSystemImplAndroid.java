package com.ustadmobile.impl;

import android.app.Activity;
import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.Hashtable;

/**
 * Created by mike on 07/06/15.
 */
public class UstadMobileSystemImplAndroid extends com.ustadmobile.impl.UstadMobileSystemImpl{

    private Activity currentActivity;

    public static final String PREFS_NAME = "ustadmobilePreferences";

    public void init() {
        File sharedContentDir = new File(getSharedContentDir());
        if(!sharedContentDir.exists() && sharedContentDir.isDirectory()) {
            sharedContentDir.mkdirs();
        }
    }

    public void setCurrentActivity(Activity activity) {

    }

    @Override
    public String getSharedContentDir() {
        File extStorage = Environment.getExternalStorageDirectory();
        File ustadContentDir = new File(extStorage, "ustadmobileContent");
        return ustadContentDir.getAbsolutePath();
    }

    @Override
    public String getUserContentDirectory(String username) {
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
        FileInputStream fin = null;
        ByteArrayOutputStream bout = null;

        try {
            fin = new FileInputStream(filename);
            bout = new ByteArrayOutputStream();
            int bytesRead = -1;
            byte[] buf = new byte[1024];
            while((bytesRead = fin.read(buf, 0, buf.length)) != -1) {
                bout.write(buf, 0, bytesRead);
            }
        }catch(IOException e) {
            ioe = e;
        }finally {
            if(fin != null) {
                fin.close();
            }
        }

        if(ioe == null) {
            String retVal = new String(bout.toByteArray(), encoding);
            return retVal;
        }else {
            throw ioe;
        }
    }

    @Override
    public int modTimeDifference(String s, String s1) {
        return 0;
    }

    @Override
    public void writeStringToFile(String str, String fileURI, String encoding) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(str.getBytes(encoding));
        FileOutputStream fout = null;
        IOException ioe = null;
        try {
            int bytesRead = -1;
            byte[] buf = new byte[1024];
            fout = new FileOutputStream(fileURI);
            while((bytesRead = bin.read(buf))!= -1) {
                fout.write(buf, 0, bytesRead);
            }
        }catch(IOException e) {
            ioe = e;
        }finally{
            if(fout != null) {
                fout.close();
            }
        }

        if(ioe != null) {
            throw ioe;
        }
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
