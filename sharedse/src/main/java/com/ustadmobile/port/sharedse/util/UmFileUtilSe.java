package com.ustadmobile.port.sharedse.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

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


}
