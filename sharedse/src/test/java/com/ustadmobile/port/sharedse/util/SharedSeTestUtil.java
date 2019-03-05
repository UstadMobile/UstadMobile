package com.ustadmobile.port.sharedse.util;

import com.ustadmobile.core.util.UMIOUtils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SharedSeTestUtil {

    public static void extractResourceToFile(String resourcePath, File destFile) throws IOException {
        try(
            FileOutputStream fout = new FileOutputStream(destFile);
            InputStream resIn = SharedSeTestUtil.class.getResourceAsStream(resourcePath);
        ) {
            UMIOUtils.readFully(resIn, fout);
        }
    }

    public static File makeTempDir(String prefix, String postfix) throws IOException{
        File tmpDir = File.createTempFile(prefix, postfix);
        if(tmpDir.delete() && tmpDir.mkdirs())
            return tmpDir;
        else
            throw new IOException("Could not delete / create tmp dir");
    }


}
