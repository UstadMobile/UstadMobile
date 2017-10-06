package com.ustadmobile.test.core;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by mike on 9/13/17.
 */

public class UMTestUtil {

    /**
     * Copy a given test resource into the first (shared) storage directory
     *
     * @return Complete path the resource was copied to
     */
    public static String copyResourceToStorageDir(String resourcePath) throws IOException{
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        OutputStream fileOut = null;
        InputStream entryIn = null;
        IOException ioe  = null;
        String outPath = null;
        try{
            entryIn = UMTestUtil.class.getResourceAsStream(resourcePath);

            Object context = PlatformTestUtil.getTargetContext();
            UMStorageDir[] storageDirs = impl.getStorageDirs(CatalogPresenter.SHARED_RESOURCE,
                    context);
            String outDir = storageDirs[0].getDirURI();
            if(!impl.dirExists(outDir)) {
                impl.makeDirectoryRecursive(outDir);
            }

            outPath = UMFileUtil.joinPaths(new String[]{outDir,
                    UMFileUtil.getFilename(resourcePath)});

            fileOut = UstadMobileSystemImpl.getInstance().openFileOutputStream(outPath, 0);
            UMIOUtils.readFully(entryIn, fileOut, 8*1024);
        }catch(IOException e) {
            ioe = e;
        }finally {
            UMIOUtils.closeInputStream(entryIn);
            UMIOUtils.closeOutputStream(fileOut);
            UMIOUtils.throwIfNotNullIO(ioe);
        }

        return outPath;
    }

}
