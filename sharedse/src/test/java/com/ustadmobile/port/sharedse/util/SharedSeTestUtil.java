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

}
