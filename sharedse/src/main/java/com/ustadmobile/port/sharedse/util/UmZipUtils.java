package com.ustadmobile.port.sharedse.util;

import com.ustadmobile.core.util.UMIOUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UmZipUtils {

    public static void unzip(File zipFile, File destDir) throws IOException {
        OutputStream entryOut = null;
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipIn.getNextEntry()) != null) {

                String fileName = zipEntry.getName();
                File fileToCreate = new File(destDir, fileName);

                File dirToCreate = zipEntry.isDirectory() ? fileToCreate : fileToCreate.getParentFile();
                if (!dirToCreate.isDirectory()) {
                    if (!dirToCreate.mkdirs()) {
                        throw new RuntimeException("Could not create directory to extract to: " +
                                fileToCreate.getParentFile());
                    }
                }
                if (!zipEntry.isDirectory()) {
                    entryOut = new FileOutputStream(fileToCreate);
                    UMIOUtils.readFully(zipIn, entryOut);
                    entryOut.close();
                }
            }
        } finally {
            UMIOUtils.closeQuietly(entryOut);
        }

    }

}
