package com.ustadmobile.port.sharedse.util;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UmFileUtilSe {

    /**
     * Check if the directory is writable
     * @param dir Directory to be checked
     * @return True if is writable otherwise is read only
     */
    public static boolean canWriteFileInDir(File dir) {
        boolean canWriteFiles = false;
        File testFile = new File(dir.getAbsoluteFile(),System.currentTimeMillis()+".txt");
        try{
            FileWriter writer = new FileWriter(testFile);
            writer.append("sampletest");
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            canWriteFiles = false;
        } catch (IOException e) {
            e.printStackTrace();
            canWriteFiles = false;
        }

        if(testFile.exists()){
            canWriteFiles = testFile.delete();
        }
        return canWriteFiles;
    }

    public static boolean deleteRecursively(File file){
        boolean allDeleted = true;
        for(File childFile : file.listFiles()){
            if(childFile.isDirectory()) {
                allDeleted &= deleteRecursively(childFile);
            }else if(!childFile.delete()) {
                UstadMobileSystemImpl.l(UMLog.WARN, 53, "WARN: delete recursively " +
                        "could not delete child file " + childFile.getAbsolutePath());
                childFile.deleteOnExit();
                allDeleted = false;
            }
        }

        boolean thisFileDeleted = file.delete();
        allDeleted &= thisFileDeleted;
        if(!thisFileDeleted) {
            file.deleteOnExit();
            UstadMobileSystemImpl.l(UMLog.WARN, 53, "WARN: delete recursively " +
                    "could not delete " + file.getAbsolutePath());
        }

        return allDeleted;
    }


    public static void copyFile(File src, File dst, int bufferSize) throws IOException{
        try (
            FileInputStream fin = new FileInputStream(src);
            FileOutputStream fout = new FileOutputStream(dst);
        ) {
            UMIOUtils.readFully(fin, fout, bufferSize);
        }
    }

    public static void copyFile(File src, File dst) throws IOException{
        copyFile(src, dst, UMIOUtils.DEFAULT_BUFFER_SIZE);
    }


    public static byte[] getMd5Sum(InputStream in, byte[] buf) throws IOException{
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            int bytesRead;
            while ((bytesRead = in.read(buf)) != -1) {
                digest.update(buf, 0, bytesRead);
            }

            return digest.digest();
        }catch(NoSuchAlgorithmException ne) {
            throw new IOException(ne);
        }finally {
            in.close();
        }
    }

    public static byte[] getMd5Sum(InputStream in) throws IOException{
        return getMd5Sum(in, new byte[UMIOUtils.DEFAULT_BUFFER_SIZE]);
    }

    public static byte[] getMd5Sum(File file, byte[] buf) throws IOException {
        try(
            FileInputStream fin = new FileInputStream(file);
        ) {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            int bytesRead;
            while ((bytesRead = fin.read(buf)) != -1) {
                digest.update(buf, 0, bytesRead);
            }

            return digest.digest();
        }catch(IOException e) {
            throw e;
        }catch(NoSuchAlgorithmException ne) {
            throw new IOException(ne);
        }
    }

    public static byte[] getMd5Sum(File file) throws IOException {
        return getMd5Sum(file, new byte[UMIOUtils.DEFAULT_BUFFER_SIZE]);
    }


}
